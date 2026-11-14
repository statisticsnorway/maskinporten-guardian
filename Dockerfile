# Distroless Java 21 runtime. The JDK trust store is built from Debian's ca-certificates,
# so CA distrust fixes (e.g. eMudhra, https://emudhra.we-have-a-problem.com/) arrive with a new base image.
# Pinned by digest (includes libbrotli1 1.1.0-2+deb13u1, CVE-2026-111497); Dependabot bumps the digest.
FROM gcr.io/distroless/java21-debian13:nonroot@sha256:a2aeb5a7a714cfe928b31fdf67b2d23f503ea6b9612705af062de22e0c50ba77

WORKDIR /app

COPY target/maskinporten-guardian-*.jar maskinporten-guardian.jar
COPY target/classes/logback*.xml /conf/

EXPOSE 10310

# Run as non-root user (distroless nonroot uid=65532)
USER 65532:65532

ENTRYPOINT ["java", "-Dcom.sun.management.jmxremote", "-Dmicronaut.bootstrap.context=true", "-jar", "/app/maskinporten-guardian.jar"]
