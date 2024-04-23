#!/bin/bash

TAG=$1
REGISTRY_ENDPOINT="471112878885.dkr.ecr.eu-south-1.amazonaws.com"
IMAGE="oneid-es-1-d-idp-ecr"

echo "Build image "
sudo docker build -t uolter/oneidentity:${TAG} .

aws ecr get-login-password --region eu-south-1 | sudo docker login \
--username AWS \
--password-stdin ${REGISTRY_ENDPOINT}

sudo docker tag uolter/oneidentity:${TAG} ${REGISTRY_ENDPOINT}/${IMAGE}:${TAG}

sudo docker push ${REGISTRY_ENDPOINT}/${IMAGE}:${TAG}