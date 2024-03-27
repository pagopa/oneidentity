#!/bin/bash

TAG=$1

echo "Build image "
podman build -t uolter/oneidentity:${TAG} .

aws ecr get-login-password --region eu-south-1 | podman login \
--username AWS \
--password-stdin 471112878885.dkr.ecr.eu-south-1.amazonaws.com

podman tag localhost/uolter/oneidentity:${TAG} 471112878885.dkr.ecr.eu-south-1.amazonaws.com/oneidentity-eu-south-1-d-ecr:${TAG}

podman push 471112878885.dkr.ecr.eu-south-1.amazonaws.com/oneidentity-eu-south-1-d-ecr:${TAG}