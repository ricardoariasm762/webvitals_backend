# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar

# 8081: HTTP / REST / WebSockets STOMP
# 9090: gRPC Server (HTTP/2 binary)
EXPOSE 8081 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
