package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.entity.Product;
import com.example.demo.entity.Wishlist;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WishlistDAOTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private ProductDAO productDAO;

    private WishlistDAO dao;

    @BeforeEach
    void setUp() {
        dao = new WishlistDAO();
        ReflectionTestUtils.setField(dao, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(dao, "productDAO", productDAO);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    static java.util.stream.Stream<Arguments> missingWishlistKeys() {
        return java.util.stream.Stream.of(
                Arguments.of(null, "P001"),
                Arguments.of("alice", null));
    }

    @ParameterizedTest
    @MethodSource("missingWishlistKeys")
    void findWishlist_rejectsNullKeys(String username, String code) {
        assertNull(dao.findWishlist(username, code));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void findWishlist_returnsNullForEmptyResult() {
        Query<Wishlist> query = wishlistQuery(Collections.emptyList());

        assertNull(dao.findWishlist("alice", "P001"));
        verify(query).setParameter("username", "alice");
        verify(query).setParameter("code", "P001");
    }

    @Test
    void findWishlist_returnsNullWhenProviderReturnsNullList() {
        wishlistQuery(null);

        assertNull(dao.findWishlist("alice", "P001"));
    }

    @Test
    void findWishlist_returnsFirstMatchingRow() {
        Wishlist first = new Wishlist("alice", "P001");
        Wishlist second = new Wishlist("alice", "P001");
        wishlistQuery(Arrays.asList(first, second));

        assertSame(first, dao.findWishlist("alice", "P001"));
    }

    @Test
    void isFavorite_reflectsWishlistPresence() {
        wishlistQuery(Collections.singletonList(new Wishlist("alice", "P001")));

        assertTrue(dao.isFavorite("alice", "P001"));
    }

    @Test
    void isFavorite_returnsFalseWhenWishlistIsMissing() {
        wishlistQuery(Collections.emptyList());

        assertFalse(dao.isFavorite("alice", "P001"));
    }

    @ParameterizedTest
    @MethodSource("missingWishlistKeys")
    void addWishlist_rejectsNullKeys(String username, String code) {
        assertFalse(dao.addWishlist(username, code));
        verify(productDAO, never()).findProduct(any());
    }

    @Test
    void addWishlist_rejectsUnknownProduct() {
        when(productDAO.findProduct("P404")).thenReturn(null);

        assertFalse(dao.addWishlist("alice", "P404"));
        verify(session, never()).save(any());
    }

    @Test
    void addWishlist_isIdempotentForExistingItem() {
        when(productDAO.findProduct("P001")).thenReturn(new Product());
        wishlistQuery(Collections.singletonList(new Wishlist("alice", "P001")));

        assertTrue(dao.addWishlist("alice", "P001"));
        verify(session, never()).save(any());
    }

    @Test
    void addWishlist_persistsNewItem() {
        when(productDAO.findProduct("P001")).thenReturn(new Product());
        wishlistQuery(Collections.emptyList());

        assertTrue(dao.addWishlist("alice", "P001"));
        verify(session).save(any(Wishlist.class));
    }

    @Test
    void addWishlist_currentlyAcceptsInactiveProduct_characterization() {
        Product inactive = new Product();
        inactive.setStatus("INACTIVE");
        when(productDAO.findProduct("P001")).thenReturn(inactive);
        wishlistQuery(Collections.emptyList());

        assertTrue(dao.addWishlist("alice", "P001"));
        verify(session).save(any(Wishlist.class));
    }

    @Test
    void addWishlist_propagatesPersistenceFailure() {
        when(productDAO.findProduct("P001")).thenReturn(new Product());
        wishlistQuery(Collections.emptyList());
        when(session.save(any(Wishlist.class))).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> dao.addWishlist("alice", "P001"));
    }

    @Test
    void removeWishlist_returnsFalseWhenMissing() {
        wishlistQuery(Collections.emptyList());

        assertFalse(dao.removeWishlist("alice", "P001"));
        verify(session, never()).delete(any());
    }

    @Test
    void removeWishlist_deletesExistingItem() {
        Wishlist item = new Wishlist("alice", "P001");
        wishlistQuery(Collections.singletonList(item));

        assertTrue(dao.removeWishlist("alice", "P001"));
        verify(session).delete(item);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void getUserWishlistProducts_returnsEmptyForMissingUsername(String username) {
        assertTrue(dao.getUserWishlistProducts(username).isEmpty());
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void getUserWishlistProducts_handlesNullProviderResult() {
        productListQuery(null);

        assertTrue(dao.getUserWishlistProducts("alice").isEmpty());
    }

    @Test
    void getUserWishlistProducts_mapsAllProductsIncludingInactive_characterization() {
        Product active = product("P001", true);
        Product inactive = product("P002", false);
        productListQuery(Arrays.asList(active, inactive));

        assertEquals(2, dao.getUserWishlistProducts("alice").size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void getWishlistCount_returnsZeroForMissingUsername(String username) {
        assertEquals(0, dao.getWishlistCount(username));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void getWishlistCount_convertsNullAggregateToZero() {
        countQuery(null);

        assertEquals(0, dao.getWishlistCount("alice"));
    }

    @Test
    void getWishlistCount_convertsLongAggregateToInt() {
        countQuery(4L);

        assertEquals(4, dao.getWishlistCount("alice"));
    }

    @SuppressWarnings("unchecked")
    private Query<Wishlist> wishlistQuery(java.util.List<Wishlist> result) {
        Query<Wishlist> query = mock(Query.class);
        when(session.createQuery(anyString(), any(Class.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(result);
        return query;
    }

    @SuppressWarnings("unchecked")
    private void productListQuery(java.util.List<Product> result) {
        Query<Product> query = mock(Query.class);
        when(session.createQuery(anyString(), any(Class.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(result);
    }

    @SuppressWarnings("unchecked")
    private void countQuery(Long result) {
        Query<Long> query = mock(Query.class);
        when(session.createQuery(anyString(), any(Class.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.uniqueResult()).thenReturn(result);
    }

    private Product product(String code, boolean active) {
        Product product = new Product();
        product.setCode(code);
        product.setName(code);
        product.setStatus(active ? "ACTIVE" : "INACTIVE");
        return product;
    }
}
