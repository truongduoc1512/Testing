package com.example.demo.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

class EntityCoverageTest {

    @Test
    void account_exposesProfileSecurityFieldsAndDefensivelyCopiesDates() {
        Account account = new Account();
        assertTrue(account.isActive());
        assertTrue(account.isAccountNonLocked());
        assertEquals(0, account.getFailedAttempts());
        assertEquals("ROLE_USER", account.getUserRole());
        assertEquals("LOCAL", account.getProvider());
        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());

        Date reset = new Date(1_000L);
        Date created = new Date(2_000L);
        Date updated = new Date(3_000L);
        Date login = new Date(4_000L);
        account.setUserName("buyer");
        account.setFullName("Demo Buyer");
        account.setEncrytedPassword("hash");
        account.setActive(false);
        account.setAccountNonLocked(false);
        account.setFailedAttempts(3);
        account.setUserRole("ROLE_ADMIN");
        account.setEmail("buyer@example.com");
        account.setPhoneNumber("0900");
        account.setAvatarUrl("avatar.png");
        account.setResetToken("token");
        account.setResetTokenExpiresAt(reset);
        account.setProvider("GOOGLE");
        account.setProviderId("provider-id");
        account.setCreatedAt(created);
        account.setUpdatedAt(updated);
        account.setLastLogin(login);
        reset.setTime(9_000L);
        created.setTime(9_000L);
        updated.setTime(9_000L);
        login.setTime(9_000L);

        assertEquals("buyer", account.getUserName());
        assertEquals("Demo Buyer", account.getFullName());
        assertEquals("hash", account.getEncrytedPassword());
        assertFalse(account.isActive());
        assertFalse(account.isAccountNonLocked());
        assertEquals(3, account.getFailedAttempts());
        assertEquals("ROLE_ADMIN", account.getUserRole());
        assertEquals("buyer@example.com", account.getEmail());
        assertEquals("0900", account.getPhoneNumber());
        assertEquals("avatar.png", account.getAvatarUrl());
        assertEquals("token", account.getResetToken());
        assertEquals(1_000L, account.getResetTokenExpiresAt().getTime());
        assertEquals("GOOGLE", account.getProvider());
        assertEquals("provider-id", account.getProviderId());
        assertEquals(2_000L, account.getCreatedAt().getTime());
        assertEquals(3_000L, account.getUpdatedAt().getTime());
        assertEquals(4_000L, account.getLastLogin().getTime());
        assertNotSame(account.getCreatedAt(), account.getCreatedAt());
        assertEquals("[buyer,Demo Buyer,ROLE_ADMIN]", account.toString());

        account.setResetTokenExpiresAt(null);
        account.setCreatedAt(null);
        account.setUpdatedAt(null);
        account.setLastLogin(null);
        assertNull(account.getResetTokenExpiresAt());
        assertNull(account.getCreatedAt());
        assertNull(account.getUpdatedAt());
        assertNull(account.getLastLogin());
    }

    @Test
    void product_copiesBinaryAndDateValuesAndSupportsCatalogueFields() {
        Product product = new Product();
        assertEquals("manager1", product.getOwnerUsername());
        assertEquals("ACTIVE", product.getStatus());
        assertFalse(product.isMall());
        assertFalse(product.isFavored());
        assertNull(product.getImage());
        assertNull(product.getCreateDate());
        assertNull(product.getUpdateDate());

        Date created = new Date(1_000L);
        Date updated = new Date(2_000L);
        byte[] image = { 1, 2, 3 };
        product.setCode("P1");
        product.setName("Runner");
        product.setPrice(100.0);
        product.setCreateDate(created);
        product.setUpdateDate(updated);
        product.setImage(image);
        product.setOwnerUsername("seller");
        product.setDiscountPercent(20);
        product.setSalesCount(5);
        product.setLocation("Hanoi");
        product.setBrand("Demo");
        product.setRating(4.5);
        product.setReviewCount(7);
        product.setStockQuantity(8);
        product.setCategory("Sneaker");
        product.setStatus("INACTIVE");
        product.setMall(true);
        product.setFavored(true);
        created.setTime(9_000L);
        updated.setTime(9_000L);
        image[0] = 9;

        assertEquals("P1", product.getCode());
        assertEquals("Runner", product.getName());
        assertEquals(100.0, product.getPrice());
        assertEquals(1_000L, product.getCreateDate().getTime());
        assertEquals(2_000L, product.getUpdateDate().getTime());
        assertArrayEquals(new byte[] { 1, 2, 3 }, product.getImage());
        byte[] returnedImage = product.getImage();
        returnedImage[0] = 8;
        assertArrayEquals(new byte[] { 1, 2, 3 }, product.getImage());
        assertEquals("seller", product.getOwnerUsername());
        assertEquals(20, product.getDiscountPercent());
        assertEquals(5, product.getSalesCount());
        assertEquals("Hanoi", product.getLocation());
        assertEquals("Demo", product.getBrand());
        assertEquals(4.5, product.getRating());
        assertEquals(7, product.getReviewCount());
        assertEquals(8, product.getStockQuantity());
        assertEquals("Sneaker", product.getCategory());
        assertEquals("INACTIVE", product.getStatus());
        assertTrue(product.isMall());
        assertTrue(product.isFavored());

        product.setImage(null);
        product.setCreateDate(null);
        product.setUpdateDate(null);
        assertNull(product.getImage());
        assertNull(product.getCreateDate());
        assertNull(product.getUpdateDate());
    }

    @Test
    void userAddress_constructorPopulatesFieldsAndTimestamps() {
        UserAddress address = new UserAddress(
                "buyer", "Receiver", "0900", "Province", "District", "Ward", "Street", "Note", true);

        assertEquals("buyer", address.getUsername());
        assertEquals("Receiver", address.getReceiverName());
        assertEquals("0900", address.getPhone());
        assertEquals("Province", address.getProvince());
        assertEquals("District", address.getDistrict());
        assertEquals("Ward", address.getWard());
        assertEquals("Street", address.getStreetAddress());
        assertEquals("Note", address.getNote());
        assertTrue(address.isDefault());
        assertNotNull(address.getCreatedAt());
        assertNotNull(address.getUpdatedAt());
    }

    @Test
    void userAddress_mutablePropertiesAndDatesRoundTrip() {
        UserAddress address = new UserAddress();
        Date created = new Date(1_000L);
        Date updated = new Date(2_000L);
        address.setId(5L);
        address.setUsername("other");
        address.setReceiverName("Other Receiver");
        address.setPhone("0911");
        address.setProvince("P");
        address.setDistrict("D");
        address.setWard("W");
        address.setStreetAddress("S");
        address.setNote("N");
        address.setDefault(false);
        address.setCreatedAt(created);
        address.setUpdatedAt(updated);
        created.setTime(9_000L);
        updated.setTime(9_000L);

        assertEquals(5L, address.getId());
        assertEquals("other", address.getUsername());
        assertEquals("Other Receiver", address.getReceiverName());
        assertEquals("0911", address.getPhone());
        assertEquals("P", address.getProvince());
        assertEquals("D", address.getDistrict());
        assertEquals("W", address.getWard());
        assertEquals("S", address.getStreetAddress());
        assertEquals("N", address.getNote());
        assertFalse(address.isDefault());
        assertEquals(1_000L, address.getCreatedAt().getTime());
        assertEquals(2_000L, address.getUpdatedAt().getTime());

        address.setCreatedAt(null);
        address.setUpdatedAt(null);

        assertNull(address.getCreatedAt());
        assertNull(address.getUpdatedAt());
    }

    @Test
    void userAddress_fullAddressJoinsEveryComponent() {
        UserAddress address = new UserAddress(
                "buyer", "Receiver", "0900", "Province", "District", "Ward", "Street", null, false);

        assertEquals("Street, Ward, District, Province", address.getFullAddressString());
    }

    @Test
    void userAddress_fullAddressIsEmptyWhenEveryComponentIsMissing() {
        UserAddress address = new UserAddress();

        address.setStreetAddress(null);
        address.setWard(null);
        address.setDistrict(null);
        address.setProvince(null);

        assertEquals("", address.getFullAddressString());
    }

    @Test
    void userAddress_fullAddressOmitsMissingComponents() {
        UserAddress address = new UserAddress();

        address.setProvince("Only province");

        assertEquals("Only province", address.getFullAddressString());
    }

    @Test
    void order_exposesEveryPersistedValueAndDefensivelyCopiesOrderDate() {
        Order order = new Order();
        assertEquals("PENDING", order.getStatus());
        assertNull(order.getOrderDate());
        Date date = new Date(1_000L);
        order.setId("O1");
        order.setOrderDate(date);
        order.setOrderNum(3);
        order.setAmount(120.0);
        order.setCustomerName("Buyer");
        order.setCustomerAddress("Address");
        order.setCustomerEmail("buyer@example.com");
        order.setCustomerPhone("0900");
        order.setStatus("COMPLETED");
        order.setCustomerUsername("buyer");
        date.setTime(9_000L);

        assertEquals("O1", order.getId());
        assertEquals(1_000L, order.getOrderDate().getTime());
        Date returned = order.getOrderDate();
        returned.setTime(8_000L);
        assertEquals(1_000L, order.getOrderDate().getTime());
        assertEquals(3, order.getOrderNum());
        assertEquals(120.0, order.getAmount());
        assertEquals("Buyer", order.getCustomerName());
        assertEquals("Address", order.getCustomerAddress());
        assertEquals("buyer@example.com", order.getCustomerEmail());
        assertEquals("0900", order.getCustomerPhone());
        assertEquals("COMPLETED", order.getStatus());
        assertEquals("buyer", order.getCustomerUsername());
        order.setOrderDate(null);
        assertNull(order.getOrderDate());
    }

    @Test
    void orderDetail_exposesEveryPersistedValue() {
        Order order = new Order();
        Product product = new Product();
        OrderDetail detail = new OrderDetail();
        detail.setId("D1");
        detail.setOrder(order);
        detail.setProduct(product);
        detail.setQuanity(2);
        detail.setPrice(60.0);
        detail.setAmount(120.0);
        assertEquals("D1", detail.getId());
        assertEquals(order, detail.getOrder());
        assertEquals(product, detail.getProduct());
        assertEquals(2, detail.getQuanity());
        assertEquals(60.0, detail.getPrice());
        assertEquals(120.0, detail.getAmount());
    }

    @Test
    void orderReturn_usesPendingDefault() {
        OrderReturn request = new OrderReturn();

        assertEquals("PENDING", request.getStatus());
    }

    @Test
    void orderReturn_constructorPopulatesRequestAndTimestamps() {
        OrderReturn request = new OrderReturn("O1", "buyer", "Damaged", "one.png");

        assertEquals("O1", request.getOrderId());
        assertEquals("buyer", request.getUsername());
        assertEquals("Damaged", request.getReason());
        assertEquals("one.png", request.getImageUrls());
        assertEquals("PENDING", request.getStatus());
        assertNotNull(request.getCreatedAt());
        assertNotNull(request.getUpdatedAt());
    }

    @Test
    void orderReturn_mutablePropertiesRoundTrip() {
        OrderReturn request = new OrderReturn();

        request.setId(8L);
        request.setOrderId("O2");
        request.setUsername("other");
        request.setReason("Wrong size");
        request.setImageUrls("two.png");
        request.setStatus("APPROVED");
        request.setAdminNote("Accepted");

        assertEquals(8L, request.getId());
        assertEquals("O2", request.getOrderId());
        assertEquals("other", request.getUsername());
        assertEquals("Wrong size", request.getReason());
        assertEquals("two.png", request.getImageUrls());
        assertEquals("APPROVED", request.getStatus());
        assertEquals("Accepted", request.getAdminNote());
    }

    @Test
    void orderReturn_defensivelyCopiesAndAcceptsNullDates() {
        OrderReturn request = new OrderReturn();
        Date created = new Date(1_000L);
        Date updated = new Date(2_000L);

        request.setCreatedAt(created);
        request.setUpdatedAt(updated);
        created.setTime(9_000L);
        updated.setTime(9_000L);

        assertEquals(1_000L, request.getCreatedAt().getTime());
        assertEquals(2_000L, request.getUpdatedAt().getTime());
        assertNotSame(request.getCreatedAt(), request.getCreatedAt());

        request.setCreatedAt(null);
        request.setUpdatedAt(null);

        assertNull(request.getCreatedAt());
        assertNull(request.getUpdatedAt());
    }

    @Test
    void voucher_normalizesCodesAndCopiesAllDates() {
        Voucher empty = new Voucher();
        assertEquals(Voucher.TYPE_PERCENT, empty.getDiscountType());
        assertTrue(empty.isActive());
        assertEquals(100, empty.getUsageLimit());
        assertEquals(0, empty.getUsedCount());
        assertEquals(1, empty.getPerUserLimit());
        assertNotNull(empty.getCreatedAt());

        Date expiry = new Date(1_000L);
        Voucher voucher = new Voucher(" save10 ", Voucher.TYPE_FIXED, 10.0, 20.0, 100.0,
                expiry, false, 5, 2);
        expiry.setTime(9_000L);
        assertEquals("SAVE10", voucher.getCode());
        assertEquals(Voucher.TYPE_FIXED, voucher.getDiscountType());
        assertEquals(10.0, voucher.getDiscountValue());
        assertEquals(20.0, voucher.getMaxDiscount());
        assertEquals(100.0, voucher.getMinOrderValue());
        assertEquals(1_000L, voucher.getExpiryDate().getTime());
        assertFalse(voucher.isActive());
        assertEquals(5, voucher.getUsageLimit());
        assertEquals(2, voucher.getPerUserLimit());

        Date created = new Date(2_000L);
        voucher.setCode(" second ");
        voucher.setDiscountType(Voucher.TYPE_PERCENT);
        voucher.setDiscountValue(15.0);
        voucher.setMaxDiscount(null);
        voucher.setMinOrderValue(50.0);
        voucher.setExpiryDate(null);
        voucher.setActive(true);
        voucher.setUsageLimit(9);
        voucher.setUsedCount(3);
        voucher.setPerUserLimit(4);
        voucher.setCreatedAt(created);
        created.setTime(9_000L);
        assertEquals("SECOND", voucher.getCode());
        assertEquals(Voucher.TYPE_PERCENT, voucher.getDiscountType());
        assertEquals(15.0, voucher.getDiscountValue());
        assertNull(voucher.getMaxDiscount());
        assertEquals(50.0, voucher.getMinOrderValue());
        assertNull(voucher.getExpiryDate());
        assertTrue(voucher.isActive());
        assertEquals(9, voucher.getUsageLimit());
        assertEquals(3, voucher.getUsedCount());
        assertEquals(4, voucher.getPerUserLimit());
        assertEquals(2_000L, voucher.getCreatedAt().getTime());

        voucher.setCode(null);
        voucher.setCreatedAt(null);
        assertNull(voucher.getCode());
        assertNull(voucher.getCreatedAt());
        Voucher nullCode = new Voucher(null, Voucher.TYPE_PERCENT, 0, null, 0, null, true, 1, 1);
        assertNull(nullCode.getCode());
        assertNull(nullCode.getExpiryDate());
    }

    @Test
    void voucherUsage_normalizesCodeAndDefensivelyCopiesTimestamp() {
        VoucherUsage defaultUsage = new VoucherUsage();
        assertNotNull(defaultUsage.getUsedAt());

        VoucherUsage usage = new VoucherUsage(" save10 ", "buyer", "O1");
        assertEquals("SAVE10", usage.getVoucherCode());
        assertEquals("buyer", usage.getUsername());
        assertEquals("O1", usage.getOrderId());
        assertNotNull(usage.getUsedAt());

        usage.setId(1L);
        usage.setVoucherCode(" next ");
        usage.setUsername("other");
        usage.setOrderId("O2");
        Date usedAt = new Date(1_000L);
        usage.setUsedAt(usedAt);
        usedAt.setTime(9_000L);
        assertEquals(1L, usage.getId());
        assertEquals("NEXT", usage.getVoucherCode());
        assertEquals("other", usage.getUsername());
        assertEquals("O2", usage.getOrderId());
        assertEquals(1_000L, usage.getUsedAt().getTime());
        usage.setVoucherCode(null);
        usage.setUsedAt(null);
        assertNull(usage.getVoucherCode());
        assertNull(usage.getUsedAt());
        assertNull(new VoucherUsage(null, "buyer", null).getVoucherCode());
    }

    @Test
    void wishlist_exposesPropertiesAndDefensivelyCopiesTimestamp() {
        Wishlist defaultWishlist = new Wishlist();
        assertNotNull(defaultWishlist.getCreatedAt());

        Wishlist wishlist = new Wishlist("buyer", "P1");
        assertEquals("buyer", wishlist.getUsername());
        assertEquals("P1", wishlist.getProductCode());
        assertNotNull(wishlist.getCreatedAt());
        wishlist.setId(2L);
        wishlist.setUsername("other");
        wishlist.setProductCode("P2");
        Date created = new Date(2_000L);
        wishlist.setCreatedAt(created);
        created.setTime(9_000L);
        assertEquals(2L, wishlist.getId());
        assertEquals("other", wishlist.getUsername());
        assertEquals("P2", wishlist.getProductCode());
        assertEquals(2_000L, wishlist.getCreatedAt().getTime());
        wishlist.setCreatedAt(null);
        assertNull(wishlist.getCreatedAt());
    }

    @Test
    void productReview_defaultsCreatedTimestamp() {
        ProductReview review = new ProductReview();

        assertNotNull(review.getCreatedAt());
    }

    @Test
    void productReview_constructorPopulatesReviewAndTimestamp() {
        ProductReview review = new ProductReview("P1", "buyer", 5, "Great");

        assertEquals("P1", review.getProductCode());
        assertEquals("buyer", review.getUsername());
        assertEquals(5, review.getRatingValue());
        assertEquals("Great", review.getComment());
        assertNotNull(review.getCreatedAt());
    }

    @Test
    void productReview_mutablePropertiesRoundTrip() {
        ProductReview review = new ProductReview();

        review.setReviewId(3L);
        review.setProductCode("P2");
        review.setUsername("other");
        review.setRatingValue(4);
        review.setComment("Good");
        review.setImageUrl("image.png");

        assertEquals(3L, review.getReviewId());
        assertEquals("P2", review.getProductCode());
        assertEquals("other", review.getUsername());
        assertEquals(4, review.getRatingValue());
        assertEquals("Good", review.getComment());
        assertEquals("image.png", review.getImageUrl());
    }

    @Test
    void productReview_defensivelyCopiesAndAcceptsNullTimestamp() {
        ProductReview review = new ProductReview();
        Date created = new Date(1_000L);

        review.setCreatedAt(created);
        created.setTime(9_000L);

        assertEquals(1_000L, review.getCreatedAt().getTime());

        review.setCreatedAt(null);

        assertNull(review.getCreatedAt());
    }
}
