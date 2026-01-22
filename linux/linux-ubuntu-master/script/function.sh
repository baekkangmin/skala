#!/bin/bash

# 'function' 키워드는 생략 가능
my_function() {
  echo "함수 호출됨"
  echo "전달된 인자: $@"
}

# 함수 호출 (정의보다 뒤에 위치해야 함)
my_function
my_function arg1 arg2 # 인자 전달.  출력: arg1 arg2

