package com.udea.bancodigital.shared.web;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SharedWebTest {

    @Test
    void testApiResponse() {
        ApiResponse<String> success = ApiResponse.ok("Data");
        assertTrue(success.isSuccess());
        assertEquals("Data", success.getData());
        assertNull(success.getError());

        ApiError error = ApiError.builder().message("Error").build();
        ApiResponse<String> failure = ApiResponse.error(error);
        assertFalse(failure.isSuccess());
        assertNull(failure.getData());
        assertEquals(error, failure.getError());
    }

    @Test
    void testApiError() {
        ApiError error = ApiError.builder()
                .errorCode("ERR-001")
                .message("Message")
                .httpStatus(400)
                .traceId("trace-123")
                .details(List.of("Detail 1"))
                .build();

        assertEquals("ERR-001", error.getErrorCode());
        assertEquals("Message", error.getMessage());
        assertEquals(400, error.getHttpStatus());
        assertEquals("trace-123", error.getTraceId());
        assertEquals(1, error.getDetails().size());
    }
}
