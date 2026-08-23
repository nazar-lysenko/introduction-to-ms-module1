# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /storage-service

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /storage-service

COPY --from=build /storage-service/target/*.jar ./storage-service.jar

EXPOSE 8083

CMD ["java", "-jar", "storage-service.jar"]
