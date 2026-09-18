# BẢNG MA TRẬN KIỂM THỬ KHỞI ĐỘNG SPRING BOOT CONTEXT & MAIN APPLICATION
**Đường dẫn file mã nguồn:** [SpringShoppingCart2Application.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/main/java/com/example/demo/SpringShoppingCart2Application.java)  
**Đường dẫn file kiểm thử:** [SpringShoppingCart2ApplicationMainTest.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/SpringShoppingCart2ApplicationMainTest.java), [SpringShoppingCart2ApplicationTests.java](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/SpringShoppingCart2ApplicationTests.java)

---

## 1. TỔNG QUAN ĐẶC ĐIỂM KIỂM THỬ TÍCH HỢP
* **Phạm vi kiểm thử:** Khởi động ứng dụng Spring Boot, tải ngữ cảnh `ApplicationContext` và hàm điểm vào `main(String[] args)` của hệ thống.
* **Mục tiêu:** Đảm bảo cấu hình Spring Boot (`@SpringBootApplication`) toàn vẹn, nạp được toàn bộ bean cần thiết và có khả năng khởi động linh hoạt trong chế độ Non-Web/Lazy Context.

---

## 2. MA TRẬN TEST CASES KIỂM THỬ TÍCH HỢP (2 TEST CASES)

| Mã TC | Hàm ở src/main | Hàm kiểm thử ở src/test | Giá trị Test đầu vào | Kịch bản tích hợp / Logic mục tiêu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **TC_ITG_APP_01** | `main(String[])` | `main_startsLazyNonWebContextWithoutCreatingDatabaseBeans()` | Arguments khởi động: `--spring.main.web-application-type=none`, `--spring.main.lazy-initialization=true`, `--spring.flyway.enabled=false` | Kiểm tra khả năng bootstrap của hàm `main`: khởi động context độc lập không cần mở port web hay kết nối trước DB | Context khởi động active (`context.isActive() == true`), các bean nặng (`dataSource`, `sessionFactory`) không bị khởi tạo sớm |
| **TC_ITG_APP_02** | `contextLoads` | `contextLoads()` | Cấu hình Spring Boot mặc định của dự án | Kiểm tra toàn bộ Spring ApplicationContext được nạp đầy đủ các component, bean, cấu hình security và hibernate | Ngữ cảnh ứng dụng nạp thành công không gặp lỗi cấu hình hay xung đột bean |
