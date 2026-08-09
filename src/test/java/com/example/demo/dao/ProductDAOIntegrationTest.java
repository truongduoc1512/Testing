package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import com.example.demo.entity.Product;
import com.example.demo.form.ProductForm;
import com.example.demo.model.ProductInfo;
import com.example.demo.pagination.PaginationResult;

@EnabledIfSystemProperty(named = "dao.integration.enabled", matches = "true")
@SpringBootTest
class ProductDAOIntegrationTest {

    private static final String PREFIX = "ITP_" + UUID.randomUUID().toString().substring(0, 8) + "_";
    private static final String OWNER = "it_owner";

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanBeforeAndAuthenticate() {
        deleteTestProducts();
        DaoTestSupport.authenticate(OWNER, "ROLE_MANAGER");
    }

    @AfterEach
    void cleanAfter() {
        DaoTestSupport.clearAuthentication();
        deleteTestProducts();
    }

    @Test
    void save_commitsNewProductAndDatabaseAppliesMappedFields() {
        String code = PREFIX + "CREATE";

        productDAO.save(form(code, " Database Product ", 125.5, 15, 9));

        Map<String, Object> row = productRow(code);
        assertEquals(code, row.get("CODE"));
        assertEquals("Database Product", row.get("NAME"));
        assertEquals(125.5, ((Number) row.get("PRICE")).doubleValue(), 0.0001);
        assertEquals(15, ((Number) row.get("DISCOUNT_PERCENT")).intValue());
        assertEquals(9, ((Number) row.get("STOCK_QUANTITY")).intValue());
        assertEquals("ACTIVE", row.get("STATUS"));
        assertEquals(OWNER, row.get("OWNER_USERNAME"));
        assertNotNull(row.get("CREATE_DATE"));
    }

    @Test
    void save_updatesOwnedProductAcrossCommittedTransactions() {
        String code = PREFIX + "UPDATE";
        productDAO.save(form(code, "Before", 100, 0, 5));

        productDAO.save(form(code, " After ", 220, 20, 12));

        Map<String, Object> row = productRow(code);
        assertEquals("After", row.get("NAME"));
        assertEquals(220, ((Number) row.get("PRICE")).doubleValue(), 0.0001);
        assertEquals(20, ((Number) row.get("DISCOUNT_PERCENT")).intValue());
        assertEquals(12, ((Number) row.get("STOCK_QUANTITY")).intValue());
    }

    @Test
    void save_rejectsDifferentOwnerAndPreservesCommittedRow() {
        String code = PREFIX + "OWNER";
        productDAO.save(form(code, "Owned", 100, 0, 5));
        DaoTestSupport.authenticate("other_owner", "ROLE_MANAGER");

        assertThrows(AccessDeniedException.class,
                () -> productDAO.save(form(code, "Hijacked", 999, 0, 5)));

        Map<String, Object> row = productRow(code);
        assertEquals("Owned", row.get("NAME"));
        assertEquals(100, ((Number) row.get("PRICE")).doubleValue(), 0.0001);
        assertEquals(OWNER, row.get("OWNER_USERNAME"));
    }

    @Test
    void deleteProduct_commitsSoftDeleteAndActiveLookupStopsReturningIt() {
        String code = PREFIX + "DELETE";
        productDAO.save(form(code, "Delete me", 100, 0, 5));
        assertNotNull(productDAO.findActiveProduct(code));

        productDAO.deleteProduct(code);

        assertEquals("INACTIVE", productRow(code).get("STATUS"));
        assertNull(productDAO.findActiveProduct(code));
    }

    @Test
    void queryProducts_executesGeneratedHqlAndReturnsPersistedProjection() {
        String code = PREFIX + "QUERY";
        String uniqueName = "Integration Query 260809";
        productDAO.save(form(code, uniqueName, 200, 25, 7));

        PaginationResult<ProductInfo> page = productDAO.queryProducts(
                1, 10, 5, "Query 260809", null, "priceAsc",
                140.0, 160.0, null, null, null, null, null, null);

        List<ProductInfo> matches = page.getList();
        assertEquals(1, matches.size());
        assertEquals(code, matches.get(0).getCode());
        assertEquals(150, matches.get(0).getPrice(), 0.0001);
        assertEquals(7, matches.get(0).getStockQuantity());
    }

    @Test
    void save_persistsUploadedImageBytesAsBlob() {
        String code = PREFIX + "IMAGE";
        byte[] image = new byte[] { 1, 2, 3, 4, 5 };
        ProductForm form = form(code, "Image", 100, 0, 5);
        form.setFileData(new MockMultipartFile("image", "test.png", "image/png", image));

        productDAO.save(form);

        byte[] stored = jdbcTemplate.queryForObject(
                "SELECT IMAGE FROM Products WHERE CODE = ?", byte[].class, code);
        assertArrayEquals(image, stored);
    }

    @Test
    void findProductForUpdate_readsPersistedProductThroughRealHibernateSession() {
        String code = PREFIX + "LOCK";
        productDAO.save(form(code, "Locked", 100, 0, 5));

        Product locked = productDAO.findProductForUpdate(code);

        assertNotNull(locked);
        assertEquals(code, locked.getCode());
        assertEquals(OWNER, locked.getOwnerUsername());
    }

    @Test
    void cleanupScope_doesNotMatchExistingNonTestProducts() {
        Integer outsidePrefix = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Products WHERE LEFT(CODE, ?) <> ?", Integer.class,
                PREFIX.length(), PREFIX);

        assertNotNull(outsidePrefix);
        assertTrue(outsidePrefix > 0);
        assertEquals(0, countTestProducts());
    }

    private ProductForm form(String code, String name, double price, int discount, int stock) {
        ProductForm form = new ProductForm();
        form.setCode(code);
        form.setName(name);
        form.setPrice(price);
        form.setDiscountPercent(discount);
        form.setStockQuantity(stock);
        return form;
    }

    private Map<String, Object> productRow(String code) {
        return jdbcTemplate.queryForMap(
                "SELECT CODE, NAME, PRICE, DISCOUNT_PERCENT, STOCK_QUANTITY, STATUS, "
                        + "OWNER_USERNAME, CREATE_DATE FROM Products WHERE CODE = ?",
                code);
    }

    private int countTestProducts() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Products WHERE LEFT(CODE, ?) = ?", Integer.class,
                PREFIX.length(), PREFIX);
        return count == null ? 0 : count;
    }

    private void deleteTestProducts() {
        jdbcTemplate.update("DELETE FROM Products WHERE LEFT(CODE, ?) = ?", PREFIX.length(), PREFIX);
    }
}
