package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dao.OrderDAO;
import com.example.demo.dao.OrderReturnDAO;
import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderReturn;
import com.example.demo.entity.Product;
import com.example.demo.form.OrderReturnForm;
import com.example.demo.model.CartInfo;
import com.example.demo.model.CustomerInfo;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.ProductInfo;

@SpringBootTest
@Transactional
class OrderCancelReturnTests {

    private static final String CUSTOMER_USERNAME = "customer88";
    private static final String PRODUCT_CODE = "S001";
    private static final String SELLER_USERNAME = "manager1";

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private OrderReturnDAO orderReturnDAO;

    @Autowired
    private ProductDAO productDAO;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cancelOrder_restoresInventoryAndMarksOrderCancelled() {
        Product product = productDAO.findProduct(PRODUCT_CODE);
        assertNotNull(product);

        int initialStock = product.getStockQuantity();
        String orderId = createOrderForCustomer(2);

        Product productAfterOrder = productDAO.findProduct(PRODUCT_CODE);
        assertEquals(initialStock - 2, productAfterOrder.getStockQuantity());

        boolean cancelled = orderReturnDAO.cancelOrder(CUSTOMER_USERNAME, orderId);

        assertTrue(cancelled);
        Order cancelledOrder = orderDAO.findOrder(orderId);
        assertEquals("CANCELLED", cancelledOrder.getStatus());
        assertEquals(initialStock, productDAO.findProduct(PRODUCT_CODE).getStockQuantity());
    }

    @Test
    void createReturnRequest_rejectsDuplicateForSameOrder() {
        String orderId = createOrderForCustomer(1);
        orderDAO.updateOrderStatus(orderId, "COMPLETED");
        OrderReturnForm form = new OrderReturnForm("Lỗi sản xuất", null);

        OrderReturn firstRequest = orderReturnDAO.createReturnRequest(
                CUSTOMER_USERNAME, orderId, form);

        assertNotNull(firstRequest);
        assertEquals("PENDING", firstRequest.getStatus());
        assertThrows(IllegalStateException.class,
                () -> orderReturnDAO.createReturnRequest(CUSTOMER_USERNAME, orderId, form));
    }

    @Test
    void approveReturn_restoresInventoryAndMarksOrderReturned() {
        String orderId = createOrderForCustomer(1);
        int stockAfterOrder = productDAO.findProduct(PRODUCT_CODE).getStockQuantity();
        orderDAO.updateOrderStatus(orderId, "COMPLETED");
        OrderReturnForm form = new OrderReturnForm("Giày bị chật size", null);

        OrderReturn request = orderReturnDAO.createReturnRequest(CUSTOMER_USERNAME, orderId, form);
        assertNotNull(request);

        OrderReturn approvedRequest = orderReturnDAO.updateReturnStatus(
                SELLER_USERNAME, orderId, "APPROVE", "Đã duyệt nhận lại hàng");
        assertEquals("APPROVED", approvedRequest.getStatus());

        Order returnedOrder = orderDAO.findOrder(orderId);
        assertEquals("RETURNED", returnedOrder.getStatus());
        assertEquals(stockAfterOrder + 1, productDAO.findProduct(PRODUCT_CODE).getStockQuantity());
    }

    private String createOrderForCustomer(int quantity) {
        Product product = productDAO.findProduct(PRODUCT_CODE);
        assertNotNull(product);

        authenticateCustomer();

        CartInfo cart = new CartInfo();
        cart.setCustomerInfo(validCustomer());
        cart.addProduct(new ProductInfo(product), quantity);

        orderDAO.saveOrder(cart);

        List<OrderInfo> customerOrders = orderDAO.listOrderInfo(
                1, 1, 5, CUSTOMER_USERNAME, "ROLE_USER").getList();
        assertFalse(customerOrders.isEmpty());
        return customerOrders.get(0).getId();
    }

    private void authenticateCustomer() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        CUSTOMER_USERNAME,
                        "n/a",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    private CustomerInfo validCustomer() {
        CustomerInfo customer = new CustomerInfo();
        customer.setName("Test User");
        customer.setEmail("test@shoeshop.com");
        customer.setPhone("0912345678");
        customer.setAddress("123 Test Street");
        customer.setValid(true);
        return customer;
    }
}
