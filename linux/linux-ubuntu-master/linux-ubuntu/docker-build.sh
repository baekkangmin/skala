#!/bin/bash
IMAGE_NAME="container-linux"
VERSION="1.0"
#IS_CACHE="--no-cache"

# Docker 이미지 빌드
docker build \
  --tag ${IMAGE_NAME}:${VERSION} \
  --file Dockerfile \
  ${IS_CACHE} .
