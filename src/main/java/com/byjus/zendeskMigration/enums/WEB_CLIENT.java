package com.byjus.zendeskMigration.enums;

/**
 * @author koushik.majhi1@byjus.com
 * @created 05/07/2023 - 12:19 pm
 */
public enum WEB_CLIENT {

    CONFIGURATION("CONFIGURATION"),

    TICKET("TICKET");

    private final String client;

    WEB_CLIENT(String client) {
        this.client = client;
    }
}
