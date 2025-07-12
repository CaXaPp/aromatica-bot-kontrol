FROM maven:3.8.7-openjdk-11-slim AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim

COPY --from=build /app/target/aromatica-bot-kontrol-0.0.1-SNAPSHOT.war /app/app.war

# Команда запуска
ENTRYPOINT ["java", "-jar", "/app/app.war"]
