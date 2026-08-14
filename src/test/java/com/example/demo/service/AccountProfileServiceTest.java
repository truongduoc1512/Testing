package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;
import com.example.demo.form.UserProfileForm;

class AccountProfileServiceTest {

    private AccountDAO accountDAO;
    private AccountProfileService service;

    @BeforeEach
    void setUp() {
        accountDAO = mock(AccountDAO.class);
        service = new AccountProfileService();
        ReflectionTestUtils.setField(service, "accountDAO", accountDAO);
    }

    @Test
    void validate_rejectsNullAccount() {
        assertEquals("Thông tin hồ sơ không hợp lệ!", service.validate(null, validProfileForm()));
    }

    @Test
    void validate_rejectsNullForm() {
        assertEquals("Thông tin hồ sơ không hợp lệ!", service.validate(accountWithUsername("buyer"), null));
    }

    @Test
    void validate_rejectsFullNameAboveMaximumLength() {
        assertLengthViolation(form -> form.setFullName(textOfLength('n', 101)));
    }

    @Test
    void validate_rejectsPhoneNumberAboveMaximumLength() {
        assertLengthViolation(form -> form.setPhoneNumber(textOfLength('p', 21)));
    }

    @Test
    void validate_rejectsAvatarUrlAboveMaximumLength() {
        assertLengthViolation(form -> form.setAvatarUrl(textOfLength('a', 256)));
    }

    @Test
    void validate_allowsBlankEmail() {
        UserProfileForm emptyEmail = validProfileForm();
        emptyEmail.setEmail("   ");

        assertNull(service.validate(accountWithUsername("buyer"), emptyEmail));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidEmails")
    void validate_rejectsInvalidEmail(String caseName, String email) {
        UserProfileForm form = validProfileForm();
        form.setEmail(email);

        assertEquals("Email không hợp lệ!", service.validate(accountWithUsername("buyer"), form));
    }

    static Stream<Arguments> invalidEmails() {
        return Stream.of(
                Arguments.of("above maximum length", textOfLength('e', 101)),
                Arguments.of("malformed address", "not-an-email"));
    }

    @Test
    void validate_acceptsNormalizedAvailableEmail() {
        Account account = accountWithUsername("buyer");
        UserProfileForm validEmail = validProfileForm();
        validEmail.setEmail(" Buyer@Example.COM ");
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(null);

        assertNull(service.validate(account, validEmail));
    }

    @Test
    void validate_allowsEmailOwnedBySameAccount() {
        Account account = accountWithUsername("buyer");
        UserProfileForm form = validProfileForm();
        Account sameOwner = accountWithUsername("buyer");
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(sameOwner);

        assertNull(service.validate(account, form));
    }

    @Test
    void validate_rejectsEmailOwnedByAnotherAccount() {
        Account account = accountWithUsername("buyer");
        UserProfileForm form = validProfileForm();
        Account foreignOwner = accountWithUsername("other");
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(foreignOwner);

        assertEquals("Email đã được sử dụng bởi tài khoản khác!", service.validate(account, form));
    }

    @Test
    void apply_mapsBlankValuesToNullAndPreservesBlankAvatar() {
        Account account = accountWithUsername("buyer");
        account.setAvatarUrl("existing.png");
        UserProfileForm form = new UserProfileForm();
        form.setFullName(null);
        form.setEmail("   ");
        form.setPhoneNumber("");
        form.setAvatarUrl("   ");

        service.apply(account, form);

        assertNull(account.getFullName());
        assertNull(account.getEmail());
        assertNull(account.getPhoneNumber());
        assertEquals("existing.png", account.getAvatarUrl());
    }

    @Test
    void apply_trimsAndNormalizesNonBlankValues() {
        Account account = accountWithUsername("buyer");
        UserProfileForm form = validProfileForm();
        form.setFullName(" Buyer Name ");
        form.setEmail(" Buyer@Example.COM ");
        form.setPhoneNumber(" 0900 ");
        form.setAvatarUrl(" avatar.png ");

        service.apply(account, form);

        assertEquals("Buyer Name", account.getFullName());
        assertEquals("buyer@example.com", account.getEmail());
        assertEquals("0900", account.getPhoneNumber());
        assertEquals("avatar.png", account.getAvatarUrl());
    }

    @ParameterizedTest
    @MethodSource("recognizedRoles")
    void normalizeRole_normalizesRecognizedRole(String input, String expected) {
        assertEquals(expected, service.normalizeRole(input));
    }

    static Stream<Arguments> recognizedRoles() {
        return Stream.of(
                Arguments.of(" role_admin ", Account.ROLE_ADMIN),
                Arguments.of("user", Account.ROLE_USER));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "manager")
    void normalizeRole_returnsNullForMissingOrUnrecognizedRole(String role) {
        assertNull(service.normalizeRole(role));
    }

    private void assertLengthViolation(Consumer<UserProfileForm> makeInvalid) {
        UserProfileForm form = validProfileForm();
        makeInvalid.accept(form);

        assertEquals("Thông tin hồ sơ vượt quá độ dài cho phép!",
                service.validate(accountWithUsername("buyer"), form));
    }

    private static UserProfileForm validProfileForm() {
        UserProfileForm form = new UserProfileForm();
        form.setFullName("Buyer");
        form.setEmail("buyer@example.com");
        form.setPhoneNumber("0900");
        form.setAvatarUrl("avatar.png");
        return form;
    }

    private static Account accountWithUsername(String username) {
        Account account = new Account();
        account.setUserName(username);
        return account;
    }

    private static String textOfLength(char value, int count) {
        return String.join("", Collections.nCopies(count, String.valueOf(value)));
    }
}
