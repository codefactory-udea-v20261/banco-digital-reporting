package com.udea.bancodigital.reporting.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataMigrationUseCaseTest {

    @Mock
    private JdbcTemplate reportingJdbcTemplate;

    @Mock
    private JdbcTemplate coreJdbcTemplate;

    private DataMigrationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DataMigrationUseCase(reportingJdbcTemplate, coreJdbcTemplate);
    }

    @Test
    void run_DebeMigrarDatosCorrectamente() throws Exception {

        Map<String, Object> mockData = Collections.singletonMap("id", 1);
        when(coreJdbcTemplate.queryForList(anyString())).thenReturn(List.of(mockData));

        useCase.run();

        // Verifica que se intentó leer del core
        verify(coreJdbcTemplate, atLeastOnce()).queryForList(anyString());
    }

    @Test
    void run_DebeManejarExcepcion() throws Exception {

        when(coreJdbcTemplate.queryForList(anyString())).thenThrow(new RuntimeException("DB Error"));

        useCase.run();

        // No debe lanzar excepción hacia afuera, solo loguear
        verify(coreJdbcTemplate, atLeastOnce()).queryForList(anyString());
    }
}
