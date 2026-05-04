package com.udea.bancodigital.reporting;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@org.springframework.context.annotation.Import(com.udea.bancodigital.reporting.config.TestContainersConfig.class)
@ActiveProfiles("test")
class ReportingApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true);
    }
}
