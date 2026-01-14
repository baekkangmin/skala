# -------------------------------------------------------------
# 작성자 : Cloud 1기 백강민
# 작성목적 : SKALA Python Day3 - 비동기 I/O 기반 MSA 통신 및 고성능 인증 보안
# 변경사항 내역 :
#   2026-01-14 - 최초 작성
# -------------------------------------------------------------

from __future__ import annotations

import asyncio
import random
import time
from dataclasses import dataclass
from typing import Any, Dict, List, Tuple

from aiohttp import ClientSession, ClientTimeout, web


# -----------------------------
# 유틸
# -----------------------------
def now_ms() -> int:
    return int(time.time() * 1000)


@dataclass(frozen=True)
class CallResult:
    name: str
    ok: bool
    status: int
    data: Dict[str, Any]
    elapsed_ms: float
    error: str | None = None


async def fetch_json(
    session: ClientSession,
    name: str,
    url: str,
    timeout_s: float = 1.0,
) -> CallResult:
    """단일 마이크로서비스 호출(HTTP GET) + 지연시간 측정"""
    started = time.perf_counter()
    try:
        async with session.get(url, timeout=ClientTimeout(total=timeout_s)) as resp:
            status = resp.status
            payload = await resp.json()
            elapsed_ms = (time.perf_counter() - started) * 1000
            return CallResult(
                name=name,
                ok=(200 <= status < 300),
                status=status,
                data=payload,
                elapsed_ms=elapsed_ms,
            )
    except Exception as e:
        elapsed_ms = (time.perf_counter() - started) * 1000
        return CallResult(
            name=name,
            ok=False,
            status=0,
            data={},
            elapsed_ms=elapsed_ms,
            error=f"{type(e).__name__}: {e}",
        )


def summarize_results(results: List[CallResult]) -> Dict[str, Any]:
    return {
        "services": [
            {
                "name": r.name,
                "ok": r.ok,
                "status": r.status,
                "elapsed_ms": round(r.elapsed_ms, 2),
                "error": r.error,
                "data": r.data if r.ok else None,
            }
            for r in results
        ],
        "ok_count": sum(1 for r in results if r.ok),
        "fail_count": sum(1 for r in results if not r.ok),
    }


# -----------------------------
# "마이크로서비스" (Mock APIs)
# -----------------------------
# - 각 서비스가 서로 다른 지연(네트워크/외부API 느낌)을 가지도록
async def mock_latency(base_ms: int, jitter_ms: int) -> None:
    await asyncio.sleep((base_ms + random.randint(0, jitter_ms)) / 1000)


async def svc_user(request: web.Request) -> web.Response:
    user_id = request.query.get("user_id", "unknown")
    await mock_latency(base_ms=180, jitter_ms=120)
    return web.json_response(
        {"user_id": user_id, "name": "Kim", "tier": "gold", "ts_ms": now_ms()}
    )


async def svc_orders(request: web.Request) -> web.Response:
    user_id = request.query.get("user_id", "unknown")
    await mock_latency(base_ms=250, jitter_ms=180)
    # 10% 확률로 실패(테스트용)
    if random.random() < 0.10:
        return web.json_response({"error": "orders service failure"}, status=503)
    return web.json_response(
        {
            "user_id": user_id,
            "orders": [
                {"order_id": "A-100", "amount": 42000},
                {"order_id": "B-200", "amount": 18000},
            ],
            "ts_ms": now_ms(),
        }
    )


async def svc_reco(request: web.Request) -> web.Response:
    user_id = request.query.get("user_id", "unknown")
    await mock_latency(base_ms=120, jitter_ms=220)
    return web.json_response(
        {
            "user_id": user_id,
            "recommendations": ["coffee", "protein_bar", "wireless_earbuds"],
            "ts_ms": now_ms(),
        }
    )


# -----------------------------
# Aggregator
# -----------------------------
def build_service_urls(request: web.Request) -> List[Tuple[str, str]]:
    base = f"{request.scheme}://{request.host}"
    user_id = request.query.get("user_id", "unknown")
    return [
        ("user-service", f"{base}/svc/user?user_id={user_id}"),
        ("order-service", f"{base}/svc/orders?user_id={user_id}"),
        ("reco-service", f"{base}/svc/reco?user_id={user_id}"),
    ]


# -----------------------------
# 동기 방식
# -----------------------------
async def aggregate_sequential(request: web.Request) -> web.Response:
    urls = build_service_urls(request)

    started_total = time.perf_counter()
    async with ClientSession() as session:
        results: List[CallResult] = []
        for name, url in urls:
            results.append(await fetch_json(session, name, url, timeout_s=1.5))
    total_ms = (time.perf_counter() - started_total) * 1000

    # 출력
    print(
        f"[SEQUENTIAL] total={total_ms:.2f}ms | "
        + ", ".join(f"{r.name}:{r.elapsed_ms:.2f}ms(ok={r.ok})" for r in results)
    )

    merged = {
        "mode": "sequential",
        "total_ms": round(total_ms, 2),
        **summarize_results(results),
    }
    return web.json_response(merged)


# -----------------------------
# 비동기 방식
# -----------------------------
async def aggregate_concurrent(request: web.Request) -> web.Response:
    urls = build_service_urls(request)

    started_total = time.perf_counter()
    async with ClientSession() as session:
        tasks = [fetch_json(session, name, url, timeout_s=1.5) for name, url in urls]
        results = await asyncio.gather(*tasks)
    total_ms = (time.perf_counter() - started_total) * 1000

    # 출력
    print(
        f"[CONCURRENT] total={total_ms:.2f}ms | "
        + ", ".join(f"{r.name}:{r.elapsed_ms:.2f}ms(ok={r.ok})" for r in results)
    )

    merged = {
        "mode": "concurrent",
        "total_ms": round(total_ms, 2),
        **summarize_results(list(results)),
    }
    return web.json_response(merged)


# -----------------------------
# 벤치마크(동기 vs 비동기 이득 측정)
# -----------------------------
def stats(values: List[float]) -> Dict[str, float]:
    values_sorted = sorted(values)
    n = len(values_sorted)
    if n == 0:
        return {"avg": 0.0, "min": 0.0, "max": 0.0, "p95": 0.0}
    avg = sum(values_sorted) / n
    p95_idx = max(0, min(n - 1, int(round(0.95 * (n - 1)))))
    return {
        "avg": round(avg, 2),
        "min": round(values_sorted[0], 2),
        "max": round(values_sorted[-1], 2),
        "p95": round(values_sorted[p95_idx], 2),
    }


async def benchmark(request: web.Request) -> web.Response:
    repeat = int(request.query.get("repeat", "5"))
    repeat = max(1, min(repeat, 50))  # 너무 큰 값 방지
    user_id = request.query.get("user_id", "unknown")

    base = f"{request.scheme}://{request.host}"

    sequential_times: List[float] = []
    concurrent_times: List[float] = []

    async with ClientSession() as session:
        for i in range(1, repeat + 1):
            # 동기 방식 측정
            t0 = time.perf_counter()
            async with session.get(
                f"{base}/aggregate/sequential?user_id={user_id}"
            ) as r1:
                await r1.json()
            sequential_times.append((time.perf_counter() - t0) * 1000)

            # 비동기 방식 측정
            t1 = time.perf_counter()
            async with session.get(
                f"{base}/aggregate/concurrent?user_id={user_id}"
            ) as r2:
                await r2.json()
            concurrent_times.append((time.perf_counter() - t1) * 1000)

            print(
                f"[BENCH {i}/{repeat}] "
                f"seq={sequential_times[-1]:.2f}ms, con={concurrent_times[-1]:.2f}ms"
            )

    seq_s = stats(sequential_times)
    con_s = stats(concurrent_times)

    # 이득 계산 (avg 기준)
    gain_ms = round(seq_s["avg"] - con_s["avg"], 2)
    gain_pct = round((gain_ms / seq_s["avg"] * 100) if seq_s["avg"] > 0 else 0.0, 2)

    print(
        f"[BENCH SUMMARY] seq(avg)={seq_s['avg']}ms, con(avg)={con_s['avg']}ms "
        f"=> gain={gain_ms}ms ({gain_pct}%)"
    )

    return web.json_response(
        {
            "repeat": repeat,
            "sequential_ms": seq_s,
            "concurrent_ms": con_s,
            "gain": {"avg_ms_saved": gain_ms, "avg_percent_saved": gain_pct},
        }
    )


# -----------------------------
# 앱 구성
# -----------------------------
def create_app() -> web.Application:
    app = web.Application()

    # mock microservices
    app.router.add_get("/svc/user", svc_user)
    app.router.add_get("/svc/orders", svc_orders)
    app.router.add_get("/svc/reco", svc_reco)

    # aggregator endpoints
    app.router.add_get("/aggregate/sequential", aggregate_sequential)
    app.router.add_get("/aggregate/concurrent", aggregate_concurrent)

    # benchmark
    app.router.add_get("/benchmark", benchmark)

    return app


if __name__ == "__main__":
    web.run_app(create_app(), host="127.0.0.1", port=8080)
