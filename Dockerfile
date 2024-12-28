ARG APPLICATION_JRE_VERSION="17.0.9_9"

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


FROM eclipse-temurin:${APPLICATION_JRE_VERSION}-jre-alpine

ARG APPLICATION_USER=schemaspy
ARG APPLICATION_USER_ID=1000
ARG APPLICATION_GROUP=${APPLICATION_USER}
ARG APPLICATION_GROUP_ID=${APPLICATION_USER_ID}

ENV SCHEMASPY_DRIVERS=/drivers
ENV SCHEMASPY_OUTPUT=/output

RUN \
  set -eux \
# extra packages
; apk add --update --no-cache \
    graphviz \
    font-opensans \
    tini \
# dedicated user and group
; addgroup -g "${APPLICATION_GROUP_ID}" -S "${APPLICATION_GROUP}" \
; adduser -u "${APPLICATION_USER_ID}" -S -D -G "${APPLICATION_GROUP}" -H -h "$SCHEMASPY_OUTPUT" -s /bin/sh "${APPLICATION_USER}" \
# extra directories
; mkdir /usr/local/lib/schemaspy/ \
; mkdir "$SCHEMASPY_OUTPUT" \
; chown -R "${APPLICATION_USER}":"${APPLICATION_GROUP}" "$SCHEMASPY_OUTPUT"

COPY --from=drivers /drivers_inc /drivers_inc
ADD target/schema*-app.jar /usr/local/lib/schemaspy/schemaspy-app.jar
ADD docker-entrypoint.sh /usr/local/bin/schemaspy

WORKDIR /
VOLUME ${SCHEMASPY_OUTPUT}
USER ${APPLICATION_USER}

ENTRYPOINT ["/sbin/tini", "--", "/usr/local/bin/schemaspy"]
