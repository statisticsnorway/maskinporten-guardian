# Distroless Java 21 runtime. The JDK trust store is built from Debian's ca-certificates,
# so CA distrust fixes (e.g. eMudhra, https://emudhra.we-have-a-problem.com/) arrive with a new base image.
# Pinned by digest (includes Temurin 21.0.13.1 for CVE-2026-114938, zlib1g 1.3.1-1+deb13u1 for CVE-2026-85091,
# libbrotli1 1.1.0-2+deb13u1 for CVE-2026-111497); Dependabot bumps the digest.
FROM gcr.io/distroless/java21-debian13:nonroot@sha256:cddca4cc6b1dfc76142e5c1a1d00ce7a79bcc1292ad65d34227794f9d371a496

WORKDIR /app

COPY target/maskinporten-guardian-*.jar maskinporten-guardian.jar
COPY target/classes/logback*.xml /conf/

EXPOSE 10310

# Run as non-root user (distroless nonroot uid=65532)
USER 65532:65532

ENTRYPOINT ["java", "-Dcom.sun.management.jmxremote", "-Dmicronaut.bootstrap.context=true", "-jar", "/app/maskinporten-guardian.jar"]
