FROM azul/zulu-openjdk:21
# Distrust the compromised eMudhra intermediates (https://emudhra.we-have-a-problem.com/)
COPY docker/emudhra-blocked.certs /tmp/emudhra-blocked.certs
RUN apt-get -qq update && apt-get -y --no-install-recommends install curl ca-certificates \
    && sed -i -e '$a\' "$JAVA_HOME/lib/security/blocked.certs" \
    && cat /tmp/emudhra-blocked.certs >> "$JAVA_HOME/lib/security/blocked.certs" && rm /tmp/emudhra-blocked.certs \
    && groupadd --system --gid 1000 app && useradd --system --uid 1000 --gid app app
COPY target/maskinporten-guardian-*.jar maskinporten-guardian.jar
COPY target/classes/logback*.xml /conf/
RUN chown app:app maskinporten-guardian.jar /conf/*.xml
USER app
EXPOSE 10310
CMD ["java", "-Dcom.sun.management.jmxremote", "-Dmicronaut.bootstrap.context=true", "-jar", "maskinporten-guardian.jar"]
