FROM openjdk:11-jre-slim

COPY target/aromatica-bot-kontrol-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
