#!/bin/bash

mkdir -p my-lab/test
cd my-lab

cat > env.properties <<'EOF'
# 환경 설정 예시
APP_NAME=my-app
APP_PORT=8080
DB_HOST=localhost
DB_USER=admin
DB_PASS=secret
EOF

cat > test/app-config.t <<'EOF'
apiVersion: v1
kind: Config
metadata:
  name: ${{APP_NAME}}
spec:
  port: ${{APP_PORT}}
  db:
    host: ${{DB_HOST}}
    user: ${{DB_USER}}
    pass: ${{DB_PASS}}
EOF

cp ../render-env-template.sh ./render-env.sh
chmod +x render-env.sh

