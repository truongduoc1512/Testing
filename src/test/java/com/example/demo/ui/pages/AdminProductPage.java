package com.example.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AdminProductPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By codeInput = By.cssSelector(".product-form input[name='code'], input#code");
    private By nameInput = By.cssSelector(".product-form input[name='name'], input#name");
    private By priceInput = By.cssSelector(".product-form input[name='price'], input#price");
    private By discountInput = By.cssSelector(".product-form input[name='discountPercent'], input#discountPercent");
    private By stockInput = By.cssSelector(".product-form input[name='stockQuantity'], input#stockQuantity");
    private By fileInput = By.id("file-upload-input");
    private By submitButton = By.cssSelector(".product-form button[type='submit'], button.btn-submit-primary");
    private By errorMessage = By.cssSelector(".error-msg-block, .alert-danger");

    public AdminProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void enterCode(String code) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(codeInput));
        el.clear();
        el.sendKeys(code);
    }

    public void enterName(String name) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        el.clear();
        el.sendKeys(name);
    }

    public void enterPrice(String price) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(priceInput));
        el.clear();
        el.sendKeys(price);
    }

    public void enterDiscount(String discount) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(discountInput));
        el.clear();
        el.sendKeys(discount);
    }

    public void enterStock(String stock) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(stockInput));
        el.clear();
        el.sendKeys(stock);
    }

    public void uploadImage(String absoluteFilePath) {
        // Find input[type='file'] even if hidden, and send file path directly
        WebElement el = driver.findElement(fileInput);
        el.sendKeys(absoluteFilePath);
    }

    public void clickSaveProduct() {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        el.click();
    }

    public boolean isErrorDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
