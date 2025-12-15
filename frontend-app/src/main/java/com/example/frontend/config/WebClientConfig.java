package com.example.frontend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;

import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for WebClient with OAuth2 client credentials.
 * It manages OAuth2 authorized clients and configures the WebClient
 * to automatically attach OAuth2 tokens when calling protected resources.
 */
@Configuration
public class WebClientConfig {

    /**
     * Base URL for the calendar service.
     * Default is http://localhost:9090 but can be overridden in application properties.
     */
    @Value("${calendar.service.url:http://localhost:9090}")
    private String calendarUrl;

    /**
     * Configures OAuth2AuthorizedClientManager that supports authorization code
     * and refresh token grant types to handle user-authorized OAuth2 clients.
     *
     * @param clientRegistrationRepository Repository of client registrations.
     * @param authorizedClientRepository Repository managing authorized client instances per user.
     * @return OAuth2AuthorizedClientManager managing OAuth2 clients for the app.
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    /**
     * Configures WebClient with OAuth2 support that:
     * - Automatically injects Bearer tokens for requests.
     * - Sets base URL to calendar service.
     * - Increases in-memory buffer size to handle larger responses.
     *
     * @param authorizedClientManager The OAuth2AuthorizedClientManager to authorize requests.
     * @return WebClient instance for calling calendar service.
     */
    @Bean
    public WebClient calendarWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        // Default client registration ID should match your Keycloak registration in the app
        oauth2Filter.setDefaultClientRegistrationId("keycloak");

        return WebClient.builder()
                .baseUrl(calendarUrl)
                .filter(oauth2Filter)
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                                .build())
                .build();
    }
}
