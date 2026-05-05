package com.udea.bancodigital.reporting.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiGroupsConfigTest {

    private final ApiGroupsConfig config = new ApiGroupsConfig();

    @Test
    void testGroups() {
        io.swagger.v3.oas.models.OpenAPI openAPI = new io.swagger.v3.oas.models.OpenAPI();
        GroupedOpenApi reporting = config.reportingApi();
        assertNotNull(reporting);

        reporting.getOpenApiCustomizers().forEach(c -> c.customise(openAPI));
        assertNotNull(openAPI.getInfo());
    }
}
