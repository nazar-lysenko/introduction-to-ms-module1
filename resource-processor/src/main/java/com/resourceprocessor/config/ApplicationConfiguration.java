package com.resourceprocessor.config;

import org.apache.tika.parser.AutoDetectParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public AutoDetectParser autoDetectParser() {
        return new AutoDetectParser();
    }
}
