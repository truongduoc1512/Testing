package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.LockModeType;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderDetail;
import com.example.demo.entity.Product;
import com.example.demo.model.CartInfo;
import com.example.demo.model.CartLineInfo;
import com.example.demo.model.CustomerInfo;
import com.example.demo.model.OrderDetailInfo;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.OrderStatus;
import com.example.demo.model.ProductInfo;
import com.example.demo.model.VoucherApplyResult;
import com.example.demo.pagination.PaginationResult;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderDAOTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private VoucherDAO voucherDAO;

    private OrderDAO dao;

    @BeforeEach
    void setUp() {
        dao = new OrderDAO();
        ReflectionTestUtils.setField(dao, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(dao, "productDAO", productDAO);
        ReflectionTestUtils.setField(dao, "voucherDAO", voucherDAO);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @AfterEach
    void tearDown() {
        DaoTestSupport.clearAuthentication();
    }

    @Test
    void saveOrder_rejectsNullCart() {
        assertThrows(IllegalArgumentException.class, () -> dao.saveOrder(null));
        verify(session, never()).persist(any());
    }

    @Test
    void saveOrder_rejectsEmptyCart() {
        CartInfo cart = new CartInfo();
        cart.setCustomerInfo(validCustomer());

        assertThrows(IllegalArgumentException.class, () -> dao.saveOrder(cart));
    }

    @Test
    void saveOrder_rejectsInvalidCustomer() {
        CartInfo cart = validCart("P001", 1);
        cart.getCustomerInfo().setValid(false);

        assertThrows(IllegalArgumentException.class, () -> dao.saveOrder(cart));
        verify(productDAO, never()).findProductForUpdate(any());
    }

    static Stream<CartLineInfo> structurallyInvalidLines() {
        CartLineInfo missingProduct = new CartLineInfo();
        missingProduct.setQuantity(1);
        CartLineInfo missingCode = new CartLineInfo();
        missingCode.setProductInfo(new ProductInfo());
        missingCode.setQuantity(1);
        return Stream.of(null, missingProduct, missingCode);
    }

    @ParameterizedTest
    @MethodSource("structurallyInvalidLines")
    void saveOrder_rejectsMissingLineStructure(CartLineInfo line) {
        CartInfo cart = cartWithLine(line);

        assertThrows(IllegalArgumentException.class, () -> dao.saveOrder(cart));
        verify(productDAO, never()).findProductForUpdate(any());
    }

    @Test
    void saveOrder_rejectsQuantityBelowOne() {
        CartInfo cart = validCart("P001", 0);

        assertThrows(IllegalArgumentException.class, () -> dao.saveOrder(cart));
        verify(productDAO, never()).findProductForUpdate(any());
    }

    @ParameterizedTest
    @MethodSource("unavailableProducts")
    void saveOrder_rejectsMissingOrInactiveProduct(Product product) {
        CartInfo cart = validCart("P001", 1);
        when(productDAO.findProductForUpdate("P001")).thenReturn(product);

        assertThrows(IllegalStateException.class, () -> dao.saveOrder(cart));
        verify(session, never()).persist(any());
    }

    static Stream<Product> unavailableProducts() {
        return Stream.of(null, product("P001", "INACTIVE", 10), product("P001", "DRAFT", 10));
    }

    @ParameterizedTest
    @MethodSource("stockBoundaries")
    void saveOrder_enforcesStockBoundary(int stock, int quantity, boolean succeeds) {
        CartInfo cart = validCart("P001", quantity);
        Product product = product("P001", "ACTIVE", stock);
        when(productDAO.findProductForUpdate("P001")).thenReturn(product, product);
        nativeMaxQuery(0);

        if (succeeds) {
            dao.saveOrder(cart);
            assertEquals(stock - quantity, product.getStockQuantity());
        } else {
            assertThrows(IllegalStateException.class, () -> dao.saveOrder(cart));
            verify(session, never()).persist(any());
        }
    }

    static Stream<Arguments> stockBoundaries() {
        return Stream.of(Arguments.of(1, 2, false), Arguments.of(2, 2, true), Arguments.of(3, 2, true));
    }

    @Test
    void saveOrder_refreshesServerPriceCreatesGuestOrderAndMutatesInventory() {
        CartInfo cart = validCart("P001", 2);
        cart.getCartLines().get(0).getProductInfo().setPrice(1);
        Product product = product("P001", "ACTIVE", 5);
        product.setName("Server name");
        product.setPrice(200);
        product.setDiscountPercent(10);
        product.setSalesCount(7);
        when(productDAO.findProductForUpdate("P001")).thenReturn(product, product);
        nativeMaxQuery(9);

        dao.saveOrder(cart);

        assertEquals("Server name", cart.getCartLines().get(0).getProductInfo().getName());
        assertEquals(180, cart.getCartLines().get(0).getProductInfo().getPrice(), 0.0001);
        assertEquals(3, product.getStockQuantity());
        assertEquals(9, product.getSalesCount());
        assertEquals(10, cart.getOrderNum());
        assertEquals(0, cart.getDiscountAmount());
        Order persistedOrder = capturePersistedOrderAndDetail();
        assertNull(persistedOrder.getCustomerUsername());
        assertEquals(OrderStatus.PENDING, persistedOrder.getStatus());
        verify(session).flush();
    }

    @Test
    void saveOrder_recordsAuthenticatedCustomer() {
        DaoTestSupport.authenticate("alice", "ROLE_USER");
        CartInfo cart = validCart("P001", 1);
        Product product = product("P001", "ACTIVE", 5);
        when(productDAO.findProductForUpdate("P001")).thenReturn(product, product);
        nativeMaxQuery(0);

        dao.saveOrder(cart);

        Order persistedOrder = capturePersistedOrderAndDetail();
        assertEquals("alice", persistedOrder.getCustomerUsername());
    }

    @Test
    void saveOrder_appliesNormalizedVoucherAndRecordsUsage() {
        DaoTestSupport.authenticate("alice", "ROLE_USER");
        CartInfo cart = validCart("P001", 1);
        cart.setVoucherCode(" sale10 ");
        Product product = product("P001", "ACTIVE", 5);
        when(productDAO.findProductForUpdate("P001")).thenReturn(product, product);
        VoucherApplyResult result = new VoucherApplyResult(true, "ok");
        result.setVoucherCode("SALE10");
        result.setDiscountAmount(12);
        when(voucherDAO.validateAndApplyVoucherForCheckout(" sale10 ", 100, "alice")).thenReturn(result);
        nativeMaxQuery(0);

        dao.saveOrder(cart);

        assertEquals("SALE10", cart.getVoucherCode());
        assertEquals(12, cart.getDiscountAmount());
        verify(voucherDAO).recordVoucherUsage(org.mockito.ArgumentMatchers.eq("SALE10"),
                org.mockito.ArgumentMatchers.eq("alice"), anyString());
    }

    @Test
    void saveOrder_rejectsInvalidVoucherBeforeCreatingOrder() {
        CartInfo cart = validCart("P001", 1);
        cart.setVoucherCode("BAD");
        Product product = product("P001", "ACTIVE", 5);
        when(productDAO.findProductForUpdate("P001")).thenReturn(product);
        when(voucherDAO.validateAndApplyVoucherForCheckout("BAD", 100, null))
                .thenReturn(new VoucherApplyResult(false, "invalid voucher"));

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> dao.saveOrder(cart));

        assertEquals("invalid voucher", error.getMessage());
        verify(session, never()).persist(any());
    }

    @Test
    void saveOrder_locksLinesInStableProductCodeOrder() {
        CartInfo cart = validCart("P002", 1);
        cart.getCartLines().add(validLine("P001", 1));
        Product p1 = product("P001", "ACTIVE", 5);
        Product p2 = product("P002", "ACTIVE", 5);
        when(productDAO.findProductForUpdate("P001")).thenReturn(p1, p1);
        when(productDAO.findProductForUpdate("P002")).thenReturn(p2, p2);
        nativeMaxQuery(0);

        dao.saveOrder(cart);

        InOrder ordered = inOrder(productDAO);
        ordered.verify(productDAO).findProductForUpdate("P001");
        ordered.verify(productDAO).findProductForUpdate("P002");
        ordered.verify(productDAO).findProductForUpdate("P001");
        ordered.verify(productDAO).findProductForUpdate("P002");
    }

    @Test
    void saveOrder_treatsNullMaxOrderNumberAsZero() {
        CartInfo cart = validCart("P001", 1);
        Product product = product("P001", "ACTIVE", 5);
        when(productDAO.findProductForUpdate("P001")).thenReturn(product, product);
        nativeMaxQuery(null);

        dao.saveOrder(cart);

        assertEquals(1, cart.getOrderNum());
    }

    @ParameterizedTest
    @MethodSource("listScopes")
    void listOrderInfo_buildsExpectedScope(String username, String role, String expectedFragment,
            boolean bindsUsername) {
        Query<OrderInfo> query = emptyOrderInfoQuery();

        dao.listOrderInfo(1, 10, 5, username, role);

        assertOrderInfoQueryContains(expectedFragment);
        if (bindsUsername) {
            verify(query).setParameter("username", username);
        } else {
            verify(query, never()).setParameter(org.mockito.ArgumentMatchers.eq("username"), any());
        }
    }

    static Stream<Arguments> listScopes() {
        return Stream.of(
                Arguments.of("alice", "ROLE_USER", "ord.customerUsername = :username", true),
                Arguments.of("alice", "ROLE_CUSTOMER", "ord.customerUsername = :username", true),
                Arguments.of("seller", "ROLE_ADMIN", "od.product.ownerUsername = :username", true),
                Arguments.of("seller", "ROLE_MANAGER", "od.product.ownerUsername = :username", true),
                Arguments.of("alice", "ROLE_UNKNOWN", "from com.example.demo.entity.Order ord", false),
                Arguments.of(" ", "ROLE_USER", "from com.example.demo.entity.Order ord", false));
    }

    @Test
    void listOrderInfo_noPrincipalOverloadReturnsUnscopedRows_characterization() {
        emptyOrderInfoQuery();

        PaginationResult<OrderInfo> result = dao.listOrderInfo(1, 10, 5);

        assertEquals(0, result.getTotalRecords());
    }

    @Test
    void findOrder_delegatesLookup() {
        Order order = order("O1", OrderStatus.PENDING);
        when(session.find(Order.class, "O1")).thenReturn(order);

        assertSame(order, dao.findOrder("O1"));
    }

    @Test
    void findOrderForUpdate_usesPessimisticLock() {
        Order order = order("O1", OrderStatus.PENDING);
        when(session.find(Order.class, "O1", LockModeType.PESSIMISTIC_WRITE)).thenReturn(order);

        assertSame(order, dao.findOrderForUpdate("O1"));
    }

    static Stream<Arguments> invalidAccessKeys() {
        return Stream.of(Arguments.of(null, "alice"), Arguments.of("O1", null), Arguments.of("O1", " "));
    }

    @ParameterizedTest
    @MethodSource("invalidAccessKeys")
    void canAccessOrder_rejectsMissingKeys(String orderId, String username) {
        assertFalse(dao.canAccessOrder(orderId, username, "ROLE_USER"));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @ParameterizedTest
    @MethodSource("accessRoleResults")
    void canAccessOrder_usesRoleSpecificOwnershipQuery(String role, long count, boolean expected) {
        Query<Long> query = countQuery(count);

        assertEquals(expected, dao.canAccessOrder("O1", "alice", role));
        verify(query).setParameter("orderId", "O1");
        verify(query).setParameter("username", "alice");
    }

    static Stream<Arguments> accessRoleResults() {
        return Stream.of(
                Arguments.of("ROLE_USER", 0L, false), Arguments.of("ROLE_CUSTOMER", 1L, true),
                Arguments.of("ROLE_ADMIN", 1L, true), Arguments.of("ROLE_MANAGER", 0L, false));
    }

    @Test
    void canAccessOrder_rejectsUnknownRoleWithoutQuery() {
        assertFalse(dao.canAccessOrder("O1", "alice", "ROLE_UNKNOWN"));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @ParameterizedTest
    @MethodSource("invalidAccessKeys")
    void canManageOrder_rejectsMissingKeys(String orderId, String username) {
        assertFalse(dao.canManageOrder(orderId, username));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void canManageOrder_returnsFalseForOrderWithoutLines() {
        countQuery(0L);

        assertFalse(dao.canManageOrder("O1", "seller"));
    }

    @ParameterizedTest
    @MethodSource("managedLineCounts")
    void canManageOrder_requiresSellerToOwnEveryLine(long owned, boolean expected) {
        Query<Long> total = mockLongQuery(2L);
        Query<Long> ownedQuery = mockLongQuery(owned);
        when(session.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(Long.class)))
                .thenReturn(total, ownedQuery);

        assertEquals(expected, dao.canManageOrder("O1", "seller"));
        verify(ownedQuery).setParameter("username", "seller");
    }

    static Stream<Arguments> managedLineCounts() {
        return Stream.of(Arguments.of(0L, false), Arguments.of(1L, false), Arguments.of(2L, true));
    }

    @ParameterizedTest
    @MethodSource("invalidAccessKeys")
    void isOrderCustomer_rejectsMissingKeys(String orderId, String username) {
        assertFalse(dao.isOrderCustomer(orderId, username));
    }

    @ParameterizedTest
    @MethodSource("booleanCounts")
    void isOrderCustomer_mapsCountToBoolean(long count, boolean expected) {
        countQuery(count);

        assertEquals(expected, dao.isOrderCustomer("O1", "alice"));
    }

    static Stream<Arguments> booleanCounts() {
        return Stream.of(Arguments.of(0L, false), Arguments.of(1L, true), Arguments.of(2L, true));
    }

    @Test
    void getOrderInfo_returnsNullWhenOrderMissing() {
        when(session.find(Order.class, "O404")).thenReturn(null);

        assertNull(dao.getOrderInfo("O404"));
    }

    @Test
    void getOrderInfo_mapsOrderFields() {
        Order order = order("O1", OrderStatus.SHIPPING);
        order.setOrderNum(7);
        order.setAmount(123);
        when(session.find(Order.class, "O1")).thenReturn(order);

        OrderInfo info = dao.getOrderInfo("O1");

        assertEquals("O1", info.getId());
        assertEquals(7, info.getOrderNum());
        assertEquals(123, info.getAmount());
        assertEquals(OrderStatus.SHIPPING, info.getStatus());
    }

    @Test
    void listOrderDetailInfos_returnsAllLinesForOrder() {
        Query<OrderDetailInfo> query = detailQuery();

        assertEquals(1, dao.listOrderDetailInfos("O1").size());
        verify(query).setParameter("orderId", "O1");
        verify(query, never()).setParameter(org.mockito.ArgumentMatchers.eq("ownerUsername"), any());
    }

    @Test
    void listOrderDetailInfosForPrincipal_filtersSellerWhenNotCustomer() {
        Query<Long> customerQuery = countQuery(0L);
        Query<OrderDetailInfo> detailQuery = detailQuery();

        dao.listOrderDetailInfosForPrincipal("O1", "seller", "ROLE_MANAGER");

        verify(customerQuery).setParameter("username", "seller");
        verify(detailQuery).setParameter("ownerUsername", "seller");
    }

    @Test
    void listOrderDetailInfosForPrincipal_returnsAllLinesForCustomerSeller() {
        countQuery(1L);
        Query<OrderDetailInfo> detailQuery = detailQuery();

        dao.listOrderDetailInfosForPrincipal("O1", "seller", "ROLE_ADMIN");

        verify(detailQuery, never()).setParameter(org.mockito.ArgumentMatchers.eq("ownerUsername"), any());
    }

    @Test
    void listOrderDetailInfosForPrincipal_nonSellerRoleDoesNotCheckCustomer() {
        Query<OrderDetailInfo> detailQuery = detailQuery();

        dao.listOrderDetailInfosForPrincipal("O1", "alice", "ROLE_USER");

        verify(detailQuery, never()).setParameter(org.mockito.ArgumentMatchers.eq("ownerUsername"), any());
    }

    @Test
    void updateOrderStatus_doesNothingWhenOrderMissing() {
        when(session.find(Order.class, "O404", LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);

        dao.updateOrderStatus("O404", OrderStatus.APPROVED);

        verify(session, never()).flush();
    }

    @ParameterizedTest
    @MethodSource("validStatusAliases")
    void updateOrderStatus_normalizesValidTransition(String requested, String expected) {
        Order order = order("O1", OrderStatus.PENDING);
        when(session.find(Order.class, "O1", LockModeType.PESSIMISTIC_WRITE)).thenReturn(order);

        dao.updateOrderStatus("O1", requested);

        assertEquals(expected, order.getStatus());
        verify(session).flush();
    }

    static Stream<Arguments> validStatusAliases() {
        return Stream.of(Arguments.of(" approved ", OrderStatus.APPROVED),
                Arguments.of("SHIPPED", OrderStatus.SHIPPING),
                Arguments.of("DELIVERED", OrderStatus.COMPLETED));
    }

    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void updateOrderStatus_rejectsInvalidTransition(String current, String requested) {
        Order order = order("O1", current);
        when(session.find(Order.class, "O1", LockModeType.PESSIMISTIC_WRITE)).thenReturn(order);

        assertThrows(IllegalStateException.class, () -> dao.updateOrderStatus("O1", requested));
        verify(session, never()).flush();
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(Arguments.of(OrderStatus.COMPLETED, OrderStatus.PENDING),
                Arguments.of(OrderStatus.CANCELLED, OrderStatus.APPROVED),
                Arguments.of(OrderStatus.PENDING, "BOGUS"),
                Arguments.of(OrderStatus.PENDING, null));
    }

    @ParameterizedTest
    @MethodSource("countScopes")
    void getTotalOrdersCount_scopesAndMapsNullableAggregate(String username, String role, Long aggregate,
            long expected, String expectedHql) {
        Query<Long> query = aggregateQuery(Long.class, aggregate);

        assertEquals(expected, dao.getTotalOrdersCount(username, role));
        verify(session).createQuery(org.mockito.ArgumentMatchers.contains(expectedHql),
                org.mockito.ArgumentMatchers.eq(Long.class));
        if (username != null && !username.trim().isEmpty()
                && (role.equals("ROLE_USER") || role.equals("ROLE_ADMIN"))) {
            verify(query).setParameter("username", username);
        }
    }

    static Stream<Arguments> countScopes() {
        return Stream.of(
                Arguments.of("alice", "ROLE_USER", 2L, 2L, "o.customerUsername = :username"),
                Arguments.of("seller", "ROLE_ADMIN", 3L, 3L, "count(distinct o.id)"),
                Arguments.of(null, null, null, 0L, "Select count(o.id)"),
                Arguments.of("alice", "ROLE_UNKNOWN", 4L, 4L, "Select count(o.id)"));
    }

    @ParameterizedTest
    @MethodSource("revenueScopes")
    void getTotalRevenue_scopesAndMapsNullableAggregate(String username, String role, Double aggregate,
            double expected, String expectedHql) {
        aggregateQuery(Double.class, aggregate);

        assertEquals(expected, dao.getTotalRevenue(username, role), 0.0001);
        verify(session).createQuery(org.mockito.ArgumentMatchers.contains(expectedHql),
                org.mockito.ArgumentMatchers.eq(Double.class));
    }

    static Stream<Arguments> revenueScopes() {
        return Stream.of(
                Arguments.of("alice", "ROLE_USER", 20.0, 20.0, "o.customerUsername = :username"),
                Arguments.of("seller", "ROLE_MANAGER", 30.0, 30.0, "Select sum(od.amount)"),
                Arguments.of(null, null, null, 0.0, "Select sum(o.amount)"),
                Arguments.of("alice", "ROLE_UNKNOWN", 40.0, 40.0, "Select sum(o.amount)"));
    }

    @Test
    void getTotalOrdersCount_withoutScopeReturnsGlobalAggregate() {
        aggregateQuery(Long.class, 5L);

        assertEquals(5L, dao.getTotalOrdersCount());
        verify(session).createQuery(org.mockito.ArgumentMatchers.contains("Select count(o.id)"),
                org.mockito.ArgumentMatchers.eq(Long.class));
    }

    @Test
    void getTotalRevenue_withoutScopeReturnsGlobalAggregate() {
        aggregateQuery(Double.class, 25.5);

        assertEquals(25.5, dao.getTotalRevenue(), 0.0001);
        verify(session).createQuery(org.mockito.ArgumentMatchers.contains("Select sum(o.amount)"),
                org.mockito.ArgumentMatchers.eq(Double.class));
    }

    private Order capturePersistedOrderAndDetail() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(session, times(2)).persist(captor.capture());

        List<Object> persisted = captor.getAllValues();
        assertEquals(1L, persisted.stream().filter(Order.class::isInstance).count());
        assertEquals(1L, persisted.stream().filter(OrderDetail.class::isInstance).count());
        return persisted.stream().filter(Order.class::isInstance).map(Order.class::cast).findFirst().get();
    }

    private void nativeMaxQuery(Number value) {
        NativeQuery<?> query = mock(NativeQuery.class);
        when(session.createNativeQuery(anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(value);
    }

    @SuppressWarnings("unchecked")
    private Query<OrderInfo> emptyOrderInfoQuery() {
        Query<OrderInfo> query = mock(Query.class);
        ScrollableResults scroll = mock(ScrollableResults.class);
        when(session.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(OrderInfo.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.scroll(ScrollMode.SCROLL_INSENSITIVE)).thenReturn(scroll);
        when(scroll.first()).thenReturn(false);
        when(scroll.getRowNumber()).thenReturn(-1);
        return query;
    }

    private void assertOrderInfoQueryContains(String fragment) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(session).createQuery(captor.capture(), org.mockito.ArgumentMatchers.eq(OrderInfo.class));
        assertTrue(captor.getValue().contains(fragment), captor.getValue());
    }

    @SuppressWarnings("unchecked")
    private Query<Long> countQuery(long count) {
        Query<Long> query = mockLongQuery(count);
        when(session.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(Long.class))).thenReturn(query);
        return query;
    }

    @SuppressWarnings("unchecked")
    private Query<Long> mockLongQuery(long count) {
        Query<Long> query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(count);
        return query;
    }

    @SuppressWarnings("unchecked")
    private <T> Query<T> aggregateQuery(Class<T> type, T aggregate) {
        Query<T> query = mock(Query.class);
        when(session.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(type))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(aggregate);
        return query;
    }

    @SuppressWarnings("unchecked")
    private Query<OrderDetailInfo> detailQuery() {
        Query<OrderDetailInfo> query = mock(Query.class);
        when(session.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(OrderDetailInfo.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new OrderDetailInfo()));
        return query;
    }

    private static CartInfo validCart(String code, int quantity) {
        return cartWithLine(validLine(code, quantity));
    }

    private static CartInfo cartWithLine(CartLineInfo line) {
        CartInfo cart = new CartInfo();
        cart.setCustomerInfo(validCustomer());
        cart.getCartLines().add(line);
        return cart;
    }

    private static CartLineInfo validLine(String code, int quantity) {
        ProductInfo info = new ProductInfo();
        info.setCode(code);
        info.setName(code);
        info.setPrice(100);
        info.setStockQuantity(100);
        CartLineInfo line = new CartLineInfo();
        line.setProductInfo(info);
        line.setQuantity(quantity);
        return line;
    }

    private static CustomerInfo validCustomer() {
        CustomerInfo customer = new CustomerInfo();
        customer.setName("Alice");
        customer.setEmail("alice@example.com");
        customer.setPhone("0900");
        customer.setAddress("Address");
        customer.setValid(true);
        return customer;
    }

    private static Product product(String code, String status, int stock) {
        Product product = new Product();
        product.setCode(code);
        product.setName(code);
        product.setStatus(status);
        product.setPrice(100);
        product.setStockQuantity(stock);
        return product;
    }

    private static Order order(String id, String status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setOrderDate(new Date());
        order.setCustomerName("Alice");
        order.setCustomerAddress("Address");
        order.setCustomerEmail("alice@example.com");
        order.setCustomerPhone("0900");
        return order;
    }
}
