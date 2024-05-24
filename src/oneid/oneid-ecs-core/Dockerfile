## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21 AS build
# Copy all needed files and directories
# Parent pom
COPY  --chown=quarkus:quarkus ../pom.xml /code/
# Maven wrapper files
COPY  --chown=quarkus:quarkus ../mvnw /code/mvnw
COPY  --chown=quarkus:quarkus ../.mvn /code/.mvn
# oneid-common, oneid-ecs-core
COPY --chown=quarkus:quarkus ../oneid-common /code/oneid-common
COPY --chown=quarkus:quarkus ../oneid-ecs-core /code/oneid-ecs-core

USER quarkus

WORKDIR /code

# Build oneid-ecs-core using oneid-ecs-core-aggregate and native profile
RUN ./mvnw -f pom.xml -B package -P oneid-ecs-core-aggregate,native

## Stage 2 : create the docker final image
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/
COPY --from=build /code/oneid-ecs-core/target/*-runner /work/application

# set up permissions for user `1001`
RUN chmod 775 /work /work/application \
  && chown -R 1001 /work \
  && chmod -R "g+rwX" /work \
  && chown -R 1001:root /work

EXPOSE 8080
USER 1001

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]