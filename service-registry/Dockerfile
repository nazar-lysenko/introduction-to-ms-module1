# Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /service-registry

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /service-registry

COPY --from=build /service-registry/target/*.jar ./service-registry.jar

EXPOSE 8761

CMD ["java", "-jar", "service-registry.jar"]
