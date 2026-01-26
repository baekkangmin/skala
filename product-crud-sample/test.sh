#!/bin/bash

# 테스트 환경 실행 스크립트
# 포트: 8081

PROFILE="test"
PORT=8081

echo "=========================================="
echo "  Product CRUD Application - 테스트 환경"
echo "=========================================="
echo "프로파일: $PROFILE"
echo "포트: $PORT"
echo "=========================================="
echo ""

# JAR 파일이 있으면 JAR로 실행, 없으면 Gradle로 실행
if [ -f "build/libs/product-crud-0.0.1-SNAPSHOT.jar" ]; then
    echo "JAR 파일로 실행합니다..."
    java -jar build/libs/product-crud-0.0.1-SNAPSHOT.jar --spring.profiles.active=$PROFILE
else
    echo "Gradle로 실행합니다..."
    ./gradlew bootRun --args="--spring.profiles.active=$PROFILE"
fi
