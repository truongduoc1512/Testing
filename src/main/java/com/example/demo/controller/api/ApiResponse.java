package com.example.demo.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

final class ApiResponse {

    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";

    private ApiResponse() {
    }

    static Map<String, Object> error(String message) {
        return message(false, message);
    }

    static Map<String, Object> error(Errors errors) {
        return error(firstErrorMessage(errors));
    }

    static boolean containsErrorCodePrefix(Errors errors, String prefix) {
        return errors.getAllErrors().stream()
                .map(ObjectError::getCode)
                .anyMatch(code -> code != null && code.startsWith(prefix));
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

    private static String firstErrorMessage(Errors errors) {
        if (errors == null || errors.getAllErrors().isEmpty()) {
            return "Dữ liệu không hợp lệ.";
        }
        ObjectError error = errors.getAllErrors().get(0);
        String defaultMessage = error.getDefaultMessage();
        if (defaultMessage != null && !defaultMessage.trim().isEmpty()) {
            return defaultMessage;
        }
        return error.getCode() == null ? "Dữ liệu không hợp lệ." : error.getCode();
    }
}
