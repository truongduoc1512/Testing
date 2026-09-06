package com.example.demo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.Product;
import com.example.demo.form.CustomerForm;

class ModelCoverageTest {

    @Test
    void productInfo_mapsEveryProductEntityField() {
        Product product = productEntity("P1", 200.0, 10, 8);
        product.setName("Runner");
        product.setSalesCount(12);
        product.setLocation("Da Nang");
        product.setBrand("Demo");
        product.setRating(4.5);
        product.setReviewCount(7);
        product.setCategory("Sneaker");
        product.setStatus("ACTIVE");
        product.setMall(true);
        product.setFavored(true);

        ProductInfo mapped = new ProductInfo(product);

        assertEquals("P1", mapped.getCode());
        assertEquals("Runner", mapped.getName());
        assertEquals(180.0, mapped.getPrice());
        assertEquals(200.0, mapped.getOriginalPrice());
        assertEquals(10, mapped.getDiscountPercent());
        assertEquals(12, mapped.getSalesCount());
        assertEquals("Da Nang", mapped.getLocation());
        assertEquals("Demo", mapped.getBrand());
        assertEquals(4.5, mapped.getRating());
        assertEquals(7, mapped.getReviewCount());
        assertEquals(8, mapped.getStockQuantity());
        assertEquals("Sneaker", mapped.getCategory());
        assertEquals("ACTIVE", mapped.getStatus());
        assertTrue(mapped.isMall());
        assertTrue(mapped.getIsMall());
        assertTrue(mapped.isFavored());
        assertTrue(mapped.getIsFavored());
    }

    @Test
    void productInfo_constructorOverloadsPopulateTheirOptionalFields() {
        ProductInfo projection = new ProductInfo("P2", "Basic", 50.0);
        assertEquals(50.0, projection.getOriginalPrice());
        assertEquals(50.0, projection.getPrice());

        ProductInfo listed = new ProductInfo(
                "P3", "Listed", 100.0, 20, 3, "Hue", "Brand", 4.0, true, false);
        assertEquals(80.0, listed.getPrice());

        ProductInfo reviewed = new ProductInfo(
                "P4", "Reviewed", 100.0, 5, 4, "Hanoi", "Brand", 4.2, false, true, 9, 6);
        assertEquals(9, reviewed.getReviewCount());
        assertEquals(6, reviewed.getStockQuantity());

        ProductInfo complete = new ProductInfo(
                "P5", "Complete", 120.0, 25, 5, "HCMC", "Brand", 4.8,
                true, true, 10, 11, "Boot", "INACTIVE");
        assertEquals("Boot", complete.getCategory());
        assertEquals("INACTIVE", complete.getStatus());
    }

    @Test
    void productInfo_mutablePropertiesRoundTrip() {
        ProductInfo mutable = new ProductInfo();
        mutable.setCode("PX");
        mutable.setName("Mutable");
        mutable.setPrice(90.0);
        mutable.setOriginalPrice(100.0);
        mutable.setDiscountPercent(10);
        mutable.setSalesCount(2);
        mutable.setLocation("Can Tho");
        mutable.setBrand("Local");
        mutable.setRating(3.5);
        mutable.setReviewCount(4);
        mutable.setStockQuantity(5);
        mutable.setCategory("Sandal");
        mutable.setStatus("ACTIVE");
        mutable.setMall(true);
        mutable.setFavored(true);

        assertEquals("PX", mutable.getCode());
        assertEquals("Mutable", mutable.getName());
        assertEquals(90.0, mutable.getPrice());
        assertEquals(100.0, mutable.getOriginalPrice());
        assertEquals(10, mutable.getDiscountPercent());
        assertEquals(2, mutable.getSalesCount());
        assertEquals("Can Tho", mutable.getLocation());
        assertEquals("Local", mutable.getBrand());
        assertEquals(3.5, mutable.getRating());
        assertEquals(4, mutable.getReviewCount());
        assertEquals(5, mutable.getStockQuantity());
        assertEquals("Sandal", mutable.getCategory());
        assertEquals("ACTIVE", mutable.getStatus());
        assertTrue(mutable.isMall());
        assertTrue(mutable.isFavored());
    }

    @Test
    void cartInfo_startsEmptyWithZeroTotalsAndNoValidCustomer() {
        CartInfo cart = new CartInfo();

        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getQuantityTotal());
        assertEquals(0.0, cart.getAmountTotal());
        assertFalse(cart.isValidCustomer());
    }

    @Test
    void cartInfo_rejectsInvalidCustomer() {
        CartInfo cart = new CartInfo();
        CustomerInfo customer = new CustomerInfo();
        customer.setValid(false);

        cart.setCustomerInfo(customer);

        assertFalse(cart.isValidCustomer());
    }

    @Test
    void cartInfo_exposesCheckoutPropertiesAndAcceptsValidCustomer() {
        CartInfo cart = new CartInfo();
        CustomerInfo customer = new CustomerInfo();
        customer.setValid(true);

        cart.setCustomerInfo(customer);
        cart.setOrderNum(12);
        cart.setVoucherCode("SAVE10");

        assertEquals(12, cart.getOrderNum());
        assertEquals(customer, cart.getCustomerInfo());
        assertEquals("SAVE10", cart.getVoucherCode());
        assertTrue(cart.isValidCustomer());
    }

    @Test
    void cartInfo_addProductAccumulatesQuantityAndCapsItAtStock() {
        CartInfo cart = new CartInfo();
        ProductInfo product = productInfoWithStock("P1", 10.0, 3);

        cart.addProduct(product, 2);
        cart.addProduct(product, 10);

        assertEquals(3, quantityFor(cart, "P1"));
        assertEquals(1, cart.getCartLines().size());
    }

    @Test
    void cartInfo_addProductRemovesLineWhenQuantityBecomesNonPositive() {
        CartInfo cart = new CartInfo();
        ProductInfo product = productInfoWithStock("P1", 10.0, 3);

        cart.addProduct(product, 2);
        cart.addProduct(product, -2);

        assertTrue(cart.isEmpty());
    }

    @Test
    void cartInfo_addProductDoesNotRetainANewLineForNegativeQuantity() {
        CartInfo cart = new CartInfo();
        ProductInfo product = productInfoWithStock("P1", 10.0, 3);

        cart.addProduct(product, -1);

        assertTrue(cart.isEmpty());
    }

    @Test
    void cartInfo_updateProductIgnoresUnknownCode() {
        CartInfo cart = cartContaining(productInfoWithStock("P1", 10.0, 3), 1);

        cart.updateProduct("missing", 2);

        assertEquals(1, quantityFor(cart, "P1"));
    }

    @Test
    void cartInfo_updateProductCapsQuantityAtStock() {
        CartInfo cart = cartContaining(productInfoWithStock("P1", 10.0, 3), 1);

        cart.updateProduct("P1", 99);

        assertEquals(3, quantityFor(cart, "P1"));
    }

    @Test
    void cartInfo_updateProductChangesPositiveQuantity() {
        CartInfo cart = cartContaining(productInfoWithStock("P1", 10.0, 3), 1);

        cart.updateProduct("P1", 2);

        assertEquals(2, quantityFor(cart, "P1"));
    }

    @Test
    void cartInfo_updateProductRemovesLineForNonPositiveQuantity() {
        CartInfo cart = cartContaining(productInfoWithStock("P1", 10.0, 3), 1);

        cart.updateProduct("P1", 0);

        assertTrue(cart.isEmpty());
    }

    @Test
    void cartInfo_removeProductIgnoresUnknownProduct() {
        ProductInfo existing = productInfoWithStock("P1", 10.0, 3);
        CartInfo cart = cartContaining(existing, 1);

        cart.removeProduct(productInfoWithStock("missing", 20.0, 4));

        assertEquals(1, cart.getCartLines().size());
    }

    @Test
    void cartInfo_removeProductRemovesMatchingProduct() {
        ProductInfo existing = productInfoWithStock("P1", 10.0, 3);
        CartInfo cart = cartContaining(existing, 1);

        cart.removeProduct(existing);

        assertTrue(cart.isEmpty());
    }

    @Test
    void cartInfo_calculatesQuantityAmountAndDiscountedFinalAmount() {
        CartInfo cart = new CartInfo();
        cart.addProduct(productInfoWithStock("P1", 10.0, 3), 3);
        cart.addProduct(productInfoWithStock("P2", 20.0, 4), 2);

        cart.setDiscountAmount(15.0);

        assertEquals(5, cart.getQuantityTotal());
        assertEquals(70.0, cart.getAmountTotal());
        assertEquals(15.0, cart.getDiscountAmount());
        assertEquals(55.0, cart.getFinalAmount());
    }

    @Test
    void cartInfo_clampsFinalAmountAtZero() {
        CartInfo cart = cartContaining(productInfoWithStock("P1", 10.0, 3), 1);

        cart.setDiscountAmount(1000.0);

        assertEquals(0.0, cart.getFinalAmount());
    }

    @Test
    void cartInfo_updateQuantityUpdatesOnlyMatchingCartLines() {
        ProductInfo first = productInfoWithStock("P1", 10.0, 3);
        ProductInfo second = productInfoWithStock("P2", 20.0, 4);
        CartInfo cart = cartContaining(first, 1);
        CartInfo updateForm = new CartInfo();
        updateForm.addProduct(first, 3);
        updateForm.addProduct(second, 1);

        cart.updateQuantity(updateForm);

        assertEquals(3, quantityFor(cart, "P1"));
        assertEquals(1, cart.getCartLines().size());
    }

    @Test
    void cartInfo_updateQuantityIgnoresEmptyForm() {
        CartInfo cart = cartContaining(productInfoWithStock("P1", 10.0, 3), 3);

        cart.updateQuantity(new CartInfo());

        assertEquals(3, cart.getQuantityTotal());
    }

    @Test
    void cartInfo_updateQuantityIgnoresNullForm() {
        CartInfo cart = cartContaining(productInfoWithStock("P1", 10.0, 3), 3);

        cart.updateQuantity(null);

        assertEquals(3, cart.getQuantityTotal());
    }

    @Test
    void customerInfo_mapsAndNormalizesValidatedForm() {
        CustomerForm form = new CustomerForm();
        form.setName("  Demo User  ");
        form.setAddress("  1 Main Street  ");
        form.setEmail("  USER@Example.COM  ");
        form.setPhone("  0900000000  ");
        form.setValid(true);

        CustomerInfo mapped = new CustomerInfo(form);

        assertEquals("Demo User", mapped.getName());
        assertEquals("1 Main Street", mapped.getAddress());
        assertEquals("user@example.com", mapped.getEmail());
        assertEquals("0900000000", mapped.getPhone());
        assertTrue(mapped.isValid());
    }

    @Test
    void customerInfo_mutablePropertiesRoundTrip() {
        CustomerInfo mutable = new CustomerInfo();
        mutable.setName("Other");
        mutable.setAddress("Address");
        mutable.setEmail("other@example.com");
        mutable.setPhone("0911111111");
        mutable.setValid(false);
        assertEquals("Other", mutable.getName());
        assertEquals("Address", mutable.getAddress());
        assertEquals("other@example.com", mutable.getEmail());
        assertEquals("0911111111", mutable.getPhone());
        assertFalse(mutable.isValid());
    }

    @Test
    void orderInfo_constructorCopiesDateAndExposesSummaryProperties() {
        Date date = new Date(1_000L);
        OrderInfo order = new OrderInfo(
                "O1", date, 7, 125.0, "Buyer", "Address", "buyer@example.com", "0900", "PENDING");
        date.setTime(2_000L);

        assertEquals(1_000L, order.getOrderDate().getTime());
        Date returnedDate = order.getOrderDate();
        returnedDate.setTime(3_000L);
        assertEquals(1_000L, order.getOrderDate().getTime());
        assertEquals("O1", order.getId());
        assertEquals(7, order.getOrderNum());
        assertEquals(125.0, order.getAmount());
        assertEquals("Buyer", order.getCustomerName());
        assertEquals("Address", order.getCustomerAddress());
        assertEquals("buyer@example.com", order.getCustomerEmail());
        assertEquals("0900", order.getCustomerPhone());
        assertEquals("PENDING", order.getStatus());
    }

    @Test
    void orderInfo_detailsAreDefensivelyCopiedAndAcceptNull() {
        OrderInfo order = new OrderInfo();
        OrderDetailInfo detail = new OrderDetailInfo("D1", "P1", "Shoe", 2, 50.0, 100.0);
        List<OrderDetailInfo> details = new ArrayList<>(Arrays.asList(detail));

        order.setDetails(details);
        details.clear();

        assertEquals(1, order.getDetails().size());
        List<OrderDetailInfo> returnedDetails = order.getDetails();
        returnedDetails.clear();
        assertEquals(1, order.getDetails().size());

        order.setDetails(null);

        assertNull(order.getDetails());
    }

    @Test
    void orderInfo_mutablePropertiesRoundTrip() {
        OrderInfo mutable = new OrderInfo();
        mutable.setId("O2");
        mutable.setOrderDate(new Date(4_000L));
        mutable.setOrderNum(8);
        mutable.setAmount(200.0);
        mutable.setCustomerName("Second");
        mutable.setCustomerAddress("Second address");
        mutable.setCustomerEmail("second@example.com");
        mutable.setCustomerPhone("0912");
        mutable.setStatus("COMPLETED");
        assertEquals("O2", mutable.getId());
        assertEquals(4_000L, mutable.getOrderDate().getTime());
        assertEquals(8, mutable.getOrderNum());
        assertEquals(200.0, mutable.getAmount());
        assertEquals("Second", mutable.getCustomerName());
        assertEquals("Second address", mutable.getCustomerAddress());
        assertEquals("second@example.com", mutable.getCustomerEmail());
        assertEquals("0912", mutable.getCustomerPhone());
        assertEquals("COMPLETED", mutable.getStatus());
    }

    @Test
    void orderInfo_acceptsNullDates() {
        OrderInfo order = new OrderInfo("O3", null, 1, 0, null, null, null, null, null);

        order.setOrderDate(null);

        assertNull(order.getOrderDate());
    }

    @Test
    void orderDetailInfo_constructorExposesEveryValue() {
        OrderDetailInfo detail = new OrderDetailInfo("D1", "P1", "Shoe", 2, 50.0, 100.0);

        assertEquals("D1", detail.getId());
        assertEquals("P1", detail.getProductCode());
        assertEquals("Shoe", detail.getProductName());
        assertEquals(2, detail.getQuanity());
        assertEquals(50.0, detail.getPrice());
        assertEquals(100.0, detail.getAmount());
    }

    @Test
    void orderDetailInfo_mutablePropertiesRoundTrip() {
        OrderDetailInfo mutableDetail = new OrderDetailInfo();
        mutableDetail.setId("D2");
        mutableDetail.setProductCode("P2");
        mutableDetail.setProductName("Boot");
        mutableDetail.setQuanity(3);
        mutableDetail.setPrice(40.0);
        mutableDetail.setAmount(120.0);
        assertEquals("D2", mutableDetail.getId());
        assertEquals("P2", mutableDetail.getProductCode());
        assertEquals("Boot", mutableDetail.getProductName());
        assertEquals(3, mutableDetail.getQuanity());
        assertEquals(40.0, mutableDetail.getPrice());
        assertEquals(120.0, mutableDetail.getAmount());
    }

    @Test
    void orderStatus_normalizesAliasesAndIdentifiesAdminStatuses() {
        assertNull(OrderStatus.normalize(null));
        assertEquals(OrderStatus.SHIPPING, OrderStatus.normalize(" shipped "));
        assertEquals(OrderStatus.COMPLETED, OrderStatus.normalize("delivered"));
        assertEquals(OrderStatus.PENDING, OrderStatus.normalize("1"));
        assertEquals(OrderStatus.APPROVED, OrderStatus.normalize(" approved "));
        assertTrue(OrderStatus.isAdminStatus("pending"));
        assertFalse(OrderStatus.isAdminStatus("returned"));
        assertFalse(OrderStatus.isAdminStatus(null));
    }

    @Test
    void orderStatus_rejectsMissingAndUnsupportedTransitions() {
        assertFalse(OrderStatus.canTransition(null, OrderStatus.PENDING));
        assertFalse(OrderStatus.canTransition(OrderStatus.PENDING, null));
        assertFalse(OrderStatus.canTransition(OrderStatus.PENDING, OrderStatus.RETURNED));
    }

    @Test
    void orderStatus_allowsEveryPendingTransition() {
        assertTrue(OrderStatus.canTransition(OrderStatus.PENDING, OrderStatus.PENDING));
        assertTrue(OrderStatus.canTransition(OrderStatus.PENDING, OrderStatus.APPROVED));
        assertTrue(OrderStatus.canTransition(OrderStatus.PENDING, OrderStatus.SHIPPING));
        assertTrue(OrderStatus.canTransition(OrderStatus.PENDING, OrderStatus.COMPLETED));
        assertTrue(OrderStatus.canTransition(OrderStatus.PENDING, OrderStatus.CANCELLED));
    }

    @Test
    void orderStatus_validatesApprovedShippingAndTerminalTransitionFamilies() {
        assertTrue(OrderStatus.canTransition(OrderStatus.APPROVED, OrderStatus.APPROVED));
        assertTrue(OrderStatus.canTransition(OrderStatus.APPROVED, OrderStatus.SHIPPING));
        assertTrue(OrderStatus.canTransition(OrderStatus.APPROVED, OrderStatus.COMPLETED));
        assertTrue(OrderStatus.canTransition(OrderStatus.APPROVED, OrderStatus.CANCELLED));
        assertFalse(OrderStatus.canTransition(OrderStatus.APPROVED, OrderStatus.PENDING));

        assertTrue(OrderStatus.canTransition(OrderStatus.SHIPPING, OrderStatus.COMPLETED));
        assertTrue(OrderStatus.canTransition(OrderStatus.SHIPPING, OrderStatus.CANCELLED));
        assertFalse(OrderStatus.canTransition(OrderStatus.SHIPPING, OrderStatus.APPROVED));
        assertFalse(OrderStatus.canTransition(OrderStatus.COMPLETED, OrderStatus.PENDING));
        assertFalse(OrderStatus.canTransition(OrderStatus.RETURN_PENDING, OrderStatus.PENDING));
    }

    @Test
    void voucherApplyResult_failureConstructorRetainsFailureDetails() {
        VoucherApplyResult failure = new VoucherApplyResult(false, "invalid");
        assertFalse(failure.isSuccess());
        assertEquals("invalid", failure.getMessage());
    }

    @Test
    void voucherApplyResult_mutableSuccessPayloadExposesEveryValue() {
        VoucherApplyResult result = new VoucherApplyResult();
        result.setSuccess(true);
        result.setMessage("applied");
        result.setVoucherCode("SAVE10");
        result.setDiscountType("PERCENT");
        result.setDiscountValue(10.0);
        result.setDiscountAmount(15.0);
        result.setOriginalAmount(150.0);
        result.setFinalAmount(135.0);

        assertTrue(result.isSuccess());
        assertEquals("applied", result.getMessage());
        assertEquals("SAVE10", result.getVoucherCode());
        assertEquals("PERCENT", result.getDiscountType());
        assertEquals(10.0, result.getDiscountValue());
        assertEquals(15.0, result.getDiscountAmount());
        assertEquals(150.0, result.getOriginalAmount());
        assertEquals(135.0, result.getFinalAmount());
    }

    private static Product productEntity(String code, double price, int discount, int stock) {
        Product product = new Product();
        product.setCode(code);
        product.setPrice(price);
        product.setDiscountPercent(discount);
        product.setStockQuantity(stock);
        return product;
    }

    private static ProductInfo productInfoWithStock(String code, double price, int stock) {
        ProductInfo product = new ProductInfo(code, code, price);
        product.setStockQuantity(stock);
        return product;
    }

    private static CartInfo cartContaining(ProductInfo product, int quantity) {
        CartInfo cart = new CartInfo();
        cart.addProduct(product, quantity);
        return cart;
    }

    private static int quantityFor(CartInfo cart, String code) {
        return cart.getCartLines().stream()
                .filter(line -> code.equals(line.getProductInfo().getCode()))
                .findFirst()
                .map(CartLineInfo::getQuantity)
                .orElse(0);
    }
}
