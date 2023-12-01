FROM openjdk:17-slim-bullseye

WORKDIR /app

COPY ./build/install/lombok-plugin-repository /app

VOLUME ["/app/config.yaml"]

RUN useradd -u 1000 runner &&\
 apt update &&\
 apt install findutils -y &&\
 chown -R runner:runner /app
USER 1000

ENTRYPOINT ["/app/bin/lombok-plugin-repository"]