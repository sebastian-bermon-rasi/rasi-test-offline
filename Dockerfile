# --- build (Maven + JDK 8) ---
FROM maven:3.9.6-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# --- runtime (JRE 8 pequeño) ---
FROM eclipse-temurin:8-jre
ENV JAVA_OPTS="-Xms128m -Xmx384m"
WORKDIR /app
# copia el jar cualquiera que sea el nombre:
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
