## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21@sha256:5f2a1c5004b1fd7996d8a04c8e8798db57827fbd92629690ec6a7ffe319993ac AS build

WORKDIR /code

# Copy all needed files and directories
# Parent pom
COPY --chown=quarkus:quarkus ../pom.xml .
# Maven wrapper files
COPY --chown=quarkus:quarkus ../mvnw .
COPY --chown=quarkus:quarkus ../.mvn ./.mvn
# oneid-common and oneid-ecs-internal-idp
COPY --chown=quarkus:quarkus ../oneid-common ./oneid-common
COPY --chown=quarkus:quarkus ../oneid-ecs-internal-idp ./oneid-ecs-internal-idp

USER quarkus

ARG SKIP_DEPCHECK=false

# Build oneid-ecs-internal-idp using oneid-ecs-internal-idp-aggregate
# Start downloading dependencies
RUN \
  --mount=type=secret,id=maven_settings,target=/run/secrets/maven_settings,required=false,uid=1001,gid=1001,mode=0400 \
  --mount=type=cache,target=/tmp/.build-cache,sharing=locked,uid=1001,gid=1001 \
  if [ "$SKIP_DEPCHECK" = "true" ]; then \
    ./mvnw -f pom.xml -B verify clean -P oneid-ecs-internal-idp-aggregate -Dmaven.repo.local=/tmp/.build-cache --fail-never -DskipDepcheck=true; \
  else \
    if [ ! -f /run/secrets/maven_settings ]; then \
      echo "ERROR: depcheck is enabled but Maven settings secret is missing. Provide BuildKit secret id=maven_settings."; \
      exit 1; \
    fi; \
    ./mvnw -f pom.xml -B verify clean -P oneid-ecs-internal-idp-aggregate -Dmaven.repo.local=/tmp/.build-cache --fail-never -s /run/secrets/maven_settings; \
  fi

RUN \
  --mount=type=secret,id=maven_settings,target=/run/secrets/maven_settings,required=false,uid=1001,gid=1001,mode=0400 \
  --mount=type=cache,target=/tmp/.build-cache,sharing=locked,uid=1001,gid=1001 \
  if [ "$SKIP_DEPCHECK" = "true" ]; then \
    ./mvnw -f pom.xml -B package -P oneid-ecs-internal-idp-aggregate -Dmaven.repo.local=/tmp/.build-cache -Dmaven.test.skip=true -DskipDepcheck=true; \
  else \
    if [ ! -f /run/secrets/maven_settings ]; then \
      echo "ERROR: depcheck is enabled but Maven settings secret is missing. Provide BuildKit secret id=maven_settings."; \
      exit 1; \
    fi; \
    ./mvnw -f pom.xml -B package -P oneid-ecs-internal-idp-aggregate -Dmaven.repo.local=/tmp/.build-cache -Dmaven.test.skip=true -s /run/secrets/maven_settings; \
  fi


## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:1.20-2@sha256:6a3242526aebd99245eee76feb55c0b9a10325cddfc9530b24c096064a5ed81e

ARG IDP_ASSETS_BASE_URL
ENV IDP_ASSETS_BASE_URL=${IDP_ASSETS_BASE_URL}
ARG IDP_LOGIN_ENDPOINT
ENV IDP_LOGIN_ENDPOINT=${IDP_LOGIN_ENDPOINT}
ARG IDP_CONSENT_ENDPOINT
ENV IDP_CONSENT_ENDPOINT=${IDP_CONSENT_ENDPOINT}
ARG ACS_ENDPOINT
ENV ACS_ENDPOINT=${ACS_ENDPOINT}

COPY --from=build /code/oneid-ecs-internal-idp/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /code/oneid-ecs-internal-idp/target/quarkus-app/*.jar /deployments/
COPY --from=build /code/oneid-ecs-internal-idp/target/quarkus-app/app/ /deployments/app/
COPY --from=build /code/oneid-ecs-internal-idp/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8082
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
