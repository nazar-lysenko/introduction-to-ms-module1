package com.resourceprocessor.config;

import org.apache.tika.parser.AutoDetectParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

@EnableRetry
@Configuration
public class ApplicationConfiguration {
    @Bean
    public AutoDetectParser autoDetectParser() {
        return new AutoDetectParser();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
