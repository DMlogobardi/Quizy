# ===== Stage 1: Build del WAR =====
FROM maven:3.9.2-eclipse-temurin-24 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ===== Stage 2: Runtime =====
FROM tomcat:10.1.39-jdk24
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
