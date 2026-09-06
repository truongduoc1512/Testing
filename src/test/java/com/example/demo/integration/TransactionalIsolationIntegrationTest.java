package com.example.demo.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
class TransactionalIsolationIntegrationTest extends MySqlIntegrationTestBase {

    private static final String PRODUCT_PREFIX = "T24T";
    private static final String EMAIL_PREFIX = "test24-rollback-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String productCode;
    private String customerEmail;

    @BeforeTransaction
    void prepareTransactionFixtureNames() {
        cleanStaleFixtures();
        productCode = PRODUCT_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        customerEmail = EMAIL_PREFIX + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    @Test
    void shouldRollbackControllerServiceDaoAndDatabaseChangesAfterTest() throws Exception {
        insertProduct();
        MockHttpSession session = new MockHttpSession();

        String itemJson = "{\"code\":\"" + productCode + "\",\"quantity\":2}";
        mockMvc.perform(post("/api/v1/cart/items")
                        .session(session)
                        .with(user("manager1").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());

        String customerJson = "{\"name\":\"Rollback Customer\","
                + "\"address\":\"24 Transaction Street\","
                + "\"email\":\"" + customerEmail + "\",\"phone\":\"0900000024\"}";
        mockMvc.perform(post("/api/v1/cart/customer")
                        .session(session)
                        .with(user("manager1").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/cart/checkout")
                        .session(session)
                        .with(user("manager1").roles("USER")))
                .andExpect(status().isCreated());

        assertEquals(1, count("SELECT COUNT(*) FROM Orders WHERE CUSTOMER_EMAIL = ?", customerEmail));
        assertEquals(1, count("SELECT COUNT(*) FROM Order_details WHERE PRODUCT_ID = ?", productCode));
        assertEquals(6, count("SELECT STOCK_QUANTITY FROM Products WHERE CODE = ?", productCode));
    }

    @AfterTransaction
    void verifyRollbackRemovedAllTestData() {
        assertEquals(0, count("SELECT COUNT(*) FROM Orders WHERE CUSTOMER_EMAIL = ?", customerEmail));
        assertEquals(0, count("SELECT COUNT(*) FROM Order_details WHERE PRODUCT_ID = ?", productCode));
        assertEquals(0, count("SELECT COUNT(*) FROM Products WHERE CODE = ?", productCode));
    }

    private void insertProduct() {
        jdbcTemplate.update("INSERT INTO Products "
                        + "(CODE, NAME, PRICE, CREATE_DATE, OWNER_USERNAME, DISCOUNT_PERCENT, SALES_COUNT, "
                        + "LOCATION, BRAND, RATING, REVIEW_COUNT, STOCK_QUANTITY, CATEGORY, STATUS, "
                        + "IS_MALL, IS_FAVORED) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                productCode, "TEST-24 Transaction Product", 50.0, "manager1", 0, 0,
                "Ho Chi Minh", "TEST-24", 5.0, 0, 8, "Integration", "ACTIVE", false, false);
    }

    private int count(String sql, String value) {
        Integer result = jdbcTemplate.queryForObject(sql, new Object[] { value }, Integer.class);
        return result == null ? 0 : result;
    }

    private void cleanStaleFixtures() {
        jdbcTemplate.update("DELETE od FROM Order_details od JOIN Products p ON p.CODE = od.PRODUCT_ID "
                + "WHERE p.CODE LIKE ?", PRODUCT_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM Orders WHERE CUSTOMER_EMAIL LIKE ?", EMAIL_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM Products WHERE CODE LIKE ?", PRODUCT_PREFIX + "%");
    }
}
