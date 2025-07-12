FROM maven:3.8.6-openjdk-11-slim AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests && ls -l target/

FROM openjdk:11-jre-slim

COPY --from=build /app/target/crm-2.7.6.war /app/app.war

ENTRYPOINT ["java", "-jar", "/app/app.war"]
