FROM openjdk:8-jre-alpine

ENV LC_ALL=C

ADD entrypoint.sh /

RUN set -o pipefail && \
    adduser java -h / -D && \
    apk update && apk add --no-cache curl unzip jq graphviz fontconfig && \
    cd /tmp && \
    curl https://www.fontsquirrel.com/fonts/download/open-sans -J -O && \
    unzip open-sans.zip -d /usr/share/fonts && \
    rm open-sans.zip && \
    cd / && \
    fc-cache -fv && \
    curl -s https://api.github.com/repos/schemaspy/schemaspy/releases/latest | jq -r ".assets[] | select(.name | test(\"jar\")) | .browser_download_url" | xargs curl -JOL && \
    echo "export MAIN_CLASS=$(unzip -p schemaspy*.jar META-INF/MANIFEST.MF | grep Main-Class | awk -F ': ' '{sub(/\r/,"",$2);print $2}')" > .env &&\
    mkdir /drivers && \
    mkdir /output && \
    mkdir /config && \
    chown -R java /schema* && \
    chown -R java /drivers && \
    chown -R java /output && \
    chown -R java /config && \
    chmod ugo+x entrypoint.sh && \
    apk del curl unzip jq && \
    rm -rf /var/cache/*



USER java

WORKDIR /

VOLUME /drivers /output /config

ENTRYPOINT ["/entrypoint.sh"]