package com.example.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage {

    private final WebDriver driver;

    // Locators
    private final By usernameInput =
            By.name("userName");

    private final By passwordInput =
            By.name("password");

    private final By rememberMeCheckbox =
            By.id("remember-me-checkbox");

    private final By loginButton =
            By.cssSelector(".btn-login-submit");

    private final By googleLoginButton =
            By.cssSelector("a[href*='/oauth2/authorization/google']");

    private final By forgotPasswordLink =
            By.cssSelector(".forgot-password-link");

    private final By registerLink =
            By.cssSelector("a[href='/register']");

    private final By errorMessage =
            By.cssSelector(".alert-danger");


    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }


    public LoginPage enterUsername(String username) {
        WebElement element = driver.findElement(usernameInput);
        element.clear();
        element.sendKeys(username);
        return this;
    }


    public LoginPage enterPassword(String password) {
        WebElement element = driver.findElement(passwordInput);
        element.clear();
        element.sendKeys(password);
        return this;
    }


    public LoginPage clickRememberMe() {
        driver.findElement(rememberMeCheckbox).click();
        return this;
    }


    public void clickLogin() {
        driver.findElement(loginButton).click();
    }


    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }


    public void clickGoogleLogin() {
        driver.findElement(googleLoginButton).click();
    }


    public void clickForgotPassword() {
        driver.findElement(forgotPasswordLink).click();
    }


    public void clickRegister() {
        driver.findElement(registerLink).click();
    }


    public boolean isErrorDisplayed() {
        return !driver.findElements(errorMessage).isEmpty();
    }


    public String getErrorMessage() {
        if (isErrorDisplayed()) {
            return driver.findElement(errorMessage).getText();
        }

        return "";
    }


    public String getPageTitle() {
        return driver.getTitle();
    }


    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}