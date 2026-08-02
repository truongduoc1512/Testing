package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dao.ProductDAO;

class CartApiControllerTest {

    @Test
    void removeCartItem_returnsNotFoundForUnknownProduct() {
        CartApiController controller = new CartApiController();
        ProductDAO productDAO = mock(ProductDAO.class);
        ReflectionTestUtils.setField(controller, "productDAO", productDAO);
        when(productDAO.findProduct("missing")).thenReturn(null);

        ResponseEntity<?> response = controller.removeCartItem(new MockHttpServletRequest(), "missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
