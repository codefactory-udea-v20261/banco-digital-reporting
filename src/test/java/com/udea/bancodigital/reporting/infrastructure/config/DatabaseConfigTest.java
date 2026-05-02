package com.udea.bancodigital.reporting.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class DatabaseConfigTest {

    private final DatabaseConfig config = new DatabaseConfig();

    @Test
    void testBeans() {
        DataSource ds = mock(DataSource.class);
        assertNotNull(config.reportingDataSourceProperties());
        assertNotNull(config.coreDataSource());
        assertNotNull(config.reportingJdbcTemplate(ds));
        assertNotNull(config.coreJdbcTemplate(ds));
        assertNotNull(config.reportingNamedParameterJdbcTemplate(ds));
        assertNotNull(config.coreNamedParameterJdbcTemplate(ds));
    }
}
