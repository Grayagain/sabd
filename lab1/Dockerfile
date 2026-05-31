FROM gradle:9.2.0-jdk17 AS builder

WORKDIR /workspace
COPY . .

RUN gradle --no-daemon :app:installDist

FROM eclipse-temurin:8-jre

WORKDIR /app
COPY --from=builder /workspace/app/build/install/app/ /app/

ENTRYPOINT ["/app/bin/app"]
