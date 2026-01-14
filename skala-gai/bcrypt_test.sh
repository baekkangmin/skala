#!/usr/bin/env bash
# -------------------------------------------------------------
# 작성자 : Cloud 1기 백강민
# 작성목적 : SKALA Python Day3 - Bcrypt 해시 기반 Rate Limiting 인증 시스템 (테스트 스크립트)
# 변경사항 내역 :
#   2026-01-14 - 최초 작성
# -------------------------------------------------------------

set -euo pipefail

BASE_URL="http://127.0.0.1:8081"
MAX_FAIL_STEPS=3          # 실패 누적 횟수(요구사항: 3번)
EXTRA_SLEEP_SEC=0.1      # retry_after 이후 최소 여유
DEFAULT_IP="10.0.0.1"     # 1~3번 IP
IP_A="10.0.0.31"          # 4번 IP 분리
IP_B="10.0.0.32"          # 4번 IP 분리


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

# 401을 만들고 싶을 때: 429면 retry_after만큼 기다렸다가 다시 시도해서 "401이 나올 때까지" 진행
post_until_401() {
  local url="$1"
  local json="$2"
  shift 2

  while true; do
    http_post "$url" "$json" "$@"
    local st
    st="$(status_code)"
    if [[ "$st" == "401" ]]; then
      return 0
    fi
    if [[ "$st" == "429" ]]; then
      # 차단이면 풀릴 때까지 대기 후 재시도
      sleep_retry_after
      continue
    fi
    # 예상 외(200/400 등)면 그대로 종료
    return 0
  done
}

# 200(성공)을 만들고 싶을 때: 429면 retry_after만큼 기다렸다가 다시 시도해서 "200이 나올 때까지" 진행
post_until_200() {
  local url="$1"
  local json="$2"
  shift 2

  while true; do
    http_post "$url" "$json" "$@"
    local st
    st="$(status_code)"
    if [[ "$st" == "200" ]]; then
      return 0
    fi
    if [[ "$st" == "429" ]]; then
      sleep_retry_after
      continue
    fi
    # 예상 외(401/400 등)면 그대로 종료
    return 0
  done
}

# ---------- tests ----------

title "1) Health check"
http_get "$BASE_URL/health"
show

title "2) 정상 로그인"
http_post "$BASE_URL/login" '{"username":"user1","password":"Password123!"}' -H "X-Forwarded-For: ${DEFAULT_IP}"
show

title "3) 로그인 실패 3회 시도"
for ((i=1;i<=MAX_FAIL_STEPS;i++)); do
  echo "[FAIL ROUND $i/$MAX_FAIL_STEPS] (need 401 to increase fail_count)"
  post_until_401 "$BASE_URL/login" '{"username":"user1","password":"wrong"}' -H "X-Forwarded-For: ${DEFAULT_IP}"
  show

  echo "[IMMEDIATE RETRY] expect 429"
  http_post "$BASE_URL/login" '{"username":"user1","password":"wrong"}' -H "X-Forwarded-For: ${DEFAULT_IP}"
  show
  sleep_retry_after
done

title "4) IP별 카운트 분리 (fail_count=1 확인)"
echo "[IP_A=${IP_A}]"
post_until_401 "$BASE_URL/login" '{"username":"user1","password":"wrong"}' -H "X-Forwarded-For: ${IP_A}"
show
sleep_retry_after

echo "[IP_B=${IP_B}]"
post_until_401 "$BASE_URL/login" '{"username":"user1","password":"wrong"}' -H "X-Forwarded-For: ${IP_B}"
show
sleep_retry_after

title "5) 차단 해제 후 정상 로그인 성공 확인 (429 -> 200)"
http_post "$BASE_URL/login" '{"username":"user1","password":"Password123!"}' -H "X-Forwarded-For: ${DEFAULT_IP}"
show

title "DONE"
