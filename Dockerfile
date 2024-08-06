FROM azul/zulu-openjdk:21
RUN apt-get -qq update && apt-get -y --no-install-recommends install curl
COPY target/maskinporten-guardian-*.jar maskinporten-guardian.jar
COPY target/classes/logback*.xml /conf/
EXPOSE 10310
CMD ["java", "-Dcom.sun.management.jmxremote", "-Dmicronaut.bootstrap.context=true", "-jar", "maskinporten-guardian.jar"]
