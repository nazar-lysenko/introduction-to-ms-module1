# Testing Strategy

## 1. Unit Tests — `resource-processor` (JUnit 5 + Mockito + AssertJ)

Covers MP3 metadata extraction (Apache Tika), DTO mapping, field validation/normalization, and event orchestration in isolation — no Spring context. 
`resource-processor` is chosen because it contains the most complex pure-Java business logic, making it the highest-value target for fast, cheap unit coverage.

## 2. Integration Tests — `resource-service` slices (JUnit 5 + Testcontainers)

Each test targets a single infrastructure boundary: 
`@DataJpaTest` + `PostgreSQLContainer` for repository CRUD
`LocalStackContainer` for S3 upload/download
`KafkaContainer` for producer serialization
`@WebMvcTest` for controller validation

Testcontainers provides real infrastructure (Postgres 17, Kafka, LocalStack S3) so tests catch schema drift and serialization issues that in-memory fakes would miss.

## 3. Component Tests — per service (Cucumber 7 + Spring Boot Test + Testcontainers + WireMock)

Each service is booted with full Spring context against real infrastructure containers 
while downstream HTTP services are replaced by WireMock stubs — isolating one service at a time at the business level. 
Gherkin scenarios serve as living documentation and verify retry/resilience logic (e.g., song-service temporarily unavailable) without requiring the whole system.

## 4. Contract Tests — HTTP + Kafka (Spring Cloud Contract)

Contracts are defined on the producer side (resource-service, song-service), verified during the producer build, 
and packaged as stub JARs installed to the local Maven repo. 
Consumers (resource-processor, resource-service) pull those stubs via `@AutoConfigureStubRunner`, 
ensuring they always test against the real producer API shape and preventing silent breaking changes across deploys.

## 5. End-to-End Tests — critical journeys (Cucumber 7 + REST Assured + Awaitility + Docker Compose)

The full system — all five Spring Boot services plus Kafka, Postgres, S3, and the API Gateway — is started via `compose.yaml`; 
tests hit `localhost:8084` and use Awaitility to handle async Kafka-driven metadata processing. 
Scenarios cover upload→process→retrieve, cascading delete, and resilience (processor restart recovery), 
validating that environment configuration and service wiring work correctly together.
