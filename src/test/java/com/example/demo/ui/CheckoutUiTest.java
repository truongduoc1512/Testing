package com.example.demo.ui;

import com.example.demo.ui.pages.CartPage;
import com.example.demo.ui.pages.CheckoutPage;
import com.example.demo.ui.pages.LoginPage;
import com.example.demo.ui.pages.ProductDetailPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckoutUiTest extends BaseUiTest {

    @Test
    void TC01_endToendCheckoutJourney() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("employee1", "123456");

        driver.get(BASE_URL + "/productList");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Xem Chi Tiết')]"))).click();

        ProductDetailPage productPage = new ProductDetailPage(driver);
        productPage.addToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.proceedToCheckout();

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.confirmOrder();

        boolean isSuccess = driver.getCurrentUrl().contains("order") 
                         || driver.getCurrentUrl().contains("success") 
                         || driver.getPageSource().contains("Đặt hàng thành công")
                         || driver.getPageSource().contains("Order");

        assertTrue(isSuccess, "Đơn hàng phải được đặt thành công!");
    }
}