#!/bin/bash

# 전역 변수
global_var="Hello"

my_function() {
  # 지역 변수
  local local_var="World" # local 변수는 함수 내에서만 유효. 함수 밖의 같은 이름의 변수에 영향 X
  echo "지역 변수: $local_var"  # 출력: 지역 변수: World
  global_var="Modified Global" # 전역 변수 수정
}

my_function

echo "전역 변수: $global_var" # 출력: 전역 변수: Modified Global


# 환경 변수 (자식 스크립트에서도 사용 가능)
export MY_ENV_VAR="Environment Value"

# 자식 스크립트 실행 예시 (child.sh)
./child.sh  # child.sh 스크립트 안에서  echo $MY_ENV_VAR 실행

