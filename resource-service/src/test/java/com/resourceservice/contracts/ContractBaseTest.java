package com.resourceservice.contracts;

import com.resourceservice.resource.ResourceController;
import com.resourceservice.resource.ResourceService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.mockito.Mockito.when;

@WebMvcTest(ResourceController.class)
public class ContractBaseTest {

    private static final long EXISTING_RESOURCE_ID = 1L;
    private static final long NON_EXISTING_RESOURCE_ID = 999L;
    private static final String SAMPLE_MP3_PATH = "/contracts/resourceprocessor/sample.mp3";
    private static final String NOT_FOUND_MESSAGE = "Resource with ID=" + NON_EXISTING_RESOURCE_ID + " not found";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResourceService resourceService;

    @BeforeEach
    void setup() throws IOException {
        RestAssuredMockMvc.mockMvc(mockMvc);

        byte[] sampleData = getClass().getResourceAsStream(SAMPLE_MP3_PATH).readAllBytes();
        when(resourceService.getResource(EXISTING_RESOURCE_ID))
                .thenReturn(sampleData);

        when(resourceService.getResource(NON_EXISTING_RESOURCE_ID))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_MESSAGE));
    }
}
