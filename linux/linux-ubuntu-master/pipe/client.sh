#!/bin/bash

PIPE="/tmp/my.pipe"

# FIFO가 없으면 안내 후 종료
if [ ! -p "$PIPE" ]; then
  echo "FIFO가 없습니다: $PIPE"
  echo "먼저 server.sh를 실행하거나, mkfifo $PIPE 로 생성하세요."
  exit 1
fi


echo "전달 메시지를 입력하세요. 중단하려면 quit, q를 입력하세요"
while true; do
    read -rp "send message (q|quit)> " msg

  # 종료 조건
  case "$msg" in
    q|quit)
      echo "종료합니다."
      break
      ;;
  esac

  # FIFO로 전송
  # (여러 줄 방지를 위해 항상 한 줄로 write)
  printf 'client: %s\n' "$msg" > "$PIPE"
done
