FROM adoptopenjdk/openjdk17:alpine
RUN apk --no-cache add curl
COPY target/maskinporten-guardian-*.jar maskinporten-guardian.jar
COPY target/classes/logback*.xml /conf/
EXPOSE 8080
CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005", "-Dcom.sun.management.jmxremote", "-Dmicronaut.bootstrap.context=true", "-Xmx256m", "-jar", "maskinporten-guardian.jar"]
