FROM --platform=$BUILDPLATFORM python:3.10-alpine@sha256:b8541313282fa2e680cd19592c52aaa97adb833c7b8fcd73e22ecf0799ae549e

WORKDIR /app

COPY ../../docker_mock/dummy_client/requirements.txt /app
RUN --mount=type=cache,target=/root/.cache/pip \
    pip3 install -r requirements.txt

COPY ../../docker_mock/dummy_client /app

ENTRYPOINT ["python3"]
CMD ["oneid_dummy_client.py"]
