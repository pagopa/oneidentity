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

ARG ENVIRONMENT
ARG ENVFILE=/code/oneid-ecs-core/src/main/webui/.env.${ENVIRONMENT}

# Build oneid-ecs-core using oneid-ecs-core-aggregate
# Start downloading dependencies
RUN ["./mvnw", "verify", "clean", "--fail-never"]

# This will use a workaround to load an env file, since is
# not supported in docker build
RUN export $(grep -v ^# ${ENVFILE}| xargs); ./mvnw -f pom.xml -B package -P oneid-ecs-core-aggregate

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:1.20-2
WORKDIR /work/

COPY --from=build /code/oneid-ecs-core/metadata/ /deployments/metadata/
COPY --from=build /code/oneid-ecs-core/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /code/oneid-ecs-core/target/quarkus-app/*.jar /deployments/
COPY --from=build /code/oneid-ecs-core/target/quarkus-app/app/ /deployments/app/
COPY --from=build /code/oneid-ecs-core/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
