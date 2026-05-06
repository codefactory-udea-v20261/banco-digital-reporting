package com.udea.bancodigital.reporting.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties reportingDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @FlywayDataSource
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource reportingDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    public JdbcTemplate reportingJdbcTemplate(DataSource reportingDataSource) {
        return new JdbcTemplate(reportingDataSource);
    }

    @Bean
    @Primary
    public NamedParameterJdbcTemplate reportingNamedParameterJdbcTemplate(DataSource reportingDataSource) {
        return new NamedParameterJdbcTemplate(reportingDataSource);
    }
}
