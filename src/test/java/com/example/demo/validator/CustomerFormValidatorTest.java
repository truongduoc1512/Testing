package com.example.demo.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import com.example.demo.form.CustomerForm;
import com.example.demo.form.ProductForm;

class CustomerFormValidatorTest {

    private CustomerFormValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CustomerFormValidator();
    }

    @Test
    void supports_customerForm_returnsTrue() {
        assertThat(validator.supports(CustomerForm.class)).isTrue();
    }

    @Test
    void supports_otherClass_returnsFalse() {
        assertThat(validator.supports(ProductForm.class)).isFalse();
    }

    @Test
    void validate_validCustomer_normalizesInputAndHasNoErrors() {
        CustomerForm form = validForm();
        form.setName("  Alice  ");
        form.setAddress("  123 Main Street  ");
        form.setEmail("  ALICE@EXAMPLE.COM  ");
        form.setPhone("  0901234567  ");
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasErrors()).isFalse();
        assertThat(form.getName()).isEqualTo("Alice");
        assertThat(form.getAddress()).isEqualTo("123 Main Street");
        assertThat(form.getEmail()).isEqualTo("alice@example.com");
        assertThat(form.getPhone()).isEqualTo("0901234567");
    }

    @ParameterizedTest(name = "{0} with value [{1}] is required")
    @MethodSource("requiredFieldValues")
    void validate_blankRequiredField_rejectsOnlyRequiredCode(String field, String value) {
        CustomerForm form = validForm();
        setField(form, field, value);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.getFieldErrors(field))
                .extracting(FieldError::getCode)
                .containsExactly("NotEmpty.customerForm." + field);
    }

    @Test
    void validate_invalidEmail_rejectsPatternCode() {
        CustomerForm form = validForm();
        form.setEmail("invalid-email");
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "email", "Pattern.customerForm.email");
    }

    // Standard BVA (4n+1)
    @ParameterizedTest(name = "name length {0} is valid")
    @ValueSource(ints = { 1, 2, 50, 254, 255 })
    void validate_nameAtStandardBoundary_hasNoNameError(int length) {
        CustomerForm form = validForm();
        form.setName(repeat('n', length));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasFieldErrors("name")).isFalse();
    }

    // Robustness BVA (6n+1)
    @ParameterizedTest(name = "name length {0} is rejected with {1}")
    @CsvSource({
            "0, NotEmpty.customerForm.name",
            "256, Length.customerForm.name"
    })
    void validate_nameOutsideBoundary_rejectsExpectedCode(int length, String expectedCode) {
        CustomerForm form = validForm();
        form.setName(repeat('n', length));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "name", expectedCode);
    }

    @Test
    void validate_addressAtMaximumLength_hasNoAddressError() {
        CustomerForm form = validForm();
        form.setAddress(repeat('a', 255));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasFieldErrors("address")).isFalse();
    }

    @Test
    void validate_addressOverMaximumLength_rejectsLengthCode() {
        CustomerForm form = validForm();
        form.setAddress(repeat('a', 256));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "address", "Length.customerForm.address");
    }

    @Test
    void validate_emailAtMaximumLength_hasNoEmailError() {
        CustomerForm form = validForm();
        form.setEmail(emailOfLength(128));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasFieldErrors("email")).isFalse();
    }

    @Test
    void validate_emailOverMaximumLength_rejectsOnlyLengthCode() {
        CustomerForm form = validForm();
        form.setEmail(emailOfLength(129));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.getFieldErrors("email"))
                .extracting(FieldError::getCode)
                .containsExactly("Length.customerForm.email");
    }

    @Test
    void validate_phoneAtMaximumLength_hasNoPhoneError() {
        CustomerForm form = validForm();
        form.setPhone(repeat('1', 128));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasFieldErrors("phone")).isFalse();
    }

    @Test
    void validate_phoneOverMaximumLength_rejectsLengthCode() {
        CustomerForm form = validForm();
        form.setPhone(repeat('1', 129));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "phone", "Length.customerForm.phone");
    }

    private static Stream<Arguments> requiredFieldValues() {
        return Stream.of(
                arguments("name", null),
                arguments("name", "   "),
                arguments("address", null),
                arguments("address", "   "),
                arguments("email", null),
                arguments("email", "   "),
                arguments("phone", null),
                arguments("phone", "   "));
    }

    private CustomerForm validForm() {
        CustomerForm form = new CustomerForm();
        form.setName("Alice");
        form.setAddress("123 Main Street");
        form.setEmail("alice@example.com");
        form.setPhone("0901234567");
        return form;
    }

    private BeanPropertyBindingResult errorsFor(CustomerForm form) {
        return new BeanPropertyBindingResult(form, "customerForm");
    }

    private void setField(CustomerForm form, String field, String value) {
        switch (field) {
        case "name":
            form.setName(value);
            break;
        case "address":
            form.setAddress(value);
            break;
        case "email":
            form.setEmail(value);
            break;
        case "phone":
            form.setPhone(value);
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
