package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.entity.Product;
import com.example.demo.entity.Wishlist;

import java.util.Collections;

class WishlistDAOTest {

    @Test
    void addWishlist_doesNotReportSuccessWhenPersistenceFails() {
        WishlistDAO wishlistDAO = new WishlistDAO();
        SessionFactory sessionFactory = mock(SessionFactory.class);
        Session session = mock(Session.class);
        ProductDAO productDAO = mock(ProductDAO.class);
        @SuppressWarnings("unchecked")
        Query<Wishlist> query = mock(Query.class);

        ReflectionTestUtils.setField(wishlistDAO, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(wishlistDAO, "productDAO", productDAO);

        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(productDAO.findProduct("P001")).thenReturn(new Product());
        when(session.createQuery(any(String.class), any(Class.class))).thenReturn(query);
        when(query.setParameter(any(String.class), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
        when(session.save(any(Wishlist.class))).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class,
                () -> wishlistDAO.addWishlist("alice", "P001"));
    }
}
