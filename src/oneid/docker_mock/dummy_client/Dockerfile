FROM --platform=$BUILDPLATFORM python:3.10-alpine@sha256:4c4097e46608e9b9025a486e3b72d628ac8947a3caa20f142c2e8c0029a12b21

WORKDIR /app

COPY ../../docker_mock/dummy_client/requirements.txt /app
RUN --mount=type=cache,target=/root/.cache/pip \
    pip3 install -r requirements.txt

COPY ../../docker_mock/dummy_client /app

ENTRYPOINT ["python3"]
CMD ["oneid_dummy_client.py"]
