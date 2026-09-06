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

import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;

import javax.persistence.LockModeType;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.entity.Product;
import com.example.demo.entity.ProductReview;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductReviewDAOTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    private ProductReviewDAO dao;

    @BeforeEach
    void setUp() {
        dao = new ProductReviewDAO();
        ReflectionTestUtils.setField(dao, "sessionFactory", sessionFactory);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void findReview_returnsNullForNullId() {
        assertNull(dao.findReview(null));
        verify(session, never()).find(any(Class.class), any());
    }

    @Test
    void findReview_delegatesLookup() {
        ProductReview review = validReview();
        when(session.find(ProductReview.class, 7L)).thenReturn(review);

        assertSame(review, dao.findReview(7L));
    }

    static Stream<ProductReview> invalidReviews() {
        ProductReview nullCode = validReview();
        nullCode.setProductCode(null);
        ProductReview blankCode = validReview();
        blankCode.setProductCode("   ");
        ProductReview longCode = validReview();
        longCode.setProductCode(repeat('P', 21));
        ProductReview nullUser = validReview();
        nullUser.setUsername(null);
        ProductReview blankUser = validReview();
        blankUser.setUsername(" ");
        ProductReview lowRating = validReview();
        lowRating.setRatingValue(0);
        ProductReview highRating = validReview();
        highRating.setRatingValue(6);
        ProductReview nullComment = validReview();
        nullComment.setComment(null);
        ProductReview blankComment = validReview();
        blankComment.setComment(" ");
        ProductReview longComment = validReview();
        longComment.setComment(repeat('x', 2001));
        return Stream.of(null, nullCode, blankCode, longCode, nullUser, blankUser,
                lowRating, highRating, nullComment, blankComment, longComment);
    }

    @ParameterizedTest
    @MethodSource("invalidReviews")
    void saveReview_rejectsEachInvalidField(ProductReview review) {
        assertThrows(IllegalArgumentException.class, () -> dao.saveReview(review));
        verify(session, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("unavailableProducts")
    void saveReview_rejectsMissingOrNonActiveProduct(Product product) {
        ProductReview review = validReview();
        when(session.find(Product.class, "P001")).thenReturn(product);

        assertThrows(IllegalArgumentException.class, () -> dao.saveReview(review));
        verify(session, never()).save(any());
    }

    static Stream<Product> unavailableProducts() {
        Product inactive = product("INACTIVE");
        Product draft = product("DRAFT");
        return Stream.of(null, inactive, draft);
    }

    @Test
    void saveReview_acceptsCaseInsensitiveActiveStatusAndRecalculatesRoundedCache() {
        ProductReview review = validReview();
        Product product = product("active");
        stubReviewAggregation(product, new Object[] { 3L, 4.26 });

        dao.saveReview(review);

        verify(session).save(review);
        assertEquals(3, product.getReviewCount());
        assertEquals(4.3, product.getRating(), 0.0001);
        verify(session).update(product);
    }

    @Test
    void saveReview_skipsCacheQueryWhenLockedProductDisappears() {
        ProductReview review = validReview();
        when(session.find(Product.class, "P001")).thenReturn(product("ACTIVE"));
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);

        dao.saveReview(review);

        verify(session, never()).createQuery(anyString());
    }

    @Test
    void saveReview_leavesCacheUntouchedWhenAggregateIsNull() {
        Product product = product("ACTIVE");
        product.setReviewCount(8);
        product.setRating(2.5);
        stubReviewAggregation(product, null);

        dao.saveReview(validReview());

        assertEquals(8, product.getReviewCount());
        assertEquals(2.5, product.getRating());
        verify(session, never()).update(product);
    }

    @ParameterizedTest
    @MethodSource("emptyAggregates")
    void saveReview_resetsCacheForEmptyOrIncompleteAggregate(Object[] aggregate) {
        Product product = product("ACTIVE");
        stubReviewAggregation(product, aggregate);

        dao.saveReview(validReview());

        assertEquals(0, product.getReviewCount());
        assertEquals(5.0, product.getRating());
        verify(session).update(product);
    }

    static Stream<Arguments> emptyAggregates() {
        return Stream.of(
                Arguments.of((Object) new Object[] { null, 4.0 }),
                Arguments.of((Object) new Object[] { 0L, 4.0 }),
                Arguments.of((Object) new Object[] { 1L, null }));
    }

    static Stream<Arguments> invalidUpdates() {
        return Stream.of(
                Arguments.of(null, 3, "ok"),
                Arguments.of("alice", 0, "ok"),
                Arguments.of("alice", 6, "ok"),
                Arguments.of("alice", 3, null),
                Arguments.of("alice", 3, " "),
                Arguments.of("alice", 3, repeat('x', 2001)));
    }

    @ParameterizedTest
    @MethodSource("invalidUpdates")
    void updateReview_rejectsInvalidInput(String username, int rating, String comment) {
        assertFalse(dao.updateReview(1L, username, rating, comment));
        verify(session, never()).find(any(Class.class), any());
    }

    @Test
    void updateReview_returnsFalseWhenReviewMissing() {
        when(session.find(ProductReview.class, 1L)).thenReturn(null);

        assertFalse(dao.updateReview(1L, "alice", 3, "ok"));
    }

    @Test
    void updateReview_returnsFalseForDifferentOwner() {
        ProductReview review = validReview();
        when(session.find(ProductReview.class, 1L)).thenReturn(review);

        assertFalse(dao.updateReview(1L, "bob", 3, "ok"));
        verify(session, never()).update(review);
    }

    @Test
    void updateReview_returnsFalseOutsideFiveMinuteWindow() {
        ProductReview review = validReview();
        review.setCreatedAt(new Date(System.currentTimeMillis() - 301_000));
        when(session.find(ProductReview.class, 1L)).thenReturn(review);

        assertFalse(dao.updateReview(1L, "ALICE", 3, "ok"));
        verify(session, never()).update(review);
    }

    @Test
    void updateReview_updatesWithinWindowAndRefreshesCache() {
        ProductReview review = validReview();
        review.setCreatedAt(new Date(System.currentTimeMillis() - 299_000));
        Product product = product("ACTIVE");
        when(session.find(ProductReview.class, 1L)).thenReturn(review);
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(product);
        aggregateQuery(new Object[] { 1L, 5.0 });

        assertTrue(dao.updateReview(1L, "ALICE", 5, " updated "));
        assertEquals(5, review.getRatingValue());
        assertEquals(" updated ", review.getComment());
        verify(session).update(review);
    }

    @Test
    void deleteReview_returnsFalseWhenMissing() {
        when(session.find(ProductReview.class, 1L)).thenReturn(null);

        assertFalse(dao.deleteReview(1L, "alice"));
        verify(session, never()).delete(any());
    }

    @Test
    void deleteReview_returnsFalseForDifferentOwner() {
        when(session.find(ProductReview.class, 1L)).thenReturn(validReview());

        assertFalse(dao.deleteReview(1L, "bob"));
        verify(session, never()).delete(any());
    }

    @Test
    void deleteReview_deletesAndRefreshesProductCache() {
        ProductReview review = validReview();
        Product product = product("ACTIVE");
        when(session.find(ProductReview.class, 1L)).thenReturn(review);
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(product);
        aggregateQuery(new Object[] { 0L, null });

        assertTrue(dao.deleteReview(1L, "ALICE"));
        verify(session).delete(review);
        assertEquals(0, product.getReviewCount());
        assertEquals(5.0, product.getRating());
    }

    @Test
    void getReviewsByProductCode_bindsCodeAndReturnsRows() {
        @SuppressWarnings("unchecked")
        Query<ProductReview> query = mock(Query.class);
        when(session.createQuery(anyString(), any(Class.class))).thenReturn(query);
        when(query.setParameter("code", "P001")).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(validReview()));

        assertEquals(1, dao.getReviewsByProductCode("P001").size());
        verify(query).setParameter("code", "P001");
    }

    private void stubReviewAggregation(Product product, Object[] aggregate) {
        when(session.find(Product.class, "P001")).thenReturn(product);
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(product);
        aggregateQuery(aggregate);
    }

    @SuppressWarnings("unchecked")
    private void aggregateQuery(Object[] aggregate) {
        Query<Object> query = mock(Query.class);
        when(session.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.uniqueResult()).thenReturn(aggregate);
    }

    private static ProductReview validReview() {
        return new ProductReview("P001", "alice", 4, "good");
    }

    private static Product product(String status) {
        Product product = new Product();
        product.setCode("P001");
        product.setStatus(status);
        return product;
    }

    private static String repeat(char value, int count) {
        StringBuilder text = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            text.append(value);
        }
        return text.toString();
    }
}
