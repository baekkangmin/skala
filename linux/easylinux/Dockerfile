FROM nginx:stable-alpine

# 메인 nginx 설정 파일로 교체
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

# 포그라운드 실행
CMD ["nginx", "-g", "daemon off;"]

