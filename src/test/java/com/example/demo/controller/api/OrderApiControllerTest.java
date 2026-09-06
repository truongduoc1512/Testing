package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dao.OrderDAO;
import com.example.demo.model.OrderDetailInfo;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.OrderStatus;
import com.example.demo.pagination.PaginationResult;

class OrderApiControllerTest {

    private OrderDAO orderDAO;
    private OrderApiController controller;

    @BeforeEach
    void setUp() {
        orderDAO = mock(OrderDAO.class);
        controller = new OrderApiController();
        ReflectionTestUtils.setField(controller, "orderDAO", orderDAO);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("orderListScopes")
    void getOrders_normalizesPageAndResolvesPrincipalScope(String scopeCase,
            int requestedPage, Authentication authentication, int expectedPage, String expectedRole) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        @SuppressWarnings("unchecked")
        PaginationResult<OrderInfo> expected = mock(PaginationResult.class);
        when(orderDAO.listOrderInfo(expectedPage, 10, 10, authentication.getName(), expectedRole))
                .thenReturn(expected);

        assertSame(expected, controller.getOrders(requestedPage).getBody());
        verify(orderDAO).listOrderInfo(expectedPage, 10, 10, authentication.getName(), expectedRole);
    }

    @Test
    void getOrder_returnsNotFoundBeforeAuthorization() {
        ResponseEntity<?> response = controller.getOrderById("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertErrorBody(response);
        verify(orderDAO, never()).canAccessOrder("missing", null, "");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void getOrder_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        OrderInfo order = new OrderInfo();
        when(orderDAO.getOrderInfo("O1")).thenReturn(order);

        assertForbidden(controller.getOrderById("O1"));
        verify(orderDAO, never()).canAccessOrder(anyString(), anyString(), anyString());
    }

    @Test
    void getOrder_rejectsAuthenticatedPrincipalOutsideOrderScope() {
        authenticate("buyer", "ROLE_USER");
        OrderInfo order = new OrderInfo();
        when(orderDAO.getOrderInfo("O1")).thenReturn(order);
        when(orderDAO.canAccessOrder("O1", "buyer", "ROLE_USER")).thenReturn(false);

        assertForbidden(controller.getOrderById("O1"));
        verify(orderDAO, never()).listOrderDetailInfosForPrincipal("O1", "buyer", "ROLE_USER");
    }

    @Test
    void getOrder_loadsUserDetailsWithoutRecalculatingAmount() {
        authenticate("buyer", "ROLE_USER");
        OrderInfo order = new OrderInfo();
        order.setAmount(99.0);
        List<OrderDetailInfo> details = Arrays.asList(orderDetailWithAmount(10.0), orderDetailWithAmount(15.0));
        when(orderDAO.getOrderInfo("O1")).thenReturn(order);
        when(orderDAO.canAccessOrder("O1", "buyer", "ROLE_USER")).thenReturn(true);
        when(orderDAO.listOrderDetailInfosForPrincipal("O1", "buyer", "ROLE_USER"))
                .thenReturn(details);

        ResponseEntity<?> response = controller.getOrderById("O1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(order, response.getBody());
        assertEquals(details, order.getDetails());
        assertEquals(99.0, order.getAmount());
        verify(orderDAO, never()).isOrderCustomer("O1", "buyer");
    }

    @Test
    void getOrder_preservesAmountWhenAdminIsOrderCustomer() {
        authenticate("admin", "ROLE_ADMIN");
        OrderInfo customerOrder = new OrderInfo();
        customerOrder.setAmount(80.0);
        List<OrderDetailInfo> customerDetails = Collections.singletonList(orderDetailWithAmount(10.0));
        when(orderDAO.getOrderInfo("C1")).thenReturn(customerOrder);
        when(orderDAO.canAccessOrder("C1", "admin", "ROLE_ADMIN")).thenReturn(true);
        when(orderDAO.listOrderDetailInfosForPrincipal("C1", "admin", "ROLE_ADMIN"))
                .thenReturn(customerDetails);
        when(orderDAO.isOrderCustomer("C1", "admin")).thenReturn(true);

        ResponseEntity<?> response = controller.getOrderById("C1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(customerOrder, response.getBody());
        assertEquals(80.0, customerOrder.getAmount());
    }

    @Test
    void getOrder_recalculatesAmountWhenAdminIsNotOrderCustomer() {
        authenticate("admin", "ROLE_ADMIN");
        OrderInfo sellerOrder = new OrderInfo();
        List<OrderDetailInfo> sellerDetails = Arrays.asList(
                orderDetailWithAmount(10.0), orderDetailWithAmount(15.0));
        when(orderDAO.getOrderInfo("S1")).thenReturn(sellerOrder);
        when(orderDAO.canAccessOrder("S1", "admin", "ROLE_ADMIN")).thenReturn(true);
        when(orderDAO.listOrderDetailInfosForPrincipal("S1", "admin", "ROLE_ADMIN"))
                .thenReturn(sellerDetails);
        when(orderDAO.isOrderCustomer("S1", "admin")).thenReturn(false);

        ResponseEntity<?> response = controller.getOrderById("S1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(sellerOrder, response.getBody());
        assertEquals(25.0, sellerOrder.getAmount());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStatusPayloads")
    void updateStatus_rejectsInvalidPayload(String payloadCase, Map<String, String> invalidPayload) {
        authenticate("admin", "ROLE_ADMIN");

        assertBadRequest(controller.updateOrderStatus("O1", invalidPayload));

        verify(orderDAO, never()).getOrderInfo("O1");
    }

    @Test
    void updateStatus_returnsNotFoundWhenOrderDoesNotExist() {
        authenticate("admin", "ROLE_ADMIN");
        assertEquals(HttpStatus.NOT_FOUND,
                controller.updateOrderStatus("missing", statusPayload("PENDING")).getStatusCode());
        verify(orderDAO, never()).canManageOrder("missing", "admin");
    }

    @Test
    void updateStatus_rejectsPrincipalOutsideManagementScope() {
        authenticate("admin", "ROLE_ADMIN");
        OrderInfo order = new OrderInfo();
        when(orderDAO.getOrderInfo("O1")).thenReturn(order);
        when(orderDAO.canManageOrder("O1", "admin")).thenReturn(false);

        ResponseEntity<?> forbidden = controller.updateOrderStatus("O1", statusPayload("PENDING"));

        assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());
        assertErrorBody(forbidden);
        verify(orderDAO, never()).updateOrderStatus("O1", OrderStatus.PENDING);
    }

    @Test
    void updateStatus_normalizesAndReturnsUpdatedOrder() {
        authenticate("admin", "ROLE_ADMIN");
        OrderInfo original = new OrderInfo();
        OrderInfo updated = new OrderInfo();
        when(orderDAO.getOrderInfo("O1")).thenReturn(original, updated);
        when(orderDAO.canManageOrder("O1", "admin")).thenReturn(true);

        ResponseEntity<?> response = controller.updateOrderStatus("O1", statusPayload(" shipped "));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, response.getBody());
        verify(orderDAO).updateOrderStatus("O1", OrderStatus.SHIPPING);
    }

    @ParameterizedTest(name = "{0} -> {4}")
    @MethodSource("statusUpdateFailures")
    void updateStatus_mapsDaoException(String failureCase, String orderId,
            String requestedStatus, String normalizedStatus, HttpStatus expectedStatus,
            RuntimeException failure) {
        authenticate("admin", "ROLE_ADMIN");
        when(orderDAO.getOrderInfo(orderId)).thenReturn(new OrderInfo());
        when(orderDAO.canManageOrder(orderId, "admin")).thenReturn(true);
        doThrow(failure).when(orderDAO).updateOrderStatus(orderId, normalizedStatus);

        ResponseEntity<?> response = controller.updateOrderStatus(orderId, statusPayload(requestedStatus));

        assertEquals(expectedStatus, response.getStatusCode());
        assertErrorBody(response);
    }

    private void assertForbidden(ResponseEntity<?> response) {
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
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

    private static Map<String, String> statusPayload(String status) {
        Map<String, String> payload = new HashMap<>();
        payload.put("status", status);
        return payload;
    }

    private static OrderDetailInfo orderDetailWithAmount(double amount) {
        OrderDetailInfo detail = new OrderDetailInfo();
        detail.setAmount(amount);
        return detail;
    }

    private static Stream<Arguments> orderListScopes() {
        return Stream.of(
                Arguments.of("admin with negative page", -1,
                        authenticated("admin", "ROLE_ADMIN"), 1, "ROLE_ADMIN"),
                Arguments.of("user role selected after unsupported manager role", 2,
                        authenticated("buyer", "ROLE_MANAGER", "ROLE_USER"), 2, "ROLE_USER"),
                Arguments.of("unsupported manager role uses blank scope", 1,
                        authenticated("manager", "ROLE_MANAGER"), 1, ""));
    }

    private static Stream<Arguments> loginRequiredAuthentications() {
        return Stream.of(
                Arguments.of("missing authentication", null),
                Arguments.of("unauthenticated token", unauthenticated("buyer")),
                Arguments.of("anonymous principal", authenticated("anonymousUser", "ROLE_USER")));
    }

    private static Stream<Arguments> statusUpdateFailures() {
        return Stream.of(
                Arguments.of("invalid transition", "state", "COMPLETED", OrderStatus.COMPLETED,
                        HttpStatus.CONFLICT, new IllegalStateException("invalid transition")),
                Arguments.of("unexpected persistence failure", "failure", "CANCELLED", OrderStatus.CANCELLED,
                        HttpStatus.INTERNAL_SERVER_ERROR, new RuntimeException("database unavailable")));
    }

    private static Stream<Arguments> invalidStatusPayloads() {
        return Stream.of(
                Arguments.of("missing payload", (Object) null),
                Arguments.of("missing status", new HashMap<>()),
                Arguments.of("blank status", statusPayload("   ")),
                Arguments.of("non-admin status", statusPayload("RETURNED")));
    }

    private Authentication authenticate(String username, String... roles) {
        Authentication auth = authenticated(username, roles);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return auth;
    }

    private static Authentication authenticated(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .collect(java.util.stream.Collectors.toList());
        return new UsernamePasswordAuthenticationToken(username, "n/a", authorities);
    }

    private static Authentication unauthenticated(String username) {
        return new UsernamePasswordAuthenticationToken(username, "n/a");
    }
}
