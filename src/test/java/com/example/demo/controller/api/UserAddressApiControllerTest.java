package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dao.UserAddressDAO;
import com.example.demo.entity.UserAddress;
import com.example.demo.form.UserAddressForm;

class UserAddressApiControllerTest {

    private UserAddressDAO userAddressDAO;
    private UserAddressApiController controller;

    @BeforeEach
    void setUp() {
        userAddressDAO = mock(UserAddressDAO.class);
        controller = new UserAddressApiController();
        ReflectionTestUtils.setField(controller, "userAddressDAO", userAddressDAO);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void getAddresses_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.getUserAddresses());
        verify(userAddressDAO, never()).getUserAddresses(anyString());
    }

    @Test
    void getAddresses_returnsAuthenticatedUserList() {
        authenticate("buyer");
        List<UserAddress> addresses = Arrays.asList(addressOwnedBy("buyer"), addressOwnedBy("buyer"));
        when(userAddressDAO.getUserAddresses("buyer")).thenReturn(addresses);

        ResponseEntity<?> response = controller.getUserAddresses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(addresses, response.getBody());
    }

    @Test
    void createAddress_requiresAuthentication() {
        assertUnauthorized(controller.createAddress(validForm()));
        verify(userAddressDAO, never()).saveAddress(anyString(), any(UserAddressForm.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidAddressForms")
    void createAddress_rejectsInvalidForm(String formCase, UserAddressForm invalidForm) {
        authenticate("buyer");

        assertBadRequest(controller.createAddress(invalidForm));

        verify(userAddressDAO, never()).saveAddress(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createAddress_clearsClientIdAndReturnsCreatedEntity() {
        authenticate("buyer");
        UserAddressForm form = validForm();
        form.setId(99L);
        form.setNote(" short note ");
        UserAddress saved = addressOwnedBy("buyer");
        when(userAddressDAO.saveAddress("buyer", form)).thenReturn(saved);

        ResponseEntity<?> response = controller.createAddress(form);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(form.getId());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(Boolean.TRUE, body.get("success"));
        assertSame(saved, body.get("address"));
    }

    @Test
    void updateAddress_requiresAuthentication() {
        assertUnauthorized(controller.updateAddress(1L, validForm()));
        verify(userAddressDAO, never()).getAddressById(anyLong());
    }

    @Test
    void updateAddress_forbidsMissingAddress() {
        authenticate("buyer");

        assertEquals(HttpStatus.FORBIDDEN,
                controller.updateAddress(1L, validForm()).getStatusCode());
        verify(userAddressDAO, never()).saveAddress(anyString(), any(UserAddressForm.class));
    }

    @Test
    void updateAddress_forbidsAddressOwnedByAnotherUser() {
        authenticate("buyer");
        UserAddress foreign = addressOwnedBy("other");
        when(userAddressDAO.getAddressById(2L)).thenReturn(foreign);

        assertEquals(HttpStatus.FORBIDDEN,
                controller.updateAddress(2L, validForm()).getStatusCode());
        verify(userAddressDAO, never()).saveAddress(anyString(), any(UserAddressForm.class));
    }

    @Test
    void updateAddress_rejectsInvalidOwnedAddressForm() {
        authenticate("buyer");
        UserAddress owned = addressOwnedBy("buyer");
        when(userAddressDAO.getAddressById(1L)).thenReturn(owned);
        UserAddressForm form = validForm();
        form.setPhone(null);

        assertBadRequest(controller.updateAddress(1L, form));
        verify(userAddressDAO, never()).saveAddress(anyString(), any(UserAddressForm.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateAddress_storesValidOwnedAddress() {
        authenticate("buyer");
        UserAddress owned = addressOwnedBy("buyer");
        when(userAddressDAO.getAddressById(1L)).thenReturn(owned);
        UserAddressForm form = validForm();
        UserAddress updated = addressOwnedBy("buyer");
        updated.setId(1L);
        when(userAddressDAO.saveAddress("buyer", form)).thenReturn(updated);

        ResponseEntity<?> response = controller.updateAddress(1L, form);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, form.getId());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(Boolean.TRUE, body.get("success"));
        assertSame(updated, body.get("address"));
    }

    @Test
    void deleteAddress_requiresAuthentication() {
        assertUnauthorized(controller.deleteAddress(1L));
        verify(userAddressDAO, never()).deleteAddress(anyString(), anyLong());
    }

    @Test
    void deleteAddress_returnsForbiddenWhenDaoRejectsDeletion() {
        authenticate("buyer");
        when(userAddressDAO.deleteAddress("buyer", 1L)).thenReturn(false);

        ResponseEntity<?> response = controller.deleteAddress(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertErrorBody(response);
    }

    @Test
    void deleteAddress_returnsSuccessWhenDaoDeletesAddress() {
        authenticate("buyer");
        when(userAddressDAO.deleteAddress("buyer", 2L)).thenReturn(true);

        ResponseEntity<?> response = controller.deleteAddress(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, body(response).get("success"));
    }

    @Test
    void setDefault_requiresAuthentication() {
        assertUnauthorized(controller.setDefaultAddress(1L));
        verify(userAddressDAO, never()).setDefaultAddress(anyString(), anyLong());
    }

    @Test
    void setDefault_returnsNotFoundWhenDaoRejectsUpdate() {
        authenticate("buyer");
        when(userAddressDAO.setDefaultAddress("buyer", 1L)).thenReturn(false);

        ResponseEntity<?> response = controller.setDefaultAddress(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertErrorBody(response);
    }

    @Test
    void setDefault_returnsSuccessWhenDaoUpdatesAddress() {
        authenticate("buyer");
        when(userAddressDAO.setDefaultAddress("buyer", 2L)).thenReturn(true);

        ResponseEntity<?> response = controller.setDefaultAddress(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, body(response).get("success"));
    }

    private void assertUnauthorized(ResponseEntity<?> response) {
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertErrorBody(response);
    }

    private void assertBadRequest(ResponseEntity<?> response) {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorBody(response);
    }

    private void assertErrorBody(ResponseEntity<?> response) {
        assertEquals(Boolean.FALSE, body(response).get("success"));
        assertTrue(body(response).containsKey("message"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> body(ResponseEntity<?> response) {
        return (Map<String, Object>) response.getBody();
    }

    private static UserAddressForm validForm() {
        return addressForm("Buyer", "0900", "Province", "District", "Ward", "Street", null);
    }

    private static UserAddressForm addressForm(String receiverName, String phone, String province,
            String district, String ward, String streetAddress, String note) {
        UserAddressForm form = new UserAddressForm();
        form.setReceiverName(receiverName);
        form.setPhone(phone);
        form.setProvince(province);
        form.setDistrict(district);
        form.setWard(ward);
        form.setStreetAddress(streetAddress);
        form.setNote(note);
        return form;
    }

    private static UserAddress addressOwnedBy(String username) {
        UserAddress address = new UserAddress();
        address.setUsername(username);
        return address;
    }

    private Authentication authenticate(String username) {
        Authentication auth = authenticated(username);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return auth;
    }

    private static Stream<Arguments> loginRequiredAuthentications() {
        return Stream.of(
                Arguments.of("missing authentication", null),
                Arguments.of("unauthenticated token", unauthenticated("buyer")),
                Arguments.of("anonymous principal", authenticated("anonymousUser")));
    }

    private static Stream<Arguments> invalidAddressForms() {
        return Stream.of(
                Arguments.of("missing form", (Object) null),
                Arguments.of("missing receiver name",
                        addressForm(null, "0900", "Province", "District", "Ward", "Street", null)),
                Arguments.of("blank receiver name",
                        addressForm("   ", "0900", "Province", "District", "Ward", "Street", null)),
                Arguments.of("missing phone",
                        addressForm("Buyer", null, "Province", "District", "Ward", "Street", null)),
                Arguments.of("missing province",
                        addressForm("Buyer", "0900", null, "District", "Ward", "Street", null)),
                Arguments.of("missing district",
                        addressForm("Buyer", "0900", "Province", null, "Ward", "Street", null)),
                Arguments.of("missing ward",
                        addressForm("Buyer", "0900", "Province", "District", null, "Street", null)),
                Arguments.of("missing street address",
                        addressForm("Buyer", "0900", "Province", "District", "Ward", null, null)),
                Arguments.of("receiver name above maximum length",
                        addressForm(textOfLength('n', 101), "0900", "Province", "District", "Ward",
                                "Street", null)),
                Arguments.of("phone above maximum length",
                        addressForm("Buyer", textOfLength('p', 21), "Province", "District", "Ward",
                                "Street", null)),
                Arguments.of("province above maximum length",
                        addressForm("Buyer", "0900", textOfLength('p', 101), "District", "Ward",
                                "Street", null)),
                Arguments.of("district above maximum length",
                        addressForm("Buyer", "0900", "Province", textOfLength('d', 101), "Ward",
                                "Street", null)),
                Arguments.of("ward above maximum length",
                        addressForm("Buyer", "0900", "Province", "District", textOfLength('w', 101),
                                "Street", null)),
                Arguments.of("street address above maximum length",
                        addressForm("Buyer", "0900", "Province", "District", "Ward",
                                textOfLength('s', 256), null)),
                Arguments.of("note above maximum length",
                        addressForm("Buyer", "0900", "Province", "District", "Ward", "Street",
                                textOfLength('x', 256))));
    }

    private static Authentication authenticated(String username) {
        return new UsernamePasswordAuthenticationToken(username, "n/a",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private static Authentication unauthenticated(String username) {
        return new UsernamePasswordAuthenticationToken(username, "n/a");
    }

    private static String textOfLength(char value, int count) {
        return String.join("", Collections.nCopies(count, String.valueOf(value)));
    }
}
