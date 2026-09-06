package com.example.demo.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;
import com.example.demo.form.ProductForm;
import com.example.demo.form.RegisterForm;

@ExtendWith(MockitoExtension.class)
class RegisterFormValidatorTest {

    @Mock
    private AccountDAO accountDAO;

    private RegisterFormValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RegisterFormValidator(accountDAO);
    }

    @Test
    void supports_registerForm_returnsTrue() {
        assertThat(validator.supports(RegisterForm.class)).isTrue();
    }

    @Test
    void supports_otherClass_returnsFalse() {
        assertThat(validator.supports(ProductForm.class)).isFalse();
    }

    @Test
    void validate_validRegistration_normalizesInputAndQueriesDao() {
        RegisterForm form = validForm();
        form.setUserName("  alice  ");
        form.setEmail("  ALICE@EXAMPLE.COM  ");
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasErrors()).isFalse();
        assertThat(form.getUserName()).isEqualTo("alice");
        assertThat(form.getEmail()).isEqualTo("alice@example.com");
        verify(accountDAO).findAccount("alice");
        verify(accountDAO).findAccountByEmail("alice@example.com");
    }

    @ParameterizedTest(name = "{0} with value [{1}] is required")
    @MethodSource("requiredFieldValues")
    void validate_blankRequiredField_rejectsOnlyRequiredCodeAndSkipsDao(String field, String value) {
        RegisterForm form = validForm();
        setField(form, field, value);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.getFieldErrors(field))
                .extracting(FieldError::getCode)
                .containsExactly("NotEmpty.registerForm." + field);
        verifyNoInteractions(accountDAO);
    }

    @Test
    void validate_invalidEmail_rejectsPatternAndSkipsDao() {
        RegisterForm form = validForm();
        form.setEmail("invalid-email");
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "email", "Pattern.registerForm.email");
        verifyNoInteractions(accountDAO);
    }

    @Test
    void validate_passwordMismatch_rejectsConfirmationAndSkipsDao() {
        RegisterForm form = validForm();
        form.setConfirmPassword("different-password");
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "confirmPassword", "Match.registerForm.confirmPassword");
        verifyNoInteractions(accountDAO);
    }

    @Test
    void validate_duplicateUsername_rejectsUsername() {
        RegisterForm form = validForm();
        when(accountDAO.findAccount("alice")).thenReturn(new Account());
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "userName", "Duplicate.registerForm.userName");
        verify(accountDAO).findAccountByEmail("alice@example.com");
    }

    @Test
    void validate_duplicateEmail_rejectsEmail() {
        RegisterForm form = validForm();
        when(accountDAO.findAccountByEmail("alice@example.com")).thenReturn(new Account());
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "email", "Duplicate.registerForm.email");
        verify(accountDAO).findAccount("alice");
    }

    @Test
    void validate_duplicateUsernameAndEmail_rejectsBothFields() {
        RegisterForm form = validForm();
        when(accountDAO.findAccount("alice")).thenReturn(new Account());
        when(accountDAO.findAccountByEmail("alice@example.com")).thenReturn(new Account());
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "userName", "Duplicate.registerForm.userName");
        assertFieldHasCode(errors, "email", "Duplicate.registerForm.email");
    }

    @Test
    void validate_usernameAtMaximumLength_hasNoUsernameError() {
        RegisterForm form = validForm();
        form.setUserName(repeat('u', 50));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasFieldErrors("userName")).isFalse();
        verify(accountDAO).findAccount(form.getUserName());
    }

    @Test
    void validate_usernameOverMaximumLength_rejectsLengthAndSkipsDao() {
        RegisterForm form = validForm();
        form.setUserName(repeat('u', 51));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "userName", "Length.registerForm.userName");
        verifyNoInteractions(accountDAO);
    }

    @ParameterizedTest(name = "password length {0} is accepted")
    @MethodSource("validPasswordLengths")
    void validate_passwordAtBoundary_hasNoPasswordError(int length) {
        RegisterForm form = validForm();
        String password = repeat('p', length);
        form.setPassword(password);
        form.setConfirmPassword(password);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasFieldErrors("password")).isFalse();
        verify(accountDAO).findAccount("alice");
        verify(accountDAO).findAccountByEmail("alice@example.com");
    }

    @ParameterizedTest(name = "password length {0} is rejected")
    @MethodSource("invalidPasswordLengths")
    void validate_passwordOutsideBoundary_rejectsLengthAndSkipsDao(int length) {
        RegisterForm form = validForm();
        String password = repeat('p', length);
        form.setPassword(password);
        form.setConfirmPassword(password);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "password", "Length.registerForm.password");
        verifyNoInteractions(accountDAO);
    }

    @Test
    void validate_emailAtMaximumLength_hasNoEmailError() {
        RegisterForm form = validForm();
        form.setEmail(emailOfLength(128));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasFieldErrors("email")).isFalse();
        verify(accountDAO).findAccountByEmail(form.getEmail());
    }

    @Test
    void validate_emailOverMaximumLength_rejectsOnlyLengthAndSkipsDao() {
        RegisterForm form = validForm();
        form.setEmail(emailOfLength(129));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.getFieldErrors("email"))
                .extracting(FieldError::getCode)
                .containsExactly("Length.registerForm.email");
        verifyNoInteractions(accountDAO);
    }

    private static Stream<Arguments> requiredFieldValues() {
        return Stream.of(
                arguments("userName", null),
                arguments("userName", "   "),
                arguments("email", null),
                arguments("email", "   "),
                arguments("password", null),
                arguments("password", "   "),
                arguments("confirmPassword", null),
                arguments("confirmPassword", "   "));
    }

    private static Stream<Integer> validPasswordLengths() {
        return Stream.of(8, 72);
    }

    private static Stream<Integer> invalidPasswordLengths() {
        return Stream.of(7, 73);
    }

    private RegisterForm validForm() {
        return new RegisterForm("alice", "alice@example.com", "password123", "password123");
    }

    private BeanPropertyBindingResult errorsFor(RegisterForm form) {
        return new BeanPropertyBindingResult(form, "registerForm");
    }

    private void setField(RegisterForm form, String field, String value) {
        switch (field) {
        case "userName":
            form.setUserName(value);
            break;
        case "email":
            form.setEmail(value);
            break;
        case "password":
            form.setPassword(value);
            break;
        case "confirmPassword":
            form.setConfirmPassword(value);
            break;
        default:
            throw new IllegalArgumentException("Unsupported field: " + field);
        }
    }

    private void assertFieldHasCode(BeanPropertyBindingResult errors, String field, String code) {
        assertThat(errors.getFieldErrors(field))
                .extracting(FieldError::getCode)
                .contains(code);
    }

    private String emailOfLength(int length) {
        int domainLabelLength = length - 69;
        return repeat('a', 64) + "@" + repeat('b', domainLabelLength) + ".com";
    }

    private String repeat(char character, int count) {
        return String.join("", Collections.nCopies(count, String.valueOf(character)));
    }
}
