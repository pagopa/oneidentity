FROM amazon/aws-cli@sha256:55b1cb7f57437933bf907660bbf01f85d12737e2af67dd94256c070d53199f0a

RUN yum install -y openssl \
  && yum clean all \
  && rm -rf /var/cache/yum
