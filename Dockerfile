FROM openjdk:17

WORKDIR /app

COPY build/libs/lombok-plugin-repository-*.jar /app/app.jar

VOLUME ["/app/config.yaml"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]