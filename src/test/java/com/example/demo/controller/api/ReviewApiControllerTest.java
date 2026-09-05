package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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

import com.example.demo.dao.ProductReviewDAO;
import com.example.demo.entity.ProductReview;
import com.example.demo.form.ProductReviewForm;

class ReviewApiControllerTest {

    private ProductReviewDAO productReviewDAO;
    private ReviewApiController controller;

    @BeforeEach
    void setUp() {
        productReviewDAO = mock(ProductReviewDAO.class);
        controller = new ReviewApiController();
        ReflectionTestUtils.setField(controller, "productReviewDAO", productReviewDAO);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getReviews_returnsDaoList() {
        List<ProductReview> reviews = Arrays.asList(new ProductReview(), new ProductReview());
        when(productReviewDAO.getReviewsByProductCode("P1")).thenReturn(reviews);

        ResponseEntity<List<ProductReview>> response = controller.getReviewsByProductCode("P1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(reviews, response.getBody());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void saveReview_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.saveReview(review("P1", 5, "great")));
        verify(productReviewDAO, never()).saveReview(any(ProductReview.class));
    }

    @Test
    void saveReview_rejectsAdminRole() {
        authenticate("admin", "ROLE_USER", "ROLE_ADMIN");
        ResponseEntity<?> forbidden = controller.saveReview(review("P1", 5, "great"));

        assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());
        assertErrorBody(forbidden);
        verify(productReviewDAO, never()).saveReview(any(ProductReview.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidReviewForms")
    void saveReview_rejectsInvalidForm(String formCase, ProductReviewForm invalidForm) {
        authenticate("buyer", "ROLE_USER");

        assertBadRequest(controller.saveReview(invalidForm));

        verify(productReviewDAO, never()).saveReview(any(ProductReview.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveReview_trimsAndReturnsCreatedReview() {
        authenticate("buyer", "ROLE_USER");

        ResponseEntity<?> response = controller.saveReview(review(" P1 ", 5, " great "));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(Boolean.TRUE, body.get("success"));
        ProductReview savedReview = (ProductReview) body.get("review");
        assertEquals("P1", savedReview.getProductCode());
        assertEquals("buyer", savedReview.getUsername());
        assertEquals(5, savedReview.getRatingValue());
        assertEquals("great", savedReview.getComment());
        verify(productReviewDAO).saveReview(savedReview);
    }

    @Test
    void saveReview_mapsDomainExceptionToBadRequest() {
        authenticate("buyer", "ROLE_USER");
        doThrow(new IllegalArgumentException("invalid product"))
                .when(productReviewDAO).saveReview(any(ProductReview.class));

        ResponseEntity<?> domainError = controller.saveReview(review("P1", 4, "valid"));

        assertBadRequest(domainError);
        assertEquals("invalid product", body(domainError).get("message"));
    }

    @Test
    void saveReview_mapsUnexpectedExceptionToServerError() {
        authenticate("buyer", "ROLE_USER");
        doThrow(new IllegalStateException("database unavailable"))
                .when(productReviewDAO).saveReview(any(ProductReview.class));

        ResponseEntity<?> unexpectedError = controller.saveReview(review("P1", 4, "valid"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, unexpectedError.getStatusCode());
        assertErrorBody(unexpectedError);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void updateReview_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.updateReview(1L, updatePayload(5, "great")));
        verify(productReviewDAO, never()).updateReview(anyLong(), anyString(), anyInt(), anyString());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdatePayloads")
    void updateReview_rejectsInvalidPayload(String payloadCase, Map<String, Object> invalidPayload) {
        authenticate("buyer", "ROLE_USER");

        assertBadRequest(controller.updateReview(1L, invalidPayload));

        verify(productReviewDAO, never()).updateReview(anyLong(), anyString(), anyInt(), anyString());
    }

    @Test
    void updateReview_returnsUpdatedEntity() {
        authenticate("buyer", "ROLE_USER");
        ProductReview updated = new ProductReview();
        when(productReviewDAO.updateReview(1L, "buyer", 5, "updated")).thenReturn(true);
        when(productReviewDAO.findReview(1L)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateReview(1L, updatePayload(5L, " updated "));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, response.getBody());
    }

    @Test
    void updateReview_returnsBadRequestWhenDaoRejectsUpdate() {
        authenticate("buyer", "ROLE_USER");
        when(productReviewDAO.updateReview(2L, "buyer", 4, "valid")).thenReturn(false);

        assertBadRequest(controller.updateReview(2L, updatePayload(4, "valid")));
    }

    @Test
    void updateReview_boundary_23h59m_allowed() {
        authenticate("buyer", "ROLE_USER");
        ProductReview existing = new ProductReview();
        existing.setReviewId(1L);
        existing.setUsername("buyer");
        existing.setCreatedAt(new java.util.Date(System.currentTimeMillis() - 86_340_000L)); // 23h59m ago

        ProductReview updated = new ProductReview();
        when(productReviewDAO.findReview(1L)).thenReturn(existing).thenReturn(updated);
        when(productReviewDAO.updateReview(1L, "buyer", 5, "updated")).thenReturn(true);

        ResponseEntity<?> response = controller.updateReview(1L, updatePayload(5, "updated"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, response.getBody());
    }

    @Test
    void updateReview_boundary_24h01m_forbidden() {
        authenticate("buyer", "ROLE_USER");
        ProductReview existing = new ProductReview();
        existing.setReviewId(1L);
        existing.setUsername("buyer");
        existing.setCreatedAt(new java.util.Date(System.currentTimeMillis() - 86_460_000L)); // 24h01m ago

        when(productReviewDAO.findReview(1L)).thenReturn(existing);

        ResponseEntity<?> response = controller.updateReview(1L, updatePayload(5, "updated"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> body = body(response);
        assertEquals(Boolean.FALSE, body.get("success"));
        assertEquals("Chỉ có thể chỉnh sửa đánh giá trong vòng 24 giờ", body.get("message"));
        verify(productReviewDAO, never()).updateReview(anyLong(), anyString(), anyInt(), anyString());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void deleteReview_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.deleteReview(1L));
        verify(productReviewDAO, never()).deleteReview(anyLong(), anyString());
    }

    @Test
    void deleteReview_returnsSuccessWhenDaoDeletesReview() {
        authenticate("buyer", "ROLE_USER");
        when(productReviewDAO.deleteReview(1L, "buyer")).thenReturn(true);
        ResponseEntity<?> response = controller.deleteReview(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, body(response).get("success"));
    }

    @Test
    void deleteReview_returnsBadRequestWhenDaoRejectsDeletion() {
        authenticate("buyer", "ROLE_USER");
        when(productReviewDAO.deleteReview(2L, "buyer")).thenReturn(false);

        assertBadRequest(controller.deleteReview(2L));
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

    private static ProductReviewForm review(String productCode, int rating, String comment) {
        ProductReviewForm form = new ProductReviewForm();
        form.setProductCode(productCode);
        form.setRatingValue(rating);
        form.setComment(comment);
        return form;
    }

    private static Map<String, Object> updatePayload(Object rating, Object comment) {
        Map<String, Object> payload = new HashMap<>();
        if (rating != null) {
            payload.put("ratingValue", rating);
        }
        if (comment != null) {
            payload.put("comment", comment);
        }
        return payload;
    }

    private void authenticate(String username, String... roles) {
        SecurityContextHolder.getContext().setAuthentication(authenticated(username, roles));
    }

    private static Stream<Arguments> loginRequiredAuthentications() {
        return Stream.of(
                Arguments.of("missing authentication", null),
                Arguments.of("unauthenticated token", unauthenticated("buyer")),
                Arguments.of("anonymous principal", authenticated("anonymousUser", "ROLE_USER")));
    }

    private static Stream<Arguments> invalidReviewForms() {
        return Stream.of(
                Arguments.of("missing form", (Object) null),
                Arguments.of("missing product code", review(null, 5, "great")),
                Arguments.of("blank product code", review("   ", 5, "great")),
                Arguments.of("product code above maximum length",
                        review(textOfLength('p', 21), 5, "great")),
                Arguments.of("missing comment", review("P1", 5, null)),
                Arguments.of("blank comment", review("P1", 5, "   ")),
                Arguments.of("comment above maximum length",
                        review("P1", 5, textOfLength('c', 2001))),
                Arguments.of("rating below minimum", review("P1", 0, "great")),
                Arguments.of("rating above maximum", review("P1", 6, "great")));
    }

    private static Stream<Arguments> invalidUpdatePayloads() {
        return Stream.of(
                Arguments.of("missing payload", (Object) null),
                Arguments.of("non-text comment", updatePayload(5, 123)),
                Arguments.of("blank comment", updatePayload(5, "   ")),
                Arguments.of("comment above maximum length",
                        updatePayload(5, textOfLength('c', 2001))),
                Arguments.of("text rating", updatePayload("5", "valid")),
                Arguments.of("NaN rating", updatePayload(Double.NaN, "valid")),
                Arguments.of("infinite rating", updatePayload(Double.POSITIVE_INFINITY, "valid")),
                Arguments.of("fractional rating", updatePayload(4.5d, "valid")),
                Arguments.of("rating below minimum", updatePayload(0, "valid")),
                Arguments.of("rating above maximum", updatePayload(6, "valid")));
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
