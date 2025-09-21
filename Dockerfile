FROM curlimages/curl:8.2.1 AS drivers

ENV MYSQL_VERSION=8.4.0
ENV MARIADB_VERSION=1.1.10
ENV POSTGRESQL_VERSION=42.7.2
ENV JTDS_VERSION=1.3.1

RUN mkdir -p /tmp/drivers_inc
WORKDIR /tmp/drivers_inc

RUN curl -L "https://search.maven.org/remotecontent?filepath=com/mysql/mysql-connector-j/${MYSQL_VERSION}/mysql-connector-j-${MYSQL_VERSION}.jar" \
          -o "mysql-connector-j-${MYSQL_VERSION}.jar" && \
    curl -L "https://search.maven.org/remotecontent?filepath=org/mariadb/jdbc/mariadb-java-client/${MARIADB_VERSION}/mariadb-java-client-${MARIADB_VERSION}.jar" \
          -o "mariadb-java-client-${MARIADB_VERSION}.jar" && \
    curl -L "https://search.maven.org/remotecontent?filepath=org/postgresql/postgresql/${POSTGRESQL_VERSION}/postgresql-${POSTGRESQL_VERSION}.jar" \
          -o "postgresql-${POSTGRESQL_VERSION}.jar" && \
    curl -L "https://search.maven.org/remotecontent?filepath=net/sourceforge/jtds/jtds/${JTDS_VERSION}/jtds-${JTDS_VERSION}.jar" \
          -o "jtds-${JTDS_VERSION}.jar"

FROM eclipse-temurin:17.0.9_9-jre-jammy AS base

ADD docker/open-sans.tar.gz /usr/share/fonts

ENV DEBIAN_FRONTEND=noninteractive

RUN rm /var/lib/dpkg/info/libc-bin.* && \
    apt-get clean && \
    apt-get update && \
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
