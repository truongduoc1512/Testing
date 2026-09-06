package com.example.demo.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
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

@EnabledIfSystemProperty(named = "test24.actual-ai.url", matches = "https?://.+")
@ContextConfiguration(initializers = ActualFastApiIntegrationTest.ActualAiInitializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ActualFastApiIntegrationTest extends MySqlIntegrationTestBase {

    private static final String PRODUCT_PREFIX = "T24F";
    private static final byte[] TINY_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanProducts() {
        jdbcTemplate.update("DELETE FROM Products WHERE CODE LIKE ?", PRODUCT_PREFIX + "%");
    }

    @Test
    void shouldRenderRejectionAndAvoidPersistenceWhenActualFastApiRejectsSmallImage() throws Exception {
        String code = PRODUCT_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        MockMultipartFile file = new MockMultipartFile(
                "fileData", "tiny.png", MediaType.IMAGE_PNG_VALUE, TINY_PNG);

        mockMvc.perform(multipart("/admin/product")
                        .file(file)
                        .param("code", code)
                        .param("name", "TEST-24 Actual FastAPI Product")
                        .param("price", "125.0")
                        .param("discountPercent", "10")
                        .param("stockQuantity", "6")
                        .param("newProduct", "true")
                        .with(user("manager1").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("product"))
                .andExpect(model().attribute("aiError", containsString("100x100px")))
                .andExpect(model().attributeExists("aiMetrics"));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Products WHERE CODE = ?", new Object[] { code }, Integer.class);
        assertEquals(0, count == null ? 0 : count.intValue());
    }

    static final class ActualAiInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of("ai.service.url=" + System.getProperty("test24.actual-ai.url"))
                    .applyTo(context.getEnvironment());
        }
    }
}
