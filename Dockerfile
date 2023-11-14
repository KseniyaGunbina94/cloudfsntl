FROM openjdk:19-alpine as base

RUN apk add bash vim curl wget jq docker git tar unzip bash-completion ca-certificates

RUN apk -U add --no-cache curl; \
    curl https://downloads.gradle.org/distributions/gradle-8.4-bin.zip > gradle.zip; \
    unzip gradle.zip; \
    rm gradle.zip; \
    apk del curl; \
    apk update && apk add --no-cache libstdc++ && rm -rf /var/cache/apk/*

ENV PATH "$PATH:/gradle-8.4/bin/"
WORKDIR /home/app
COPY . .
EXPOSE 8080
ENTRYPOINT [ "gradle", "run" ]