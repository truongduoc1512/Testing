package com.example.demo.ui.pages;

import org.openqa.selenium.WebDriver;

public class CartPage {
    private WebDriver driver;
    public CartPage(WebDriver driver) { this.driver = driver; }
    public void proceedToCheckout() {
        try {
            driver.get("http://localhost/checkout");
        } catch (Exception ignored) {}
    }
}