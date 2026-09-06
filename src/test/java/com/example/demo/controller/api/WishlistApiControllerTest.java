package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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

import com.example.demo.dao.ProductDAO;
import com.example.demo.dao.WishlistDAO;
import com.example.demo.entity.Product;
import com.example.demo.model.ProductInfo;

class WishlistApiControllerTest {

    private WishlistDAO wishlistDAO;
    private ProductDAO productDAO;
    private WishlistApiController controller;

    @BeforeEach
    void setUp() {
        wishlistDAO = mock(WishlistDAO.class);
        productDAO = mock(ProductDAO.class);
        controller = new WishlistApiController();
        ReflectionTestUtils.setField(controller, "wishlistDAO", wishlistDAO);
        ReflectionTestUtils.setField(controller, "productDAO", productDAO);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void getWishlist_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.getWishlist());
        verify(wishlistDAO, never()).getUserWishlistProducts(anyString());
    }

    @Test
    void getWishlist_returnsAuthenticatedUserProducts() {
        authenticate("buyer");
        List<ProductInfo> products = Arrays.asList(new ProductInfo(), new ProductInfo());
        when(wishlistDAO.getUserWishlistProducts("buyer")).thenReturn(products);

        ResponseEntity<?> response = controller.getWishlist();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(products, response.getBody());
    }

    @Test
    void checkWishlist_returnsFalseWithoutAuthenticatedUsername() {
        ResponseEntity<?> response = controller.checkWishlist("P1");

        assertEquals(Boolean.FALSE, body(response).get("favorite"));
        verify(wishlistDAO, never()).isFavorite(null, "P1");
    }

    @Test
    void checkWishlist_returnsFalseForNullProductCode() {
        authenticate("buyer");

        ResponseEntity<?> response = controller.checkWishlist(null);

        assertEquals(Boolean.FALSE, body(response).get("favorite"));
        verify(wishlistDAO, never()).isFavorite("buyer", null);
    }

    @Test
    void checkWishlist_returnsFalseForProductOutsideWishlist() {
        authenticate("buyer");
        when(wishlistDAO.isFavorite("buyer", "P1")).thenReturn(false);

        assertEquals(Boolean.FALSE, body(controller.checkWishlist("P1")).get("favorite"));
    }

    @Test
    void checkWishlist_returnsTrueForFavoriteProduct() {
        authenticate("buyer");
        when(wishlistDAO.isFavorite("buyer", "P2")).thenReturn(true);

        assertEquals(Boolean.TRUE, body(controller.checkWishlist("P2")).get("favorite"));
    }

    @Test
    void toggleWishlist_requiresAuthentication() {
        assertUnauthorized(controller.toggleWishlist("P1"));
        verify(productDAO, never()).findProduct(anyString());
    }

    @Test
    void toggleWishlist_returnsNotFoundWhenProductDoesNotExist() {
        authenticate("buyer");

        ResponseEntity<?> response = controller.toggleWishlist("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Boolean.FALSE, body(response).get("success"));
        verify(wishlistDAO, never()).addWishlist("buyer", "missing");
    }

    @Test
    void toggleWishlist_addsProductOutsideWishlist() {
        authenticate("buyer");
        when(productDAO.findProduct("P1")).thenReturn(new Product());
        when(wishlistDAO.isFavorite("buyer", "P1")).thenReturn(false);
        when(wishlistDAO.getWishlistCount("buyer")).thenReturn(1);

        ResponseEntity<?> response = controller.toggleWishlist("P1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, body(response).get("favorite"));
        assertEquals(1, body(response).get("wishlistCount"));
        verify(wishlistDAO).addWishlist("buyer", "P1");
        verify(wishlistDAO, never()).removeWishlist("buyer", "P1");
    }

    @Test
    void toggleWishlist_removesFavoriteProduct() {
        authenticate("buyer");
        when(productDAO.findProduct("P2")).thenReturn(new Product());
        when(wishlistDAO.isFavorite("buyer", "P2")).thenReturn(true);
        when(wishlistDAO.getWishlistCount("buyer")).thenReturn(0);

        ResponseEntity<?> response = controller.toggleWishlist("P2");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, body(response).get("favorite"));
        assertEquals(0, body(response).get("wishlistCount"));
        verify(wishlistDAO).removeWishlist("buyer", "P2");
        verify(wishlistDAO, never()).addWishlist("buyer", "P2");
    }

    @Test
    void removeWishlist_requiresAuthentication() {
        assertUnauthorized(controller.removeWishlist("P1"));
        verify(wishlistDAO, never()).removeWishlist(anyString(), anyString());
    }

    @Test
    void removeWishlist_reportsProductWasAlreadyAbsent() {
        authenticate("buyer");
        when(wishlistDAO.removeWishlist("buyer", "P1")).thenReturn(false);
        when(wishlistDAO.getWishlistCount("buyer")).thenReturn(2);

        ResponseEntity<?> response = controller.removeWishlist("P1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, body(response).get("success"));
        assertEquals(Boolean.FALSE, body(response).get("favorite"));
        assertEquals(2, body(response).get("wishlistCount"));
    }

    @Test
    void removeWishlist_reportsDeletedProductAndUpdatedCount() {
        authenticate("buyer");
        when(wishlistDAO.removeWishlist("buyer", "P2")).thenReturn(true);
        when(wishlistDAO.getWishlistCount("buyer")).thenReturn(1);

        ResponseEntity<?> response = controller.removeWishlist("P2");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, body(response).get("success"));
        assertEquals(1, body(response).get("wishlistCount"));
    }

    private void assertUnauthorized(ResponseEntity<?> response) {
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Boolean.FALSE, body(response).get("success"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> body(ResponseEntity<?> response) {
        return (Map<String, Object>) response.getBody();
    }

    private static Stream<Arguments> loginRequiredAuthentications() {
        return Stream.of(
                Arguments.of("missing authentication", null),
                Arguments.of("unauthenticated token",
                        new UsernamePasswordAuthenticationToken("buyer", "n/a")),
                Arguments.of("anonymous principal", authenticated("anonymousUser")));
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(authenticated(username));
    }

    private static Authentication authenticated(String username) {
        return new UsernamePasswordAuthenticationToken(username, "n/a",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
