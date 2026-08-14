package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.demo.dao.OrderDAO;
import com.example.demo.dao.ProductReviewDAO;
import com.example.demo.dao.WishlistDAO;
import com.example.demo.entity.ProductReview;
import com.example.demo.form.ProductReviewForm;
import com.example.demo.model.OrderDetailInfo;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.OrderStatus;
import com.example.demo.model.ProductInfo;
import com.example.demo.pagination.PaginationResult;

class WebControllerCoverageTest {

    private ProductReviewDAO productReviewDAO;
    private OrderDAO orderDAO;
    private WishlistDAO wishlistDAO;
    private ReviewController reviewController;
    private OrderController orderController;
    private WishlistController wishlistController;

    @BeforeEach
    void setUp() {
        productReviewDAO = mock(ProductReviewDAO.class);
        orderDAO = mock(OrderDAO.class);
        wishlistDAO = mock(WishlistDAO.class);
        reviewController = new ReviewController();
        orderController = new OrderController();
        wishlistController = new WishlistController();
        ReflectionTestUtils.setField(reviewController, "productReviewDAO", productReviewDAO);
        ReflectionTestUtils.setField(orderController, "orderDAO", orderDAO);
        ReflectionTestUtils.setField(wishlistController, "wishlistDAO", wishlistDAO);
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void wishlistPage_redirectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals("redirect:/admin/login", wishlistController.wishlistPage(new ExtendedModelMap()));
        verify(wishlistDAO, never()).getUserWishlistProducts(anyString());
    }

    @Test
    void wishlistPage_showsProductsForAuthenticatedUser() {
        authenticate("buyer", "ROLE_USER");
        List<ProductInfo> products = Arrays.asList(new ProductInfo(), new ProductInfo());
        when(wishlistDAO.getUserWishlistProducts("buyer")).thenReturn(products);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("wishlist", wishlistController.wishlistPage(model));
        assertSame(products, model.get("wishlistProducts"));
        assertEquals(2, model.get("wishlistCount"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void saveReview_redirectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/login",
                reviewController.saveReview(new ExtendedModelMap(), validReview(), redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verify(productReviewDAO, never()).saveReview(any(ProductReview.class));
    }

    @ParameterizedTest(name = "role={0}")
    @ValueSource(strings = { "ROLE_MANAGER", "ROLE_ADMIN", "MANAGER", "ADMIN" })
    void saveReview_rejectsManagementRole(String role) {
        authenticate("manager", role);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productDetail?code=P1",
                reviewController.saveReview(new ExtendedModelMap(), validReview(), redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verify(productReviewDAO, never()).saveReview(any(ProductReview.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidReviewContent")
    void saveReview_rejectsInvalidContent(String inputPartition, String productCode, String comment) {
        authenticate("buyer", "ROLE_USER");

        assertInvalidSave(review(productCode, 5, comment));
    }

    @ParameterizedTest(name = "rating={0}")
    @ValueSource(ints = { 0, 6 })
    void saveReview_rejectsRatingOutsideOneToFive(int rating) {
        authenticate("buyer", "ROLE_USER");

        assertInvalidSave(review("P1", rating, "ok"));
    }

    @Test
    void saveReview_savesValidTrimmedReview() {
        authenticate("buyer", "ROLE_USER");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productDetail?code=P1",
                reviewController.saveReview(new ExtendedModelMap(), review("P1", 5, "  great  "), redirect));
        verify(productReviewDAO).saveReview(any(ProductReview.class));
        assertTrue(redirect.getFlashAttributes().containsKey("reviewMessage"));
    }

    @Test
    void saveReview_redirectsToProductListWhenDaoRejectsProduct() {
        authenticate("buyer", "ROLE_USER");
        doThrow(new IllegalArgumentException("invalid product"))
                .when(productReviewDAO).saveReview(any(ProductReview.class));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productList",
                reviewController.saveReview(new ExtendedModelMap(), validReview(), redirect));
        assertEquals("invalid product", redirect.getFlashAttributes().get("errorMessage"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void editReview_redirectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals("redirect:/admin/login",
                reviewController.editReview(1L, "P1", 5, "ok", new RedirectAttributesModelMap()));
        verify(productReviewDAO, never()).updateReview(anyLong(), anyString(), anyInt(), anyString());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidEditedReviews")
    void editReview_rejectsInvalidContentOrRating(String inputPartition, String comment, int rating) {
        authenticate("buyer", "ROLE_USER");

        assertInvalidEdit(comment, rating);
    }

    @Test
    void editReview_reportsSuccessfulUpdate() {
        authenticate("buyer", "ROLE_USER");
        when(productReviewDAO.updateReview(1L, "buyer", 5, "updated")).thenReturn(true);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productDetail?code=P1",
                reviewController.editReview(1L, "P1", 5, "  updated  ", redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("reviewMessage"));
    }

    @Test
    void editReview_reportsRejectedUpdate() {
        authenticate("buyer", "ROLE_USER");
        when(productReviewDAO.updateReview(2L, "buyer", 4, "valid")).thenReturn(false);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productDetail?code=P1",
                reviewController.editReview(2L, "P1", 4, "valid", redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void deleteReview_redirectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals("redirect:/admin/login",
                reviewController.deleteReview(1L, "P1", new RedirectAttributesModelMap()));
        verify(productReviewDAO, never()).deleteReview(anyLong(), anyString());
    }

    @Test
    void deleteReview_reportsSuccessfulDeletion() {
        authenticate("buyer", "ROLE_USER");
        when(productReviewDAO.deleteReview(1L, "buyer")).thenReturn(true);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productDetail?code=P1", reviewController.deleteReview(1L, "P1", redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("reviewMessage"));
    }

    @Test
    void deleteReview_reportsRejectedDeletion() {
        authenticate("buyer", "ROLE_USER");
        when(productReviewDAO.deleteReview(2L, "buyer")).thenReturn(false);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productDetail?code=P1", reviewController.deleteReview(2L, "P1", redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    void orderList_usesBlankScopeForManagerRole() {
        authenticate("manager", "ROLE_MANAGER");
        @SuppressWarnings("unchecked")
        PaginationResult<OrderInfo> managerPage = mock(PaginationResult.class);
        when(orderDAO.listOrderInfo(1, 5, 10, "manager", "")).thenReturn(managerPage);
        ExtendedModelMap managerModel = new ExtendedModelMap();

        assertEquals("orderList", orderController.orderList(managerModel, "1"));
        assertSame(managerPage, managerModel.get("paginationResult"));
    }

    @Test
    void orderList_fallsBackToFirstPageForInvalidPage() {
        authenticate("admin", "ROLE_ADMIN");
        @SuppressWarnings("unchecked")
        PaginationResult<OrderInfo> page = mock(PaginationResult.class);
        when(orderDAO.listOrderInfo(1, 5, 10, "admin", "ROLE_ADMIN")).thenReturn(page);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("orderList", orderController.orderList(model, "invalid"));
        assertSame(page, model.get("paginationResult"));
    }

    @Test
    void orderView_redirectsWhenOrderIdIsNull() {
        assertEquals("redirect:/admin/orderList", orderController.orderView(new ExtendedModelMap(), null));
        verify(orderDAO, never()).getOrderInfo(any());
    }

    @Test
    void orderView_redirectsWhenOrderDoesNotExist() {
        when(orderDAO.getOrderInfo("missing")).thenReturn(null);

        assertEquals("redirect:/admin/orderList",
                orderController.orderView(new ExtendedModelMap(), "missing"));
    }

    @Test
    void orderView_redirectsWhenPrincipalCannotAccessOrder() {
        authenticate("buyer", "ROLE_USER");
        when(orderDAO.getOrderInfo("O1")).thenReturn(new OrderInfo());
        when(orderDAO.canAccessOrder("O1", "buyer", "ROLE_USER")).thenReturn(false);

        assertEquals("redirect:/admin/orderList",
                orderController.orderView(new ExtendedModelMap(), "O1"));
    }

    @Test
    void orderView_loadsDetailsForUserOrder() {
        OrderInfo userOrder = new OrderInfo();
        List<OrderDetailInfo> userDetails = Collections.singletonList(detail(25.0));
        authenticate("buyer", "ROLE_USER");
        when(orderDAO.getOrderInfo("U1")).thenReturn(userOrder);
        when(orderDAO.canAccessOrder("U1", "buyer", "ROLE_USER")).thenReturn(true);
        when(orderDAO.listOrderDetailInfosForPrincipal("U1", "buyer", "ROLE_USER")).thenReturn(userDetails);
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("order", orderController.orderView(model, "U1"));
        assertEquals(userDetails, userOrder.getDetails());
        assertSame(userOrder, model.get("orderInfo"));
    }

    @Test
    void orderView_recalculatesAmountWhenAdminIsNotCustomer() {
        OrderInfo adminOrder = new OrderInfo();
        List<OrderDetailInfo> adminDetails = Arrays.asList(detail(10.0), detail(15.0));
        authenticate("admin", "ROLE_ADMIN");
        when(orderDAO.getOrderInfo("A1")).thenReturn(adminOrder);
        when(orderDAO.canAccessOrder("A1", "admin", "ROLE_ADMIN")).thenReturn(true);
        when(orderDAO.listOrderDetailInfosForPrincipal("A1", "admin", "ROLE_ADMIN")).thenReturn(adminDetails);
        when(orderDAO.isOrderCustomer("A1", "admin")).thenReturn(false);

        assertEquals("order", orderController.orderView(new ExtendedModelMap(), "A1"));
        assertEquals(25.0, adminOrder.getAmount());
    }

    @Test
    void orderView_preservesAmountWhenAdminIsCustomer() {
        OrderInfo ownAdminOrder = new OrderInfo();
        ownAdminOrder.setAmount(99.0);
        authenticate("admin", "ROLE_ADMIN");
        when(orderDAO.getOrderInfo("A2")).thenReturn(ownAdminOrder);
        when(orderDAO.canAccessOrder("A2", "admin", "ROLE_ADMIN")).thenReturn(true);
        when(orderDAO.listOrderDetailInfosForPrincipal("A2", "admin", "ROLE_ADMIN"))
                .thenReturn(Collections.singletonList(detail(1.0)));
        when(orderDAO.isOrderCustomer("A2", "admin")).thenReturn(true);
        assertEquals("order", orderController.orderView(new ExtendedModelMap(), "A2"));
        assertEquals(99.0, ownAdminOrder.getAmount());
    }

    @Test
    void updateOrderStatus_rejectsMissingAuthentication() {
        assertEquals("redirect:/403",
                updateOrderStatus("O1", "PENDING", new RedirectAttributesModelMap()));
        verify(orderDAO, never()).canManageOrder(any(), any());
    }

    @Test
    void updateOrderStatus_rejectsNonAdminUser() {
        authenticate("buyer", "ROLE_USER");
        assertEquals("redirect:/403",
                updateOrderStatus("O1", "PENDING", new RedirectAttributesModelMap()));
        verify(orderDAO, never()).canManageOrder(any(), any());
    }

    @Test
    void updateOrderStatus_rejectsOrderOutsideAdminScope() {
        authenticate("admin", "ROLE_ADMIN");
        when(orderDAO.canManageOrder("O1", "admin")).thenReturn(false);

        assertEquals("redirect:/403",
                updateOrderStatus("O1", "PENDING", new RedirectAttributesModelMap()));
    }

    @Test
    void updateOrderStatus_skipsUpdateWhenOrderIdIsNull() {
        authenticate("admin", "ROLE_ADMIN");
        when(orderDAO.canManageOrder(null, "admin")).thenReturn(true);

        assertEquals("redirect:/admin/order?orderId=null",
                updateOrderStatus(null, "PENDING", new RedirectAttributesModelMap()));
        verify(orderDAO, never()).updateOrderStatus(any(), any());
    }

    @Test
    void updateOrderStatus_skipsUpdateWhenStatusIsNull() {
        authenticate("admin", "ROLE_ADMIN");
        when(orderDAO.canManageOrder("O2", "admin")).thenReturn(true);

        assertEquals("redirect:/admin/order?orderId=O2",
                updateOrderStatus("O2", null, new RedirectAttributesModelMap()));
        verify(orderDAO, never()).updateOrderStatus(any(), any());
    }

    @Test
    void updateOrderStatus_rejectsStatusOutsideAdminTransitions() {
        authenticate("admin", "ROLE_ADMIN");
        when(orderDAO.canManageOrder("O2", "admin")).thenReturn(true);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/order?orderId=O2",
                updateOrderStatus("O2", "RETURNED", redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verify(orderDAO, never()).updateOrderStatus(any(), any());
    }

    @Test
    void updateOrderStatus_normalizesAndSavesValidStatus() {
        authenticate("admin", "ROLE_ADMIN");
        when(orderDAO.canManageOrder(any(), eq("admin"))).thenReturn(true);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/order?orderId=O1",
                updateOrderStatus("O1", "shipped", redirect));
        verify(orderDAO).updateOrderStatus("O1", OrderStatus.SHIPPING);
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void updateOrderStatus_surfacesInvalidTransitionMessage() {
        authenticate("admin", "ROLE_ADMIN");
        when(orderDAO.canManageOrder("O2", "admin")).thenReturn(true);
        doThrow(new IllegalStateException("invalid transition"))
                .when(orderDAO).updateOrderStatus("O2", OrderStatus.COMPLETED);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/order?orderId=O2", updateOrderStatus("O2", "COMPLETED", redirect));
        assertEquals("invalid transition", redirect.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void updateOrderStatus_reportsUnexpectedDaoFailure() {
        authenticate("admin", "ROLE_ADMIN");
        when(orderDAO.canManageOrder("O3", "admin")).thenReturn(true);
        doThrow(new RuntimeException("database unavailable"))
                .when(orderDAO).updateOrderStatus("O3", OrderStatus.CANCELLED);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/order?orderId=O3", updateOrderStatus("O3", "CANCELLED", redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    private void assertInvalidSave(ProductReviewForm form) {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/productDetail?code=" + form.getProductCode(),
                reviewController.saveReview(new ExtendedModelMap(), form, redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    private void assertInvalidEdit(String comment, int rating) {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/productDetail?code=P1",
                reviewController.editReview(1L, "P1", rating, comment, redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    private ProductReviewForm validReview() {
        return review("P1", 5, "Great");
    }

    private ProductReviewForm review(String code, int rating, String comment) {
        ProductReviewForm form = new ProductReviewForm(code);
        form.setRatingValue(rating);
        form.setComment(comment);
        return form;
    }

    private OrderDetailInfo detail(double amount) {
        OrderDetailInfo detail = new OrderDetailInfo();
        detail.setAmount(amount);
        return detail;
    }

    private void authenticate(String username, String... roles) {
        SecurityContextHolder.getContext().setAuthentication(authenticated(username, roles));
    }

    private String updateOrderStatus(String orderId, String status, RedirectAttributesModelMap redirect) {
        return orderController.updateOrderStatus(new ExtendedModelMap(), orderId, status, redirect);
    }

    private static Stream<Arguments> loginRequiredAuthentications() {
        return Stream.of(
                Arguments.of("missing authentication", null),
                Arguments.of("unauthenticated token", unauthenticated("buyer")),
                Arguments.of("anonymous principal", authenticated("anonymousUser", "ROLE_USER")));
    }

    private static Stream<Arguments> invalidReviewContent() {
        return Stream.of(
                Arguments.of("null product code", null, "ok"),
                Arguments.of("blank product code", "   ", "ok"),
                Arguments.of("product code above 20 characters", textOfLength('P', 21), "ok"),
                Arguments.of("null comment", "P1", null),
                Arguments.of("blank comment", "P1", "   "),
                Arguments.of("comment above 2,000 characters", "P1", textOfLength('c', 2001)));
    }

    private static Stream<Arguments> invalidEditedReviews() {
        return Stream.of(
                Arguments.of("null comment", null, 5),
                Arguments.of("blank comment", "   ", 5),
                Arguments.of("comment above 2,000 characters", textOfLength('x', 2001), 5),
                Arguments.of("rating below one", "ok", 0),
                Arguments.of("rating above five", "ok", 6));
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
