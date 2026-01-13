# -------------------------------------------------------------
# 작성자 : 백강민
# 작성목적 : SKALA Python Day2 - 구조적 로깅 및 컨텍스트 추적 (Contextual Logging)
# 변경사항 내역 :
#   2025-01-13 - 최초 작성
# -------------------------------------------------------------

from __future__ import annotations

import csv
import json
import multiprocessing as mp
import os
import random
import re
import time
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple


# -----------------------------
# Task 모델
# -----------------------------
@dataclass(frozen=True)
class Task:
    batch_id: str
    task_id: str
    job_type: str
    input_size_mb: int
    priority: str
    created_at: str  # CSV의 ISO 문자열 그대로

    @staticmethod
    def from_row(row: Dict[str, str]) -> "Task":
        return Task(
            batch_id=row["batch_id"].strip(),
            task_id=row["task_id"].strip(),
            job_type=row["job_type"].strip(),
            input_size_mb=int(row["input_size_mb"]),
            priority=row["priority"].strip(),
            created_at=row["created_at"].strip(),
        )


# -----------------------------
# 유틸: ISO8601 with milliseconds + Z
# -----------------------------
def iso_utc_now_ms() -> str:
    return (
        datetime.now(timezone.utc)
        .isoformat(timespec="milliseconds")
        .replace("+00:00", "Z")
    )


# -----------------------------
# 프로세스 이름을 worker-1로 표준화
# -----------------------------
_WORKER_NAME_RE = re.compile(r"(?:SpawnPoolWorker|ForkPoolWorker)-(\d+)$")


def normalize_worker_name(proc_name: str) -> str:
    m = _WORKER_NAME_RE.search(proc_name.strip())
    return f"worker-{m.group(1)}" if m else (proc_name.strip() or "worker-unknown")


def make_log(
    *,
    level: str,
    task: Task,
    process_id: int,
    thread_id: str,
    stage: str,
    message: str,
    context: Optional[Dict[str, Any]] = None,
    exception: Optional[Dict[str, Any]] = None,
) -> Dict[str, Any]:
    payload: Dict[str, Any] = {
        "timestamp": iso_utc_now_ms(),
        "level": level,
        "batch_id": task.batch_id,
        "task_id": task.task_id,
        "process_id": process_id,
        "thread_id": thread_id,
        "stage": stage,
        "message": message,
        "context": context or {},
    }
    if exception:
        payload["exception"] = exception
    return payload


# -----------------------------
# CSV 로드
# -----------------------------
def load_tasks(csv_path: Path) -> List[Task]:
    with csv_path.open("r", encoding="utf-8", newline="") as f:
        return [Task.from_row(r) for r in csv.DictReader(f)]


# -----------------------------
# 메인: Queue 수집 -> logs.json 출력
# -----------------------------
def drain_queue(log_q: mp.Queue, out: List[Dict[str, Any]]) -> None:
    while True:
        try:
            out.append(log_q.get_nowait())
        except Exception:
            break


def write_logs(logs: List[Dict[str, Any]], out_path: Path) -> None:
    logs.sort(key=lambda x: x.get("timestamp", ""))
    with out_path.open("w", encoding="utf-8") as f:
        json.dump(logs, f, ensure_ascii=False, indent=2)


# -----------------------------
# worker_run: 프로세스 이름을 표준화해서 worker id로 사용
# -----------------------------
def worker_run_with_proc_name(task: Task, log_q: mp.Queue) -> Tuple[str, str]:
    pid = os.getpid()
    thread_id = normalize_worker_name(mp.current_process().name)

    def emit(
        level: str,
        stage: str,
        message: str,
        context: Optional[Dict[str, Any]] = None,
        exception: Optional[Dict[str, Any]] = None,
    ) -> None:
        log_q.put(
            make_log(
                level=level,
                task=task,
                process_id=pid,
                thread_id=thread_id,
                stage=stage,
                message=message,
                context=context,
                exception=exception,
            )
        )

    emit(
        "INFO",
        "LOAD",
        "Task started",
        context={"job_type": task.job_type, "input_size_mb": task.input_size_mb},
    )
    time.sleep(random.uniform(0.01, 0.05))

    if task.job_type == "DATA_CLEANING":
        rows = random.randint(10000, 30000)
        emit("INFO", "CLEANING", "Null values removed", context={"rows_affected": rows})
        time.sleep(random.uniform(0.01, 0.06))
        removed = random.randint(50, 800)
        emit("INFO", "CLEANING", "Outliers filtered", context={"rows_removed": removed})

    elif task.job_type == "IMAGE_PROCESSING":
        emit("INFO", "PREPROCESS", "Resize completed", context={"target": "1080p"})
        time.sleep(random.uniform(0.01, 0.06))
        emit(
            "INFO",
            "PROCESS",
            "Augmentation completed",
            context={"ops": ["flip", "crop"]},
        )
        time.sleep(random.uniform(0.01, 0.06))
        emit(
            "INFO",
            "FINISH",
            "Image processing completed",
            context={"output": "s3://bucket/path"},
        )

    elif task.job_type == "MODEL_TRAINING":
        emit(
            "INFO",
            "PREPROCESS",
            "Data normalization completed",
            context={"elapsed_ms": random.randint(500, 4000)},
        )
        time.sleep(random.uniform(0.01, 0.06))

        if random.random() < 0.25:
            emit(
                "WARN",
                "TRAINING",
                "GPU contention detected",
                context={"gpu_id": 0, "lock_wait_ms": random.randint(100, 800)},
            )
            time.sleep(random.uniform(0.01, 0.06))

        if random.random() < 0.15:
            emit(
                "ERROR",
                "TRAINING",
                "CUDA out of memory",
                exception={
                    "type": "OutOfMemoryError",
                    "stacktrace": "trainer.py:188 -> allocate_tensor()",
                },
                context={"retry_count": 1},
            )
            time.sleep(random.uniform(0.01, 0.05))
            emit(
                "INFO",
                "RETRY",
                "Retrying training with reduced batch size",
                context={"new_batch_size": 16},
            )

        epoch = random.randint(1, 5)
        emit(
            "INFO",
            "TRAINING",
            f"Epoch {epoch} completed",
            context={
                "loss": round(random.uniform(0.1, 1.0), 3),
                "accuracy": round(random.uniform(0.7, 0.99), 2),
            },
        )
        time.sleep(random.uniform(0.01, 0.06))
        emit(
            "INFO",
            "FINISH",
            "Model training completed",
            context={"total_epochs": 10, "total_time_sec": random.randint(120, 900)},
        )
    else:
        emit("INFO", "FINISH", "Task completed")

    return task.task_id, "done"


def main() -> None:
    base = Path(".")
    tasks = load_tasks(base / "input_tasks.csv")

    manager = mp.Manager()
    log_q: mp.Queue = manager.Queue()

    logs: List[Dict[str, Any]] = []

    ctx = mp.get_context("spawn")
    nprocs = min(8, os.cpu_count() or 4)

    with ctx.Pool(processes=nprocs) as pool:
        results = [
            pool.apply_async(worker_run_with_proc_name, args=(t, log_q)) for t in tasks
        ]

        while True:
            drain_queue(log_q, logs)
            if all(r.ready() for r in results):
                break
            time.sleep(0.02)

        for r in results:
            r.get()

    drain_queue(log_q, logs)
    write_logs(logs, base / "logs.json")


if __name__ == "__main__":
    main()
