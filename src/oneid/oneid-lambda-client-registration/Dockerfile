## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21@sha256:e22f7e0eeb061e04075703b03f3d025b2fc5db8031f13e85401d8884416fdca6 AS build
# Copy all needed files and directories
# Parent pom
COPY --chown=quarkus:quarkus ../pom.xml /code/
# Maven wrapper files
COPY --chown=quarkus:quarkus ../mvnw /code/mvnw
COPY --chown=quarkus:quarkus ../.mvn /code/.mvn
# oneid-common, oneid-lambda-client-registration
COPY --chown=quarkus:quarkus ../oneid-common /code/oneid-common
COPY --chown=quarkus:quarkus ../oneid-lambda-client-registration /code/oneid-lambda-client-registration

USER quarkus
EXPOSE 8080

WORKDIR /code

# Build oneid-lambda-client-registration using oneid-lambda-client-registration-aggregate
# Start downloading dependencies
RUN ["./mvnw", "clean", "compile", "-P", "oneid-lambda-client-registration-aggregate"]

ENTRYPOINT ["./mvnw", "-f", "pom.xml", "-B", "quarkus:dev", "-P", "oneid-lambda-client-registration-aggregate"]

