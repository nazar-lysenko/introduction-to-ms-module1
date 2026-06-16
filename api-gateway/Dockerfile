# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /api-gateway

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /api-gateway

COPY --from=build /api-gateway/target/*.jar ./api-gateway.jar

EXPOSE 8084

CMD ["java", "-jar", "api-gateway.jar"]
