package com.udea.bancodigital.shared.web;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SharedWebTest {

    @Test
    void testApiResponseOk() {
        ApiResponse<String> success = ApiResponse.ok("Data");
        assertTrue(success.isSuccess());
        assertEquals("Data", success.getData());
        assertNull(success.getMessage());
        assertNull(success.getError());
        assertNotNull(success.getTimestamp());

        ApiResponse<String> successWithMessage = ApiResponse.ok("Success message", "Data");
        assertTrue(successWithMessage.isSuccess());
        assertEquals("Success message", successWithMessage.getMessage());
        assertEquals("Data", successWithMessage.getData());
    }

    @Test
    void testApiResponseCreated() {
        ApiResponse<String> created = ApiResponse.created("New resource");
        assertTrue(created.isSuccess());
        assertEquals("Recurso creado exitosamente", created.getMessage());
        assertEquals("New resource", created.getData());
    }

    @Test
    void testApiResponseError() {
        ApiError error = ApiError.builder().message("Error").build();
        ApiResponse<String> failure = ApiResponse.error(error);
        assertFalse(failure.isSuccess());
        assertNull(failure.getData());
        assertEquals(error, failure.getError());
    }

    @Test
    void testApiResponseBuilder() {
        ApiResponse<String> custom = ApiResponse.<String>builder()
                .success(true)
                .message("Custom")
                .data("Data")
                .build();
        
        assertTrue(custom.isSuccess());
        assertEquals("Custom", custom.getMessage());
        assertEquals("Data", custom.getData());
        assertNotNull(custom.getTimestamp());
    }

    @Test
    void testApiResponseBuilderWithTimestamp() {
        Instant now = Instant.now();
        ApiResponse<String> custom = ApiResponse.<String>builder()
                .success(true)
                .timestamp(now)
                .build();
        
        assertEquals(now, custom.getTimestamp());
    }

    @Test
    void testApiError() {
        ApiError error1 = ApiError.builder()
                .errorCode("ERR-001")
                .message("Message")
                .httpStatus(400)
                .traceId("trace-123")
                .details(List.of("Detail 1"))
                .build();

        assertEquals("ERR-001", error1.getErrorCode());
        assertEquals("Message", error1.getMessage());
        assertEquals(400, error1.getHttpStatus());
        ApiError error2 = ApiError.builder()
                .errorCode("ERR-001")
                .message("Message")
                .build();

        assertEquals("ERR-001", error2.getErrorCode());
        assertEquals("Message", error2.getMessage());
    }
}
