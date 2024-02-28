#!/bin/bash

aws ecr get-login-password --region eu-south-1 | podman login \
--username AWS \
--password-stdin 471112878885.dkr.ecr.eu-south-1.amazonaws.com

podman tag localhost/uolter/hello-go:1.0 471112878885.dkr.ecr.eu-south-1.amazonaws.com/oneidentity-eu-south-1-d-ecr:1.0

podman push 471112878885.dkr.ecr.eu-south-1.amazonaws.com/oneidentity-eu-south-1-d-ecr:1.0