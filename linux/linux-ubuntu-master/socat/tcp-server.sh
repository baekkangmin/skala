#!/usr/bin/env bash

set -euo pipefail

port="8080"

mode="${1:-stdio}"  # http | line

if [[ "$mode" == "http" ]]; then
  exec socat TCP-LISTEN:"$port",fork,reuseaddr EXEC:"$(pwd)/http-handler.sh"
else
  exec socat TCP-LISTEN:"$port",fork,reuseaddr EXEC:"$(pwd)/handler.sh"
fi
