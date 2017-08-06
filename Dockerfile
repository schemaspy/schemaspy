FROM openjdk:8u111-jre-alpine

ENV LC_ALL=C

ARG GIT_BRANCH=local
ARG GIT_REVISION=local

ENV MYSQL_VERSION=6.0.6
ENV MARIADB_VERSION=1.1.10
ENV POSTGRESQL_VERSION=42.1.1
ENV JTDS_VERSION=1.3.1

LABEL MYSQL_VERSION=$MYSQL_VERSION
LABEL MARIADB_VERSION=$MARIADB_VERSION
LABEL POSTGRESQL_VERSION=$POSTGRESQL_VERSION
LABEL JTDS_VERSION=$JTDS_VERSION

LABEL GIT_BRANCH=$GIT_BRANCH
LABEL GIT_REVISION=$GIT_REVISION

RUN adduser java -h / -D && \
    set -x && \
    apk add --no-cache curl unzip graphviz fontconfig && \
    cd /tmp && \
    curl https://www.fontsquirrel.com/fonts/download/open-sans -J -O && \
    unzip open-sans.zip -d /usr/share/fonts && \
    fc-cache -fv && \
    cd / && \
    rm -f /tmp/open-sans.zip && \
    mkdir /drivers_inc && \
    cd /drivers_inc && \
    curl -JLO http://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/$MYSQL_VERSION/mysql-connector-java-$MYSQL_VERSION.jar && \
    curl -JLO http://search.maven.org/remotecontent?filepath=org/mariadb/jdbc/mariadb-java-client/$MARIADB_VERSION/mariadb-java-client-$MARIADB_VERSION.jar && \
    curl -JLO http://search.maven.org/remotecontent?filepath=org/postgresql/postgresql/$POSTGRESQL_VERSION.jre7/postgresql-$POSTGRESQL_VERSION.jre7.jar && \
    curl -JLO http://search.maven.org/remotecontent?filepath=net/sourceforge/jtds/jtds/$JTDS_VERSION/jtds-$JTDS_VERSION.jar && \
    mkdir /drivers && \
    mkdir /output && \
    mkdir /config && \
    chown -R java /drivers_inc && \
    chown -R java /drivers && \
    chown -R java /output && \
    chown -R java /config && \
    apk del curl


ADD target/schema*.jar /
ADD docker/entrypoint.sh /

USER java
WORKDIR /

VOLUME /drivers /output /config

ENTRYPOINT ["/entrypoint.sh"]
