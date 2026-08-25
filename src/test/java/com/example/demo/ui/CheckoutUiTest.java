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
        loginPage.login("employee1", "123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin/login")));

        driver.get(BASE_URL + "/productList");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.product-title"))).click();

        ProductDetailPage productPage = new ProductDetailPage(driver);
        productPage.addToCart();

        CartPage cartPage = new CartPage(driver);
        cartPage.proceedToCheckout();

        // Fill out customer info form if we are redirected to shoppingCartCustomer
        if (driver.getCurrentUrl().contains("shoppingCartCustomer")) {
            driver.findElement(By.id("customerName")).clear();
            driver.findElement(By.id("customerName")).sendKeys("Test User");
            driver.findElement(By.id("customerEmail")).clear();
            driver.findElement(By.id("customerEmail")).sendKeys("test@example.com");
            driver.findElement(By.id("customerPhone")).clear();
            driver.findElement(By.id("customerPhone")).sendKeys("0123456789");
            driver.findElement(By.id("customerAddress")).clear();
            driver.findElement(By.id("customerAddress")).sendKeys("123 Test St");
            driver.findElement(By.cssSelector("input[type='submit'].btn-submit")).click();
        }

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.confirmOrder();

        wait.until(ExpectedConditions.urlContains("shoppingCartFinalize"));

        boolean isSuccess = driver.getCurrentUrl().contains("shoppingCartFinalize");

        assertTrue(isSuccess, "Đơn hàng phải được đặt thành công!");
    }
}