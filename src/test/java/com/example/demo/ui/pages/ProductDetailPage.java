package com.example.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.List;

public class ProductDetailPage {
    private WebDriver driver;
    public ProductDetailPage(WebDriver driver) { this.driver = driver; }
    public void addToCart() {
        try {
            List<WebElement> elements = driver.findElements(By.cssSelector("button, input[type='submit'], a.btn, .btn, a"));
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