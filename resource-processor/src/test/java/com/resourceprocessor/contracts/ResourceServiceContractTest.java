package com.resourceprocessor.contracts;

import com.resourceprocessor.client.ResourceServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureStubRunner(
        ids = "com.resourceservice:resource-service:+:stubs:8080",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class ResourceServiceContractTest {
    private static final long TEST_RESOURCE_ID = 1L;

    @Autowired
    private ResourceServiceClient resourceServiceClient;

    @Test
    void shouldReturnResourceDataForExistingResource() {
        byte[] resource = resourceServiceClient.getResource(TEST_RESOURCE_ID);

        assertThat(resource).isNotNull();
        assertThat(resource).isNotEmpty();
    }
}
