FROM alpine:latest AS drivers

ARG MYSQL_VERSION=8.0.28
ENV MYSQL_VERSION="${MYSQL_VERSION}"

ARG MARIADB_VERSION=1.1.10
ENV MARIADB_VERSION="${MARIADB_VERSION}"

ARG POSTGRESQL_VERSION=42.3.8
ENV POSTGRESQL_VERSION="${POSTGRESQL_VERSION}"

ARG JTDS_VERSION=1.3.1
ENV JTDS_VERSION="${JTDS_VERSION}"

RUN mkdir -p /tmp/drivers_inc
WORKDIR /tmp/drivers_inc

RUN \
  set -eux \
; wget -qO "mysql-connector-java-${MYSQL_VERSION}.jar" \
    "https://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/${MYSQL_VERSION}/mysql-connector-java-${MYSQL_VERSION}.jar" \
; wget -qO "mariadb-java-client-${MARIADB_VERSION}.jar" \
    "https://search.maven.org/remotecontent?filepath=org/mariadb/jdbc/mariadb-java-client/${MARIADB_VERSION}/mariadb-java-client-${MARIADB_VERSION}.jar" \
; wget -qO "postgresql-${POSTGRESQL_VERSION}.jar" \
    "https://search.maven.org/remotecontent?filepath=org/postgresql/postgresql/${POSTGRESQL_VERSION}/postgresql-${POSTGRESQL_VERSION}.jar" \
; wget -qO "jtds-${JTDS_VERSION}.jar" \
    "https://search.maven.org/remotecontent?filepath=net/sourceforge/jtds/jtds/${JTDS_VERSION}/jtds-${JTDS_VERSION}.jar"

FROM eclipse-temurin:17.0.9_9-jre-jammy AS base

ADD docker/open-sans.tar.gz /usr/share/fonts

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y graphviz -o APT::Install-Suggests=0 -o APT::Install-Recommends=0 && \
    apt-get clean && \
    useradd -ms /bin/bash java && \
    mkdir /output && \
    chown -R java /output

USER java

FROM base
COPY --from=drivers /tmp/drivers_inc /drivers_inc
ADD target/schema*-app.jar /usr/local/lib/schemaspy/schemaspy-app.jar
ADD docker/schemaspy.sh /usr/local/bin/schemaspy

WORKDIR /

ENV SCHEMASPY_DRIVERS=/drivers
ENV SCHEMASPY_OUTPUT=/output

ENTRYPOINT ["/usr/local/bin/schemaspy"]
