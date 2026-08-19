package com.example.demo.ui;

import com.example.demo.ui.pages.LoginPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticationUiTest extends BaseUiTest {

    @Test
    void TC01_customerLoginWithValidCredentials() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("employee1", "123456");

        assertFalse(
                loginPage.getCurrentUrl().contains("/admin/login?error"),
                "Customer login should be successful"
        );
    }

    @Test
    void TC02_adminLoginWithValidCredentials() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("manager1", "123456");

        assertFalse(
                loginPage.getCurrentUrl().contains("/admin/login?error"),
                "Admin login should be successful"
        );
    }

    @Test
    void TC03_invalidLoginShouldDisplayError() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("employee1", "wrongpassword");

        assertTrue(
                loginPage.isErrorDisplayed(),
                "Invalid login should display an error message"
        );
    }

    @Test
    void TC04_loginWithEmptyUsername() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.enterPassword("123456");
        loginPage.clickLogin();

        assertTrue(
                driver.getCurrentUrl().contains("/admin/login"),
                "User should remain on login page"
        );
    }

    @Test
    void TC05_loginWithEmptyPassword() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.enterUsername("employee1");
        loginPage.clickLogin();

        assertTrue(
                driver.getCurrentUrl().contains("/admin/login"),
                "User should remain on login page"
        );
    }

    @Test
    void TC06_customerRegistrationNavigation() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.clickRegister();

        assertTrue(
                loginPage.getCurrentUrl().contains("/register"),
                "Clicking register should navigate to the registration page"
        );
    }
}