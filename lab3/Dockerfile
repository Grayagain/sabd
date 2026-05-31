FROM gradle:8.8-jdk21 AS builder

WORKDIR /workspace
COPY . .

RUN gradle --no-daemon :app:bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /workspace/app/build/libs/*.jar /app/app.jar

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["server"]
