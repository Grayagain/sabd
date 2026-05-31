FROM gradle:9.2.0-jdk17 AS builder

WORKDIR /workspace
COPY . .

RUN gradle --no-daemon :app:installDist

FROM eclipse-temurin:8-jre

WORKDIR /app
COPY --from=builder /workspace/app/build/install/app/ /app/
COPY --from=builder /workspace/app/public.cer /app/public.cer
COPY --from=builder /workspace/app/private.p12 /app/private.p12

ENTRYPOINT ["/app/bin/app"]
