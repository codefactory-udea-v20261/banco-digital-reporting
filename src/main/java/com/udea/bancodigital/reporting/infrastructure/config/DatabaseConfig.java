package com.udea.bancodigital.reporting.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import org.springframework.context.annotation.Profile;

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

    @Bean(name = "coreDataSource")
    @Profile("!prod")
    @ConfigurationProperties(prefix = "core.datasource")
    public HikariDataSource coreDataSource() {
        return new HikariDataSource();
    }

    @Bean
    public JdbcTemplate reportingJdbcTemplate(DataSource reportingDataSource) {
        return new JdbcTemplate(reportingDataSource);
    }

    @Bean(name = "coreJdbcTemplate")
    @Profile("!prod")
    public JdbcTemplate coreJdbcTemplate(@Qualifier("coreDataSource") DataSource coreDataSource) {
        return new JdbcTemplate(coreDataSource);
    }

    @Bean
    @Primary
    public NamedParameterJdbcTemplate reportingNamedParameterJdbcTemplate(DataSource reportingDataSource) {
        return new NamedParameterJdbcTemplate(reportingDataSource);
    }

    @Bean(name = "coreNamedParameterJdbcTemplate")
    @Profile("!prod")
    public NamedParameterJdbcTemplate coreNamedParameterJdbcTemplate(@Qualifier("coreDataSource") DataSource coreDataSource) {
        return new NamedParameterJdbcTemplate(coreDataSource);
    }
}
