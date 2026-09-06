package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.demo.dao.OrderDAO;
import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Product;
import com.example.demo.form.CustomerForm;
import com.example.demo.model.CartInfo;
import com.example.demo.model.CartLineInfo;
import com.example.demo.model.CustomerInfo;
import com.example.demo.model.ProductInfo;
import com.example.demo.pagination.PaginationResult;
import com.example.demo.validator.CustomerFormValidator;

class CartControllerCoverageTest {

    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private CustomerFormValidator validator;
    private CartController controller;

    @BeforeEach
    void setUp() {
        orderDAO = mock(OrderDAO.class);
        productDAO = mock(ProductDAO.class);
        validator = mock(CustomerFormValidator.class);
        when(validator.supports(CustomerForm.class)).thenReturn(true);
        controller = new CartController();
        ReflectionTestUtils.setField(controller, "orderDAO", orderDAO);
        ReflectionTestUtils.setField(controller, "productDAO", productDAO);
        ReflectionTestUtils.setField(controller, "customerFormValidator", validator);
    }

    @Test
    void initBinder_setsValidatorOnlyForCustomerForm() {
        WebDataBinder nullBinder = new WebDataBinder(null);
        controller.myInitBinder(nullBinder);
        assertTrue(nullBinder.getValidators().isEmpty());

        WebDataBinder otherBinder = new WebDataBinder("other");
        controller.myInitBinder(otherBinder);
        assertTrue(otherBinder.getValidators().isEmpty());

        WebDataBinder customerBinder = new WebDataBinder(new CustomerForm());
        controller.myInitBinder(customerBinder);
        assertSame(validator, customerBinder.getValidator());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void buyProduct_rejectsMissingCode(String code) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/shoppingCart", controller.listProductHandler(request,
                new ExtendedModelMap(), code, redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verifyNoInteractions(productDAO);
    }

    @Test
    void buyProduct_reportsUnknownProduct() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/shoppingCart", controller.listProductHandler(request,
                new ExtendedModelMap(), "missing", redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verify(productDAO).findActiveProduct("missing");
    }

    @Test
    void buyProduct_redirectsSoldOutProductToProductList() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Product soldOut = productWithStock("P0", 0);
        when(productDAO.findActiveProduct("P0")).thenReturn(soldOut);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productList", controller.listProductHandler(request,
                new ExtendedModelMap(), "P0", redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    void buyProduct_addsAvailableProductToCart() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Product available = productWithStock("P1", 5);
        when(productDAO.findActiveProduct("P1")).thenReturn(available);

        assertEquals("redirect:/shoppingCart", controller.listProductHandler(request,
                new ExtendedModelMap(), "P1", new RedirectAttributesModelMap()));

        assertEquals(1, cartFromSession(request).getQuantityTotal());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void addToCart_rejectsMissingCode(String code) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productList", controller.addToCartHandler(request,
                new ExtendedModelMap(), code, redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verifyNoInteractions(productDAO);
    }

    @Test
    void addToCart_rejectsSoldOutProduct() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Product soldOut = productWithStock("P0", -1);
        when(productDAO.findActiveProduct("P0")).thenReturn(soldOut);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productList", controller.addToCartHandler(request,
                new ExtendedModelMap(), "P0", redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    void addToCart_addsAvailableProductAndSuccessMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Product available = productWithStock("P1", 3);
        when(productDAO.findActiveProduct("P1")).thenReturn(available);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productList", controller.addToCartHandler(request,
                new ExtendedModelMap(), "P1", redirect));
        assertEquals(1, cartFromSession(request).getQuantityTotal());
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void removeProduct_rejectsMissingCode(String code) {
        MockHttpServletRequest request = requestWithCartItem("P1", 3, 2);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/shoppingCart", controller.removeProductHandler(request,
                new ExtendedModelMap(), code, redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verifyNoInteractions(productDAO);
    }

    @Test
    void removeProduct_removesExistingCartLine() {
        MockHttpServletRequest request = requestWithCartItem("P1", 3, 2);
        Product product = productWithStock("P1", 3);
        when(productDAO.findProduct("P1")).thenReturn(product);

        assertEquals("redirect:/shoppingCart", controller.removeProductHandler(request,
                new ExtendedModelMap(), "P1", new RedirectAttributesModelMap()));

        assertTrue(cartFromSession(request).isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidQuantityForms")
    void updateQuantity_rejectsInvalidForm(String formCase, CartInfo invalidForm) {
        MockHttpServletRequest request = requestWithCartItem("P1", 5, 1);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.shoppingCartUpdateQty(
                request, new ExtendedModelMap(), invalidForm, redirect);

        assertUpdateRejected(view, redirect);
        assertEquals(1, cartFromSession(request).getQuantityTotal());
    }

    @Test
    void updateQuantity_rejectsMissingProduct() {
        MockHttpServletRequest request = requestWithCartItem("P1", 10, 1);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.shoppingCartUpdateQty(
                request, new ExtendedModelMap(), quantityForm("missing", 2), redirect);

        assertUpdateRejected(view, redirect);
    }

    @Test
    void updateQuantity_rejectsSoldOutProduct() {
        MockHttpServletRequest request = requestWithCartItem("P1", 10, 1);
        Product soldOut = productWithStock("P0", 0);
        when(productDAO.findActiveProduct("P0")).thenReturn(soldOut);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.shoppingCartUpdateQty(
                request, new ExtendedModelMap(), quantityForm("P0", 2), redirect);

        assertUpdateRejected(view, redirect);
    }

    @Test
    void updateQuantity_capsQuantityAtAvailableStock() {
        MockHttpServletRequest request = requestWithCartItem("P1", 10, 1);
        Product available = productWithStock("P1", 3);
        when(productDAO.findActiveProduct("P1")).thenReturn(available);

        assertEquals("redirect:/shoppingCart", controller.shoppingCartUpdateQty(request,
                new ExtendedModelMap(), quantityForm("P1", 8), new RedirectAttributesModelMap()));

        assertEquals(3, cartFromSession(request).getQuantityTotal());
    }

    @Test
    void shoppingCartView_omitsRecommendationsWhenQueryReturnsNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("shoppingCart", controller.shoppingCartHandler(request, model));

        assertSame(model.get("cartForm"), model.get("myCart"));
        assertFalse(model.containsAttribute("recommendedProducts"));
    }

    @Test
    void shoppingCartView_addsReturnedRecommendations() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        @SuppressWarnings("unchecked")
        PaginationResult<ProductInfo> recommendations = mock(PaginationResult.class);
        when(recommendations.getList()).thenReturn(Collections.singletonList(new ProductInfo()));
        when(productDAO.queryProducts(1, 4, 5, null, null, null, null, null,
                null, null, null, null, null)).thenReturn(recommendations);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("shoppingCart", controller.shoppingCartHandler(request, model));

        assertEquals(1, ((java.util.List<?>) model.get("recommendedProducts")).size());
    }

    @Test
    void customerForm_redirectsEmptyCart() {
        MockHttpServletRequest emptyRequest = new MockHttpServletRequest();

        assertEquals("redirect:/shoppingCart", controller.shoppingCartCustomerForm(emptyRequest,
                new ExtendedModelMap()));
    }

    @Test
    void customerForm_mapsExistingCustomer() {
        MockHttpServletRequest request = requestWithCartItem("P1", 3, 1);
        cartFromSession(request).setCustomerInfo(customerInfo(true));
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("shoppingCartCustomer", controller.shoppingCartCustomerForm(request, model));

        assertEquals("Buyer", ((CustomerForm) model.get("customerForm")).getName());
    }

    @Test
    void customerSave_returnsFormForBindingErrors() {
        MockHttpServletRequest request = requestWithCartItem("P1", 3, 1);
        CustomerForm invalid = validCustomerForm();
        BeanPropertyBindingResult errors = bindingResult(invalid);
        errors.rejectValue("name", "invalid");

        assertEquals("shoppingCartCustomer", controller.shoppingCartCustomerSave(request,
                new ExtendedModelMap(), invalid, errors, new RedirectAttributesModelMap()));

        assertFalse(invalid.isValid());
    }

    @Test
    void customerSave_storesValidatedCustomerInCart() {
        MockHttpServletRequest request = requestWithCartItem("P1", 3, 1);
        CustomerForm valid = validCustomerForm();

        assertEquals("redirect:/shoppingCartConfirmation", controller.shoppingCartCustomerSave(request,
                new ExtendedModelMap(), valid, bindingResult(valid), new RedirectAttributesModelMap()));

        assertTrue(valid.isValid());
        assertTrue(cartFromSession(request).isValidCustomer());
        assertEquals("Buyer", cartFromSession(request).getCustomerInfo().getName());
    }

    @Test
    void confirmationReview_redirectsEmptyCart() {
        MockHttpServletRequest empty = new MockHttpServletRequest();

        assertEquals("redirect:/shoppingCart", controller.shoppingCartConfirmationReview(empty,
                new ExtendedModelMap()));
    }

    @Test
    void confirmationReview_redirectsCartWithInvalidCustomer() {
        MockHttpServletRequest invalid = requestWithCartItem("P1", 3, 1);

        assertEquals("redirect:/shoppingCartCustomer", controller.shoppingCartConfirmationReview(invalid,
                new ExtendedModelMap()));
    }

    @Test
    void confirmationReview_showsValidCart() {
        MockHttpServletRequest request = requestWithCartItem("P1", 3, 1);
        cartFromSession(request).setCustomerInfo(customerInfo(true));
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("shoppingCartConfirmation", controller.shoppingCartConfirmationReview(request, model));

        assertSame(cartFromSession(request), model.get("myCart"));
    }

    @Test
    void confirmationSave_redirectsEmptyCart() {
        MockHttpServletRequest empty = new MockHttpServletRequest();

        assertEquals("redirect:/shoppingCart", controller.shoppingCartConfirmationSave(empty,
                new ExtendedModelMap()));
    }

    @Test
    void confirmationSave_redirectsCartWithInvalidCustomer() {
        MockHttpServletRequest invalid = requestWithCartItem("P1", 3, 1);

        assertEquals("redirect:/shoppingCartCustomer", controller.shoppingCartConfirmationSave(invalid,
                new ExtendedModelMap()));
    }

    @Test
    void confirmationSave_preservesCartWhenOrderSaveFails() {
        MockHttpServletRequest failing = requestWithCartItem("P2", 3, 1);
        CartInfo failingCart = cartFromSession(failing);
        failingCart.setCustomerInfo(customerInfo(true));
        doThrow(new IllegalStateException("stock changed")).when(orderDAO).saveOrder(failingCart);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("shoppingCartConfirmation", controller.shoppingCartConfirmationSave(failing, model));

        assertTrue(model.containsAttribute("errorMessage"));
        assertSame(failingCart, failing.getSession().getAttribute("myCart"));
    }

    @Test
    void confirmationSave_movesSuccessfulCartToLastOrder() {
        MockHttpServletRequest success = requestWithCartItem("P3", 3, 1);
        CartInfo successfulCart = cartFromSession(success);
        successfulCart.setCustomerInfo(customerInfo(true));

        assertEquals("redirect:/shoppingCartFinalize", controller.shoppingCartConfirmationSave(success,
                new ExtendedModelMap()));

        verify(orderDAO).saveOrder(successfulCart);
        assertNull(success.getSession().getAttribute("myCart"));
        assertSame(successfulCart, success.getSession().getAttribute("lastOrderedCart"));
    }

    @Test
    void finalize_redirectsWithoutLastOrder() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertEquals("redirect:/shoppingCart", controller.shoppingCartFinalize(request,
                new ExtendedModelMap()));
    }

    @Test
    void finalize_showsStoredLastOrder() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CartInfo last = new CartInfo();
        request.getSession().setAttribute("lastOrderedCart", last);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("shoppingCartFinalize", controller.shoppingCartFinalize(request, model));

        assertSame(last, model.get("lastOrderedCart"));
    }

    @Test
    void ajaxQuantity_rejectsMissingProduct() {
        MockHttpServletRequest request = requestWithCartItem("P1", 10, 1);
        Map<String, Object> response = controller.updateCartQuantityAjax(request, "missing", 2);

        assertFalse((Boolean) response.get("success"));
    }

    @Test
    void ajaxQuantity_rejectsNonPositiveQuantity() {
        MockHttpServletRequest request = requestWithCartItem("P1", 10, 1);
        Product available = productWithStock("P1", 3);
        when(productDAO.findActiveProduct("P1")).thenReturn(available);
        Map<String, Object> response = controller.updateCartQuantityAjax(request, "P1", 0);

        assertFalse((Boolean) response.get("success"));
    }

    @Test
    void ajaxQuantity_rejectsSoldOutProduct() {
        MockHttpServletRequest request = requestWithCartItem("P1", 10, 1);
        Product soldOut = productWithStock("P0", 0);
        when(productDAO.findActiveProduct("P0")).thenReturn(soldOut);
        Map<String, Object> response = controller.updateCartQuantityAjax(request, "P0", 2);

        assertFalse((Boolean) response.get("success"));
    }

    @Test
    void ajaxQuantity_capsRequestedQuantityAtAvailableStock() {
        MockHttpServletRequest request = requestWithCartItem("P1", 10, 1);
        when(productDAO.findActiveProduct("P1")).thenReturn(productWithStock("P1", 3));
        Map<String, Object> response = controller.updateCartQuantityAjax(request, "P1", 8);

        assertTrue((Boolean) response.get("success"));
        assertEquals(3, response.get("actualQuantity"));
        assertTrue((Boolean) response.get("capped"));
        assertTrue(response.containsKey("message"));
        assertEquals(30.0, response.get("lineAmount"));
    }

    @Test
    void ajaxQuantity_updatesExistingLineWithoutCap() {
        MockHttpServletRequest request = requestWithCartItem("P1", 10, 1);
        when(productDAO.findActiveProduct("P1")).thenReturn(productWithStock("P1", 3));
        Map<String, Object> response = controller.updateCartQuantityAjax(request, "P1", 2);

        assertTrue((Boolean) response.get("success"));
        assertFalse((Boolean) response.get("capped"));
        assertEquals(2, response.get("actualQuantity"));
        assertFalse(response.containsKey("message"));
        assertEquals(20.0, response.get("lineAmount"));
    }

    @Test
    void ajaxQuantity_returnsZeroLineAmountWhenProductIsAbsentFromCart() {
        MockHttpServletRequest request = requestWithCartItem("P1", 10, 1);
        Product absentFromCart = productWithStock("P2", 5);
        when(productDAO.findActiveProduct("P2")).thenReturn(absentFromCart);
        Map<String, Object> response = controller.updateCartQuantityAjax(request, "P2", 2);

        assertTrue((Boolean) response.get("success"));
        assertEquals(0.0, response.get("lineAmount"));
    }

    @Test
    void ajaxRemove_reportsMissingProduct() {
        MockHttpServletRequest request = requestWithCartItem("P1", 3, 2);

        Map<String, Object> response = controller.removeCartProductAjax(request, "missing");

        assertEquals(Boolean.FALSE, response.get("success"));
    }

    @Test
    void ajaxRemove_removesProductAndReturnsUpdatedTotals() {
        MockHttpServletRequest request = requestWithCartItem("P1", 3, 2);
        Product product = productWithStock("P1", 3);
        when(productDAO.findProduct("P1")).thenReturn(product);

        Map<String, Object> response = controller.removeCartProductAjax(request, "P1");

        assertEquals(Boolean.TRUE, response.get("success"));
        assertEquals(0, response.get("quantityTotal"));
        assertEquals(0.0, response.get("amountTotal"));
    }

    private void assertUpdateRejected(String view, RedirectAttributesModelMap redirect) {
        assertEquals("redirect:/shoppingCart", view);
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    private static Stream<Arguments> invalidQuantityForms() {
        CartInfo nullLines = mock(CartInfo.class);
        when(nullLines.getCartLines()).thenReturn(null);

        CartInfo nullLine = new CartInfo();
        nullLine.getCartLines().add(null);

        CartInfo nullProduct = new CartInfo();
        CartLineInfo lineWithoutProduct = new CartLineInfo();
        lineWithoutProduct.setQuantity(1);
        nullProduct.getCartLines().add(lineWithoutProduct);

        CartInfo nullCode = new CartInfo();
        CartLineInfo lineWithoutCode = new CartLineInfo();
        lineWithoutCode.setProductInfo(new ProductInfo());
        lineWithoutCode.setQuantity(1);
        nullCode.getCartLines().add(lineWithoutCode);

        return Stream.of(
                Arguments.of("missing form", (Object) null),
                Arguments.of("missing cart lines", nullLines),
                Arguments.of("null cart line", nullLine),
                Arguments.of("line without product", nullProduct),
                Arguments.of("product without code", nullCode),
                Arguments.of("non-positive quantity", quantityForm("P1", 0)));
    }

    private Product productWithStock(String code, int stock) {
        Product product = new Product();
        product.setCode(code);
        product.setName("Product " + code);
        product.setPrice(10.0);
        product.setStockQuantity(stock);
        product.setStatus("ACTIVE");
        return product;
    }

    private MockHttpServletRequest requestWithCartItem(String code, int stock, int quantity) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CartInfo cart = new CartInfo();
        cart.addProduct(new ProductInfo(productWithStock(code, stock)), quantity);
        request.getSession().setAttribute("myCart", cart);
        return request;
    }

    private CartInfo cartFromSession(MockHttpServletRequest request) {
        return (CartInfo) request.getSession().getAttribute("myCart");
    }

    private static CartInfo quantityForm(String code, int quantity) {
        CartInfo form = new CartInfo();
        CartLineInfo line = new CartLineInfo();
        ProductInfo info = new ProductInfo();
        info.setCode(code);
        line.setProductInfo(info);
        line.setQuantity(quantity);
        form.getCartLines().add(line);
        return form;
    }

    private CustomerInfo customerInfo(boolean valid) {
        CustomerInfo customer = new CustomerInfo();
        customer.setName("Buyer");
        customer.setAddress("Address");
        customer.setEmail("buyer@example.com");
        customer.setPhone("0900");
        customer.setValid(valid);
        return customer;
    }

    private CustomerForm validCustomerForm() {
        CustomerForm form = new CustomerForm();
        form.setName(" Buyer ");
        form.setAddress(" Address ");
        form.setEmail(" Buyer@Example.com ");
        form.setPhone(" 0900 ");
        return form;
    }

    private BeanPropertyBindingResult bindingResult(CustomerForm form) {
        return new BeanPropertyBindingResult(form, "customerForm");
    }
}
