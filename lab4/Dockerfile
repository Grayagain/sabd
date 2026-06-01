FROM gradle:6.9.4-jdk11 AS build

WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY settings.gradle settings.gradle
COPY gradle.properties gradle.properties
COPY app app

RUN ./gradlew --no-daemon :app:test :app:bootJar

FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=build /workspace/app/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
