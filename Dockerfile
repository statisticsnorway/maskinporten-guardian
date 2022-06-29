FROM azul/zulu-openjdk:17
RUN apt-get -qq update && apt-get -y --no-install-recommends install curl
COPY target/maskinporten-guardian-*.jar maskinporten-guardian.jar
COPY target/classes/logback*.xml /conf/
EXPOSE 10310
CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005", "-Dcom.sun.management.jmxremote", "-Dmicronaut.bootstrap.context=true", "-jar", "maskinporten-guardian.jar"]
