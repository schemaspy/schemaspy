FROM openjdk:8-jre-alpine

ENV LC_ALL=C

RUN adduser java -h / -D && \
    set -x && \
    apk add --no-cache curl git unzip graphviz fontconfig openjdk8="$JAVA_ALPINE_VERSION" && \
    mkdir -p /opt/build && cd /opt/build && \
    curl https://www.fontsquirrel.com/fonts/download/open-sans -J -O && \
    unzip open-sans.zip -d /usr/share/fonts && \
    fc-cache -fv && \
    git clone https://github.com/schemaspy/schemaspy.git && \
    cd schemaspy && \
    ./mvnw package && \
    cp target/schemaspy*.jar / && \
    cd / && \doc
    rm -rf /opt/build && \
    rm -rf ~/.m2 && \
    apk del curl git unzip openjdk8 && \
    mkdir /drivers && \
    mkdir /output && \
    mkdir /config && \
    chown -R java /schema* && \
    chown -R java /drivers && \
    chown -R java /output && \
    chown -R java /config

USER java

WORKDIR /

VOLUME /drivers /output /config

ENTRYPOINT ["java","-cp","*:/drivers/*","org.springframework.boot.loader.JarLauncher", "-o", "/output"]