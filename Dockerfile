FROM openjdk:8-jre-alpine

ENV LC_ALL=C

ENV MYSQL_VERSION=6.0.6

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
    curl -JLO http://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/6.0.6/mysql-connector-java-6.0.6.jar && \
    curl -JLO http://search.maven.org/remotecontent?filepath=org/mariadb/jdbc/mariadb-java-client/1.1.10/mariadb-java-client-1.1.10.jar && \
    curl -JLO http://search.maven.org/remotecontent?filepath=org/postgresql/postgresql/42.1.1.jre7/postgresql-42.1.1.jre7.jar && \
    curl -JLO http://search.maven.org/remotecontent?filepath=net/sourceforge/jtds/jtds/1.3.1/jtds-1.3.1.jar && \
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
