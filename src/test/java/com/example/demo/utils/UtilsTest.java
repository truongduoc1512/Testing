package com.example.demo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.example.demo.model.CartInfo;

class UtilsTest {

    @Test
    void constructor_isInstantiable() {
        assertNotNull(new Utils());
    }

    @Test
    void getCartInSession_createsAndReusesSessionCart() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        CartInfo created = Utils.getCartInSession(request);
        assertNotNull(created);
        assertSame(created, request.getSession().getAttribute("myCart"));
        assertSame(created, Utils.getCartInSession(request));
    }

    @Test
    void removeCartInSession_removesExistingSessionCart() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Utils.getCartInSession(request);

        Utils.removeCartInSession(request);
        assertNull(request.getSession().getAttribute("myCart"));
    }

    @Test
    void lastOrderedCart_roundTripsThroughSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CartInfo ordered = new CartInfo();
        ordered.setOrderNum(7);
        Utils.storeLastOrderedCartInSession(request, ordered);
        assertSame(ordered, Utils.getLastOrderedCartInSession(request));
        assertEquals(7, Utils.getLastOrderedCartInSession(request).getOrderNum());
    }
}
