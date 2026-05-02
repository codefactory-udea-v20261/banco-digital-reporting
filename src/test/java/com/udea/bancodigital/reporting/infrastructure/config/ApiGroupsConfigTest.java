package com.udea.bancodigital.reporting.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiGroupsConfigTest {

    private final ApiGroupsConfig config = new ApiGroupsConfig();

    @Test
    void testGroups() {
        GroupedOpenApi auth = config.authApi();
        assertNotNull(auth);
        
        // Execute the customizer lambda
        io.swagger.v3.oas.models.OpenAPI openAPI = new io.swagger.v3.oas.models.OpenAPI();
        auth.getOpenApiCustomizers().forEach(c -> c.customise(openAPI));
        assertNotNull(openAPI.getInfo());
        
        assertNotNull(config.customersApi());
        assertNotNull(config.accountsApi());
        assertNotNull(config.reportingApi());
    }
}
