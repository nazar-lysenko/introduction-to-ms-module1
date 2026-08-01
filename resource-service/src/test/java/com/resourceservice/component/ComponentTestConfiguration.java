package com.resourceservice.component;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("component-test")
public class ComponentTestConfiguration {

    public static final PostgreSQLContainer<?> postgres;
    public static final KafkaContainer kafka;
    public static final LocalStackContainer localstack;
    public static final WireMockServer wireMockServer;

    static {
        postgres = new PostgreSQLContainer<>("postgres:17-alpine");
        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));
        localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3"))
                .withServices(S3);

        postgres.start();
        kafka.start();
        localstack.start();

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            wireMockServer.stop();
            localstack.stop();
            kafka.stop();
            postgres.stop();
        }));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("aws.s3.endpoint", () -> localstack.getEndpointOverride(S3).toString());
        registry.add("song-service.url", () -> "http://localhost:" + wireMockServer.port());
        registry.add("storage-service.url", () -> "http://localhost:" + wireMockServer.port());
    }
}
