FROM ubuntu:22.04
ARG DEBIAN_FRONTEND="noninteractive"

RUN apt-get update
RUN apt-get install -y nginx curl sudo npm git wget tar gzip
RUN apt-get install -y nodejs jq net-tools netcat vim maven gradle lsof 
 RUN apt-get install -y python3 python3-pip  python3-venv

RUN apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN pip3 install fastapi uvicorn psutil python-multipart

ENV TZ="Asia/Seoul"

ENV TZ="Asia/Seoul"
