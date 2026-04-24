package com.udea.bancodigital.infrastructure.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGroupsConfig {

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/internal/users/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info()
                                .title("Banco Digital API - Autenticacion")
                                .version("v1.0.0")
                                .description("API de autenticacion, consulta de identidad y contratos internos del futuro identity-service.")))
                .build();
    }

    @Bean
    public GroupedOpenApi customersApi() {
        return buildGroup(
                "clientes",
                "/api/v1/clientes/**",
                "Banco Digital API - Clientes",
                "API de gestion del ciclo de vida de clientes: registro, consulta y actualizacion de perfil."
        );
    }

    @Bean
    public GroupedOpenApi accountsApi() {
        return buildGroup(
                "cuentas",
                "/api/v1/cuentas/**",
                "Banco Digital API - Cuentas",
                "API de administracion de cuentas y consultas de saldo para clientes autenticados."
        );
    }

    @Bean
    public GroupedOpenApi reportingApi() {
        return buildGroup(
                "reportes",
                "/api/v1/reportes/**",
                "Banco Digital API - Reportes",
                "API de consultas analiticas y reportes construida para evolucionar a un servicio independiente."
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
