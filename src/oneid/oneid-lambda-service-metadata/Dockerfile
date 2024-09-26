## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21 AS build
# Copy all needed files and directories
# Parent pom
COPY --chown=quarkus:quarkus ../pom.xml /code/
# Maven wrapper files
COPY --chown=quarkus:quarkus ../mvnw /code/mvnw
COPY --chown=quarkus:quarkus ../.mvn /code/.mvn
# oneid-common, oneid-lambda-service-metadata
COPY --chown=quarkus:quarkus ../oneid-common /code/oneid-common
COPY --chown=quarkus:quarkus ../oneid-lambda-service-metadata /code/oneid-lambda-service-metadata

USER quarkus
EXPOSE 8080

WORKDIR /code

# Build oneid-lambda-service-metadata using oneid-lambda-service-metadata-aggregate
# Start downloading dependencies
RUN ["./mvnw", "clean", "compile", "-P", "oneid-lambda-service-metadata-aggregate"]

ENTRYPOINT ["./mvnw", "-f", "pom.xml", "-B", "quarkus:dev", "-P", "oneid-lambda-service-metadata-aggregate"]
