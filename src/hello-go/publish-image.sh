#!/bin/bash

aws ecr get-login-password --region eu-south-1 | podman login \
--username AWS \
--password-stdin 471112878885.dkr.ecr.eu-south-1.amazonaws.com

podman tag localhost/uolter/oneidentity:2.0 471112878885.dkr.ecr.eu-south-1.amazonaws.com/oneidentity-eu-south-1-d-ecr:2.0

podman push 471112878885.dkr.ecr.eu-south-1.amazonaws.com/oneidentity-eu-south-1-d-ecr:2.0