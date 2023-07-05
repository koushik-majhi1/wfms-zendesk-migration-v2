package com.byjus.zendeskMigration.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static com.byjus.zendeskMigration.constant.client.WebClientsConstant.CONFIGURATION_SERVICE_BASE_URL;
import static com.byjus.zendeskMigration.constant.client.WebClientsConstant.TICKET_SERVICE_BASE_URL;


@Component
public class WebClients {

    private final ExchangeStrategies STRATEGIES;

    private final ReactorClientHttpConnector REACTOR_CLIENT_HTTP_CONNECTOR;

    private final String CONFIGURATION_SERVICE_HOST;

    private final String TICKET_SERVICE_HOST;

    public WebClients(
            final @Value(CONFIGURATION_SERVICE_BASE_URL) String CONFIGURATION_SERVICE_HOST,
            final @Value(TICKET_SERVICE_BASE_URL) String TICKET_SERVICE_HOST
    ){

        this.CONFIGURATION_SERVICE_HOST = CONFIGURATION_SERVICE_HOST;
        this.TICKET_SERVICE_HOST = TICKET_SERVICE_HOST;
        this.STRATEGIES = getDefaultExchangeStrategies();
        this.REACTOR_CLIENT_HTTP_CONNECTOR = getReactorClientHttpConnector();
    }

    @Bean
    public WebClient getConfigurationWebClient(){
        return getDefaultWebClientBuilder()
                .baseUrl(CONFIGURATION_SERVICE_HOST)
                .build();
    }

    @Bean
    public WebClient getTicketWebClient(){
        return getDefaultWebClientBuilder()
                .baseUrl(TICKET_SERVICE_HOST)
                .build();
    }

    private WebClient.Builder getDefaultWebClientBuilder(){
        return WebClient.builder()
                        .clientConnector(REACTOR_CLIENT_HTTP_CONNECTOR)
                        .exchangeStrategies(STRATEGIES);
    }

    private ExchangeStrategies getDefaultExchangeStrategies() {
        //    todo: take this from env
        int size = 5 * 1024 * 1024; // 5MB
        return ExchangeStrategies.builder()
                                 .codecs(codecs -> codecs.defaultCodecs()
                                                         .maxInMemorySize(size)
                                 )
                                 .build();
    }

    //    https://github.com/spring-projects/spring-framework/issues/22464
    private ReactorClientHttpConnector getReactorClientHttpConnector(){
        return new ReactorClientHttpConnector(HttpClient.newConnection()
                                                        .compress(true)
                                                        .wiretap(true));
    }

}