package com.byjus.zendeskMigration.pojo.response.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author koushik.majhi1@byjus.com
 * @created 05/07/2023 - 12:23 pm
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExternalApiResponse{

    private String timestamp;

    private String message;

    private int code;

    private Object data;

}