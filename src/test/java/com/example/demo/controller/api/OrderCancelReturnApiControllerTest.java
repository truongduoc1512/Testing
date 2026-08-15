package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
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
import com.example.demo.dao.OrderReturnDAO;
import com.example.demo.entity.OrderReturn;
import com.example.demo.form.OrderReturnForm;
import com.example.demo.form.ReturnStatusUpdateForm;

class OrderCancelReturnApiControllerTest {

    private OrderReturnDAO orderReturnDAO;
    private OrderDAO orderDAO;
    private OrderCancelReturnApiController controller;

    @BeforeEach
    void setUp() {
        orderReturnDAO = mock(OrderReturnDAO.class);
        orderDAO = mock(OrderDAO.class);
        controller = new OrderCancelReturnApiController();
        ReflectionTestUtils.setField(controller, "orderReturnDAO", orderReturnDAO);
        ReflectionTestUtils.setField(controller, "orderDAO", orderDAO);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void cancelOrder_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.cancelOrder("O1"));
        verify(orderReturnDAO, never()).cancelOrder(anyString(), anyString());
    }

    @Test
    void cancelOrder_returnsSuccessfulDaoOutcome() {
        authenticate("buyer", "ROLE_USER");
        when(orderReturnDAO.cancelOrder("buyer", "O1")).thenReturn(true);

        ResponseEntity<?> response = controller.cancelOrder("O1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, body(response).get("success"));
    }

    @Test
    void cancelOrder_returnsRejectedDaoOutcome() {
        authenticate("buyer", "ROLE_USER");
        when(orderReturnDAO.cancelOrder("buyer", "O2")).thenReturn(false);

        ResponseEntity<?> response = controller.cancelOrder("O2");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, body(response).get("success"));
    }

    @ParameterizedTest(name = "{0} -> {3}")
    @MethodSource("cancelOrderFailures")
    void cancelOrder_mapsDaoException(String failureCase, String orderId,
            RuntimeException failure, HttpStatus expectedStatus) {
        authenticate("buyer", "ROLE_USER");
        when(orderReturnDAO.cancelOrder("buyer", orderId)).thenThrow(failure);

        ResponseEntity<?> response = controller.cancelOrder(orderId);

        assertEquals(expectedStatus, response.getStatusCode());
        assertErrorBody(response);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void createReturn_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.createReturnRequest("O1", validReturnForm()));
        verify(orderReturnDAO, never()).createReturnRequest(anyString(), anyString(), any(OrderReturnForm.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidReturnForms")
    void createReturn_rejectsInvalidForm(String formCase, OrderReturnForm invalidForm) {
        authenticate("buyer", "ROLE_USER");

        assertBadRequest(controller.createReturnRequest("O1", invalidForm));

        verify(orderReturnDAO, never()).createReturnRequest(anyString(), anyString(), any(OrderReturnForm.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validReturnImages")
    @SuppressWarnings("unchecked")
    void createReturn_returnsCreatedDataForValidImageBoundary(String imageCase,
            String orderId, String imageUrls) {
        authenticate("buyer", "ROLE_USER");
        OrderReturnForm form = new OrderReturnForm(" valid reason ", imageUrls);
        OrderReturn created = returnRequest(orderId, "buyer", "PENDING");
        when(orderReturnDAO.createReturnRequest("buyer", orderId, form)).thenReturn(created);

        ResponseEntity<?> response = controller.createReturnRequest(orderId, form);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(created, ((Map<String, Object>) response.getBody()).get("data"));
    }

    @ParameterizedTest(name = "{0} -> {3}")
    @MethodSource("createReturnFailures")
    void createReturn_mapsDaoException(String failureCase, String orderId,
            RuntimeException failure, HttpStatus expectedStatus) {
        authenticate("buyer", "ROLE_USER");
        OrderReturnForm form = validReturnForm();
        when(orderReturnDAO.createReturnRequest("buyer", orderId, form)).thenThrow(failure);

        ResponseEntity<?> response = controller.createReturnRequest(orderId, form);

        assertEquals(expectedStatus, response.getStatusCode());
        assertErrorBody(response);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void getReturn_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.getReturnRequest("O1"));
        verify(orderReturnDAO, never()).findReturnByOrderId(anyString());
    }

    @Test
    void getReturn_returnsNotFoundWhenRequestDoesNotExist() {
        authenticate("buyer", "ROLE_USER");

        ResponseEntity<?> response = controller.getReturnRequest("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertErrorBody(response);
    }

    @Test
    void getReturn_forbidsRequestOwnedByAnotherUser() {
        authenticate("buyer", "ROLE_USER");
        OrderReturn foreign = returnRequest("O1", "other", "PENDING");
        when(orderReturnDAO.findReturnByOrderId("O1")).thenReturn(foreign);

        assertEquals(HttpStatus.FORBIDDEN, controller.getReturnRequest("O1").getStatusCode());
        verify(orderDAO, never()).canAccessOrder(anyString(), anyString(), anyString());
    }

    @Test
    void getReturn_allowsRequestOwner() {
        authenticate("buyer", "ROLE_USER");
        OrderReturn owned = returnRequest("O2", "buyer", "PENDING");
        when(orderReturnDAO.findReturnByOrderId("O2")).thenReturn(owned);

        ResponseEntity<?> response = controller.getReturnRequest("O2");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(owned, response.getBody());
        verify(orderDAO, never()).canAccessOrder(anyString(), anyString(), anyString());
    }

    @Test
    void getReturn_forbidsAdminOutsideOrderScope() {
        authenticate("admin", "ROLE_USER", "ROLE_ADMIN");
        OrderReturn orderReturn = returnRequest("O1", "buyer", "PENDING");
        when(orderReturnDAO.findReturnByOrderId("O1")).thenReturn(orderReturn);
        when(orderDAO.canAccessOrder("O1", "admin", "ROLE_ADMIN")).thenReturn(false);

        assertEquals(HttpStatus.FORBIDDEN, controller.getReturnRequest("O1").getStatusCode());
    }

    @Test
    void getReturn_allowsAdminWithinOrderScope() {
        authenticate("admin", "ROLE_USER", "ROLE_ADMIN");
        OrderReturn orderReturn = returnRequest("O1", "buyer", "PENDING");
        when(orderReturnDAO.findReturnByOrderId("O1")).thenReturn(orderReturn);
        when(orderDAO.canAccessOrder("O1", "admin", "ROLE_ADMIN")).thenReturn(true);

        ResponseEntity<?> response = controller.getReturnRequest("O1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(orderReturn, response.getBody());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonAdminAuthentications")
    void updateStatus_rejectsNonAdminAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals(HttpStatus.FORBIDDEN,
                controller.updateReturnStatus("O1", new ReturnStatusUpdateForm("APPROVE", null)).getStatusCode());
        verify(orderReturnDAO, never()).updateReturnStatus(anyString(), anyString(), anyString(), any());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStatusUpdateForms")
    void updateStatus_rejectsInvalidForm(String formCase, ReturnStatusUpdateForm invalidForm) {
        authenticate("admin", "ROLE_ADMIN");

        assertBadRequest(controller.updateReturnStatus("O1", invalidForm));

        verify(orderReturnDAO, never()).updateReturnStatus(any(), any(), any(), any());
    }

    @ParameterizedTest(name = "action={0}")
    @MethodSource("validStatusUpdates")
    @SuppressWarnings("unchecked")
    void updateStatus_returnsUpdatedRequest(String action, String adminNote,
            String orderId, String resultingStatus) {
        authenticate("admin", "ROLE_ADMIN");
        ReturnStatusUpdateForm form = new ReturnStatusUpdateForm(action, adminNote);
        OrderReturn updated = returnRequest(orderId, "buyer", resultingStatus);
        when(orderReturnDAO.updateReturnStatus("admin", orderId, action, adminNote)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateReturnStatus(orderId, form);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, ((Map<String, Object>) response.getBody()).get("data"));
    }

    @ParameterizedTest(name = "{0} -> {3}")
    @MethodSource("statusUpdateFailures")
    void updateStatus_mapsDaoException(String failureCase, String orderId,
            RuntimeException failure, HttpStatus expectedStatus) {
        authenticate("admin", "ROLE_ADMIN");
        ReturnStatusUpdateForm form = new ReturnStatusUpdateForm("APPROVE", null);
        when(orderReturnDAO.updateReturnStatus("admin", orderId, "APPROVE", null)).thenThrow(failure);

        ResponseEntity<?> response = controller.updateReturnStatus(orderId, form);

        assertEquals(expectedStatus, response.getStatusCode());
        assertErrorBody(response);
    }

    private void assertUnauthorized(ResponseEntity<?> response) {
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
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

    private static OrderReturnForm validReturnForm() {
        return new OrderReturnForm("valid reason", "image.jpg");
    }

    private static OrderReturn returnRequest(String orderId, String username, String status) {
        OrderReturn value = new OrderReturn();
        value.setOrderId(orderId);
        value.setUsername(username);
        value.setStatus(status);
        return value;
    }

    private static Stream<Arguments> loginRequiredAuthentications() {
        return Stream.of(
                Arguments.of("missing authentication", null),
                Arguments.of("unauthenticated token", unauthenticated("buyer")),
                Arguments.of("anonymous principal", authenticated("anonymousUser", "ROLE_USER")));
    }

    private static Stream<Arguments> nonAdminAuthentications() {
        return Stream.of(
                Arguments.of("missing authentication", null),
                Arguments.of("unauthenticated admin token", unauthenticated("admin")),
                Arguments.of("authenticated non-admin", authenticated("buyer", "ROLE_USER")));
    }

    private static Stream<Arguments> cancelOrderFailures() {
        return Stream.of(
                Arguments.of("invalid order state", "state",
                        new IllegalStateException("cannot cancel"), HttpStatus.BAD_REQUEST),
                Arguments.of("missing order", "missing",
                        new IllegalArgumentException("missing order"), HttpStatus.NOT_FOUND),
                Arguments.of("unexpected persistence failure", "failure",
                        new RuntimeException("database unavailable"), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private static Stream<Arguments> validReturnImages() {
        return Stream.of(
                Arguments.of("image is optional", "O1", null),
                Arguments.of("image reaches 500-character boundary", "O2", textOfLength('i', 500)));
    }

    private static Stream<Arguments> invalidReturnForms() {
        return Stream.of(
                Arguments.of("missing form", (Object) null),
                Arguments.of("missing reason", new OrderReturnForm(null, null)),
                Arguments.of("blank reason", new OrderReturnForm("   ", null)),
                Arguments.of("reason above maximum length",
                        new OrderReturnForm(textOfLength('r', 2001), null)),
                Arguments.of("image URL above maximum length",
                        new OrderReturnForm("valid", textOfLength('i', 501))));
    }

    private static Stream<Arguments> createReturnFailures() {
        return Stream.of(
                Arguments.of("order is not returnable", "state",
                        new IllegalStateException("not returnable"), HttpStatus.BAD_REQUEST),
                Arguments.of("order does not exist", "missing",
                        new IllegalArgumentException("missing order"), HttpStatus.NOT_FOUND),
                Arguments.of("unexpected persistence failure", "failure",
                        new RuntimeException("database unavailable"), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private static Stream<Arguments> validStatusUpdates() {
        return Stream.of(
                Arguments.of("approve", " valid note ", "O1", "APPROVED"),
                Arguments.of("REJECT", null, "O2", "REJECTED"));
    }

    private static Stream<Arguments> invalidStatusUpdateForms() {
        return Stream.of(
                Arguments.of("missing form", (Object) null),
                Arguments.of("missing action", new ReturnStatusUpdateForm(null, null)),
                Arguments.of("unsupported action", new ReturnStatusUpdateForm("UNKNOWN", null)),
                Arguments.of("admin note above maximum length",
                        new ReturnStatusUpdateForm("APPROVE", textOfLength('n', 256))));
    }

    private static Stream<Arguments> statusUpdateFailures() {
        return Stream.of(
                Arguments.of("invalid action", "argument",
                        new IllegalArgumentException("invalid action"), HttpStatus.BAD_REQUEST),
                Arguments.of("status conflict", "state",
                        new IllegalStateException("already handled"), HttpStatus.CONFLICT),
                Arguments.of("unexpected persistence failure", "failure",
                        new RuntimeException("database unavailable"), HttpStatus.INTERNAL_SERVER_ERROR));
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

    private static String textOfLength(char value, int count) {
        return String.join("", Collections.nCopies(count, String.valueOf(value)));
    }
}
