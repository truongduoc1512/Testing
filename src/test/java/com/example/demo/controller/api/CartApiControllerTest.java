package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Product;
import com.example.demo.form.CustomerForm;
import com.example.demo.model.CartInfo;
import com.example.demo.model.CustomerInfo;
import com.example.demo.model.ProductInfo;
import com.example.demo.service.OrderCheckoutService;
import com.example.demo.utils.Utils;

class CartApiControllerTest {

    private ProductDAO productDAO;
    private OrderCheckoutService orderCheckoutService;
    private CartApiController controller;

    @BeforeEach
    void setUp() {
        productDAO = mock(ProductDAO.class);
        orderCheckoutService = mock(OrderCheckoutService.class);
        controller = new CartApiController();
        ReflectionTestUtils.setField(controller, "productDAO", productDAO);
        ReflectionTestUtils.setField(controller, "orderCheckoutService", orderCheckoutService);
    }

    @Test
    void getCart_returnsAndStoresTheSessionCart() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<CartInfo> response = controller.getCart(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(response.getBody(), request.getSession().getAttribute("myCart"));
        assertTrue(response.getBody().isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidAddCartItemPayloads")
    void addCartItem_rejectsInvalidPayload(String payloadCase, Map<String, Object> invalidPayload) {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertBadRequest(controller.addCartItem(request, invalidPayload));

        verify(productDAO, never()).findActiveProduct(any());
        assertTrue(Utils.getCartInSession(request).isEmpty());
    }

    @Test
    void addCartItem_returnsNotFoundWhenProductDoesNotExist() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(productDAO.findActiveProduct("missing")).thenReturn(null);

        assertEquals(HttpStatus.NOT_FOUND,
                controller.addCartItem(request, payload(" missing ", 1)).getStatusCode());
        assertTrue(Utils.getCartInSession(request).isEmpty());
    }

    @Test
    void addCartItem_rejectsSoldOutProduct() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Product soldOut = product("P0", 0);
        when(productDAO.findActiveProduct("P0")).thenReturn(soldOut);

        assertBadRequest(controller.addCartItem(request, payload("P0", 1)));
        assertTrue(Utils.getCartInSession(request).isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedAddQuantities")
    void addCartItem_acceptsSupportedQuantityRepresentation(String quantityCase,
            Object requestedQuantity, int expectedQuantity) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Product available = product("P1", 10);
        when(productDAO.findActiveProduct("P1")).thenReturn(available);

        ResponseEntity<?> response = controller.addCartItem(request, payload(" P1 ", requestedQuantity));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedQuantity, ((CartInfo) response.getBody()).getQuantityTotal());
        assertSame(response.getBody(), request.getSession().getAttribute("myCart"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateCartItemPayloads")
    void updateCartItem_rejectsInvalidPayload(String payloadCase, Map<String, Object> invalidPayload) {
        MockHttpServletRequest request = requestWithProduct(product("P1", 10), 2);

        assertBadRequest(controller.updateCartItemQuantity(request, invalidPayload));

        verify(productDAO, never()).findActiveProduct(any());
        assertEquals(2, Utils.getCartInSession(request).getQuantityTotal());
    }

    @Test
    void updateCartItem_returnsNotFoundForMissingProduct() {
        MockHttpServletRequest request = requestWithProduct(product("P1", 10), 2);
        when(productDAO.findActiveProduct("missing")).thenReturn(null);

        ResponseEntity<?> response = controller.updateCartItemQuantity(request, payload("missing", 1));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(2, Utils.getCartInSession(request).getQuantityTotal());
    }

    @Test
    void updateCartItem_rejectsSoldOutProduct() {
        MockHttpServletRequest request = requestWithProduct(product("P1", 10), 2);
        Product soldOut = product("P0", 0);
        when(productDAO.findActiveProduct("P0")).thenReturn(soldOut);

        assertBadRequest(controller.updateCartItemQuantity(request, payload("P0", 1)));
        assertEquals(2, Utils.getCartInSession(request).getQuantityTotal());
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateCartItem_reportsRequestedQuantityWhenStockIsSufficient() {
        Product available = product("P1", 5);
        MockHttpServletRequest request = requestWithProduct(available, 1);
        when(productDAO.findActiveProduct("P1")).thenReturn(available);

        ResponseEntity<?> response = controller.updateCartItemQuantity(request, payload("P1", 3));
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, body.get("actualQuantity"));
        assertEquals(Boolean.FALSE, body.get("capped"));
        assertFalse(body.containsKey("message"));
        assertEquals(3, ((CartInfo) body.get("cart")).getQuantityTotal());
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateCartItem_capsRequestedQuantityAtAvailableStock() {
        Product available = product("P1", 5);
        MockHttpServletRequest request = requestWithProduct(available, 1);
        when(productDAO.findActiveProduct("P1")).thenReturn(available);

        ResponseEntity<?> response = controller.updateCartItemQuantity(request, payload("P1", 9));
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, body.get("actualQuantity"));
        assertEquals(Boolean.TRUE, body.get("capped"));
        assertTrue(body.containsKey("message"));
        assertEquals(5, ((CartInfo) body.get("cart")).getQuantityTotal());
    }

    @Test
    void removeCartItem_returnsNotFoundWithoutChangingCart() {
        MockHttpServletRequest request = requestWithProduct(product("P1", 5), 2);
        when(productDAO.findProduct("missing")).thenReturn(null);

        ResponseEntity<?> response = controller.removeCartItem(request, "missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(2, Utils.getCartInSession(request).getQuantityTotal());
    }

    @Test
    void removeCartItem_removesExistingCartLine() {
        MockHttpServletRequest request = requestWithProduct(product("P1", 5), 2);
        Product product = product("P1", 5);
        when(productDAO.findProduct("P1")).thenReturn(product);

        ResponseEntity<?> response = controller.removeCartItem(request, "P1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((CartInfo) response.getBody()).isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCustomerForms")
    void saveCustomerInfo_rejectsInvalidForm(String formCase, CustomerForm invalidForm) {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertBadRequest(controller.saveCustomerInfo(request, invalidForm));

        assertTrue(Utils.getCartInSession(request).isEmpty());
        assertNull(Utils.getCartInSession(request).getCustomerInfo());
    }

    @Test
    void saveCustomerInfo_trimsNormalizesAndStoresValidCustomer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CustomerForm form = customer(" Buyer ", " Address ", " Buyer@Example.COM ", " 0900 ");

        ResponseEntity<?> response = controller.saveCustomerInfo(request, form);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CartInfo cart = (CartInfo) response.getBody();
        assertTrue(form.isValid());
        assertTrue(cart.isValidCustomer());
        assertEquals("Buyer", cart.getCustomerInfo().getName());
        assertEquals("buyer@example.com", cart.getCustomerInfo().getEmail());
        assertEquals("0900", cart.getCustomerInfo().getPhone());
    }

    @Test
    void checkout_rejectsEmptyCart() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertBadRequest(controller.checkoutOrder(request));
        verify(orderCheckoutService, never()).checkout(any(CartInfo.class));
    }

    @Test
    void checkout_rejectsCartWithoutValidCustomer() {
        MockHttpServletRequest request = requestWithProduct(product("P1", 5), 1);

        assertBadRequest(controller.checkoutOrder(request));
        verify(orderCheckoutService, never()).checkout(any(CartInfo.class));
    }

    @Test
    void checkout_preservesCartWhenOrderSaveFails() {
        MockHttpServletRequest request = validCheckoutRequest("P2");
        CartInfo cart = Utils.getCartInSession(request);
        doThrow(new IllegalStateException("stock changed")).when(orderCheckoutService).checkout(cart);

        assertBadRequest(controller.checkoutOrder(request));
        assertSame(cart, request.getSession().getAttribute("myCart"));
        assertNull(request.getSession().getAttribute("lastOrderedCart"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkout_storesOrderedCartAndClearsActiveCart() {
        MockHttpServletRequest request = validCheckoutRequest("P3");
        CartInfo orderedCart = Utils.getCartInSession(request);

        ResponseEntity<?> response = controller.checkoutOrder(request);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Boolean.TRUE, body.get("success"));
        assertSame(orderedCart, body.get("orderedCart"));
        assertNull(request.getSession().getAttribute("myCart"));
        assertSame(orderedCart, request.getSession().getAttribute("lastOrderedCart"));
        verify(orderCheckoutService).checkout(orderedCart);
    }

    @SuppressWarnings("unchecked")
    private void assertBadRequest(ResponseEntity<?> response) {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Boolean.FALSE, ((Map<String, Object>) response.getBody()).get("success"));
        assertTrue(((Map<String, Object>) response.getBody()).containsKey("message"));
    }

    private MockHttpServletRequest validCheckoutRequest(String code) {
        MockHttpServletRequest request = requestWithProduct(product(code, 5), 1);
        CustomerInfo customer = new CustomerInfo();
        customer.setValid(true);
        Utils.getCartInSession(request).setCustomerInfo(customer);
        return request;
    }

    private MockHttpServletRequest requestWithProduct(Product product, int quantity) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Utils.getCartInSession(request).addProduct(new ProductInfo(product), quantity);
        return request;
    }

    private Product product(String code, int stock) {
        Product product = new Product();
        product.setCode(code);
        product.setName("Product " + code);
        product.setStockQuantity(stock);
        product.setPrice(10.0);
        return product;
    }

    private static Map<String, Object> payload(Object code, Object quantity) {
        Map<String, Object> payload = new HashMap<>();
        if (code != null) {
            payload.put("code", code);
        }
        if (quantity != null) {
            payload.put("quantity", quantity);
        }
        return payload;
    }

    private static Stream<Arguments> supportedAddQuantities() {
        return Stream.of(
                Arguments.of("missing quantity defaults to one", null, 1),
                Arguments.of("numeric string quantity", "2", 2),
                Arguments.of("numeric quantity", 2L, 2));
    }

    private static Stream<Arguments> invalidAddCartItemPayloads() {
        return Stream.of(
                Arguments.of("missing payload", (Object) null),
                Arguments.of("missing code", payload(null, null)),
                Arguments.of("blank code", payload("   ", 1)),
                Arguments.of("non-numeric quantity", payload("P1", "not-a-number")),
                Arguments.of("NaN quantity", payload("P1", Double.NaN)),
                Arguments.of("infinite quantity", payload("P1", Double.POSITIVE_INFINITY)),
                Arguments.of("fractional quantity", payload("P1", 1.5d)),
                Arguments.of("quantity below integer range",
                        payload("P1", ((double) Integer.MIN_VALUE) - 1d)),
                Arguments.of("quantity above integer range",
                        payload("P1", ((double) Integer.MAX_VALUE) + 1d)),
                Arguments.of("non-positive quantity", payload("P1", 0)));
    }

    private static Stream<Arguments> invalidUpdateCartItemPayloads() {
        return Stream.of(
                Arguments.of("missing payload", (Object) null),
                Arguments.of("missing code", payload(null, 1)),
                Arguments.of("empty code", payload("", 1)),
                Arguments.of("non-numeric quantity", payload("P1", "invalid")),
                Arguments.of("non-positive quantity", payload("P1", 0)));
    }

    private static Stream<Arguments> invalidCustomerForms() {
        return Stream.of(
                Arguments.of("missing form", (Object) null),
                Arguments.of("missing name", customer(null, "Address", "a@example.com", "0900")),
                Arguments.of("blank name", customer("   ", "Address", "a@example.com", "0900")),
                Arguments.of("name above maximum length",
                        customer(textOfLength('n', 256), "Address", "a@example.com", "0900")),
                Arguments.of("missing address", customer("Buyer", null, "a@example.com", "0900")),
                Arguments.of("blank address", customer("Buyer", "   ", "a@example.com", "0900")),
                Arguments.of("address above maximum length",
                        customer("Buyer", textOfLength('a', 256), "a@example.com", "0900")),
                Arguments.of("missing email", customer("Buyer", "Address", null, "0900")),
                Arguments.of("blank email", customer("Buyer", "Address", "   ", "0900")),
                Arguments.of("email above maximum length",
                        customer("Buyer", "Address", textOfLength('e', 129), "0900")),
                Arguments.of("malformed email",
                        customer("Buyer", "Address", "invalid-email", "0900")),
                Arguments.of("missing phone", customer("Buyer", "Address", "a@example.com", null)),
                Arguments.of("blank phone", customer("Buyer", "Address", "a@example.com", "   ")),
                Arguments.of("phone above maximum length",
                        customer("Buyer", "Address", "a@example.com", textOfLength('p', 129))));
    }

    private static CustomerForm customer(String name, String address, String email, String phone) {
        CustomerForm form = new CustomerForm();
        form.setName(name);
        form.setAddress(address);
        form.setEmail(email);
        form.setPhone(phone);
        return form;
    }

    private static String textOfLength(char value, int count) {
        return String.join("", java.util.Collections.nCopies(count, String.valueOf(value)));
    }
}
