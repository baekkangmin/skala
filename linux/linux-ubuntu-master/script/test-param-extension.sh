#!/bin/bash
# test_param_expansion.sh

echo "======================================="
echo "기본 설정"
echo "======================================="
string="abc-efg-123-abc"
echo "string=\"$string\""

echo
echo "1) \${변수}"
echo "   -> 변수 값"
echo "echo \${string} => ${string}"

echo
echo "2) \${변수:위치}"
echo "   -> 위치부터 끝까지 부분 문자열"
echo "echo \${string:4} => ${string:4}"

echo
echo "3) \${변수:위치:길이}"
echo "   -> 위치부터 길이만큼 부분 문자열"
echo "echo \${string:4:3} => ${string:4:3}"

echo
echo "4) \${#변수}"
echo "   -> 문자열 길이"
echo "echo \${#string} => ${#string}"

echo
echo "5) \${변수:-단어} (unset 또는 null 이면 단어)"
unset unset_var
null_var=""
echo "unset_var 는 unset 상태, null_var는 빈 문자열(\"\") 상태"
echo "echo \${unset_var:-default} => ${unset_var:-default}"
echo "echo \${null_var:-default}  => ${null_var:-default}"

echo
echo "6) \${변수-단어} (unset 이면 단어, null 이면 그대로)"
unset unset_var
null_var=""
echo "echo \${unset_var-default} => ${unset_var-default}"
echo "echo \${null_var-default}  => ${null_var-default}"

echo
echo "7) \${변수:=단어} (unset 또는 null 이면 단어를 할당 후 반환)"
unset unset_var
null_var=""
echo "echo \${unset_var:=default} => ${unset_var:=default}"
echo "unset_var 값: $unset_var"
echo "echo \${null_var:=default}  => ${null_var:=default}"
echo "null_var 값: $null_var"

echo
echo "8) \${변수=단어} (unset 이면 단어를 할당 후 반환)"
unset unset_var
null_var=""
echo "echo \${unset_var=default} => ${unset_var=default}"
echo "unset_var 값: $unset_var"
echo "echo \${null_var=default}  => ${null_var=default}"
echo "null_var 값(빈 문자열 유지): '$null_var'"

echo
echo "9) \${변수:?메시지} (unset 또는 null 이면 에러 출력 후 종료)"
unset unset_var
null_var=""
echo "※ 실제로는 스크립트를 종료시키므로, 여기서는 서브셸에서 테스트"
(
  echo "  - unset_var 테스트:"
  echo "    결과 => ${unset_var:?Error(unset_var가 설정되지 않았습니다)}"
) 2>/dev/null || echo "  -> unset_var 에 대해 :? 사용 시 에러로 종료됨"
(
  echo "  - null_var 테스트:"
  echo "    결과 => ${null_var:?Error(null_var가 null 입니다)}"
) 2>/dev/null || echo "  -> null_var 가 빈 문자열이면 역시 에러로 종료됨"

echo
echo "10) \${변수?메시지} (unset 이면 에러, null 은 허용)"
unset unset_var
null_var=""
(
  echo "  - unset_var 테스트:"
  echo "    결과 => ${unset_var?Error(unset_var가 설정되지 않았습니다)}"
) 2>/dev/null || echo "  -> unset_var 에 대해 ? 사용 시 에러로 종료됨"
(
  echo "  - null_var 테스트:"
  echo "    결과 => '${null_var?Error}' (null 은 허용)"
)

echo
echo "11) \${변수:+단어}"
echo "    (변수가 설정되어 있으면 단어, 아니면 null)"
string="abc-efg-123-abc"
unset unset_var
echo "echo \${string:+exists} => ${string:+exists}"
echo "echo \${unset_var:+exists} => ${unset_var:+exists}"

echo
echo "12) \${변수+단어}  (위와 동일)"
echo "echo \${string+exists} => ${string+exists}"
echo "echo \${unset_var+exists} => ${unset_var+exists}"

echo
echo "13) \${변수#패턴}  (앞에서부터 가장 짧은 패턴 제거)"
echo "echo \${string#*-} => ${string#*-}"

echo
echo "14) \${변수##패턴} (앞에서부터 가장 긴 패턴 제거)"
echo "echo \${string##*-} => ${string##*-}"

echo
echo "15) \${변수%패턴}  (뒤에서부터 가장 짧은 패턴 제거)"
echo "echo \${string%-*} => ${string%-*}"

echo
echo "16) \${변수%%패턴} (뒤에서부터 가장 긴 패턴 제거)"
echo "echo \${string%%-*} => ${string%%-*}"

echo
echo "17) \${변수/패턴/대체}  (첫 번째 패턴만 대체)"
echo "echo \${string/abc/XYZ} => ${string/abc/XYZ}"

echo
echo "18) \${변수//패턴/대체} (모든 패턴 대체)"
echo "echo \${string//abc/XYZ} => ${string//abc/XYZ}"

echo
echo "19) \${변수/#패턴/대체} (문자열 시작 부분 패턴만 대체)"
echo "echo \${string/#abc/XYZ} => ${string/#abc/XYZ}"

echo
echo "20) \${변수/%패턴/대체} (문자열 끝 부분 패턴만 대체)"
echo "echo \${string/%abc/XYZ} => ${string/%abc/XYZ}"

echo
echo "21) \${!prefix*}, \${!prefix@} (prefix 로 시작하는 모든 변수 이름)"
var1=1
var2=2
other=3
echo "var1=1; var2=2; other=3"
echo "echo \${!var*} => ${!var*}"
echo "echo \${!var@} => ${!var@}"

echo
echo "======================================="
echo "테스트 종료"
echo "======================================="

