package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Product;
import com.example.demo.form.ProductForm;
import com.example.demo.model.ProductInfo;
import com.example.demo.pagination.PaginationResult;

class ProductApiControllerTest {

    private ProductDAO productDAO;
    private ProductApiController controller;

    @BeforeEach
    void setUp() {
        productDAO = mock(ProductDAO.class);
        controller = new ProductApiController();
        ReflectionTestUtils.setField(controller, "productDAO", productDAO);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProducts_normalizesPageAndPassesEveryFilter() {
        @SuppressWarnings("unchecked")
        PaginationResult<ProductInfo> firstPage = mock(PaginationResult.class);
        when(productDAO.queryProducts(1, 12, 10, "shoe", null, "price", 10.0, 20.0,
                "Hanoi", "Demo", true, false, 4, "Sneaker")).thenReturn(firstPage);

        ResponseEntity<PaginationResult<ProductInfo>> response = controller.getProducts(
                "shoe", -1, "price", 10.0, 20.0, "Hanoi", "Demo", true, false, 4, "Sneaker");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(firstPage, response.getBody());
    }

    @Test
    void getProducts_passesEmptyOptionalFiltersOnRequestedPage() {
        @SuppressWarnings("unchecked")
        PaginationResult<ProductInfo> secondPage = mock(PaginationResult.class);
        when(productDAO.queryProducts(2, 12, 10, "", null, "newest", null, null,
                null, null, null, null, null, null)).thenReturn(secondPage);

        assertSame(secondPage, controller.getProducts("", 2, "newest", null, null,
                null, null, null, null, null, null).getBody());
    }

    @Test
    void getProductByCode_returnsNotFoundWhenProductDoesNotExist() {
        ResponseEntity<?> response = controller.getProductByCode("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertErrorBody(response);
    }

    @Test
    void getProductByCode_returnsExistingProduct() {
        ProductInfo product = new ProductInfo();
        when(productDAO.findProductInfo("P1")).thenReturn(product);

        ResponseEntity<?> response = controller.getProductByCode("P1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(product, response.getBody());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidProductForms")
    void saveProduct_rejectsInvalidForm(String formCase, ProductForm invalidForm) {
        assertBadRequest(controller.saveProduct(invalidForm));

        verify(productDAO, never()).save(any(ProductForm.class));
    }

    @Test
    void saveProduct_createsNormalizedNewProduct() {
        authenticate("seller");
        ProductForm form = productForm(" P1 ", " New product ");
        ProductInfo created = new ProductInfo();
        when(productDAO.findProduct("P1")).thenReturn(null);
        when(productDAO.findProductInfo("P1")).thenReturn(created);

        ResponseEntity<?> response = controller.saveProduct(form);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(created, response.getBody());
        assertEquals("P1", form.getCode());
        assertEquals("New product", form.getName());
        verify(productDAO).save(form);
    }

    @Test
    void saveProduct_updatesProductOwnedByCurrentPrincipal() {
        authenticate("seller");
        Product existing = productOwnedBy("P2", "seller");
        ProductForm form = productForm(" P2 ", " Updated ");
        ProductInfo updated = new ProductInfo();
        when(productDAO.findProduct("P2")).thenReturn(existing);
        when(productDAO.findProductInfo("P2")).thenReturn(updated);

        ResponseEntity<?> response = controller.saveProduct(form);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, response.getBody());
        verify(productDAO).save(form);
    }

    @Test
    void saveProduct_forbidsUpdatingForeignProduct() {
        authenticate("other");
        ProductForm form = productForm("P1", "Name");
        when(productDAO.findProduct("P1")).thenReturn(productOwnedBy("P1", "seller"));

        ResponseEntity<?> response = controller.saveProduct(form);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertErrorBody(response);
        verify(productDAO, never()).save(form);
    }

    @ParameterizedTest(name = "{0} -> {4}")
    @MethodSource("saveProductFailures")
    void saveProduct_mapsDaoException(String failureCase, String code, String name,
            RuntimeException failure, HttpStatus expectedStatus) {
        authenticate("seller");
        ProductForm form = productForm(code, name);
        doThrow(failure).when(productDAO).save(form);

        ResponseEntity<?> response = controller.saveProduct(form);

        assertEquals(expectedStatus, response.getStatusCode());
        assertErrorBody(response);
        if (failure instanceof IllegalArgumentException) {
            assertEquals("invalid product", body(response).get("message"));
        }
    }

    @Test
    void deleteProduct_returnsNotFoundWhenProductDoesNotExist() {
        authenticate("seller");

        ResponseEntity<?> response = controller.deleteProduct("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productDAO, never()).deleteProduct("missing");
    }

    @Test
    void deleteProduct_forbidsProductOwnedByAnotherPrincipal() {
        authenticate("seller");
        when(productDAO.findProduct("foreign")).thenReturn(productOwnedBy("foreign", "other"));

        ResponseEntity<?> response = controller.deleteProduct("foreign");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(productDAO, never()).deleteProduct("foreign");
    }

    @Test
    void deleteProduct_deactivatesOwnedProduct() {
        authenticate("seller");
        when(productDAO.findProduct("owned")).thenReturn(productOwnedBy("owned", "seller"));

        ResponseEntity<?> response = controller.deleteProduct("owned");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, body(response).get("success"));
        verify(productDAO).deleteProduct("owned");
    }

    @Test
    void deleteProduct_returnsServerErrorWhenDaoFails() {
        authenticate("seller");
        when(productDAO.findProduct("failure")).thenReturn(productOwnedBy("failure", "seller"));
        doThrow(new RuntimeException("database unavailable")).when(productDAO).deleteProduct("failure");

        ResponseEntity<?> response = controller.deleteProduct("failure");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertErrorBody(response);
    }

    private void assertBadRequest(ResponseEntity<?> response) {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorBody(response);
    }

    private void assertErrorBody(ResponseEntity<?> response) {
        assertEquals(Boolean.FALSE, body(response).get("success"));
        assertTrue(body(response).containsKey("message"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> body(ResponseEntity<?> response) {
        return (Map<String, Object>) response.getBody();
    }

    private static ProductForm productForm(String code, String name) {
        ProductForm form = new ProductForm();
        form.setCode(code);
        form.setName(name);
        return form;
    }

    private static Product productOwnedBy(String code, String owner) {
        Product product = new Product();
        product.setCode(code);
        product.setOwnerUsername(owner);
        return product;
    }

    private static Stream<Arguments> saveProductFailures() {
        return Stream.of(
                Arguments.of("invalid product data", "P1", "Invalid",
                        new IllegalArgumentException("invalid product"), HttpStatus.BAD_REQUEST),
                Arguments.of("DAO authorization rejection", "P2", "Denied",
                        new AccessDeniedException("denied"), HttpStatus.FORBIDDEN),
                Arguments.of("unexpected persistence failure", "P3", "Failure",
                        new RuntimeException("database unavailable"), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private static Stream<Arguments> invalidProductForms() {
        return Stream.of(
                Arguments.of("missing form", (Object) null),
                Arguments.of("missing code", productForm(null, "Name")),
                Arguments.of("blank code", productForm("   ", "Name")),
                Arguments.of("missing name", productForm("P1", null)),
                Arguments.of("blank name", productForm("P1", "   ")));
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, "n/a", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }
}
