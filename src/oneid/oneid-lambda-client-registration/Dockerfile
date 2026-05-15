## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21@sha256:c3c6f9fb2fcb65c5661d3dae7a55718ae3845b866632cccc407d549826f45184 AS build
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
ARG SKIP_DEPCHECK=false
ENV SKIP_DEPCHECK=${SKIP_DEPCHECK}

# Build oneid-lambda-client-registration using oneid-lambda-client-registration-aggregate
# Start downloading dependencies
RUN \
	--mount=type=secret,id=maven_settings,target=/run/secrets/maven_settings,required=false \
	if [ "$SKIP_DEPCHECK" = "true" ]; then \
		./mvnw clean compile -P oneid-lambda-client-registration-aggregate -DskipDepcheck=true; \
	else \
		if [ ! -f /run/secrets/maven_settings ]; then \
			echo "ERROR: depcheck is enabled but Maven settings secret is missing. Provide BuildKit secret id=maven_settings."; \
			exit 1; \
		fi; \
		./mvnw clean compile -P oneid-lambda-client-registration-aggregate -s /run/secrets/maven_settings; \
	fi

ENTRYPOINT ["/bin/sh", "-c", "if [ \"$SKIP_DEPCHECK\" = \"true\" ]; then exec ./mvnw -f pom.xml -B quarkus:dev -P oneid-lambda-client-registration-aggregate -DskipDepcheck=true; else exec ./mvnw -f pom.xml -B quarkus:dev -P oneid-lambda-client-registration-aggregate; fi"]

