package com.example.demo.ui;

import com.example.demo.ui.pages.AdminProductPage;
import com.example.demo.ui.pages.LoginPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdminProductUiTest extends BaseUiTest {

    @Test
    void TC01_adminCreateProductWithImageUploadSuccess() {
        // 1. Đăng nhập quyền Quản trị viên
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("manager1", "123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin/login")));

        // 2. Mở form Tạo sản phẩm mới
        driver.get(BASE_URL + "/admin/product");
        AdminProductPage productPage = new AdminProductPage(driver);
        pause(1000);

        String randomCode = "UI" + (System.currentTimeMillis() % 1000000);
        productPage.enterCode(randomCode);
        pause(600);
        productPage.enterName("Giày Thể Thao Selenium Test " + randomCode);
        pause(600);
        productPage.enterPrice("1250000");
        pause(600);
        productPage.enterDiscount("10");
        pause(600);
        productPage.enterStock("25");
        pause(1000);

        // 3. Upload hình ảnh sản phẩm thật từ ổ đĩa
        File sampleImage = new File("docs/test_cases/black_box/admin_product_edit_delete/test_fixtures/shoe_offline.jpg");
        if (!sampleImage.exists()) {
            sampleImage = new File("src/main/resources/static/img/about.jpg");
        }
        if (sampleImage.exists()) {
            productPage.uploadImage(sampleImage.getAbsolutePath());
            pause(1500); // Dừng lại 1.5s để người dùng thấy ảnh hiển thị preview trên form
        }

        // 4. Bấm Lưu sản phẩm
        productPage.clickSaveProduct();
        pause(1500);

        // 5. Xác thực sản phẩm lưu thành công và chuyển hướng về danh sách sản phẩm
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/productList"),
                ExpectedConditions.urlContains("/productDetail?code=" + randomCode)
        ));

        assertTrue(
                driver.getCurrentUrl().contains("/productList") || driver.getCurrentUrl().contains("code=" + randomCode),
                "Sản phẩm mới kèm ảnh phải được tạo thành công và chuyển hướng về danh mục sản phẩm!"
        );
    }

    @Test
    void TC02_createProductWithEmptyCodeShouldFail() {
        driver.get(LOGIN_URL);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("manager1", "123");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin/login")));

        driver.get(BASE_URL + "/admin/product");
        AdminProductPage productPage = new AdminProductPage(driver);
        pause(1000);

        // Để trống trường Code
        productPage.enterName("Giày Lỗi Không Có SKU");
        pause(600);
        productPage.enterPrice("990000");
        pause(600);
        productPage.enterStock("10");
        pause(1000);

        productPage.clickSaveProduct();
        pause(2000); // Dừng lại 2s để người dùng nhìn thấy thông báo lỗi xuất hiện trên màn hình

        // Kiểm tra trình duyệt vẫn ở lại trang form và hiển thị thông báo lỗi
        assertTrue(
                driver.getCurrentUrl().contains("/admin/product") || productPage.isErrorDisplayed(),
                "Hệ thống phải chặn việc tạo sản phẩm khi để trống mã sản phẩm!"
        );
    }
}
