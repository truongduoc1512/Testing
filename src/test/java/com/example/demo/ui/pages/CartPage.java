package com.example.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CartPage {
    private WebDriver driver;
    private WebDriverWait wait;
    private By checkoutBtn = By.cssSelector("a[href*='checkout'], .btn-checkout, .btn-success, button[type='submit']");
    private By voucherInput = By.id("voucher-input");
    private By applyVoucherBtn = By.cssSelector("button[onclick='applyVoucher()']");
    private By voucherMessage = By.id("voucher-message");
    private By voucherDiscountText = By.id("summary-voucher-discount");

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void proceedToCheckout() {
        wait.until(ExpectedConditions.elementToBeClickable(checkoutBtn)).click();
    }

    public void enterVoucherCode(String code) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(voucherInput));
        el.clear();
        el.sendKeys(code);
    }

    public void clickApplyVoucher() {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(applyVoucherBtn));
        el.click();
    }

    public String getVoucherMessageText() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(voucherMessage));
        return el.getText().trim();
    }

    public String getVoucherDiscountText() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(voucherDiscountText));
        return el.getText().trim();
    }
}