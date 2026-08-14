package com.example.demo.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.example.demo.entity.Product;
import com.example.demo.model.CustomerInfo;

class FormCoverageTest {

    @Test
    void customerForm_acceptsNullSource() {
        CustomerForm empty = new CustomerForm(null);

        assertNull(empty.getName());
    }

    @Test
    void customerForm_mapsCustomerInfo() {
        CustomerInfo info = new CustomerInfo();
        info.setName("Buyer");
        info.setAddress("Address");
        info.setEmail("buyer@example.com");
        info.setPhone("0900");

        CustomerForm mapped = new CustomerForm(info);
        assertEquals("Buyer", mapped.getName());
        assertEquals("Address", mapped.getAddress());
        assertEquals("buyer@example.com", mapped.getEmail());
        assertEquals("0900", mapped.getPhone());
    }

    @Test
    void customerForm_mutablePropertiesRoundTrip() {
        CustomerForm mutable = new CustomerForm();
        mutable.setName("Other");
        mutable.setAddress("Other address");
        mutable.setEmail("other@example.com");
        mutable.setPhone("0911");
        mutable.setValid(true);
        assertEquals("Other", mutable.getName());
        assertEquals("Other address", mutable.getAddress());
        assertEquals("other@example.com", mutable.getEmail());
        assertEquals("0911", mutable.getPhone());
        assertTrue(mutable.isValid());
    }

    @Test
    void productForm_usesNewProductDefaults() {
        ProductForm fresh = new ProductForm();

        assertTrue(fresh.isNewProduct());
        assertEquals(100, fresh.getStockQuantity());
    }

    @Test
    void productForm_mapsProductEntity() {
        Product product = new Product();
        product.setCode("P1");
        product.setName("Runner");
        product.setPrice(125.0);
        product.setDiscountPercent(15);
        product.setStockQuantity(7);

        ProductForm mapped = new ProductForm(product);
        assertFalse(mapped.isNewProduct());
        assertEquals("P1", mapped.getCode());
        assertEquals("Runner", mapped.getName());
        assertEquals(125.0, mapped.getPrice());
        assertEquals(15, mapped.getDiscountPercent());
        assertEquals(7, mapped.getStockQuantity());
    }

    @Test
    void productForm_mutablePropertiesAndUploadedFileRoundTrip() {
        ProductForm form = new ProductForm();
        MockMultipartFile file = new MockMultipartFile("file", "shoe.png", "image/png", new byte[] { 1 });

        form.setCode("P2");
        form.setName("Boot");
        form.setPrice(200.0);
        form.setDiscountPercent(20);
        form.setStockQuantity(9);
        form.setNewProduct(false);
        form.setFileData(file);

        assertEquals("P2", form.getCode());
        assertEquals("Boot", form.getName());
        assertEquals(200.0, form.getPrice());
        assertEquals(20, form.getDiscountPercent());
        assertEquals(9, form.getStockQuantity());
        assertFalse(form.isNewProduct());
        assertEquals(file, form.getFileData());
    }

    @Test
    void productReviewForm_usesDefaultRatingAndBindsConstructorProductCode() {
        ProductReviewForm review = new ProductReviewForm();
        assertEquals(5, review.getRatingValue());

        ProductReviewForm reviewForProduct = new ProductReviewForm("P1");
        assertEquals("P1", reviewForProduct.getProductCode());
    }

    @Test
    void productReviewForm_mutablePropertiesRoundTrip() {
        ProductReviewForm review = new ProductReviewForm();

        review.setProductCode("P2");
        review.setRatingValue(4);
        review.setComment("Good");
        review.setUsername("buyer");

        assertEquals("P2", review.getProductCode());
        assertEquals(4, review.getRatingValue());
        assertEquals("Good", review.getComment());
        assertEquals("buyer", review.getUsername());
    }

    @Test
    void orderReturnForm_constructorExposesValues() {
        OrderReturnForm form = new OrderReturnForm("Damaged", "two.png");

        assertEquals("Damaged", form.getReason());
        assertEquals("two.png", form.getImageUrls());
    }

    @Test
    void orderReturnForm_mutablePropertiesRoundTrip() {
        OrderReturnForm form = new OrderReturnForm();

        form.setReason("Wrong size");
        form.setImageUrls("one.png");

        assertEquals("Wrong size", form.getReason());
        assertEquals("one.png", form.getImageUrls());
    }

    @Test
    void returnStatusUpdateForm_constructorExposesValues() {
        ReturnStatusUpdateForm form = new ReturnStatusUpdateForm("REJECT", "Invalid evidence");

        assertEquals("REJECT", form.getAction());
        assertEquals("Invalid evidence", form.getAdminNote());
    }

    @Test
    void returnStatusUpdateForm_mutablePropertiesRoundTrip() {
        ReturnStatusUpdateForm form = new ReturnStatusUpdateForm();

        form.setAction("APPROVE");
        form.setAdminNote("Accepted");

        assertEquals("APPROVE", form.getAction());
        assertEquals("Accepted", form.getAdminNote());
    }

    @Test
    void registerForm_constructorExposesValues() {
        RegisterForm form = new RegisterForm("other", "other@example.com", "secret", "secret");

        assertEquals("other", form.getUserName());
        assertEquals("other@example.com", form.getEmail());
        assertEquals("secret", form.getPassword());
        assertEquals("secret", form.getConfirmPassword());
    }

    @Test
    void registerForm_mutablePropertiesRoundTrip() {
        RegisterForm form = new RegisterForm();

        form.setUserName("user");
        form.setEmail("user@example.com");
        form.setPassword("password");
        form.setConfirmPassword("password");

        assertEquals("user", form.getUserName());
        assertEquals("user@example.com", form.getEmail());
        assertEquals("password", form.getPassword());
        assertEquals("password", form.getConfirmPassword());
    }

    @Test
    void userProfileForm_usesActiveUnlockedDefaults() {
        UserProfileForm profile = new UserProfileForm();

        assertTrue(profile.isActive());
        assertTrue(profile.isAccountNonLocked());
    }

    @Test
    void userProfileForm_retainsEveryEditableValue() {
        UserProfileForm profile = new UserProfileForm();

        profile.setUserName("buyer");
        profile.setFullName("Demo Buyer");
        profile.setEmail("buyer@example.com");
        profile.setPhoneNumber("0900");
        profile.setAvatarUrl("avatar.png");
        profile.setUserRole("ROLE_USER");
        profile.setProvider("LOCAL");
        profile.setActive(false);
        profile.setAccountNonLocked(false);
        profile.setOldPassword("old");
        profile.setNewPassword("new");
        profile.setConfirmPassword("new");

        assertEquals("buyer", profile.getUserName());
        assertEquals("Demo Buyer", profile.getFullName());
        assertEquals("buyer@example.com", profile.getEmail());
        assertEquals("0900", profile.getPhoneNumber());
        assertEquals("avatar.png", profile.getAvatarUrl());
        assertEquals("ROLE_USER", profile.getUserRole());
        assertEquals("LOCAL", profile.getProvider());
        assertFalse(profile.isActive());
        assertFalse(profile.isAccountNonLocked());
        assertEquals("old", profile.getOldPassword());
        assertEquals("new", profile.getNewPassword());
        assertEquals("new", profile.getConfirmPassword());
    }

    @Test
    void userAddressForm_retainsEveryEditableValue() {
        UserAddressForm address = new UserAddressForm();
        address.setId(9L);
        address.setReceiverName("Receiver");
        address.setPhone("0912");
        address.setProvince("Province");
        address.setDistrict("District");
        address.setWard("Ward");
        address.setStreetAddress("Street");
        address.setNote("Note");
        address.setDefault(true);
        assertEquals(9L, address.getId());
        assertEquals("Receiver", address.getReceiverName());
        assertEquals("0912", address.getPhone());
        assertEquals("Province", address.getProvince());
        assertEquals("District", address.getDistrict());
        assertEquals("Ward", address.getWard());
        assertEquals("Street", address.getStreetAddress());
        assertEquals("Note", address.getNote());
        assertTrue(address.isDefault());
    }

    @Test
    void voucherForm_usesExpectedDefaults() {
        VoucherForm form = new VoucherForm();

        assertEquals("PERCENT", form.getDiscountType());
        assertTrue(form.isActive());
        assertEquals(100, form.getUsageLimit());
        assertEquals(1, form.getPerUserLimit());
        assertNull(form.getExpiryDate());
    }

    @Test
    void voucherForm_mutableDiscountLimitAndStateFieldsRoundTrip() {
        VoucherForm form = new VoucherForm();

        form.setCode("SAVE10");
        form.setDiscountType("FIXED");
        form.setDiscountValue(10.0);
        form.setMaxDiscount(20.0);
        form.setMinOrderValue(100.0);
        form.setActive(false);
        form.setUsageLimit(5);
        form.setPerUserLimit(2);

        assertEquals("SAVE10", form.getCode());
        assertEquals("FIXED", form.getDiscountType());
        assertEquals(10.0, form.getDiscountValue());
        assertEquals(20.0, form.getMaxDiscount());
        assertEquals(100.0, form.getMinOrderValue());
        assertFalse(form.isActive());
        assertEquals(5, form.getUsageLimit());
        assertEquals(2, form.getPerUserLimit());
    }

    @Test
    void voucherForm_defensivelyCopiesAndAcceptsNullExpiryDate() {
        VoucherForm form = new VoucherForm();
        Date expiry = new Date(10_000L);

        form.setExpiryDate(expiry);
        expiry.setTime(11_000L);

        Date returned = form.getExpiryDate();
        assertEquals(10_000L, returned.getTime());
        assertNotSame(returned, form.getExpiryDate());

        form.setExpiryDate(null);

        assertNull(form.getExpiryDate());
    }
}
