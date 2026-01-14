#!/usr/bin/env bash
# -------------------------------------------------------------
# 작성자 : Cloud 1기 백강민
# 작성목적 : SKALA Python Day3 - Bcrypt 해시 기반 Rate Limiting 인증 시스템 (테스트 스크립트)
# 변경사항 내역 :
#   2026-01-14 - 최초 작성
#   2026-01-15 - 3) 첫 실패는 401 출력, 이후 시도는 429만 3회 출력하도록 수정
# -------------------------------------------------------------

set -euo pipefail

BASE_URL="http://127.0.0.1:8081"
EXTRA_SLEEP_SEC=0.1      # retry_after 이후 최소 여유
DEFAULT_IP="10.0.0.1"     # 1~3번 IP
IP_A="10.0.0.33"          # 4번 IP 분리
IP_B="10.0.0.34"          # 4번 IP 분리

# 3)에서 429를 몇 번 확인할지
BLOCK_CHECKS=3

# ---------- util ----------
hr() { printf '%*s\n' 80 '' | tr ' ' '-'; }

title() {
  hr
  echo "✅ $1"
  hr
}

pretty_json() { python -m json.tool; }

TMP_HEADERS="$(mktemp)"
TMP_BODY="$(mktemp)"
trap 'rm -f "$TMP_HEADERS" "$TMP_BODY"' EXIT

http_post() {
  local url="$1"
  local json="$2"
  shift 2

  : >"$TMP_HEADERS"
  : >"$TMP_BODY"

  # shellcheck disable=SC2068
  curl -s -D "$TMP_HEADERS" -o "$TMP_BODY" \
    -X POST "$url" \
    -H "Content-Type: application/json" \
    "$@" \
    -d "$json"
}

http_get() {
  local url="$1"
  shift

  : >"$TMP_HEADERS"
  : >"$TMP_BODY"

  # shellcheck disable=SC2068
  curl -s -D "$TMP_HEADERS" -o "$TMP_BODY" \
    "$url" \
    "$@"
}

status_code() { awk 'NR==1{print $2}' "$TMP_HEADERS"; }

sleep_retry_after() {
  python - <<PY
import json,time
try:
  ra=json.load(open("$TMP_BODY")).get("retry_after_sec",0)
  time.sleep(float(ra)+$EXTRA_SLEEP_SEC)
except:
  time.sleep(0.2)
PY
}

show() {
  echo "[HTTP] $(status_code)"
  if python -m json.tool <"$TMP_BODY" >/dev/null 2>&1; then
    cat "$TMP_BODY" | pretty_json
  else
    cat "$TMP_BODY"
  fi
  echo
}

assert_status() {
  local expected="$1"
  local got
  got="$(status_code)"
  if [[ "$got" != "$expected" ]]; then
    echo "❌ 예상한 상태코드가 아님. expected=$expected got=$got"
    echo "---- response body ----"
    cat "$TMP_BODY" || true
    echo
    exit 1
  fi
}

# ---------- tests ----------

title "1) Health check"
http_get "$BASE_URL/health"
show

title "2) 정상 로그인"
http_post "$BASE_URL/login" '{"username":"user1","password":"Password123!"}' -H "X-Forwarded-For: ${DEFAULT_IP}"
show

title "3) 로그인 실패 3회 시도 -> 1회차는 401(인증 실패), 이후는 429(차단) ERROR 확인"
echo "[FIRST FAIL] expect 401 (fail_count=1, backoff=1s로 차단 시작)"
http_post "$BASE_URL/login" '{"username":"user1","password":"wrong"}' -H "X-Forwarded-For: ${DEFAULT_IP}"
show
assert_status "401"

# fail_count=1이면 서버 로직상 blocked_until=now+1s 이므로,
# 그 1초 안에 바로 재요청하면 전부 429가 나와야 함.
for ((i=1;i<=BLOCK_CHECKS;i++)); do
  echo "[BLOCK CHECK $i/$BLOCK_CHECKS] expect 429 (차단 상태 확인)"
  http_post "$BASE_URL/login" '{"username":"user1","password":"wrong"}' -H "X-Forwarded-For: ${DEFAULT_IP}"
  show
  assert_status "429"
done

# 이후 단계 진행 전에 차단이 풀리도록 기다려주면 안정적
echo "[WAIT UNBLOCK] retry_after 기반 대기 후 다음 테스트 진행"
sleep_retry_after

title "4) IP별 카운트 분리 (fail_count=1 확인)"
echo "[IP_A=${IP_A}]"
http_post "$BASE_URL/login" '{"username":"user1","password":"wrong"}' -H "X-Forwarded-For: ${IP_A}"
show
assert_status "401"
sleep_retry_after

echo "[IP_B=${IP_B}]"
http_post "$BASE_URL/login" '{"username":"user1","password":"wrong"}' -H "X-Forwarded-For: ${IP_B}"
show
assert_status "401"
sleep_retry_after

title "5) 차단 해제 후 정상 로그인 성공 확인"
http_post "$BASE_URL/login" '{"username":"user1","password":"Password123!"}' -H "X-Forwarded-For: ${DEFAULT_IP}"
show

title "DONE"
