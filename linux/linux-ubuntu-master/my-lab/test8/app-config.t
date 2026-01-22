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
