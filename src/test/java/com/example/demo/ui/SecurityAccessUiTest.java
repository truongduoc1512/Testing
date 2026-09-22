package com.example.demo.ui;

import com.example.demo.ui.pages.LoginPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurityAccessUiTest extends BaseUiTest {

    @Test
    void TC01_anonymousUserAccessAdminProductRedirectsToLogin() {
        driver.get(BASE_URL + "/admin/product");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.urlContains("/admin/login"));

        assertTrue(
                driver.getCurrentUrl().contains("/admin/login"),
                "Khách vãng lai truy cập trang Admin Product phải bị chuyển hướng về trang Login!"
        );
    }

    @Test
    void TC02_anonymousUserAccessAdminUsersRedirectsToLogin() {
        driver.get(BASE_URL + "/admin/users");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.urlContains("/admin/login"));

        assertTrue(
                driver.getCurrentUrl().contains("/admin/login"),
                "Khách vãng lai truy cập trang Admin Users phải bị chuyển hướng về trang Login!"
        );
    }

    @Test
    void TC03_customerUserAccessAdminProductDenied() {
        // Đăng nhập với tài khoản khách hàng thường (ROLE_USER)
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("employee1", "123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin/login")));

        // Khách hàng cố tình truy cập trang quản trị sản phẩm của Admin
        driver.get(BASE_URL + "/admin/product");

        wait.until(ExpectedConditions.or(
                ExpectedConditions.titleContains("403"),
                ExpectedConditions.presenceOfElementLocated(org.openqa.selenium.By.cssSelector(".error-title, .error-page-container")),
                ExpectedConditions.urlContains("/403"),
                ExpectedConditions.urlContains("/admin/login")
        ));

        boolean isAccessDenied = driver.getTitle().contains("403")
                || driver.findElements(org.openqa.selenium.By.cssSelector(".error-title, .error-page-container")).size() > 0
                || driver.getCurrentUrl().contains("/403")
                || driver.getCurrentUrl().contains("/admin/login");

        assertTrue(
                isAccessDenied,
                "Khách hàng thường (ROLE_USER) không được phép truy cập trang quản trị Admin và phải nhận lỗi 403!"
        );
    }

    @Test
    void TC04_customerUserAccessAdminUsersDenied() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("employee1", "123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin/login")));

        // Khách hàng cố tình truy cập trang danh sách người dùng của Admin
        driver.get(BASE_URL + "/admin/users");

        wait.until(ExpectedConditions.or(
                ExpectedConditions.titleContains("403"),
                ExpectedConditions.presenceOfElementLocated(org.openqa.selenium.By.cssSelector(".error-title, .error-page-container")),
                ExpectedConditions.urlContains("/403"),
                ExpectedConditions.urlContains("/admin/login")
        ));

        boolean isAccessDenied = driver.getTitle().contains("403")
                || driver.findElements(org.openqa.selenium.By.cssSelector(".error-title, .error-page-container")).size() > 0
                || driver.getCurrentUrl().contains("/403")
                || driver.getCurrentUrl().contains("/admin/login");

        assertTrue(
                isAccessDenied,
                "Khách hàng thường (ROLE_USER) không được phép xem danh sách người dùng quản trị!"
        );
    }
}
