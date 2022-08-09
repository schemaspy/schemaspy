FROM registry.access.redhat.com/ubi8/openjdk-17:1.14-3

ENV LC_ALL=C

ARG GIT_BRANCH=local
ARG GIT_REVISION=local

ENV MYSQL_VERSION=8.0.28
ENV MARIADB_VERSION=1.1.10
ENV POSTGRESQL_VERSION=42.3.8
ENV JTDS_VERSION=1.3.1

LABEL MYSQL_VERSION=$MYSQL_VERSION
LABEL MARIADB_VERSION=$MARIADB_VERSION
LABEL POSTGRESQL_VERSION=$POSTGRESQL_VERSION
LABEL JTDS_VERSION=$JTDS_VERSION

LABEL GIT_BRANCH=$GIT_BRANCH
LABEL GIT_REVISION=$GIT_REVISION

USER root

ADD docker/open-sans.tar.gz /usr/share/fonts/

RUN set -x && \
    microdnf install -y unzip graphviz fontconfig && \
    fc-cache -fv && \
    mkdir /drivers_inc && \
    cd /drivers_inc && \
    curl -JLO https://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/$MYSQL_VERSION/mysql-connector-java-$MYSQL_VERSION.jar && \
    curl -JLO https://search.maven.org/remotecontent?filepath=org/mariadb/jdbc/mariadb-java-client/$MARIADB_VERSION/mariadb-java-client-$MARIADB_VERSION.jar && \
    curl -JLO https://search.maven.org/remotecontent?filepath=org/postgresql/postgresql/$POSTGRESQL_VERSION/postgresql-$POSTGRESQL_VERSION.jar && \
    curl -JLO https://search.maven.org/remotecontent?filepath=net/sourceforge/jtds/jtds/$JTDS_VERSION/jtds-$JTDS_VERSION.jar && \
    mkdir /output && \
    chown -R jboss /drivers_inc && \
    chown -R jboss /output


ADD target/schema*-app.jar /usr/local/lib/schemaspy/
ADD docker/schemaspy.sh /usr/local/bin/schemaspy

USER jboss
WORKDIR /

ENV SCHEMASPY_DRIVERS=/drivers
ENV SCHEMASPY_OUTPUT=/output

ENTRYPOINT ["/usr/local/bin/schemaspy"]
