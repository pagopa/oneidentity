## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21@sha256:5f2a1c5004b1fd7996d8a04c8e8798db57827fbd92629690ec6a7ffe319993ac AS build

WORKDIR /code

# Copy all needed files and directories
# Parent pom
COPY --chown=quarkus:quarkus ../pom.xml .
# Settings xml
COPY --chown=quarkus:quarkus ../settings.xml .
# Maven wrapper files
COPY --chown=quarkus:quarkus ../mvnw .
COPY --chown=quarkus:quarkus ../.mvn ./.mvn
# oneid-common, oneid-ecs-internal-idp
COPY --chown=quarkus:quarkus ../oneid-common ./oneid-common
COPY --chown=quarkus:quarkus ../oneid-ecs-internal-idp ./oneid-ecs-internal-idp

USER quarkus

# Default disable quinoa but take as build arg from ../docker-compose.yaml file to have it locally if needed
ARG QUARKUS_QUINOA=false
ENV QUARKUS_QUINOA=${QUARKUS_QUINOA}

# TODO debug logs, must be removed as soon as we're sure about caching
RUN \
  --mount=type=cache,target=/tmp/.build-cache,sharing=locked,uid=1001,gid=1001 \
  ls -la /tmp/.build-cache; id

# Build oneid-ecs-internal-idp using oneid-ecs-internal-idp-aggregate
# Start downloading dependencies
RUN \
  --mount=type=cache,target=/tmp/.build-cache,sharing=locked,uid=1001,gid=1001 \
  ["./mvnw", "verify", "clean", "-Dmaven.repo.local=/tmp/.build-cache", "--fail-never", "-s", "settings.xml"]

RUN \
  --mount=type=cache,target=/tmp/.build-cache,sharing=locked,uid=1001,gid=1001 \
  ./mvnw -f pom.xml -B package -P oneid-ecs-internal-idp-aggregate -s settings.xml -Dmaven.repo.local=/tmp/.build-cache -Dmaven.test.skip=true

# TODO debug logs, must be removed as soon as we're sure about caching
RUN \
  --mount=type=cache,target=/tmp/.build-cache,sharing=locked,uid=1001,gid=1001 \
  ls -la /tmp/.build-cache; id

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:1.20-2@sha256:6a3242526aebd99245eee76feb55c0b9a10325cddfc9530b24c096064a5ed81e

COPY --from=build /code/oneid-ecs-internal-idp/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /code/oneid-ecs-internal-idp/target/quarkus-app/*.jar /deployments/
COPY --from=build /code/oneid-ecs-internal-idp/target/quarkus-app/app/ /deployments/app/
COPY --from=build /code/oneid-ecs-internal-idp/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
