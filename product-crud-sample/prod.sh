#!/bin/bash

# 운영 환경 실행 스크립트
# 포트: 8082

set -e

PROFILE="prod,postgres"
PORT=8082
ENV_FILE=".env.prod"

# env 로드
if [ ! -f "$ENV_FILE" ]; then
  echo "[ERROR] $ENV_FILE 파일이 없습니다."
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

echo "=========================================="
echo "  Product CRUD Application - 운영 환경"
echo "=========================================="
echo "프로파일: $PROFILE"
echo "포트: $PORT"
echo "DB: $DB_NAME ($DB_HOST:$DB_PORT)"
echo "=========================================="
echo ""

# 운영 환경은 JAR 파일이 필수
if [ ! -f "build/libs/product-crud-0.0.1-SNAPSHOT.jar" ]; then
    echo "경고: JAR 파일이 없습니다. 먼저 빌드하세요."
    echo "빌드 명령: ./gradlew build -x test"
    read -p "지금 빌드하시겠습니까? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "빌드 중..."
        ./gradlew build -x test
    else
        echo "빌드를 취소했습니다."
        exit 1
    fi
fi

echo "JAR 파일로 실행합니다..."
java -jar build/libs/product-crud-0.0.1-SNAPSHOT.jar --spring.profiles.active=$PROFILE
