#!/bin/bash
#
# Bash [[ =~ ]] 정규식 및 패턴 매칭 실습
#

echo "============== 정규식(=~) 매칭 테스트 =============="

str="abc123"
echo "str=\"$str\""

# 정규식: abc + 숫자 1개 이상
if [[ $str =~ ^abc[0-9]+$ ]]; then
    echo "정규식 매칭됨 → ^abc[0-9]+$"
else
    echo "정규식 매칭되지 않음"
fi

echo
echo "============== 패턴 매칭(*, ?, [abc], [0-9]) 테스트 =============="

file="data.txt"
echo "file=\"$file\""

# *.txt 패턴 매칭
[[ $file == *.txt ]] && echo "*.txt 패턴 매칭됨"

# d로 시작?
[[ $file == d* ]] && echo "$file 은 d로 시작"

# dat?.txt (dat + 한 글자 + .txt)
[[ $file == dat?.txt ]] && echo "$file 은 dat + 한 글자 + .txt"

echo
file="bello2"
echo "file=\"$file\""

# [abc]ello? : 앞 문자가 a,b,c 중 하나 + ello + 한 글자
[[ $file == [abc]ello? ]] && echo "$file 은 a,b,c 중 하나 + ello + 한 글자"

# bello[0-9] → bello + 숫자 1개
[[ $file == bello[0-9] ]] && echo "$file 은 bello + 한 자리 숫자"

echo
echo "============== 실습 종료 =============="

