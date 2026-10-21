# Distroless Java 21 runtime. The JDK trust store is built from Debian's ca-certificates,
# so CA distrust fixes (e.g. eMudhra, https://emudhra.we-have-a-problem.com/) arrive with a rebuild.
FROM gcr.io/distroless/java21-debian13:nonroot

WORKDIR /app

COPY target/maskinporten-guardian-*.jar maskinporten-guardian.jar
COPY target/classes/logback*.xml /conf/

EXPOSE 10310

# Run as non-root user (distroless nonroot uid=65532)
USER 65532:65532

ENTRYPOINT ["java", "-Dcom.sun.management.jmxremote", "-Dmicronaut.bootstrap.context=true", "-jar", "/app/maskinporten-guardian.jar"]
