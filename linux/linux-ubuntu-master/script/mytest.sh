#!/bin/bash

declare -a KEYS

key1="C"
key2="D"
KEYS[${#KEYS[@]}]="A"       # KEYS[0]="A"
KEYS[${#KEYS[@]}]="B"       # KEYS[1]="B"
KEYS[${#KEYS[@]}]="$key1"   # KEYS[2]="C”
KEYS+=("$key2")             # Bash 배열 추가 기능

echo "${KEYS[@]}"
echo "개수 = ${#KEYS[@]}"


