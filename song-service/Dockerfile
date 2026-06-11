# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /song-service

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /song-service

COPY --from=build /song-service/target/*.jar ./song-service.jar

EXPOSE 8081

CMD ["java", "-jar", "song-service.jar"]
