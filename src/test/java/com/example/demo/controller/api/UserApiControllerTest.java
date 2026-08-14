package com.example.demo.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;
import com.example.demo.form.RegisterForm;
import com.example.demo.form.UserProfileForm;
import com.example.demo.pagination.PaginationResult;
import com.example.demo.service.AccountProfileService;
import com.example.demo.service.AuthenticatedAccountService;

class UserApiControllerTest {

    private AccountDAO accountDAO;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthenticatedAccountService authenticatedAccountService;
    private AccountProfileService accountProfileService;
    private UserApiController controller;

    @BeforeEach
    void setUp() {
        accountDAO = mock(AccountDAO.class);
        passwordEncoder = mock(BCryptPasswordEncoder.class);
        authenticatedAccountService = mock(AuthenticatedAccountService.class);
        accountProfileService = mock(AccountProfileService.class);
        controller = new UserApiController();
        ReflectionTestUtils.setField(controller, "accountDAO", accountDAO);
        ReflectionTestUtils.setField(controller, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(controller, "authenticatedAccountService", authenticatedAccountService);
        ReflectionTestUtils.setField(controller, "accountProfileService", accountProfileService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest(name = "requested page {0} uses page {1}")
    @CsvSource({ "-5, 1", "2, 2" })
    void getUsers_usesNormalizedPage(int requestedPage, int expectedPage) {
        @SuppressWarnings("unchecked")
        PaginationResult<Account> expected = mock(PaginationResult.class);
        when(accountDAO.listAccounts(expectedPage, 10, 10)).thenReturn(expected);

        assertSame(expected, controller.getUsers(requestedPage).getBody());
        verify(accountDAO).listAccounts(expectedPage, 10, 10);
    }

    @Test
    void getUserByUsername_returnsNotFoundWhenAccountDoesNotExist() {
        assertEquals(HttpStatus.NOT_FOUND, controller.getUserByUsername("missing").getStatusCode());
    }

    @Test
    void getUserByUsername_returnsExistingAccount() {
        Account account = accountWithUsername("buyer");
        when(accountDAO.findAccount("buyer")).thenReturn(account);

        ResponseEntity<?> response = controller.getUserByUsername("buyer");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(account, response.getBody());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRegistrationForms")
    void register_rejectsInvalidForm(String formCase, RegisterForm invalidForm) {
        assertBadRequest(controller.registerUser(invalidForm));

        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    @Test
    void register_rejectsExistingUsernameWithoutCheckingEmail() {
        RegisterForm duplicateUser = registerForm(" buyer ", "Buyer@Example.COM", "password", null);
        when(accountDAO.findAccount("buyer")).thenReturn(accountWithUsername("buyer"));

        assertBadRequest(controller.registerUser(duplicateUser));
        verify(accountDAO, never()).findAccountByEmail(any());
        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    @Test
    void register_rejectsExistingNormalizedEmail() {
        RegisterForm duplicateEmail = registerForm("other", " Buyer@Example.COM ", "password", "password");
        when(accountDAO.findAccount("other")).thenReturn(null);
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(accountWithUsername("existing"));

        assertBadRequest(controller.registerUser(duplicateEmail));
        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    @Test
    void register_acceptsInclusiveBoundariesAndNormalizesSavedAccount() {
        String username = textOfLength('u', 50);
        String password = textOfLength('p', 72);
        RegisterForm form = registerForm(" " + username + " ", " Buyer@Example.COM ", password, password);
        Account saved = accountWithUsername(username);
        when(accountDAO.findAccount(username)).thenReturn(null, saved);
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(null);
        when(passwordEncoder.encode(password)).thenReturn("encoded");

        ResponseEntity<?> response = controller.registerUser(form);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(saved, response.getBody());
        verify(accountDAO).saveAccount(org.mockito.ArgumentMatchers.argThat(account ->
                username.equals(account.getUserName())
                        && "buyer@example.com".equals(account.getEmail())
                        && "encoded".equals(account.getEncrytedPassword())
                        && Account.ROLE_USER.equals(account.getUserRole())
                        && "LOCAL".equals(account.getProvider())
                        && account.isActive()));
    }

    @Test
    void register_returnsServerErrorWhenPersistenceFails() {
        RegisterForm form = registerForm("buyer", "buyer@example.com", "password", null);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        doThrow(new IllegalStateException("database unavailable"))
                .when(accountDAO).saveAccount(any(Account.class));

        ResponseEntity<?> response = controller.registerUser(form);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertErrorBody(response);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void currentProfile_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.getCurrentUserProfile());
        verify(authenticatedAccountService, never()).resolve(any());
    }

    @Test
    void currentProfile_returnsNotFoundWhenAuthenticatedAccountCannotBeResolved() {
        authenticate("buyer");

        assertEquals(HttpStatus.NOT_FOUND, controller.getCurrentUserProfile().getStatusCode());
    }

    @Test
    void currentProfile_returnsResolvedAccount() {
        Authentication auth = authenticate("buyer");
        Account account = accountWithUsername("buyer");
        when(authenticatedAccountService.resolve(auth)).thenReturn(account);

        ResponseEntity<?> response = controller.getCurrentUserProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(account, response.getBody());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void updateProfile_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.updateUserProfile(profileForm("buyer")));
        verify(authenticatedAccountService, never()).resolve(any());
    }

    @Test
    void updateProfile_returnsNotFoundWhenAuthenticatedAccountCannotBeResolved() {
        authenticate("buyer");

        assertEquals(HttpStatus.NOT_FOUND, controller.updateUserProfile(profileForm("buyer")).getStatusCode());
        verify(accountProfileService, never()).validate(any(), any());
    }

    @Test
    void updateProfile_returnsValidationErrorWithoutApplyingChanges() {
        Authentication auth = authenticate("buyer");
        Account account = accountWithUsername("buyer");
        UserProfileForm form = profileForm("buyer");
        when(authenticatedAccountService.resolve(auth)).thenReturn(account);
        when(accountProfileService.validate(account, form)).thenReturn("invalid profile");

        assertBadRequest(controller.updateUserProfile(form));
        verify(accountProfileService, never()).apply(account, form);
        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    @Test
    void updateProfile_appliesAndReturnsPersistedAccount() {
        Authentication auth = authenticate("buyer");
        Account account = accountWithUsername("buyer");
        UserProfileForm form = profileForm("buyer");
        Account updated = accountWithUsername("buyer");
        when(authenticatedAccountService.resolve(auth)).thenReturn(account);
        when(accountProfileService.validate(account, form)).thenReturn(null);
        when(accountDAO.findAccount("buyer")).thenReturn(updated);

        ResponseEntity<?> response = controller.updateUserProfile(form);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, response.getBody());
        verify(accountProfileService).apply(account, form);
        verify(accountDAO).saveAccount(account);
    }

    @Test
    void updateProfile_returnsServerErrorWhenValidationThrows() {
        Authentication auth = authenticate("buyer");
        Account account = accountWithUsername("buyer");
        UserProfileForm form = profileForm("buyer");
        when(authenticatedAccountService.resolve(auth)).thenReturn(account);
        when(accountProfileService.validate(account, form)).thenThrow(new IllegalStateException("failure"));

        ResponseEntity<?> response = controller.updateUserProfile(form);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertErrorBody(response);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("loginRequiredAuthentications")
    void changePassword_rejectsLoginRequiredAuthentication(String authenticationCase,
            Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertUnauthorized(controller.changePassword(passwordPayload("old", "new-password", null)));
        verify(authenticatedAccountService, never()).resolve(any());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPasswordPayloads")
    void changePassword_rejectsInvalidPayload(String payloadCase, Map<String, String> invalidPayload) {
        authenticate("buyer");

        assertBadRequest(controller.changePassword(invalidPayload));

        verify(authenticatedAccountService, never()).resolve(any());
    }

    @Test
    void changePassword_returnsNotFoundWhenAuthenticatedAccountCannotBeResolved() {
        authenticate("buyer");

        assertEquals(HttpStatus.NOT_FOUND,
                controller.changePassword(passwordPayload("old", "new-password", null)).getStatusCode());
    }

    @Test
    void changePassword_rejectsIncorrectOldPassword() {
        Authentication auth = authenticate("buyer");
        Account account = accountWithUsername("buyer");
        account.setEncrytedPassword("old-hash");
        when(authenticatedAccountService.resolve(auth)).thenReturn(account);
        when(passwordEncoder.matches("old", "old-hash")).thenReturn(false);

        assertBadRequest(controller.changePassword(passwordPayload("old", "new-password", null)));
        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    @Test
    void changePassword_encodesAndSavesNewPassword() {
        Authentication auth = authenticate("buyer");
        Account account = accountWithUsername("buyer");
        account.setEncrytedPassword("old-hash");
        when(authenticatedAccountService.resolve(auth)).thenReturn(account);
        when(passwordEncoder.matches("old", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        ResponseEntity<?> response = controller.changePassword(
                passwordPayload("old", "new-password", "new-password"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-hash", account.getEncrytedPassword());
        assertEquals(Boolean.TRUE, body(response).get("success"));
        verify(accountDAO).saveAccount(account);
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

    private static RegisterForm registerForm(String username, String email, String password, String confirmation) {
        return new RegisterForm(username, email, password, confirmation);
    }

    private static UserProfileForm profileForm(String username) {
        UserProfileForm form = new UserProfileForm();
        form.setUserName(username);
        form.setFullName("Buyer");
        form.setEmail("buyer@example.com");
        return form;
    }

    private static Account accountWithUsername(String username) {
        Account account = new Account();
        account.setUserName(username);
        return account;
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

    private static Stream<Arguments> invalidRegistrationForms() {
        return Stream.of(
                Arguments.of("missing form", (Object) null),
                Arguments.of("missing username",
                        registerForm(null, "a@example.com", "password", null)),
                Arguments.of("blank username",
                        registerForm("   ", "a@example.com", "password", null)),
                Arguments.of("missing email",
                        registerForm("buyer", null, "password", null)),
                Arguments.of("blank email",
                        registerForm("buyer", "   ", "password", null)),
                Arguments.of("missing password",
                        registerForm("buyer", "a@example.com", null, null)),
                Arguments.of("blank password",
                        registerForm("buyer", "a@example.com", "   ", null)),
                Arguments.of("username above maximum length",
                        registerForm(textOfLength('u', 51), "a@example.com", "password", null)),
                Arguments.of("password below minimum length",
                        registerForm("buyer", "a@example.com", "1234567", null)),
                Arguments.of("password above maximum length",
                        registerForm("buyer", "a@example.com", textOfLength('p', 73), null)),
                Arguments.of("malformed email",
                        registerForm("buyer", "invalid-email", "password", null)),
                Arguments.of("confirmation mismatch",
                        registerForm("buyer", "a@example.com", "password", "different")));
    }

    private static Stream<Arguments> invalidPasswordPayloads() {
        return Stream.of(
                Arguments.of("missing payload", (Object) null),
                Arguments.of("missing old password", passwordPayload(null, "new-password", null)),
                Arguments.of("missing new password", passwordPayload("old", null, null)),
                Arguments.of("blank new password", passwordPayload("old", "   ", null)),
                Arguments.of("new password below minimum length", passwordPayload("old", "1234567", null)),
                Arguments.of("confirmation mismatch",
                        passwordPayload("old", "new-password", "different")));
    }

    private static Authentication authenticated(String username) {
        return new UsernamePasswordAuthenticationToken(username, "n/a",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private static Authentication unauthenticated(String username) {
        return new UsernamePasswordAuthenticationToken(username, "n/a");
    }

    private static Map<String, String> passwordPayload(
            String oldPassword, String newPassword, String confirmation) {
        Map<String, String> payload = new HashMap<>();
        if (oldPassword != null) {
            payload.put("oldPassword", oldPassword);
        }
        if (newPassword != null) {
            payload.put("newPassword", newPassword);
        }
        if (confirmation != null) {
            payload.put("confirmPassword", confirmation);
        }
        return payload;
    }

    private static String textOfLength(char value, int count) {
        return String.join("", Collections.nCopies(count, String.valueOf(value)));
    }
}
