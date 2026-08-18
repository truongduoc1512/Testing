package com.example.demo.integration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

@ContextConfiguration(initializers = AiServiceIntegrationTest.AiServerInitializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AiServiceIntegrationTest extends MySqlIntegrationTestBase {

    private static final String PRODUCT_PREFIX = "T24A";
    private static final byte[] IMAGE_BYTES = new byte[] { 1, 2, 3, 4, 5, 6 };
    private static final AtomicReference<AiResponse> NEXT_RESPONSE =
            new AtomicReference<>(AiResponse.approved());
    private static final AtomicReference<CapturedRequest> LAST_REQUEST = new AtomicReference<>();
    private static final HttpServer AI_SERVER = startAiServer();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanBefore() {
        cleanProducts();
        LAST_REQUEST.set(null);
        NEXT_RESPONSE.set(AiResponse.approved());
    }

    @AfterEach
    void cleanAfter() {
        cleanProducts();
    }

    @AfterAll
    static void stopAiServer() {
        AI_SERVER.stop(0);
    }

    @Test
    void shouldSendMultipartImageAndPersistProductWhenAiApproves() throws Exception {
        String code = productCode();

        performProductSave(code)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productList"))
                .andExpect(flash().attributeExists("message"));

        CapturedRequest request = LAST_REQUEST.get();
        assertNotNull(request);
        assertEquals("POST", request.method);
        assertTrue(request.contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE));
        String multipart = new String(request.body, StandardCharsets.ISO_8859_1);
        assertTrue(multipart.contains("name=\"file\""));
        assertTrue(multipart.contains("filename=\"shoe.png\""));

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT NAME, PRICE, STOCK_QUANTITY, OWNER_USERNAME, IMAGE FROM Products WHERE CODE = ?", code);
        assertEquals("TEST-24 AI Product", row.get("NAME"));
        assertEquals(125.0, ((Number) row.get("PRICE")).doubleValue(), 0.0001);
        assertEquals(6, ((Number) row.get("STOCK_QUANTITY")).intValue());
        assertEquals("manager1", row.get("OWNER_USERNAME"));
        assertArrayEquals(IMAGE_BYTES, (byte[]) row.get("IMAGE"));
    }

    @Test
    void shouldRejectProductAndPreserveDatabaseWhenAiRejectsImage() throws Exception {
        String code = productCode();
        NEXT_RESPONSE.set(AiResponse.rejected());

        performProductSave(code)
                .andExpect(status().isOk())
                .andExpect(view().name("product"))
                .andExpect(model().attribute("aiError", "Image is blurred"))
                .andExpect(model().attributeExists("aiMetrics"));

        assertEquals(0, countProduct(code));
        assertNotNull(LAST_REQUEST.get());
    }

    @Test
    void shouldWarnAndPersistProductWhenAiReturnsServerError() throws Exception {
        String code = productCode();
        NEXT_RESPONSE.set(AiResponse.serverError());

        performProductSave(code)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productList"));

        assertEquals(1, countProduct(code));
        assertNotNull(LAST_REQUEST.get());
    }

    private org.springframework.test.web.servlet.ResultActions performProductSave(String code) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "fileData", "shoe.png", MediaType.IMAGE_PNG_VALUE, IMAGE_BYTES);
        return mockMvc.perform(multipart("/admin/product")
                .file(file)
                .param("code", code)
                .param("name", "TEST-24 AI Product")
                .param("price", "125.0")
                .param("discountPercent", "10")
                .param("stockQuantity", "6")
                .param("newProduct", "true")
                .with(user("manager1").roles("ADMIN"))
                .with(csrf()));
    }

    private int countProduct(String code) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Products WHERE CODE = ?", Integer.class, code);
        return count == null ? 0 : count;
    }

    private String productCode() {
        return PRODUCT_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private void cleanProducts() {
        jdbcTemplate.update("DELETE FROM Products WHERE CODE LIKE ?", PRODUCT_PREFIX + "%");
    }

    private static HttpServer startAiServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/v1/analyze", AiServiceIntegrationTest::handleAnalyze);
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static void handleAnalyze(HttpExchange exchange) throws IOException {
        ByteArrayOutputStream requestBuffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int read;
        while ((read = exchange.getRequestBody().read(chunk)) != -1) {
            requestBuffer.write(chunk, 0, read);
        }
        byte[] requestBody = requestBuffer.toByteArray();
        LAST_REQUEST.set(new CapturedRequest(
                exchange.getRequestMethod(),
                exchange.getRequestHeaders().getFirst("Content-Type"),
                requestBody));

        AiResponse response = NEXT_RESPONSE.get();
        byte[] responseBody = response.body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        exchange.sendResponseHeaders(response.status, responseBody.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBody);
        }
    }

    static final class AiServerInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of("ai.service.url=http://127.0.0.1:" + AI_SERVER.getAddress().getPort())
                    .applyTo(context.getEnvironment());
        }
    }

    private static final class CapturedRequest {
        private final String method;
        private final String contentType;
        private final byte[] body;

        private CapturedRequest(String method, String contentType, byte[] body) {
            this.method = method;
            this.contentType = contentType;
            this.body = body;
        }
    }

    private static final class AiResponse {
        private final int status;
        private final String body;

        private AiResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }

        private static AiResponse approved() {
            return new AiResponse(200,
                    "{\"approved\":true,\"status\":\"APPROVED\",\"reason\":\"Image accepted\","
                            + "\"metrics\":{\"blur_score\":90.0}}");
        }

        private static AiResponse rejected() {
            return new AiResponse(200,
                    "{\"approved\":false,\"status\":\"REJECTED\",\"reason\":\"Image is blurred\","
                            + "\"metrics\":{\"blur_score\":10.0}}");
        }

        private static AiResponse serverError() {
            return new AiResponse(500,
                    "{\"approved\":false,\"status\":\"ERROR\",\"reason\":\"AI unavailable\"}");
        }
    }
}
