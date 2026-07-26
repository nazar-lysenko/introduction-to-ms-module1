# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /resource-processor

COPY pom.xml .
RUN mvn dependency:go-offline -q -Dmaven.resolver.transport=wagon

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /resource-processor

COPY --from=build /resource-processor/target/*.jar ./resource-processor.jar

CMD ["java", "-jar", "resource-processor.jar"]
