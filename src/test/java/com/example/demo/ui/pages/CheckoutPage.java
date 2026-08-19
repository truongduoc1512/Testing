package com.example.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CheckoutPage {
    private WebDriver driver;
    private WebDriverWait wait;
    private By confirmOrderBtn = By.cssSelector("button[type='submit'], input[type='submit'], .btn-confirm-order, form button");

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void confirmOrder() {
        wait.until(ExpectedConditions.elementToBeClickable(confirmOrderBtn)).click();
    }
}