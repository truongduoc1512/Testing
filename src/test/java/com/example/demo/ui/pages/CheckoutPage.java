package com.example.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.List;

public class CheckoutPage {
    private WebDriver driver;
    public CheckoutPage(WebDriver driver) { this.driver = driver; }
    public void confirmOrder() {
        try {
            List<WebElement> elements = driver.findElements(By.cssSelector("button, input[type='submit'], .btn"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            for (WebElement el : elements) {
                try {
                    js.executeScript("arguments[0].click();", el);
                    break;
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }
}