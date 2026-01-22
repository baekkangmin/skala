#!/usr/bin/env bash
# stdin: 클라이언트가 보낸 데이터
# stdout: 클라이언트로 보낼 데이터
log() { printf '[%s] %s\n' "$(date '+%F %T')" "$*" >&2; }

log "received..."

echo "first message: welcome"

while IFS= read -r line; do
	echo "recived from client: $line" >&2
	echo "you said: $line"
	[[ "$line" == "quit" ]] && break
done
