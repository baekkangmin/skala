#!/usr/bin/env bash
set -euo pipefail

sock="/tmp/my.sock"
rm -f "$sock"

mode="${1:-stdio}"  # http | line

if [[ "$mode" == "http" ]]; then
  exec socat UNIX-LISTEN:"$sock",fork,reuseaddr EXEC:"$(pwd)/http-handler.sh"
else
  exec socat UNIX-LISTEN:"$sock",fork,reuseaddr EXEC:"$(pwd)/handler.sh"
fi
