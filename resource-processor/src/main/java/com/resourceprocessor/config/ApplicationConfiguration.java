package com.resourceprocessor.config;

import org.apache.tika.parser.AutoDetectParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@EnableRetry
@Configuration
public class ApplicationConfiguration {

    private static final String KEYCLOAK_REGISTRATION_ID = "keycloak";
    private static final String TOKEN_RELAY_ENABLED_PROPERTY = "security.token-relay.enabled";

    @Bean
    public AutoDetectParser autoDetectParser() {
        return new AutoDetectParser();
    }

    @Bean
    @ConditionalOnProperty(name = TOKEN_RELAY_ENABLED_PROPERTY, havingValue = "true", matchIfMissing = true)
    public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
    }

    @Bean
    @ConditionalOnProperty(name = TOKEN_RELAY_ENABLED_PROPERTY, havingValue = "true", matchIfMissing = true)
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2ClientHttpRequestInterceptor interceptor =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        interceptor.setClientRegistrationIdResolver(request -> KEYCLOAK_REGISTRATION_ID);
        return builder.additionalInterceptors(interceptor).build();
    }

    @Bean
    @ConditionalOnProperty(name = TOKEN_RELAY_ENABLED_PROPERTY, havingValue = "false")
    public RestTemplate plainRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
