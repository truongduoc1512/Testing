package com.example.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AdminOrderPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By statusSelect = By.name("status");
    private By updateStatusButton = By.cssSelector(".status-form-wrapper button, .btn-update, button[type='submit']");
    private By statusBadge = By.cssSelector(".status-badge");
    private By firstOrderDetailLink = By.cssSelector("a[href*='/admin/order?orderId='], a[href*='order?orderId='], .order-card a");

    public AdminOrderPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void clickFirstOrder() {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(firstOrderDetailLink));
        el.click();
    }

    public void selectStatus(String statusValue) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(statusSelect));
        Select select = new Select(el);
        select.selectByValue(statusValue);
    }

    public void clickUpdateStatus() {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(updateStatusButton));
        el.click();
    }

    public String getCurrentStatusBadgeText() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(statusBadge));
        return el.getText().trim();
    }
}
