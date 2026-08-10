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
import java.util.List;
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

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderDetail;
import com.example.demo.entity.OrderReturn;
import com.example.demo.entity.Product;
import com.example.demo.form.OrderReturnForm;
import com.example.demo.model.OrderStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderReturnDAOTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private ProductDAO productDAO;

    private OrderReturnDAO dao;

    @BeforeEach
    void setUp() {
        dao = new OrderReturnDAO();
        ReflectionTestUtils.setField(dao, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(dao, "orderDAO", orderDAO);
        ReflectionTestUtils.setField(dao, "productDAO", productDAO);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void findReturnByOrderId_returnsNullForNullId() {
        assertNull(dao.findReturnByOrderId(null));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void findReturnByOrderId_returnsNullForEmptyRows() {
        returnQuery(Collections.emptyList());

        assertNull(dao.findReturnByOrderId("O1"));
    }

    @Test
    void findReturnByOrderId_returnsFirstRow() {
        OrderReturn first = orderReturn("PENDING");
        returnQuery(java.util.Arrays.asList(first, orderReturn("PENDING")));

        assertSame(first, dao.findReturnByOrderId("O1"));
    }

    @Test
    void cancelOrder_rejectsMissingOrder() {
        when(orderDAO.findOrderForUpdate("O404")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.cancelOrder("alice", "O404"));
    }

    @ParameterizedTest
    @MethodSource("unauthorizedCustomers")
    void cancelOrder_rejectsMissingOrDifferentCustomer(String username) {
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order(OrderStatus.PENDING));

        assertThrows(IllegalStateException.class, () -> dao.cancelOrder(username, "O1"));
        verify(session, never()).update(any());
    }

    static Stream<String> unauthorizedCustomers() {
        return Stream.of(null, "bob");
    }

    @ParameterizedTest
    @MethodSource("nonPendingStatuses")
    void cancelOrder_rejectsEveryNonPendingStatus(String status) {
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order(status));

        assertThrows(IllegalStateException.class, () -> dao.cancelOrder("alice", "O1"));
    }

    static Stream<String> nonPendingStatuses() {
        return Stream.of(OrderStatus.APPROVED, OrderStatus.SHIPPING, OrderStatus.COMPLETED,
                OrderStatus.CANCELLED, OrderStatus.RETURN_PENDING, OrderStatus.RETURNED);
    }

    @Test
    void cancelOrder_acceptsNormalizedPendingAndHandlesNoDetails() {
        Order order = order(" pending ");
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order);
        detailQuery(Collections.emptyList());

        assertTrue(dao.cancelOrder("alice", "O1"));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(session).update(order);
        verify(session).flush();
    }

    @Test
    void cancelOrder_skipsDeletedProductWhileRestoringOtherState() {
        Order order = order(OrderStatus.PENDING);
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order);
        detailQuery(Collections.singletonList(detail("P001", 2)));
        when(productDAO.findProductForUpdate("P001")).thenReturn(null);

        assertTrue(dao.cancelOrder("alice", "O1"));
        verify(session).update(order);
    }

    @ParameterizedTest
    @MethodSource("salesRestoreBoundaries")
    void cancelOrder_restoresStockAndNeverMakesSalesNegative(int sales, int expectedSales) {
        Order order = order(OrderStatus.PENDING);
        Product product = product("P001", 5, sales);
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order);
        detailQuery(Collections.singletonList(detail("P001", 2)));
        when(productDAO.findProductForUpdate("P001")).thenReturn(product);

        dao.cancelOrder("alice", "O1");

        assertEquals(7, product.getStockQuantity());
        assertEquals(expectedSales, product.getSalesCount());
        verify(session).update(product);
    }

    static Stream<Arguments> salesRestoreBoundaries() {
        return Stream.of(Arguments.of(0, 0), Arguments.of(1, 0), Arguments.of(2, 0), Arguments.of(3, 1));
    }

    static Stream<OrderReturnForm> invalidReturnForms() {
        OrderReturnForm nullReason = new OrderReturnForm(null, null);
        OrderReturnForm blankReason = new OrderReturnForm(" ", null);
        OrderReturnForm longReason = new OrderReturnForm(repeat('r', 2001), null);
        OrderReturnForm longImages = new OrderReturnForm("reason", repeat('i', 501));
        return Stream.of(null, nullReason, blankReason, longReason, longImages);
    }

    @ParameterizedTest
    @MethodSource("invalidReturnForms")
    void createReturnRequest_rejectsEachInvalidFormBoundary(OrderReturnForm form) {
        assertThrows(IllegalArgumentException.class,
                () -> dao.createReturnRequest("alice", "O1", form));
        verify(orderDAO, never()).findOrderForUpdate(any());
    }

    @Test
    void createReturnRequest_rejectsMissingOrder() {
        when(orderDAO.findOrderForUpdate("O404")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> dao.createReturnRequest("alice", "O404", validReturnForm()));
    }

    @ParameterizedTest
    @MethodSource("unauthorizedCustomers")
    void createReturnRequest_rejectsMissingOrDifferentOwner(String username) {
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order(OrderStatus.COMPLETED));

        assertThrows(IllegalStateException.class,
                () -> dao.createReturnRequest(username, "O1", validReturnForm()));
    }

    @ParameterizedTest
    @MethodSource("notCompletedStatuses")
    void createReturnRequest_rejectsNonCompletedOrder(String status) {
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order(status));

        assertThrows(IllegalStateException.class,
                () -> dao.createReturnRequest("alice", "O1", validReturnForm()));
    }

    static Stream<String> notCompletedStatuses() {
        return Stream.of(OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.SHIPPING,
                OrderStatus.CANCELLED, OrderStatus.RETURN_PENDING, OrderStatus.RETURNED);
    }

    @Test
    void createReturnRequest_rejectsDuplicateRequest() {
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order(OrderStatus.COMPLETED));
        returnQuery(Collections.singletonList(orderReturn("PENDING")));

        assertThrows(IllegalStateException.class,
                () -> dao.createReturnRequest("alice", "O1", validReturnForm()));
        verify(session, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("optionalImages")
    void createReturnRequest_trimsFieldsPersistsAndTagsOrder(String imageUrls, String expectedImages) {
        Order order = order(" completed ");
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order);
        returnQuery(Collections.emptyList());
        OrderReturnForm form = new OrderReturnForm(" reason ", imageUrls);

        OrderReturn created = dao.createReturnRequest("alice", "O1", form);

        assertEquals("reason", created.getReason());
        assertEquals(expectedImages, created.getImageUrls());
        assertEquals("PENDING", created.getStatus());
        assertEquals(OrderStatus.RETURN_PENDING, order.getStatus());
        verify(session).save(created);
        verify(session).update(order);
        verify(session).flush();
    }

    static Stream<Arguments> optionalImages() {
        return Stream.of(Arguments.of(null, null), Arguments.of(" image1,image2 ", "image1,image2"));
    }

    static Stream<Arguments> invalidStatusUpdates() {
        return Stream.of(Arguments.of(null, null), Arguments.of("", null),
                Arguments.of("UNKNOWN", null), Arguments.of("APPROVE", repeat('n', 256)));
    }

    @ParameterizedTest
    @MethodSource("invalidStatusUpdates")
    void updateReturnStatus_rejectsInvalidActionOrNote(String action, String note) {
        assertThrows(IllegalArgumentException.class,
                () -> dao.updateReturnStatus("seller", "O1", action, note));
        verify(orderDAO, never()).findOrderForUpdate(any());
    }

    @Test
    void updateReturnStatus_rejectsMissingOrder() {
        when(orderDAO.findOrderForUpdate("O404")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> dao.updateReturnStatus("seller", "O404", "APPROVE", null));
    }

    @Test
    void updateReturnStatus_rejectsSellerWithoutWholeOrderOwnership() {
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order(OrderStatus.RETURN_PENDING));
        when(orderDAO.canManageOrder("O1", "seller")).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> dao.updateReturnStatus("seller", "O1", "APPROVE", null));
    }

    @Test
    void updateReturnStatus_rejectsMissingReturnRequest() {
        stubManageableOrder(order(OrderStatus.RETURN_PENDING));
        lockedReturnQuery(Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> dao.updateReturnStatus("seller", "O1", "APPROVE", null));
    }

    @ParameterizedTest
    @MethodSource("processedReturnStatuses")
    void updateReturnStatus_rejectsAlreadyProcessedRequest(String status) {
        stubManageableOrder(order(OrderStatus.RETURN_PENDING));
        lockedReturnQuery(Collections.singletonList(orderReturn(status)));

        assertThrows(IllegalStateException.class,
                () -> dao.updateReturnStatus("seller", "O1", "APPROVE", null));
    }

    static Stream<String> processedReturnStatuses() {
        return Stream.of("APPROVED", "REJECTED", "CANCELLED");
    }

    @Test
    void updateReturnStatus_rejectsOrderOutsideReturnPendingState() {
        stubManageableOrder(order(OrderStatus.COMPLETED));
        lockedReturnQuery(Collections.singletonList(orderReturn("PENDING")));

        assertThrows(IllegalStateException.class,
                () -> dao.updateReturnStatus("seller", "O1", "APPROVE", null));
    }

    @Test
    void updateReturnStatus_approveRestoresStockAndMarksReturned() {
        Order order = order(OrderStatus.RETURN_PENDING);
        OrderReturn request = orderReturn("PENDING");
        Product product = product("P001", 5, 1);
        stubManageableOrder(order);
        lockedReturnQuery(Collections.singletonList(request));
        detailQuery(Collections.singletonList(detail("P001", 2)));
        when(productDAO.findProductForUpdate("P001")).thenReturn(product);

        OrderReturn updated = dao.updateReturnStatus("seller", "O1", " approve ".trim(), " note ");

        assertSame(request, updated);
        assertEquals("APPROVED", request.getStatus());
        assertEquals("note", request.getAdminNote());
        assertEquals(OrderStatus.RETURNED, order.getStatus());
        assertEquals(7, product.getStockQuantity());
        assertEquals(0, product.getSalesCount());
        verify(session).flush();
    }

    @Test
    void updateReturnStatus_approveSkipsDeletedProduct() {
        Order order = order(OrderStatus.RETURN_PENDING);
        stubManageableOrder(order);
        lockedReturnQuery(Collections.singletonList(orderReturn("PENDING")));
        detailQuery(Collections.singletonList(detail("P001", 2)));
        when(productDAO.findProductForUpdate("P001")).thenReturn(null);

        dao.updateReturnStatus("seller", "O1", "APPROVE", null);

        assertEquals(OrderStatus.RETURNED, order.getStatus());
    }

    @Test
    void updateReturnStatus_rejectReturnsOrderToCompletedWithoutStockMutation() {
        Order order = order(OrderStatus.RETURN_PENDING);
        OrderReturn request = orderReturn("PENDING");
        stubManageableOrder(order);
        lockedReturnQuery(Collections.singletonList(request));

        dao.updateReturnStatus("seller", "O1", "reject", " no evidence ");

        assertEquals("REJECTED", request.getStatus());
        assertEquals("no evidence", request.getAdminNote());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(productDAO, never()).findProductForUpdate(any());
        verify(session).flush();
    }

    @SuppressWarnings("unchecked")
    private Query<OrderReturn> returnQuery(List<OrderReturn> rows) {
        Query<OrderReturn> query = mock(Query.class);
        when(session.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(OrderReturn.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(rows);
        return query;
    }

    private Query<OrderReturn> lockedReturnQuery(List<OrderReturn> rows) {
        Query<OrderReturn> query = returnQuery(rows);
        when(query.setLockMode(LockModeType.PESSIMISTIC_WRITE)).thenReturn(query);
        return query;
    }

    @SuppressWarnings("unchecked")
    private void detailQuery(List<OrderDetail> rows) {
        Query<OrderDetail> query = mock(Query.class);
        when(session.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(OrderDetail.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(rows);
    }

    private void stubManageableOrder(Order order) {
        when(orderDAO.findOrderForUpdate("O1")).thenReturn(order);
        when(orderDAO.canManageOrder("O1", "seller")).thenReturn(true);
    }

    private static OrderReturnForm validReturnForm() {
        return new OrderReturnForm("reason", null);
    }

    private static Order order(String status) {
        Order order = new Order();
        order.setId("O1");
        order.setCustomerUsername("alice");
        order.setStatus(status);
        return order;
    }

    private static OrderReturn orderReturn(String status) {
        OrderReturn request = new OrderReturn();
        request.setOrderId("O1");
        request.setStatus(status);
        return request;
    }

    private static OrderDetail detail(String code, int quantity) {
        OrderDetail detail = new OrderDetail();
        Product product = new Product();
        product.setCode(code);
        detail.setProduct(product);
        detail.setQuanity(quantity);
        return detail;
    }

    private static Product product(String code, int stock, int sales) {
        Product product = new Product();
        product.setCode(code);
        product.setStockQuantity(stock);
        product.setSalesCount(sales);
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
