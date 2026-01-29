# Docker 빌드 및 실행 가이드

## 🐳 Docker 이미지 빌드

### 방법 1: Docker 단독 사용

```bash
# 이미지 빌드
docker build -t skala-stock-api:latest .

# 컨테이너 실행
docker run -d \
  --name skala-stock-api \
  -p 9080:9080 \
  -v $(pwd)/data:/app/data \
  skala-stock-api:latest
```

### 방법 2: Docker Compose 사용 (권장)

```bash
# 빌드 및 실행
docker-compose up -d

# 빌드만 (이미지만 생성)
docker-compose build

# 로그 확인
docker-compose logs -f

# 중지
docker-compose down

# 중지 및 데이터 삭제
docker-compose down -v
```

## 📦 이미지 크기 최적화

현재 Dockerfile은 다음과 같은 최적화를 포함합니다:

1. **Multi-stage build**: 빌드 도구를 최종 이미지에서 제거
2. **Alpine Linux**: 경량 베이스 이미지 사용
3. **Gradle 캐싱**: 의존성 다운로드 캐싱
4. **JRE only**: JDK 대신 JRE 사용

예상 이미지 크기: **~200-250 MB**

## 🔧 환경 변수 설정

### application.yml 오버라이드

```bash
docker run -d \
  --name skala-stock-api \
  -p 9080:9080 \
  -v $(pwd)/data:/app/data \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/skala-stock \
  skala-stock-api:latest
```

### JVM 메모리 설정

```bash
docker run -d \
  --name skala-stock-api \
  -p 9080:9080 \
  -v $(pwd)/data:/app/data \
  -e JAVA_OPTS="-Xms256m -Xmx512m" \
  skala-stock-api:latest
```

## 📊 Health Check

컨테이너는 자동으로 헬스체크를 수행합니다:

```bash
# 헬스체크 상태 확인
docker inspect --format='{{json .State.Health}}' skala-stock-api | jq

# 수동 헬스체크
curl http://localhost:9080/actuator/health
```

## 🗄️ 데이터 영속성

H2 데이터베이스는 호스트의 `./data` 디렉토리에 저장됩니다:

```bash
# 데이터 백업
tar -czf backup-$(date +%Y%m%d).tar.gz data/

# 데이터 복원
tar -xzf backup-20260129.tar.gz
```

## 🚀 프로덕션 배포

### 이미지 태그 및 푸시

```bash
# 태그 지정
docker tag skala-stock-api:latest your-registry/skala-stock-api:1.0.0

# Docker Hub 푸시
docker push your-registry/skala-stock-api:1.0.0

# 특정 버전 실행
docker run -d \
  --name skala-stock-api \
  -p 9080:9080 \
  -v $(pwd)/data:/app/data \
  your-registry/skala-stock-api:1.0.0
```

## 🔍 트러블슈팅

### 로그 확인

```bash
# 실시간 로그
docker logs -f skala-stock-api

# 최근 100줄
docker logs --tail 100 skala-stock-api

# Docker Compose 로그
docker-compose logs -f skala-stock-api
```

### 컨테이너 내부 접근

```bash
# 쉘 접속
docker exec -it skala-stock-api sh

# 파일 확인
docker exec skala-stock-api ls -la /app/data
```

### 포트 충돌 해결

```bash
# 다른 포트로 실행
docker run -d \
  --name skala-stock-api \
  -p 8080:9080 \
  -v $(pwd)/data:/app/data \
  skala-stock-api:latest
```

## 📱 접속 URL

컨테이너 실행 후 다음 URL로 접속 가능합니다:

- **API Base URL**: http://localhost:9080
- **Swagger UI**: http://localhost:9080/swagger-ui.html
- **H2 Console**: http://localhost:9080/h2-console
- **Actuator Health**: http://localhost:9080/actuator/health

## 🔐 보안 권장사항

1. **Non-root 사용자**: Dockerfile에 이미 설정됨
2. **환경 변수로 민감 정보 관리**:
   ```bash
   docker run -d \
     --name skala-stock-api \
     --env-file .env.production \
     skala-stock-api:latest
   ```
3. **네트워크 격리**: Docker Compose의 네트워크 사용
4. **읽기 전용 루트 파일시스템** (선택사항):
   ```bash
   docker run -d \
     --read-only \
     --tmpfs /tmp \
     -v $(pwd)/data:/app/data \
     skala-stock-api:latest
   ```

## 🎯 빠른 시작

```bash
# 1. 이미지 빌드
docker-compose build

# 2. 백그라운드 실행
docker-compose up -d

# 3. 로그 확인
docker-compose logs -f

# 4. Swagger UI 접속
open http://localhost:9080/swagger-ui.html

# 5. 종료
docker-compose down
```
