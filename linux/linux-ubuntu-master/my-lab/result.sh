#!/bin/bash
echo "==== 테스트 결과로 생성된 yaml 파일 내용 조회 ===="

find . -type f -name "*.t" -exec sh -c '
  y="${1%.t}.yaml"
  [ -f "$y" ] && echo "===== $y =====" && cat "$y"
' sh {} \;


