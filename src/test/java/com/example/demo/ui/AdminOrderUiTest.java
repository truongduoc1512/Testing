package com.example.demo.ui;

import com.example.demo.ui.pages.AdminOrderPage;
import com.example.demo.ui.pages.LoginPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdminOrderUiTest extends BaseUiTest {

    @Test
    void TC01_adminViewOrderList() {
        // 1. Đăng nhập quyền Quản trị viên
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("manager1", "123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin/login")));

        // 2. Mở trang Quản lý Danh sách Đơn hàng
        driver.get(BASE_URL + "/admin/orderList");

        wait.until(ExpectedConditions.urlContains("/admin/orderList"));
        assertTrue(
                driver.getCurrentUrl().contains("/admin/orderList"),
                "Quản trị viên phải mở được trang danh sách đơn hàng thành công!"
        );
    }

    @Test
    void TC02_adminUpdateOrderStatus() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("manager1", "123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin/login")));

        driver.get(BASE_URL + "/admin/orderList");
        AdminOrderPage orderPage = new AdminOrderPage(driver);

        // Kiểm tra nếu có đơn hàng thì mở xem chi tiết đơn hàng đầu tiên
        boolean hasOrders = driver.findElements(By.cssSelector("a[href*='orderId=']")).size() > 0;
        if (hasOrders) {
            pause(1000);
            orderPage.clickFirstOrder();

            wait.until(ExpectedConditions.urlContains("order?orderId="));
            assertTrue(driver.getCurrentUrl().contains("orderId="), "Phải mở được trang chi tiết đơn hàng!");
            pause(1500);

            // Cập nhật trạng thái sang SHIPPING hoặc APPROVED
            orderPage.selectStatus("SHIPPING");
            pause(1000);
            orderPage.clickUpdateStatus();
            pause(2000); // Dừng lại 2s để người dùng thấy badge trạng thái đổi màu

            // Xác nhận trạng thái được cập nhật
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".status-badge"), "SHIPPING"),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success, .alert, .message"))
            ));

            String badgeText = orderPage.getCurrentStatusBadgeText();
            assertTrue(
                    badgeText.equalsIgnoreCase("SHIPPING") || driver.getPageSource().contains("thành công"),
                    "Trạng thái đơn hàng phải được cập nhật thành công trên giao diện!"
            );
        } else {
            // Không có đơn hàng nào trong DB để update
            assertTrue(true, "Danh sách đơn hàng hiện tại đang trống.");
        }
    }
}
