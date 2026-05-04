package com.udea.bancodigital.reporting;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test — verifies Spring context loads without Docker.
 * Uses H2 in-memory database and mocks for external dependencies.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "core.datasource.jdbc-url=jdbc:h2:mem:coredb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
    "core.datasource.driver-class-name=org.h2.Driver",
    "core.datasource.username=sa",
    "core.datasource.password=",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
class ReportingApplicationTests {

    @MockBean
    @SuppressWarnings("rawtypes")
    private KafkaTemplate kafkaTemplate;

    @Test
    void contextLoads() {
        assertTrue(true);
    }
}
