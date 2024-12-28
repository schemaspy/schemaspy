ARG SCHEMASPY_JRE_VERSION="17.0.9_9"

FROM alpine:latest AS drivers

ARG MYSQL_VERSION=8.0.28
ENV MYSQL_VERSION="${MYSQL_VERSION}"

ARG MARIADB_VERSION=1.1.10
ENV MARIADB_VERSION="${MARIADB_VERSION}"

ARG POSTGRESQL_VERSION=42.3.8
ENV POSTGRESQL_VERSION="${POSTGRESQL_VERSION}"

ARG JTDS_VERSION=1.3.1
ENV JTDS_VERSION="${JTDS_VERSION}"

RUN \
  set -eux \
; mkdir -p /drivers_inc \
; cd /drivers_inc \
; wget -qO "mysql-connector-java-${MYSQL_VERSION}.jar" \
    "https://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/${MYSQL_VERSION}/mysql-connector-java-${MYSQL_VERSION}.jar" \
; wget -qO "mariadb-java-client-${MARIADB_VERSION}.jar" \
    "https://search.maven.org/remotecontent?filepath=org/mariadb/jdbc/mariadb-java-client/${MARIADB_VERSION}/mariadb-java-client-${MARIADB_VERSION}.jar" \
; wget -qO "postgresql-${POSTGRESQL_VERSION}.jar" \
    "https://search.maven.org/remotecontent?filepath=org/postgresql/postgresql/${POSTGRESQL_VERSION}/postgresql-${POSTGRESQL_VERSION}.jar" \
; wget -qO "jtds-${JTDS_VERSION}.jar" \
    "https://search.maven.org/remotecontent?filepath=net/sourceforge/jtds/jtds/${JTDS_VERSION}/jtds-${JTDS_VERSION}.jar"


FROM eclipse-temurin:${SCHEMASPY_JRE_VERSION}-jre-jammy

ARG SCHEMASPY_USER=schemaspy
ARG SCHEMASPY_USER_ID=999
ARG SCHEMASPY_GROUP=${SCHEMASPY_USER}
ARG SCHEMASPY_GROUP_ID=${SCHEMASPY_USER_ID}

ENV SCHEMASPY_DRIVERS=/drivers
ENV SCHEMASPY_OUTPUT=/output

RUN \
  set -eux \
# Dedicated user and group
; addgroup \
    --gid "${SCHEMASPY_GROUP_ID}" \
    --system \
    "${SCHEMASPY_GROUP}" \
; adduser \
    --uid "${SCHEMASPY_USER_ID}" \
    --gid "${SCHEMASPY_GROUP_ID}" \
    --system \
    --no-create-home \
    --disabled-password \
    --home "$SCHEMASPY_OUTPUT" \
    --shell /bin/sh \
    "${SCHEMASPY_USER}" \
# Extra directories
; mkdir /usr/local/lib/schemaspy/ \
; mkdir "$SCHEMASPY_OUTPUT" \
; chown -R "${SCHEMASPY_USER}":"${SCHEMASPY_GROUP}" "$SCHEMASPY_OUTPUT" \
# Extra packages
; export DEBIAN_FRONTEND=noninteractive \
; apt-get update \
; apt-get install --no-install-recommends --no-install-suggests -y \
    graphviz \
    fonts-open-sans \
    tini \
; apt-get clean \
; rm -rf /var/lib/apt/lists/*

COPY --from=drivers /drivers_inc /drivers_inc
ADD target/schema*-app.jar /usr/local/lib/schemaspy/schemaspy-app.jar
ADD docker-entrypoint.sh /usr/local/bin/schemaspy

WORKDIR /
VOLUME ${SCHEMASPY_OUTPUT}
USER ${SCHEMASPY_USER}

ENTRYPOINT ["/usr/bin/tini", "--", "/usr/local/bin/schemaspy"]
