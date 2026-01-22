#!/bin/bash

# 함수 정의
print_func() {
    echo "FUNCNAME: $FUNCNAME"
}

echo "=== 기본 환경 변수 ==="
echo "HOME: $HOME"
echo "PATH: $PATH"
echo "PWD: $PWD"
echo "USER: $USER"
echo "UID: $UID"
echo "SHELL: $SHELL"

echo
echo "=== Special 변수 확인 ==="
echo "RANDOM: $RANDOM"
echo "프로세스 ID (\$\$): $$"

# 명령 실행 상태 테스트용
ls > /dev/null
echo "마지막 명령의 종료 상태 (\$?): $?"

# 백그라운드 명령 실행
sleep 1 &
echo "마지막 백그라운드 PID (\$!): $!"

# 함수 호출
print_func

# 실행 시간 확인용
sleep 2
echo "스크립트 실행 시간 (SECONDS): $SECONDS"

