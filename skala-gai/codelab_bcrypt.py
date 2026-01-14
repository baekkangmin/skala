# -------------------------------------------------------------
# 작성자 : Cloud 1기 백강민
# 작성목적 : SKALA Python Day3 - Bcrypt 해시 기반 Rate Limiting 인증 시스템
# 변경사항 내역 :
#   2026-01-14 - 최초 작성
# -------------------------------------------------------------

from __future__ import annotations

import asyncio
import re
import time
from dataclasses import dataclass
from typing import Any, Dict, Optional, Tuple

from aiohttp import web
from passlib.hash import bcrypt


# -----------------------------
# 설정값
# -----------------------------
DUMMY_USER_DB: Dict[str, str] = {
    # 테스트 계정: user1 / Password123!
    "user1": bcrypt.hash("Password123!"),
}

BASE_BACKOFF_SEC = 1.0  # 1회 실패 시 최소 대기시간
MAX_BACKOFF_SEC = 60.0  # 백오프 최대 상한
RESET_WINDOW_SEC = 300.0  # 마지막 실패 이후 이 시간 지나면 실패카운트 리셋
MAX_USERNAME_LEN = 32
MAX_PASSWORD_LEN = 72  # bcrypt는 72바이트 이후 잘리는 이슈 방지


# -----------------------------
# 상태 저장 (IP별)
# -----------------------------
@dataclass
class IpState:
    fail_count: int = 0
    blocked_until: float = 0.0  # epoch seconds
    last_fail_at: float = 0.0


IP_STATE: Dict[str, IpState] = {}
IP_STATE_LOCK = asyncio.Lock()


# -----------------------------
# 유틸 (시간, IP 추출)
# -----------------------------
def now_s() -> float:
    return time.time()


def get_client_ip(request: web.Request) -> str:
    # 실습에서 IP를 바꿔치기 하기 쉽게 X-Forwarded-For를 우선 사용
    xff = request.headers.get("X-Forwarded-For")
    if xff:
        return xff.split(",")[0].strip()
    peer = request.transport.get_extra_info("peername")
    if peer and isinstance(peer, tuple) and len(peer) >= 1:
        return str(peer[0])
    return "unknown"


# -----------------------------
# 입력 검증 / 정제 파이프라인
# -----------------------------
_USERNAME_RE = re.compile(r"^[a-zA-Z0-9_.-]+$")


def validate_credentials(username: str, password: str) -> Tuple[bool, str]:
    if not username or not password:
        return False, "username/password required"
    if len(username) > MAX_USERNAME_LEN:
        return False, "username too long"
    if len(password) > MAX_PASSWORD_LEN:
        return False, "password too long"
    if not _USERNAME_RE.match(username):
        return False, "username has invalid characters"
    return True, "ok"


def sanitize_event(event: Dict[str, Any]) -> Dict[str, Any]:
    sanitized = dict(event)

    # 비밀번호/토큰 류는 무조건 마스킹 or 제거
    if "password" in sanitized:
        sanitized["password"] = "***MASKED***"

    # user_agent 과도하게 길면 로그 폭증 방지
    ua = sanitized.get("user_agent", "")
    if isinstance(ua, str) and len(ua) > 120:
        sanitized["user_agent"] = ua[:120] + "...(truncated)"

    # username도 로그 정규화
    un = sanitized.get("username", "")
    if isinstance(un, str):
        sanitized["username"] = un[:MAX_USERNAME_LEN]

    return sanitized


async def log_event(event: Dict[str, Any]) -> None:
    sanitized = sanitize_event(event)
    print(f"[AUDIT] {sanitized}")


# -----------------------------
# Rate Limiting
# -----------------------------
def calc_backoff_sec(fail_count: int) -> float:
    # fail_count: 1부터 증가한다고 가정
    # 1 -> 1s, 2 -> 2s, 3 -> 4s, 4 -> 8s ... (상한 적용)
    backoff = BASE_BACKOFF_SEC * (2 ** (fail_count - 1))
    return min(backoff, MAX_BACKOFF_SEC)


async def check_and_update_state_on_attempt(ip: str) -> Tuple[bool, float]:
    async with IP_STATE_LOCK:
        st = IP_STATE.get(ip)
        if not st:
            IP_STATE[ip] = IpState()
            return True, 0.0

        now = now_s()

        # 마지막 실패가 오래전이면 카운트 리셋
        if st.fail_count > 0 and (now - st.last_fail_at) >= RESET_WINDOW_SEC:
            st.fail_count = 0
            st.blocked_until = 0.0
            st.last_fail_at = 0.0

        if now < st.blocked_until:
            return False, max(0.0, st.blocked_until - now)

        return True, 0.0


# 인증 실패 시 fail_count 증가 + blocked_until 갱신
async def on_auth_fail(ip: str) -> Tuple[int, float]:
    async with IP_STATE_LOCK:
        st = IP_STATE.setdefault(ip, IpState())
        st.fail_count += 1
        st.last_fail_at = now_s()
        backoff = calc_backoff_sec(st.fail_count)
        st.blocked_until = st.last_fail_at + backoff
        return st.fail_count, backoff


async def on_auth_success(ip: str) -> None:
    async with IP_STATE_LOCK:
        st = IP_STATE.get(ip)
        if st:
            st.fail_count = 0
            st.blocked_until = 0.0
            st.last_fail_at = 0.0


# -----------------------------
# bcrypt 검증 (CPU-bound)
# -----------------------------
def verify_password_cpu_bound(username: str, password: str) -> bool:
    hashed = DUMMY_USER_DB.get(username)
    if not hashed:
        return False
    # bcrypt.verify는 내부적으로 CPU 연산(비용) 발생
    return bcrypt.verify(password, hashed)


async def verify_password(username: str, password: str, offload: bool) -> bool:
    if not offload:
        return verify_password_cpu_bound(username, password)
    return await asyncio.to_thread(verify_password_cpu_bound, username, password)


# -----------------------------
# API
# -----------------------------
async def login(request: web.Request) -> web.Response:
    ip = get_client_ip(request)
    allowed, retry_after = await check_and_update_state_on_attempt(ip)
    if not allowed:
        await log_event(
            {
                "type": "login_blocked",
                "ip": ip,
                "retry_after_sec": round(retry_after, 3),
                "user_agent": request.headers.get("User-Agent", ""),
            }
        )
        return web.json_response(
            {
                "ok": False,
                "reason": "rate_limited",
                "retry_after_sec": round(retry_after, 3),
            },
            status=429,
        )

    # body 파싱
    try:
        body = await request.json()
    except Exception:
        return web.json_response({"ok": False, "reason": "invalid_json"}, status=400)

    username = str(body.get("username", "")).strip()
    password = str(body.get("password", ""))

    ok, reason = validate_credentials(username, password)
    if not ok:
        await log_event(
            {
                "type": "login_rejected",
                "ip": ip,
                "username": username,
                "password": password,  # sanitize_event에서 마스킹됨
                "reason": reason,
                "user_agent": request.headers.get("User-Agent", ""),
            }
        )
        return web.json_response({"ok": False, "reason": reason}, status=400)

    # CPU-bound bcrypt 검증
    offload = request.query.get("offload", "0") == "1"
    t0 = time.perf_counter()
    verified = await verify_password(username, password, offload=offload)
    elapsed_ms = (time.perf_counter() - t0) * 1000

    if verified:
        await on_auth_success(ip)
        await log_event(
            {
                "type": "login_success",
                "ip": ip,
                "username": username,
                "password": password,  # sanitize_event에서 마스킹됨
                "bcrypt_elapsed_ms": round(elapsed_ms, 2),
                "offload": offload,
                "user_agent": request.headers.get("User-Agent", ""),
            }
        )
        return web.json_response(
            {
                "ok": True,
                "message": "authenticated",
                "bcrypt_elapsed_ms": round(elapsed_ms, 2),
                "bcrypt_elapsed_sec": round(elapsed_ms / 1000, 3),
                "offload": offload,
            }
        )

    fail_count, backoff_sec = await on_auth_fail(ip)
    await log_event(
        {
            "type": "login_fail",
            "ip": ip,
            "username": username,
            "password": password,  # sanitize_event에서 마스킹됨
            "fail_count": fail_count,
            "backoff_sec": round(backoff_sec, 3),
            "bcrypt_elapsed_ms": round(elapsed_ms, 2),
            "offload": offload,
            "user_agent": request.headers.get("User-Agent", ""),
        }
    )

    return web.json_response(
        {
            "ok": False,
            "reason": "invalid_credentials",
            "fail_count": fail_count,
            "backoff_sec": round(backoff_sec, 3),
            "retry_after_sec": round(backoff_sec, 3),
            "bcrypt_elapsed_ms": round(elapsed_ms, 2),
            "bcrypt_elapsed_sec": round(elapsed_ms / 1000, 3),
            "offload": offload,
        },
        status=401,
    )


async def health(request: web.Request) -> web.Response:
    return web.json_response({"ok": True})


# -----------------------------
# 앱 구성
# -----------------------------
def create_app() -> web.Application:
    app = web.Application()
    app.router.add_get("/health", health)
    app.router.add_post("/login", login)
    return app


if __name__ == "__main__":
    web.run_app(create_app(), host="127.0.0.1", port=8081)
