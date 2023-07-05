package com.byjus.zendeskMigration.factory;

import com.byjus.zendeskMigration.client.WebClients;
import com.byjus.zendeskMigration.enums.WEB_CLIENT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author koushik.majhi1@byjus.com
 * @created 05/07/2023 - 12:18 pm
 */
@Component
public class WebClientFactory {

    private final WebClients webClients;

    @Autowired
    public WebClientFactory(final WebClients webClients) {
        this.webClients = webClients;
    }

    public WebClient getInstance(final WEB_CLIENT type){

        return switch (type) {
            case CONFIGURATION -> webClients.getConfigurationWebClient();
            case TICKET -> webClients.getTicketWebClient();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

}