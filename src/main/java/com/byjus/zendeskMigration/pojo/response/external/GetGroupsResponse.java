package com.byjus.zendeskMigration.pojo.response.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author koushik.majhi1@byjus.com
 * @created 05/07/2023 - 12:26 pm
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetGroupsResponse {

    private List<Group> group;
}
