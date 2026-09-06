package com.example.demo.controller.api;

import java.util.HashMap;
import java.util.Map;

final class ApiResponse {

    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";

    private ApiResponse() {
    }

    static Map<String, Object> error(String message) {
        return message(false, message);
    }

    static Map<String, Object> success(String message) {
        return message(true, message);
    }

    static Map<String, Object> message(boolean successful, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, successful);
        response.put(MESSAGE, message);
        return response;
    }
}
