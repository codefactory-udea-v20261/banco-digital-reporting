package com.udea.bancodigital.reporting.infrastructure.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGroupsConfig {

    @Bean
    public GroupedOpenApi reportingApi() {
        return buildGroup(
                "reportes",
                "/api/v1/reportes/**",
                "Banco Digital Reporting API",
                "API del servicio de reportes y consultas analiticas."
        );
    }

    private GroupedOpenApi buildGroup(String group, String pathPattern, String title, String description) {
        return GroupedOpenApi.builder()
                .group(group)
                .pathsToMatch(pathPattern)
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info()
                                .title(title)
                                .version("v1.0.0")
                                .description(description)))
                .build();
    }
}
