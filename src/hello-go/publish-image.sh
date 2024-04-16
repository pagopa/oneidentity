#!/bin/bash

TAG=$1

echo "Build image "
sudo docker build -t uolter/oneidentity:${TAG} .

aws ecr get-login-password --region eu-south-1 | sudo docker login \
--username AWS \
--password-stdin 471112878885.dkr.ecr.eu-south-1.amazonaws.com

sudo docker tag uolter/oneidentity:${TAG} 471112878885.dkr.ecr.eu-south-1.amazonaws.com/oneid-es-1-d-ecr:${TAG}

sudo docker push 471112878885.dkr.ecr.eu-south-1.amazonaws.com/oneid-es-1-d-ecr:${TAG}