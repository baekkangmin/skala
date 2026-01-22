#!/usr/bin/env bash
# http-handler.sh
# socat이 이 스크립트의 stdin/stdout을 소켓에 연결해줌
# stdin: HTTP 요청, stdout: HTTP 응답, stderr: 서버 로그

set -u

log() { printf '[%s] %s\n' "$(date '+%F %T')" "$*" >&2; }

# 1) 요청 라인 읽기: 예) "GET / HTTP/1.1"
# curl은 보통 \r\n을 쓰므로 \r 제거 필요
IFS= read -r request_line || exit 0
request_line=${request_line%$'\r'}
log "request_line=[$request_line]"

# 비어있으면 종료
[[ -z "$request_line" ]] && exit 0

method=$(awk '{print $1}' <<<"$request_line")
path=$(awk '{print $2}' <<<"$request_line")
version=$(awk '{print $3}' <<<"$request_line")

# 2) 헤더 읽기(빈 줄 만날 때까지)
# 필요하면 Host/Content-Length 등 파싱 가능하지만, 여기선 최소 처리
content_length=0
while IFS= read -r header; do
  header=${header%$'\r'}
  [[ -z "$header" ]] && break

  # Content-Length 파싱 (POST 등 대비)
  if [[ "$header" =~ ^Content-Length:\ (.*)$ ]]; then
    content_length="${BASH_REMATCH[1]}"
  fi
done

# 3) 바디 읽기(POST일 때 대비) - content_length 만큼 읽음
body=""
if [[ "${content_length}" -gt 0 ]]; then
  # 정확히 N바이트 읽기
  IFS= read -r -n "$content_length" body || true
  log "body=[$body]"
fi

# 4) 라우팅(간단)
status="200 OK"
resp_body=""
content_type="text/plain; charset=utf-8"

case "${method} ${path}" in
  "GET /")
    resp_body="hello from UDS HTTP server (bash+socat)"
    ;;
  "GET /health")
    resp_body="OK"
    ;;
  "POST /echo")
    resp_body="$body"
    ;;
  *)
    status="404 Not Found"
    resp_body="not found: ${method} ${path}"
    ;;
esac

# 5) HTTP 응답 출력
# Content-Length는 바이트 길이로 계산 (UTF-8 텍스트 가정)
# macOS에서도 동작하도록 wc -c 사용
content_len=$(printf '%s' "$resp_body" | wc -c | tr -d ' ')

# Connection: close 로 단순화 (keep-alive 안 함)
printf "HTTP/1.1 %s\r\n" "$status"
printf "Content-Type: %s\r\n" "$content_type"
printf "Content-Length: %s\r\n" "$content_len"
printf "Connection: close\r\n"
printf "\r\n"
printf "%s" "$resp_body"

log "responded status=[$status], len=[$content_len]"
