FROM azul/zulu-openjdk:21
RUN apt-get -qq update && apt-get -y --no-install-recommends install curl \
    && groupadd --system --gid 1000 app && useradd --system --uid 1000 --gid app app
COPY target/maskinporten-guardian-*.jar maskinporten-guardian.jar
COPY target/classes/logback*.xml /conf/
RUN chown app:app maskinporten-guardian.jar /conf/*.xml
USER app
EXPOSE 10310
CMD ["java", "-Dcom.sun.management.jmxremote", "-Dmicronaut.bootstrap.context=true", "-jar", "maskinporten-guardian.jar"]
