#!/bin/bash
#
# file-test.sh
# Bash 파일 테스트 연산자 실습 코드
#

echo "===== 파일 테스트 실습 ====="

# 샘플 파일/디렉토리/링크 준비
touch sample.txt            # 빈 파일 생성
echo "hello" > data.txt     # 내용 있는 파일 생성
mkdir sample.d              # 디렉토리 생성
ln -s data.txt link.txt     # 심볼릭링크
touch old.log
sleep 1                     # 시간차
touch new.log               # newer file

echo
echo "--- 테스트 대상 준비됨 ---"
ls -l
echo

############################################################
# -e : 파일이 존재하는가?
############################################################
echo "[ -e sample.txt ] → 파일 존재?"
[ -e sample.txt ] && echo "YES (존재함)" || echo "NO (없음)"
echo

echo "[ -e /etc ] → 파일 존재?"
[ -e /etc ] && echo "YES (존재함)" || echo "NO (없음)"
echo

echo "[ -e /etc/null ] → 파일 존재?"
[ -e /etc/null ] && echo "YES (존재함)" || echo "NO (없음)"
echo

############################################################
# -f : 일반 파일인가?
############################################################
echo "[ -f sample.txt ] → 일반 파일?"
[ -f sample.txt ] && echo "YES" || echo "NO"

echo "[ -f sample.d ] → 일반 파일?"
[ -f sample.d ] && echo "YES" || echo "NO"
echo

echo "[ -e /etc ] → 파일 존재?"
[ -f /etc ] && echo "YES (존재함)" || echo "NO (없음)"
echo

echo "[ -e /etc/null ] → 파일 존재?"
[ -f /etc/null ] && echo "YES (존재함)" || echo "NO (없음)"
echo

############################################################
# -d : 디렉토리인가?
############################################################
echo "[ -d sample.d ] → 디렉토리?"
[ -d sample.d ] && echo "YES" || echo "NO"
echo

############################################################
# -s : 파일 크기가 0보다 큰가?
############################################################
echo "[ -s sample.txt ] → 크기 > 0?"
[ -s sample.txt ] && echo "YES" || echo "NO"

echo "[ -s data.txt ] → 크기 > 0?"
[ -s data.txt ] && echo "YES" || echo "NO"
echo

############################################################
# -r, -w, -x : 읽기/쓰기/실행 권한 테스트
############################################################
echo "[ -r data.txt ] → 읽기 가능?"
[ -r data.txt ] && echo "YES" || echo "NO"

echo "[ -w data.txt ] → 쓰기 가능?"
[ -w data.txt ] && echo "YES" || echo "NO"

echo "[ -x data.txt ] → 실행 가능?"
[ -x data.txt ] && echo "YES" || echo "NO"
echo

############################################################
# -h 또는 -L : 심볼릭 링크 확인
############################################################
echo "[ -L link.txt ] → 심볼릭 링크?"
[ -L link.txt ] && echo "YES" || echo "NO"
echo

############################################################
# 파일 날짜 비교 -nt (newer than), -ot (older than)
############################################################
echo "[ new.log -nt old.log ] → new.log 가 더 최신?"
[ new.log -nt old.log ] && echo "YES" || echo "NO"

echo "[ old.log -ot new.log ] → old.log 가 더 오래됨?"
[ old.log -ot new.log ] && echo "YES" || echo "NO"
echo

############################################################
# -ef : 동일 파일(하드링크 체크)
############################################################
echo "하드 링크 생성 테스트"
ln sample.txt hardlink.txt 2>/dev/null

echo "[ sample.txt -ef hardlink.txt ] → 동일 inode?"
[ sample.txt -ef hardlink.txt ] && echo "YES (같은 파일)" || echo "NO"
echo

echo "===== 테스트 종료 ====="

