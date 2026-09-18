package com.example.demo.ui;

import com.example.demo.ui.pages.CartPage;
import com.example.demo.ui.pages.LoginPage;
import com.example.demo.ui.pages.ProductDetailPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoucherUiTest extends BaseUiTest {

    private void ensureProductInCart() {
        driver.get(BASE_URL + "/productList");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.product-title"))).click();

        ProductDetailPage productPage = new ProductDetailPage(driver);
        productPage.addToCart();
    }

    @Test
    void TC01_applyValidVoucherUpdatesTotal() {
        // 1. Đăng nhập tài khoản khách hàng
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("employee1", "123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin/login")));

        // 2. Thêm sản phẩm vào giỏ để có đơn giá
        ensureProductInCart();

        // 3. Mở trang Giỏ hàng
        driver.get(BASE_URL + "/shoppingCart");
        CartPage cartPage = new CartPage(driver);
        pause(1200);

        // 4. Nhập mã voucher khuyến mãi hợp lệ
        cartPage.enterVoucherCode("WELCOME50");
        pause(1000);
        cartPage.clickApplyVoucher();
        pause(2000); // Dừng lại 2s để người dùng nhìn thấy thông báo áp voucher và số tiền giảm

        // 5. Kiểm tra thông báo thành công hoặc cập nhật số tiền chiết khấu
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.id("voucher-message")),
                ExpectedConditions.not(ExpectedConditions.textToBe(By.id("summary-voucher-discount"), "0 ₫"))
        ));

        String message = cartPage.getVoucherMessageText();
        String discountText = cartPage.getVoucherDiscountText();

        boolean isVoucherApplied = message.toLowerCase().contains("thành công") 
                || message.toLowerCase().contains("áp dụng") 
                || !discountText.equals("0 ₫") 
                || discountText.contains("-");

        assertTrue(
                isVoucherApplied,
                "Voucher hợp lệ phải được áp dụng thành công và cập nhật số tiền giảm trên giao diện!"
        );
    }

    @Test
    void TC02_applyInvalidVoucherDisplaysError() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("employee1", "123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin/login")));

        ensureProductInCart();

        driver.get(BASE_URL + "/shoppingCart");
        CartPage cartPage = new CartPage(driver);
        pause(1000);

        // Nhập mã voucher không tồn tại
        cartPage.enterVoucherCode("INVALID_VOUCHER_99999");
        pause(1000);
        cartPage.clickApplyVoucher();
        pause(2000); // Dừng lại 2s để người dùng nhìn thấy thông báo lỗi đỏ

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("voucher-message")));
        String message = cartPage.getVoucherMessageText().toLowerCase();

        boolean isErrorDetected = message.contains("không hợp lệ") 
                || message.contains("không tồn tại") 
                || message.contains("thất bại")
                || message.contains("hết hạn")
                || message.contains("lỗi");

        assertTrue(
                isErrorDetected,
                "Nhập mã voucher không tồn tại phải hiển thị thông báo lỗi trên giao diện!"
        );
    }
}
