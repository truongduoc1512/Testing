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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Product;
import com.example.demo.form.CustomerForm;
import com.example.demo.form.ProductForm;

@ExtendWith(MockitoExtension.class)
class ProductFormValidatorTest {

    @Mock
    private ProductDAO productDAO;

    private ProductFormValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProductFormValidator(productDAO);
    }

    @Test
    void supports_productForm_returnsTrue() {
        assertThat(validator.supports(ProductForm.class)).isTrue();
    }

    @Test
    void supports_otherClass_returnsFalse() {
        assertThat(validator.supports(CustomerForm.class)).isFalse();
    }

    @Test
    void validate_validNewProduct_normalizesInputAndLooksUpCodeOnce() {
        ProductForm form = validNewProduct();
        form.setCode("  P001  ");
        form.setName("  Running Shoe  ");
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasErrors()).isFalse();
        assertThat(form.getCode()).isEqualTo("P001");
        assertThat(form.getName()).isEqualTo("Running Shoe");
        verify(productDAO).findProduct("P001");
    }

    @Test
    void validate_validEditedProduct_doesNotLookUpDuplicateCode() {
        ProductForm form = validNewProduct();
        form.setNewProduct(false);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasErrors()).isFalse();
        verifyNoInteractions(productDAO);
    }

    @Test
    void validateLocalRules_validProduct_normalizesWithoutDaoLookup() {
        ProductForm form = validNewProduct();
        form.setCode("  P001  ");
        form.setName("  Running Shoe  ");
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validateLocalRules(form, errors);

        assertThat(errors.hasErrors()).isFalse();
        assertThat(form.getCode()).isEqualTo("P001");
        assertThat(form.getName()).isEqualTo("Running Shoe");
        verifyNoInteractions(productDAO);
    }

    @ParameterizedTest(name = "{0} with value [{1}] is required")
    @MethodSource("requiredFieldValues")
    void validate_blankRequiredField_rejectsOnlyRequiredCodeAndSkipsDao(String field, String value) {
        ProductForm form = validNewProduct();
        setField(form, field, value);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.getFieldErrors(field))
                .extracting(FieldError::getCode)
                .containsExactly("NotEmpty.productForm." + field);
        verifyNoInteractions(productDAO);
    }

    @Test
    void validate_duplicateNewProduct_rejectsCode() {
        ProductForm form = validNewProduct();
        Product existingProduct = new Product();
        when(productDAO.findProduct("P001")).thenReturn(existingProduct);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "code", "Duplicate.productForm.code");
        verify(productDAO).findProduct("P001");
    }

    @Test
    void validate_codeAtMaximumLength_hasNoCodeError() {
        ProductForm form = validNewProduct();
        form.setCode(repeat('c', 20));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasFieldErrors("code")).isFalse();
        verify(productDAO).findProduct(form.getCode());
    }

    @Test
    void validate_codeOverMaximumLength_rejectsLengthAndSkipsDao() {
        ProductForm form = validNewProduct();
        form.setCode(repeat('c', 21));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "code", "Length.productForm.code");
        verifyNoInteractions(productDAO);
    }

    @Test
    void validate_nameAtMaximumLength_hasNoNameError() {
        ProductForm form = validNewProduct();
        form.setName(repeat('n', 255));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertThat(errors.hasFieldErrors("name")).isFalse();
        verify(productDAO).findProduct("P001");
    }

    @Test
    void validate_nameOverMaximumLength_rejectsLengthAndSkipsDao() {
        ProductForm form = validNewProduct();
        form.setName(repeat('n', 256));
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "name", "Length.productForm.name");
        verifyNoInteractions(productDAO);
    }

    @ParameterizedTest(name = "price {0} is invalid")
    @MethodSource("invalidPrices")
    void validate_invalidPrice_rejectsMinimumAndSkipsDao(double price) {
        ProductForm form = validNewProduct();
        form.setPrice(price);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "price", "Min.productForm.price");
        verifyNoInteractions(productDAO);
    }

    @Test
    void validate_positiveFinitePrice_hasNoPriceError() {
        ProductForm form = validNewProduct();
        form.setPrice(0.01);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validateLocalRules(form, errors);

        assertThat(errors.hasFieldErrors("price")).isFalse();
        verifyNoInteractions(productDAO);
    }

    @Test
    void validate_negativeStock_rejectsMinimumAndSkipsDao() {
        ProductForm form = validNewProduct();
        form.setStockQuantity(-1);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "stockQuantity", "Min.productForm.stockQuantity");
        verifyNoInteractions(productDAO);
    }

    @Test
    void validate_zeroStock_hasNoStockError() {
        ProductForm form = validNewProduct();
        form.setStockQuantity(0);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validateLocalRules(form, errors);

        assertThat(errors.hasFieldErrors("stockQuantity")).isFalse();
    }

    @ParameterizedTest(name = "discount {0} is outside the accepted range")
    @ValueSource(ints = { -1, 101 })
    void validate_invalidDiscount_rejectsRangeAndSkipsDao(int discount) {
        ProductForm form = validNewProduct();
        form.setDiscountPercent(discount);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validate(form, errors);

        assertFieldHasCode(errors, "discountPercent", "Range.productForm.discountPercent");
        verifyNoInteractions(productDAO);
    }

    @ParameterizedTest(name = "discount boundary {0} is accepted")
    @ValueSource(ints = { 0, 100 })
    void validate_discountBoundary_hasNoDiscountError(int discount) {
        ProductForm form = validNewProduct();
        form.setDiscountPercent(discount);
        BeanPropertyBindingResult errors = errorsFor(form);

        validator.validateLocalRules(form, errors);

        assertThat(errors.hasFieldErrors("discountPercent")).isFalse();
    }

    @Test
    void validateLocalRules_existingBindingError_doesNotAddAnotherFieldError() {
        ProductForm form = validNewProduct();
        form.setPrice(0);
        BeanPropertyBindingResult errors = errorsFor(form);
        errors.rejectValue("price", "typeMismatch");

        validator.validateLocalRules(form, errors);

        assertThat(errors.getFieldErrors("price"))
                .extracting(FieldError::getCode)
                .containsExactly("typeMismatch");
        verifyNoInteractions(productDAO);
    }

    private static Stream<Arguments> requiredFieldValues() {
        return Stream.of(
                arguments("code", null),
                arguments("code", "   "),
                arguments("name", null),
                arguments("name", "   "));
    }

    private static Stream<Double> invalidPrices() {
        return Stream.of(0.0, -0.01, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    private ProductForm validNewProduct() {
        ProductForm form = new ProductForm();
        form.setCode("P001");
        form.setName("Running Shoe");
        form.setPrice(100.0);
        form.setStockQuantity(10);
        form.setDiscountPercent(0);
        form.setNewProduct(true);
        return form;
    }

    private BeanPropertyBindingResult errorsFor(ProductForm form) {
        return new BeanPropertyBindingResult(form, "productForm");
    }

    private void setField(ProductForm form, String field, String value) {
        if ("code".equals(field)) {
            form.setCode(value);
        } else if ("name".equals(field)) {
            form.setName(value);
        } else {
            throw new IllegalArgumentException("Unsupported field: " + field);
        }
    }

    private void assertFieldHasCode(BeanPropertyBindingResult errors, String field, String code) {
        assertThat(errors.getFieldErrors(field))
                .extracting(FieldError::getCode)
                .contains(code);
    }

    private String repeat(char character, int count) {
        return String.join("", Collections.nCopies(count, String.valueOf(character)));
    }
}
