# --- build (Maven + JDK 8) ---
FROM maven:3.9.6-eclipse-temurin-8 AS build
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
# fuerza file.encoding/encoding a UTF-8 durante el build
RUN mvn -q -DskipTests -Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8 package
ARG APP_BUILD=3


# --- runtime (JRE 8 pequeño) ---
FROM eclipse-temurin:8-jre
ENV JAVA_OPTS="-Xms128m -Xmx384m"
WORKDIR /app
# copia el jar cualquiera que sea el nombre:
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
