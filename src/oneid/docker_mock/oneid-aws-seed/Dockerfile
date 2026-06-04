FROM amazon/aws-cli@sha256:55b1cb7f57437933bf907660bbf01f85d12737e2af67dd94256c070d53199f0a

COPY docker_mock/oneid-aws-seed/requirements.txt /tmp/requirements.txt

RUN yum install -y openssl python3 python3-pip \
  && pip3 install --no-cache-dir -r /tmp/requirements.txt \
  && yum clean all \
  && rm -rf /var/cache/yum
