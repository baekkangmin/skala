#!/bin/bash
#
# integer-comparison.sh
# Bash 정수 비교 실습 예제
#

a=10
b=20

echo "=== 정수 비교 시작 ==="
echo "a = $a"
echo "b = $b"
echo

###############################################
# 단일 bracket [ ]: -eq, -ne, -gt, -ge, -lt, -le
###############################################
echo "[ ] (POSIX test) 방식"
[ $a -eq $b ] && echo "[ ]: a == b"        || echo "[ ]: a != b"
[ $a -ne $b ] && echo "[ ]: a != b"
[ $a -gt $b ] && echo "[ ]: a > b"         || echo "[ ]: a <= b"
[ $a -ge $b ] && echo "[ ]: a >= b"        || echo "[ ]: a < b"
[ $a -lt $b ] && echo "[ ]: a < b"         || echo "[ ]: a >= b"
[ $a -le $b ] && echo "[ ]: a <= b"        || echo "[ ]: a > b"
echo

###############################################
# double bracket [[ ]] 방식
# 정수 비교에서 -eq 계열도 되고, > < >= <= 사용도 가능
###############################################
echo "[[ ]] (Bash 조건 확장) 방식"
[[ $a == $b ]] && echo "[[ ]]: a == b"   || echo "[[ ]]: a != b"
[[ $a != $b ]] && echo "[[ ]]: a != b"
[[ $a > $b ]]  && echo "[[ ]]: a > b"     || echo "[[ ]]: a <= b"
[[ $a -ge $b ]] && echo "[[ ]]: a >= b"   || echo "[[ ]]: a < b"
[[ $a < $b ]]  && echo "[[ ]]: a < b"     || echo "[[ ]]: a >= b"
[[ $a -le $b ]] && echo "[[ ]]: a <= b"   || echo "[[ ]]: a > b"
echo

###############################################
# (( )) 산술 비교 (C-style)
# == != > < >= <=
###############################################
echo "(( )) C-style arithmetic comparison"
(( a == b )) && echo "(( )): a == b"      || echo "(( )): a != b"
(( a != b )) && echo "(( )): a != b"
(( a > b ))  && echo "(( )): a > b"       || echo "(( )): a <= b"
(( a >= b )) && echo "(( )): a >= b"      || echo "(( )): a < b"
(( a < b ))  && echo "(( )): a < b"       || echo "(( )): a >= b"
(( a <= b )) && echo "(( )): a <= b"      || echo "(( )): a > b"
echo

echo "=== 정수 비교 종료 ==="

