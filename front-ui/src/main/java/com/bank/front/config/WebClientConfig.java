package com.bank.front.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.accounts.url:http://localhost:8081}")
    private String accountsServiceUrl;

    @Value("${services.cash.url:http://localhost:8082}")
    private String cashServiceUrl;

    @Value("${services.transfer.url:http://localhost:8083}")
    private String transferServiceUrl;

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .build();

        var authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean("accountsWebClient")
    public WebClient accountsWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        return createWebClient(authorizedClientManager, accountsServiceUrl);
    }

    @Bean("cashWebClient")
    public WebClient cashWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        return createWebClient(authorizedClientManager, cashServiceUrl);
    }

    @Bean("transferWebClient")
    public WebClient transferWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        return createWebClient(authorizedClientManager, transferServiceUrl);
    }

    private WebClient createWebClient(OAuth2AuthorizedClientManager authorizedClientManager, String baseUrl) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultOAuth2AuthorizedClient(true);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }
}
