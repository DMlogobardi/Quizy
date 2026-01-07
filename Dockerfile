# ===== Stage 1: Build del WAR =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:resolve
COPY src ./src
RUN mvn -B clean package -DskipTests

# ===== Stage 2: Runtime =====
FROM tomcat:10.1-jdk21
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
