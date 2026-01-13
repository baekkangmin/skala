# -------------------------------------------------------------
# 작성자 : 백강민
# 작성목적 : SKALA Python Day2 - 멀티프로세싱 성능 최적화 및 구조적 로깅
# 변경사항 내역 :
#   2026-01-13 - 최초 작성
#   2026-01-14 - 파일명 고정 제거(--file 필수) + jsonl/csv 모두 지원
# -------------------------------------------------------------

from __future__ import annotations

import argparse
import csv
import json
import os
import re
import time
from itertools import cycle, islice
from multiprocessing import get_context
from pathlib import Path
from typing import Dict, Iterable, Iterator, List, Tuple

# -------------------------
# Regex rules (마스킹 + 정규화)
# -------------------------
EMAIL_RE = re.compile(r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b")
PHONE_RE = re.compile(r"\b(?:0\d{1,2}[- ]?)?\d{3,4}[- ]?\d{4}\b")

FORBIDDEN_MAP: Dict[str, str] = {
    "개새끼": "비속어",
    "ㅅㅂ": "비속어",
    "시발": "비속어",
    "존나": "매우",
    "ㅈㄴ": "매우",
    "졸라": "매우",
    "꺼져": "그만해",
}
FORBIDDEN_RE = re.compile(
    "|".join(map(re.escape, sorted(FORBIDDEN_MAP, key=len, reverse=True)))
)


def process_text(text: str) -> str:
    text = EMAIL_RE.sub("****", text)
    text = PHONE_RE.sub("****", text)
    return FORBIDDEN_RE.sub(lambda m: FORBIDDEN_MAP[m.group(0)], text)


# -------------------------
# Reader: JSONL / CSV (단일 파일, 확장자 기반 자동)
# -------------------------
def infer_format(path: Path) -> str:
    ext = path.suffix.lower()
    if ext == ".jsonl":
        return "jsonl"
    if ext == ".csv":
        return "csv"
    raise ValueError(f"unsupported file extension: {ext} (supported: .jsonl, .csv)")


def iter_jsonl(path: Path) -> Iterator[dict]:
    with path.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                yield json.loads(line)


def iter_csv(path: Path) -> Iterator[dict]:
    """
    CSV 기본 지원(원본에 최소 추가):
    - DictReader로 row를 dict로 반환
    - 키/값 strip + 빈 문자열은 그대로 두고 싶으면 None 처리 제거 가능
    """
    with path.open("r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            cleaned: Dict[str, object] = {}
            for k, v in row.items():
                key = (k or "").strip()
                if isinstance(v, str):
                    val = v.strip()
                    cleaned[key] = val  # 필요하면: None if val == "" else val
                else:
                    cleaned[key] = v
            yield cleaned


def iter_records(path: Path) -> Iterator[dict]:
    fmt = infer_format(path)
    if fmt == "jsonl":
        yield from iter_jsonl(path)
    else:
        yield from iter_csv(path)


def take_n(it: Iterable[dict], n: int) -> List[dict]:
    return list(islice(it, n))


def expand_to_n(base: List[dict], n: int) -> List[dict]:
    return list(islice(cycle(base), n)) if base else []


# -------------------------
# Chunking
# -------------------------
def chunkify(items: List[dict], chunk_size: int) -> List[List[dict]]:
    return [items[i : i + chunk_size] for i in range(0, len(items), chunk_size)]


def process_chunk(chunk: List[dict], text_key: str) -> int:
    processed = 0
    for r in chunk:
        t = r.get(text_key)
        if isinstance(t, str) and t.strip():
            r[text_key] = process_text(t)
            processed += 1
    return processed


# -------------------------
# Bench
# -------------------------
def bench_single(all_records: List[dict], text_key: str) -> Tuple[int, float, float]:
    t0 = time.perf_counter()
    processed = process_chunk(all_records, text_key)
    secs = time.perf_counter() - t0
    rps = processed / secs if secs > 0 else 0.0
    return processed, secs, rps


def bench_multi(
    all_records: List[dict],
    text_key: str,
    processes: int,
    chunk_size: int,
    start_method: str,
) -> Tuple[int, float, float, int]:
    chunks = chunkify(all_records, chunk_size)
    t0 = time.perf_counter()
    ctx = get_context(start_method)
    with ctx.Pool(processes=processes) as pool:
        counts = pool.starmap(process_chunk, [(c, text_key) for c in chunks])
    secs = time.perf_counter() - t0

    processed = sum(counts)
    rps = processed / secs if secs > 0 else 0.0
    return processed, secs, rps, len(chunks)


def fmt(n: float) -> str:
    return f"{n:,.2f}"


def main() -> None:
    p = argparse.ArgumentParser()

    # 파일명 고정 제거: --file로 파일 지정
    p.add_argument(
        "--file",
        required=True,
        help="입력 파일(.jsonl 또는 .csv). 예) reviews_100k.jsonl, reviews_500k.csv",
    )
    p.add_argument("--text-key", default="review_text")
    p.add_argument("--records", type=int, default=5_000_000)
    p.add_argument("--base-read", type=int, default=100_000)
    p.add_argument("--processes", type=int, default=(os.cpu_count() or 4))
    p.add_argument(
        "--start-method", default="spawn", choices=["fork", "spawn", "forkserver"]
    )
    p.add_argument("--chunk-sizes", default="1000,10000,100000")
    p.add_argument("--ipc-small", type=int, default=100)
    p.add_argument("--show-samples", type=int, default=3)
    args = p.parse_args()

    path = Path(args.file)
    if not path.exists() or not path.is_file():
        raise FileNotFoundError(f"input file not found: {path.resolve()}")

    base = take_n(iter_records(path), args.base_read)
    if not base:
        print("입력에서 레코드를 읽지 못했습니다. (빈 파일이거나 파싱 실패)")
        return

    # text_key 존재 체크(특히 csv 헤더 실수 방지)
    if args.text_key not in base[0]:
        sample_keys = sorted(list(base[0].keys()))[:30]
        raise KeyError(
            f"text-key '{args.text_key}' not found in input records.\n"
            f"- example keys (first record): {sample_keys}"
        )

    all_records = expand_to_n(base, args.records)

    # 1) 배치 프로세서
    print("\n[수정된 문장]")
    shown = 0
    for r in base:
        t = r.get(args.text_key)
        if not isinstance(t, str) or not t.strip():
            continue
        after = process_text(t)
        if after != t:
            print(f"- {after[:140]}")
            shown += 1
            if shown >= args.show_samples:
                break
    if shown == 0:
        print("해당 데이터 범위에서는 바뀔 단어가 없었습니다.")

    # 2) 핵심 포인트: Single vs Multi(best)
    _p1, s_secs, s_rps = bench_single(all_records, args.text_key)

    chunk_sizes = [int(x.strip()) for x in args.chunk_sizes.split(",") if x.strip()]
    multi = [
        (
            *bench_multi(
                all_records, args.text_key, args.processes, cs, args.start_method
            ),
            cs,
        )
        for cs in chunk_sizes
    ]
    # multi 요소: (processed, secs, rps, tasks, cs)

    best = max(multi, key=lambda x: x[2])  # rps 기준
    _best_processed, best_secs, best_rps, best_tasks, best_cs = best

    speedup = s_secs / best_secs if best_secs > 0 else 0.0

    print("\n[1] Single vs Multi")
    print(f"- single: {s_secs:.3f}s, {fmt(s_rps)} r/s")
    print(
        f"- multi : {best_secs:.3f}s, {fmt(best_rps)} r/s (chunk={best_cs:,}, tasks={best_tasks:,})"
    )
    print(f"  speedup: {speedup:.2f}x")

    # 3) Chunking 비교
    print("\n[2] Chunking throughput 비교")
    print("chunk | tasks | sec  | r/s")
    for processed, secs, rps, tasks, cs in sorted(multi, key=lambda x: x[4]):
        print(f"{cs:>5,} | {tasks:>5,} | {secs:>4.1f} | {fmt(rps)}")

    # 4) 아주 작은 chunk vs best IPC 비교
    _p3, small_secs, small_rps, small_tasks = bench_multi(
        all_records, args.text_key, args.processes, args.ipc_small, args.start_method
    )
    slower = small_secs / best_secs if best_secs > 0 else 0.0

    print("\n[3] IPC overhead 비교")
    print(
        f"- small chunk={args.ipc_small:,}: {small_secs:.3f}s, {fmt(small_rps)} r/s (tasks={small_tasks:,})"
    )
    print(
        f"- best  chunk={best_cs:,}: {best_secs:.3f}s, {fmt(best_rps)} r/s (tasks={best_tasks:,})"
    )
    print(f"  slower: {slower:.2f}x (small vs best)")


if __name__ == "__main__":
    main()
