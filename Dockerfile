# ===== Stage 1: Build =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copia il pom e scarica le dipendenze (ottimizza la cache)
COPY pom.xml .
RUN mvn -B dependency:resolve

# 2. Copia tutti i sorgenti
COPY src ./src

# 3. SOSTITUZIONE FORZATA:
# Prendiamo il file dal tuo PC e lo sovrascriviamo nel container.
# Il primo percorso è relativo alla cartella dove si trova il Dockerfile sul tuo PC.
COPY src/main/resources/META-INF/persistence.xml src/main/resources/META-INF/persistence.xml

# 4. Build del file WAR
RUN mvn -B clean package -DskipTests

# ===== Stage 2: Runtime (Qui usiamo solo Tomcat) =====
FROM tomcat:10.1-jdk21
WORKDIR /usr/local/tomcat

# Rimuovi la cartella ROOT predefinita per evitare conflitti
RUN rm -rf webapps/ROOT

# Copia il file WAR generato nello Stage 1
COPY --from=build /app/target/*.war webapps/ROOT.war

EXPOSE 8080
