package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void error_returnsStableErrorContract() {
        Map<String, Object> response = ApiResponse.error("Dữ liệu không hợp lệ");

        assertEquals(2, response.size());
        assertEquals(false, response.get("success"));
        assertEquals("Dữ liệu không hợp lệ", response.get("message"));
    }

    @Test
    void success_returnsStableSuccessContract() {
        Map<String, Object> response = ApiResponse.success("Thành công");

        assertEquals(2, response.size());
        assertEquals(true, response.get("success"));
        assertEquals("Thành công", response.get("message"));
    }
}
