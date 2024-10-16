FROM --platform=$BUILDPLATFORM python:3.10-alpine

WORKDIR /app

COPY ../../docker_mock/dummy_client/requirements.txt /app
RUN --mount=type=cache,target=/root/.cache/pip \
    pip3 install -r requirements.txt

COPY ../../docker_mock/dummy_client /app

ENTRYPOINT ["python3"]
CMD ["oneid_dummy_client.py"]
