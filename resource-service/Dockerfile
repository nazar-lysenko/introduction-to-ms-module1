# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /resource-service

COPY pom.xml .
RUN mvn dependency:go-offline -q -Dmaven.resolver.transport=wagon

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /resource-service

COPY --from=build /resource-service/target/*.jar ./resource-service.jar

EXPOSE 8080

CMD ["java", "-jar", "resource-service.jar"]
