#!/bin/bash
# bash 3.2 기준 템플릿 렌더링 스크립트
# env.properties -> 환경 변수 로딩 + ./test/*.t -> *.yaml 생성

ENV_FILE="env.properties"
TEMPLATE_DIR="./test"

# 1) env.properties 존재 여부 확인
if [ ! -f "$ENV_FILE" ]; then
    echo "ERROR: $ENV_FILE 파일이 없습니다." >&2
    exit 1
fi

if [ ! -d "$TEMPLATE_DIR" ]; then
    echo "ERROR: $TEMPLATE_DIR 디렉터리가 없습니다." >&2
    exit 1
fi

echo "[1] $ENV_FILE 읽어서 환경 변수 등록 중..."

# 2) KEY 목록을 저장할 배열 (bash 3.2에서도 사용 가능: 인덱스 배열)
KEYS=()

# 3) env.properties 한 줄씩 읽어서 KEY=VALUE 처리
#    - 빈 줄, # 로 시작하는 줄은 무시
while IFS='=' read -r key value; do
    # 앞뒤 공백 제거 (아주 간단한 방식)
    key="${key%%[[:space:]]*}"
    value="${value##[[:space:]]}"

    # 빈 줄 혹은 주석(#)인 경우 스킵
    case "$key" in
        ""|\#*) continue ;;
    esac

    # KEY를 환경변수로 export (예: APP_NAME=my-app)
    export "$key=$value"

    # 나중에 키 목록을 돌기 위해 배열에 저장
    KEYS[${#KEYS[@]}]="$key"

    echo "  - 로드: $key=$value"
done < "$ENV_FILE"

echo
echo "[2] 템플릿(.t) 파일을 .yaml 로 렌더링합니다..."
echo "    디렉터리: $TEMPLATE_DIR"

# 4) ./test/*.t 템플릿 처리
found_template=false
for tmpl in "$TEMPLATE_DIR"/*.t; do
    # *.t 가 없을 때 그대로 문자열로 남는 경우를 방지
    if [ ! -e "$tmpl" ]; then
        continue
    fi

    found_template=true

    # 출력 파일 이름: xxx.t -> xxx.yaml
    base="${tmpl%.t}"
    out="${base}.yaml"

    echo
    echo "  - 템플릿: $tmpl"
    echo "    생성 파일: $out"

    # 원본 .t 를 그대로 복사해서 .yaml 생성
    cp "$tmpl" "$out"

    # 5) env.properties 에서 읽은 KEY 들을 하나씩 치환
    for key in "${KEYS[@]}"; do
        # 간접 참조로 VALUE 얻기 (bash 3.2에서도 지원)
        value="${!key}"

        # sed 치환 시 특수문자 (&, |)가 문제되지 않도록 escape
        esc_value="${value//&/\\&}"
        esc_value="${esc_value//|/\\|}"

        # 임시 파일을 통해 안전하게 치환
        # 패턴: ${{KEY}}  ->  VALUE
        # 주의: $ 와 { } 는 리터럴로 사용
        tmpfile="${out}.tmp.$$"

        sed "s|\${{$key}}|$esc_value|g" "$out" > "$tmpfile"
        mv "$tmpfile" "$out"
    done

    echo "    => 치환 완료"
done

if [ "$found_template" != true ]; then
    echo "경고: $TEMPLATE_DIR 안에 *.t 템플릿 파일이 없습니다."
fi

echo
echo "[완료] 모든 템플릿 처리가 끝났습니다."
