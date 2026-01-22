#!/bin/bash

PIPE="/tmp/my.pipe"

echo "FIFO 대기 중: $PIPE"

while true; do
    while IFS= read -r line < "$PIPE"; do
        echo "처리: $line"
    done
done
