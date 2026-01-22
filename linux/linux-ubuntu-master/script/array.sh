#!/bin/bash

# 배열 선언 (declare -a는 필수는 아님)
declare -a my_array

# 배열 초기화
my_array=("apple" "banana" "cherry")

# 인덱스를 사용한 접근
echo ${my_array[0]}  # 첫 번째 요소 출력 (apple)

# 배열 전체 출력
echo ${my_array[@]}  # 모든 요소 출력 (apple banana cherry)

# 배열 길이
echo ${#my_array[@]} # 요소 개수 출력 (3)

# 요소 추가
my_array+=("date")   # 배열 끝에 요소 추가
my_array[4]="fig"    # 특정 인덱스에 요소 추가 (중간 인덱스 비워도 됨)

# 배열 복사
new_array=("${my_array[@]}")

echo ${new_array[@]}

# 요소 삭제
unset my_array[1]    # 두 번째 요소 삭제
echo ${my_array[@]}

unset my_array       # 배열 전체 삭제
echo "unset all" ${my_array[@]}

