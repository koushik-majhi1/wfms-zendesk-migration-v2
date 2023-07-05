package com.byjus.zendeskMigration.pojo.response.external;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author koushik.majhi1@byjus.com
 * @created 05/07/2023 - 12:33 pm
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Group {

    @JsonAlias("groupId")
    private String groupId;
    @JsonAlias("groupName")
    private String groupName;
}
