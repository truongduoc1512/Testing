package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dao.VoucherDAO;
import com.example.demo.entity.Product;
import com.example.demo.entity.Voucher;
import com.example.demo.form.VoucherForm;
import com.example.demo.model.CartInfo;
import com.example.demo.model.ProductInfo;
import com.example.demo.model.VoucherApplyResult;
import com.example.demo.utils.Utils;

class VoucherApiControllerTest {

    private VoucherDAO voucherDAO;
    private VoucherApiController controller;

    @BeforeEach
    void setUp() {
        voucherDAO = mock(VoucherDAO.class);
        controller = new VoucherApiController();
        ReflectionTestUtils.setField(controller, "voucherDAO", voucherDAO);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getActiveVouchers_returnsDaoResult() {
        List<Voucher> active = Arrays.asList(new Voucher(), new Voucher());
        when(voucherDAO.listActiveVouchers()).thenReturn(active);

        assertSame(active, controller.getActiveVouchers().getBody());
    }

    @Test
    void getAllVouchersAdmin_returnsDaoResult() {
        List<Voucher> all = Arrays.asList(new Voucher(), new Voucher(), new Voucher());
        when(voucherDAO.listAllVouchers()).thenReturn(all);

        assertSame(all, controller.getAllVouchersAdmin().getBody());
    }

    @Test
    void applyVoucher_treatsNullPayloadAsMissingCodeWithoutUsername() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        VoucherApplyResult failure = new VoucherApplyResult(false, "invalid");
        when(voucherDAO.validateAndApplyVoucher(null, 0.0, null)).thenReturn(failure);

        ResponseEntity<VoucherApplyResult> response = controller.applyVoucher(null, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertSame(failure, response.getBody());
        verify(voucherDAO).validateAndApplyVoucher(null, 0.0, null);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingCodePrincipals")
    void applyVoucher_resolvesUsernameForMissingVoucherCode(String principalCase,
            Authentication authentication, String expectedUsername) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        MockHttpServletRequest request = new MockHttpServletRequest();
        VoucherApplyResult failure = new VoucherApplyResult(false, "invalid");
        when(voucherDAO.validateAndApplyVoucher(null, 0.0, expectedUsername)).thenReturn(failure);

        ResponseEntity<VoucherApplyResult> response = controller.applyVoucher(new HashMap<>(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertSame(failure, response.getBody());
        verify(voucherDAO).validateAndApplyVoucher(null, 0.0, expectedUsername);
    }

    @Test
    void applyVoucher_usesServerCartAmountAndStoresSuccessfulDiscount() {
        authenticate("buyer");
        MockHttpServletRequest request = new MockHttpServletRequest();
        Product product = new Product();
        product.setCode("P1");
        product.setName("Product");
        product.setPrice(50.0);
        product.setStockQuantity(5);
        CartInfo cart = Utils.getCartInSession(request);
        cart.addProduct(new ProductInfo(product), 2);
        VoucherApplyResult success = new VoucherApplyResult(true, "applied");
        success.setVoucherCode("SAVE10");
        success.setDiscountAmount(10.0);
        when(voucherDAO.validateAndApplyVoucher("SAVE10", 100.0, "buyer")).thenReturn(success);
        Map<String, Object> payload = new HashMap<>();
        payload.put("voucherCode", new StringBuilder("SAVE10"));

        ResponseEntity<VoucherApplyResult> response = controller.applyVoucher(payload, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(success, response.getBody());
        assertEquals("SAVE10", cart.getVoucherCode());
        assertEquals(10.0, cart.getDiscountAmount());
        verify(voucherDAO).validateAndApplyVoucher("SAVE10", 100.0, "buyer");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidVoucherForms")
    void createVoucher_rejectsInvalidForm(String formCase, VoucherForm invalidForm) {
        assertBadRequest(controller.createVoucherAdmin(invalidForm));
    }

    @Test
    @SuppressWarnings("unchecked")
    void createVoucher_savesAndReturnsCreatedEntity() {
        VoucherForm form = new VoucherForm();
        form.setCode("save10");
        form.setDiscountValue(10.0);
        Voucher voucher = new Voucher();
        when(voucherDAO.findVoucher("save10")).thenReturn(voucher);

        ResponseEntity<?> response = controller.createVoucherAdmin(form);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(Boolean.TRUE, body.get("success"));
        assertSame(voucher, body.get("voucher"));
        assertTrue(body.get("message").toString().contains("SAVE10"));
        verify(voucherDAO).saveVoucher(form);
    }

    @Test
    void deleteVoucher_returnsNotFoundWhenDaoDoesNotDelete() {
        when(voucherDAO.deleteVoucher("missing")).thenReturn(false);

        ResponseEntity<?> response = controller.deleteVoucherAdmin("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Boolean.FALSE, body(response).get("success"));
    }

    @Test
    void deleteVoucher_returnsSuccessWhenDaoDeletes() {
        when(voucherDAO.deleteVoucher("SAVE10")).thenReturn(true);

        ResponseEntity<?> response = controller.deleteVoucherAdmin("SAVE10");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, body(response).get("success"));
    }

    private void assertBadRequest(ResponseEntity<?> response) {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Boolean.FALSE, body(response).get("success"));
        assertTrue(body(response).containsKey("message"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> body(ResponseEntity<?> response) {
        return (Map<String, Object>) response.getBody();
    }

    private static Stream<Arguments> missingCodePrincipals() {
        return Stream.of(
                Arguments.of("unauthenticated token", new UsernamePasswordAuthenticationToken("buyer", "n/a"),
                        null),
                Arguments.of("anonymous principal", authenticated("anonymousUser"), null),
                Arguments.of("authenticated user", authenticated("buyer"), "buyer"));
    }

    private static Stream<Arguments> invalidVoucherForms() {
        return Stream.of(
                Arguments.of("missing code", voucherForm(null, 10.0)),
                Arguments.of("blank code", voucherForm("   ", 10.0)),
                Arguments.of("zero discount", voucherForm("SAVE", 0.0)),
                Arguments.of("negative discount", voucherForm("SAVE", -1.0)));
    }

    private static VoucherForm voucherForm(String code, double discountValue) {
        VoucherForm form = new VoucherForm();
        form.setCode(code);
        form.setDiscountValue(discountValue);
        return form;
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(authenticated(username));
    }

    private static Authentication authenticated(String username) {
        return new UsernamePasswordAuthenticationToken(username, "n/a",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
