# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /config-service

COPY pom.xml .
RUN mvn dependency:go-offline -q -Dmaven.resolver.transport=wagon

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /config-service

COPY --from=build /config-service/target/*.jar ./config-service.jar

CMD ["java", "-jar", "config-service.jar"]
