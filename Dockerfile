FROM openjdk:8u111-jre-alpine

ENV LC_ALL=C

ARG GIT_BRANCH=local
ARG GIT_REVISION=local

ENV MYSQL_VERSION=8.0.16
ENV MARIADB_VERSION=1.1.10
ENV POSTGRESQL_VERSION=42.2.25
ENV JTDS_VERSION=1.3.1

LABEL MYSQL_VERSION=$MYSQL_VERSION
LABEL MARIADB_VERSION=$MARIADB_VERSION
LABEL POSTGRESQL_VERSION=$POSTGRESQL_VERSION
LABEL JTDS_VERSION=$JTDS_VERSION

LABEL GIT_BRANCH=$GIT_BRANCH
LABEL GIT_REVISION=$GIT_REVISION

ADD docker/open-sans.tar.gz /usr/share/fonts/

RUN adduser java -h / -D && \
    set -x && \
    apk add --no-cache curl unzip graphviz fontconfig && \
    fc-cache -fv && \
    mkdir /drivers_inc && \
    cd /drivers_inc && \
    curl -JLO http://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/$MYSQL_VERSION/mysql-connector-java-$MYSQL_VERSION.jar && \
    curl -JLO http://search.maven.org/remotecontent?filepath=org/mariadb/jdbc/mariadb-java-client/$MARIADB_VERSION/mariadb-java-client-$MARIADB_VERSION.jar && \
    curl -JLO http://search.maven.org/remotecontent?filepath=org/postgresql/postgresql/$POSTGRESQL_VERSION.jre7/postgresql-$POSTGRESQL_VERSION.jre7.jar && \
    curl -JLO http://search.maven.org/remotecontent?filepath=net/sourceforge/jtds/jtds/$JTDS_VERSION/jtds-$JTDS_VERSION.jar && \
    mkdir /output && \
    chown -R java /drivers_inc && \
    chown -R java /output && \
    apk del curl


ADD target/schema*.jar /usr/local/lib/schemaspy/
ADD docker/schemaspy.sh /usr/local/bin/schemaspy

USER java
WORKDIR /

ENV SCHEMASPY_DRIVERS=/drivers
ENV SCHEMASPY_OUTPUT=/output

ENTRYPOINT ["/usr/local/bin/schemaspy"]
