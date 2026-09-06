package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

class ShoppingCartFinalizeTemplateTest {

    @Test
    void finalizeTemplate_hasValidClosingMarkupAndRequiredOrderBindings() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/shoppingCartFinalize.html");
        String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        assertFalse(html.contains("</html>>"));
        assertTrue(html.trim().endsWith("</html>"));
        assertTrue(html.contains("lastOrderedCart.orderNum"));
        assertTrue(html.contains("lastOrderedCart.quantityTotal"));
        assertTrue(html.contains("lastOrderedCart.finalAmount"));
        assertTrue(html.contains("name=\"viewport\""));
    }
}
