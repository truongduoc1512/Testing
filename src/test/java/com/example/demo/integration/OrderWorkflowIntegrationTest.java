package com.example.demo.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.model.CartInfo;

class OrderWorkflowIntegrationTest extends MySqlIntegrationTestBase {

    private static final String PRODUCT_PREFIX = "T24O";
    private static final String EMAIL_PREFIX = "test24-order-";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanBefore() {
        cleanFixtures();
    }

    @AfterEach
    void cleanAfter() {
        cleanFixtures();
    }

    @Test
    void shouldCreateOrderDetailsAndDecreaseStockWhenCheckoutRequestIsValid() throws Exception {
        String code = productCode();
        String email = testEmail();
        insertProduct(code, 100.0, 10, 10, 4);
        MockHttpSession session = new MockHttpSession();

        addItem(session, code, 3);
        saveCustomer(session, email);

        mockMvc.perform(post("/api/v1/cart/checkout")
                        .session(session)
                        .with(user("manager1").roles("USER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.orderedCart.orderNum").isNumber())
                .andExpect(jsonPath("$.orderedCart.quantityTotal").value(3))
                .andExpect(jsonPath("$.orderedCart.amountTotal").value(270.0));

        Map<String, Object> order = jdbcTemplate.queryForMap(
                "SELECT ID, ORDER_NUM, AMOUNT, STATUS, CUSTOMER_USERNAME, CUSTOMER_EMAIL "
                        + "FROM Orders WHERE CUSTOMER_EMAIL = ?", email);
        assertNotNull(order.get("ID"));
        assertEquals(270.0, number(order, "AMOUNT"), 0.0001);
        assertEquals("PENDING", order.get("STATUS"));
        assertEquals("manager1", order.get("CUSTOMER_USERNAME"));

        Map<String, Object> detail = jdbcTemplate.queryForMap(
                "SELECT ORDER_ID, PRODUCT_ID, QUANITY, PRICE, AMOUNT FROM Order_details "
                        + "WHERE ORDER_ID = ?", order.get("ID"));
        assertEquals(order.get("ID"), detail.get("ORDER_ID"));
        assertEquals(code, detail.get("PRODUCT_ID"));
        assertEquals(3, ((Number) detail.get("QUANITY")).intValue());
        assertEquals(90.0, number(detail, "PRICE"), 0.0001);
        assertEquals(270.0, number(detail, "AMOUNT"), 0.0001);

        Map<String, Object> product = productState(code);
        assertEquals(7, ((Number) product.get("STOCK_QUANTITY")).intValue());
        assertEquals(7, ((Number) product.get("SALES_COUNT")).intValue());
        assertNull(session.getAttribute("myCart"));
        assertNotNull(session.getAttribute("lastOrderedCart"));
    }

    @Test
    void shouldNotPersistPartialStateWhenStockChangesBeforeCheckout() throws Exception {
        String code = productCode();
        String email = testEmail();
        insertProduct(code, 50.0, 0, 2, 0);
        MockHttpSession session = new MockHttpSession();

        addItem(session, code, 2);
        saveCustomer(session, email);
        jdbcTemplate.update("UPDATE Products SET STOCK_QUANTITY = 1 WHERE CODE = ?", code);

        mockMvc.perform(post("/api/v1/cart/checkout")
                        .session(session)
                        .with(user("manager1").roles("USER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        assertEquals(0, countOrders(email));
        assertEquals(0, countDetails(code));
        Map<String, Object> product = productState(code);
        assertEquals(1, ((Number) product.get("STOCK_QUANTITY")).intValue());
        assertEquals(0, ((Number) product.get("SALES_COUNT")).intValue());
        assertNotNull(session.getAttribute("myCart"));
    }

    @Test
    void shouldRollbackOrderDetailsAndStockWhenDatabaseRejectsOrder() throws Exception {
        String code = productCode();
        String email = testEmail();
        insertProduct(code, 75.0, 0, 5, 2);
        MockHttpSession session = new MockHttpSession();

        addItem(session, code, 2);
        saveCustomer(session, email);
        CartInfo cart = (CartInfo) session.getAttribute("myCart");
        assertNotNull(cart);
        cart.getCustomerInfo().setName(repeat("X", 256));

        mockMvc.perform(post("/api/v1/cart/checkout")
                        .session(session)
                        .with(user("manager1").roles("USER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        assertEquals(0, countOrders(email));
        assertEquals(0, countDetails(code));
        Map<String, Object> product = productState(code);
        assertEquals(5, ((Number) product.get("STOCK_QUANTITY")).intValue());
        assertEquals(2, ((Number) product.get("SALES_COUNT")).intValue());
        assertNotNull(session.getAttribute("myCart"));
    }

    private void addItem(MockHttpSession session, String code, int quantity) throws Exception {
        String json = "{\"code\":\"" + code + "\",\"quantity\":" + quantity + "}";
        mockMvc.perform(post("/api/v1/cart/items")
                        .session(session)
                        .with(user("manager1").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityTotal").value(quantity));
    }

    private void saveCustomer(MockHttpSession session, String email) throws Exception {
        String json = "{\"name\":\"Test Customer\",\"address\":\"24 Integration Street\","
                + "\"email\":\"" + email + "\",\"phone\":\"0900000024\"}";
        mockMvc.perform(post("/api/v1/cart/customer")
                        .session(session)
                        .with(user("manager1").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerInfo.valid").value(true));
    }

    private void insertProduct(String code, double price, int discount, int stock, int sales) {
        jdbcTemplate.update("INSERT INTO Products "
                        + "(CODE, NAME, PRICE, CREATE_DATE, OWNER_USERNAME, DISCOUNT_PERCENT, SALES_COUNT, "
                        + "LOCATION, BRAND, RATING, REVIEW_COUNT, STOCK_QUANTITY, CATEGORY, STATUS, "
                        + "IS_MALL, IS_FAVORED) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                code, "TEST-24 Order Product", price, "manager1", discount, sales,
                "Ho Chi Minh", "TEST-24", 5.0, 0, stock, "Integration", "ACTIVE", false, false);
    }

    private Map<String, Object> productState(String code) {
        return jdbcTemplate.queryForMap(
                "SELECT STOCK_QUANTITY, SALES_COUNT FROM Products WHERE CODE = ?", code);
    }

    private int countOrders(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Orders WHERE CUSTOMER_EMAIL = ?", Integer.class, email);
        return count == null ? 0 : count;
    }

    private int countDetails(String code) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Order_details WHERE PRODUCT_ID = ?", Integer.class, code);
        return count == null ? 0 : count;
    }

    private double number(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).doubleValue();
    }

    private String productCode() {
        return PRODUCT_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String testEmail() {
        return EMAIL_PREFIX + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    private String repeat(String value, int count) {
        StringBuilder result = new StringBuilder(value.length() * count);
        for (int i = 0; i < count; i++) {
            result.append(value);
        }
        return result.toString();
    }

    private void cleanFixtures() {
        jdbcTemplate.update("DELETE od FROM Order_details od JOIN Products p ON p.CODE = od.PRODUCT_ID "
                + "WHERE p.CODE LIKE ?", PRODUCT_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM Orders WHERE CUSTOMER_EMAIL LIKE ?", EMAIL_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM Products WHERE CODE LIKE ?", PRODUCT_PREFIX + "%");
        Integer products = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Products WHERE CODE LIKE ?", Integer.class, PRODUCT_PREFIX + "%");
        assertTrue(products != null && products == 0, "TEST-24 product cleanup must be deterministic");
    }
}
