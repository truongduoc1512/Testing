# 🌐 KẾ HOẠCH & KỊCH BẢN KIỂM THỬ ĐA TRÌNH DUYỆT (CROSS-BROWSER & MOBILE TEST PLAN) - TASK TEST-25

> **Mã Task Jira:** TEST-25  
> **Tiêu đề Task:** Cross-browser testing  
> **Dự án:** ShoeShop Quality Assurance & Testing  
> **Người thực hiện:** QA Automation Lead  

---

## 📌 1. MỤC TIÊU KIỂM THỬ
Tài liệu này xác định chiến lược, ma trận tương thích và các kịch bản kiểm thử chi tiết cho ứng dụng Web ShoeShop trên nhiều loại trình duyệt (Google Chrome, Mozilla Firefox, Microsoft Edge) và các thiết bị di động (iOS Safari, Android Chrome viewports).

Mục tiêu chính:
1. Đảm bảo giao diện người dùng (UI Layout), hệ thống CSS Grid/Flexbox hiển thị nhất quán trên mọi kích thước màn hình và trình duyệt.
2. Kiểm tra tính năng tương tác (JS Interactivity, Touch Events vs Mouse Hover, Form Input Validation).
3. Đảm bảo hiệu năng tải trang, phông chữ và định dạng hình ảnh (PNG, JPEG, WebP) hiển thị chuẩn xác không bị vỡ bố cục.

---

## 🖥️ 2. DANH SÁCH MÔI TRƯỜNG VÀ TRÌNH DUYỆT KIỂM THỬ

### 2.1. Cấu hình Trình duyệt Desktop & Mobile

| Tên Trình duyệt / Môi trường | Loại thiết bị | User-Agent Identifier | Kích thước Màn hình (Viewport) | Hệ điều hành thử nghiệm |
| :--- | :--- | :--- | :---: | :--- |
| **Google Chrome Desktop** | Desktop | `Chrome/120.0.0.0` | 1920 x 1080 (FHD) | Windows 11 / macOS Sonoma |
| **Mozilla Firefox Desktop** | Desktop | `Firefox/121.0` | 1920 x 1080 (FHD) | Windows 11 / Linux Ubuntu |
| **Microsoft Edge Desktop** | Desktop | `Edg/120.0.0.0` | 1920 x 1080 (FHD) | Windows 11 |
| **Mobile iOS Safari** | Mobile | `iPhone; CPU iPhone OS 17_2 like Mac OS X` | 390 x 844 (iPhone 14/15) | iOS 17.2 |
| **Mobile Android Chrome** | Mobile | `Android 14; Mobile; SM-S918B` | 360 x 800 (Galaxy S23) | Android 14 |

---

## 📊 3. MA TRẬN TƯƠNG THÍCH TRÌNH DUYỆT (COMPATIBILITY MATRIX)

| Hạng mục Kiểm tra (Feature Category) | Desktop Chrome | Desktop Firefox | Desktop Edge | Mobile iOS Safari | Mobile Android Chrome |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **CSS Grid & Flexbox Layout** | PASS | PASS | PASS | PASS | PASS |
| **Custom Web Fonts & Typography** | PASS | PASS | PASS | PASS | PASS |
| **HTML5 Form Input & Validation** | PASS | PASS | PASS | PASS | PASS |
| **JavaScript Async/Await & Fetch API**| PASS | PASS | PASS | PASS | PASS |
| **Touch vs Hover Events** | Mouse Hover | Mouse Hover | Mouse Hover | Touch Event | Touch Event |
| **Responsive Navigation Drawer / Burger Menu** | N/A (Full Bar)| N/A (Full Bar)| N/A (Full Bar)| Drawer Menu | Drawer Menu |
| **Image Loading (YOLOv8 & Product Photos)** | PASS | PASS | PASS | PASS | PASS |

---

## 🧪 4. MA TRẬN KỊCH BẢN KIỂM THỬ CHI TIẾT (TEST CASES MATRIX)

### 4.1. Nhóm Kịch bản Kiểm thử Giao diện & Bố cục (UI/UX Layout & Responsiveness)

| Mã Test Case | Tên Kịch bản | Trình duyệt thử nghiệm | Thao tác thực hiện | Kết quả kỳ vọng (Assertions) |
| :--- | :--- | :--- | :--- | :--- |
| `TC_XB_UI_01` | Hiển thị Banner & Grid Sản phẩm trên Desktop | Chrome, Firefox, Edge | Mở trang chủ ShoeShop (`/`) ở độ phân giải 1920x1080. | Grid sản phẩm hiển thị đúng 4 cột/dòng, khoảng cách margin/padding đồng nhất, không bị tràn viền (overflow). |
| `TC_XB_UI_02` | Hiển thị Responsive Layout trên Di động | iOS Safari, Android Chrome | Mở trang chủ trên Mobile Viewport (390x844 & 360x800). | Grid tự động co về 1 hoặc 2 cột. Header hiển thị nút Burger Menu, không xuất hiện thanh cuộn ngang (horizontal scrollbar). |
| `TC_XB_UI_03` | Hiển thị Font chữ & Icons chuẩn | Tất cả 5 trình duyệt | Kiểm tra phần Header, Footer, Giá tiền và Nút bấm. | Custom Font (Inter/Roboto) nạp thành công, không bị lỗi ô vuông font (missing glyphs). |

---

### 4.2. Nhóm Kịch bản Tương tác & Nghiệp vụ (Interactivity & Core Flows)

| Mã Test Case | Tên Kịch bản | Trình duyệt thử nghiệm | Thao tác thực hiện | Kết quả kỳ vọng (Assertions) |
| :--- | :--- | :--- | :--- | :--- |
| `TC_XB_INT_01` | Thao tác Thêm vào Giỏ hàng | tất cả 5 trình duyệt | Bấm nút "Thêm vào giỏ" tại một sản phẩm bất kỳ. | Badge số lượng giỏ hàng trên Header cập nhật tức thì (`+1`), popup Toast thông báo thành công hiển thị rõ ràng. |
| `TC_XB_INT_02` | Thao tác Vuốt Touch Scroll & Click trên Mobile | iOS Safari, Android Chrome | Dùng thao tác vuốt cảm ứng trên màn hình danh sách ảnh sản phẩm. | Slider chuyển ảnh mượt mà, bấm chạm nút không bị độ trễ (delay > 300ms). |
| `TC_XB_INT_03` | Nhập liệu Form Tìm kiếm & Bàn phím Di động | iOS Safari, Android Chrome | Chạm vào thanh Search Input trên di động. | Bàn phím ảo xuất hiện không che khuất nút "Tìm kiếm", nút Enter trên bàn phím di động thực thi tìm kiếm thành công. |

---

### 4.3. Nhóm Kịch bản Hiệu năng & Lưu trữ Session (Performance & Session Management)

| Mã Test Case | Tên Kịch bản | Trình duyệt thử nghiệm | Thao tác thực hiện | Kết quả kỳ vọng (Assertions) |
| :--- | :--- | :--- | :--- | :--- |
| `TC_XB_PERF_01` | Đồng bộ Giỏ hàng & Session Cookies | Chrome, Firefox, Edge | Thêm sản phẩm vào giỏ, sau đó reload trang (`F5` / `Cmd+R`). | Giỏ hàng và thông tin Session giữ nguyên không bị mất dữ liệu (Persistence). |
| `TC_XB_PERF_02` | Tốc độ Tải trang (Page Load Speed) | Tất cả 5 trình duyệt | Đo thời gian `DOMContentLoaded` khi truy cập trang danh sách sản phẩm. | Thời gian tải trang hoàn tất trong `< 2.0 giây` trên mọi trình duyệt. |

---

## 💻 5. HƯỚNG DẪN KHỞI CHẠY BỘ KIỂM THỬ TỰ ĐỘNG (CROSS-BROWSER SUITE)

Dự án trang bị sẵn module kiểm thử tự động đa trình duyệt trong file: [`scripts/test_cross_browser.py`](file:///c:/shoeshopp/Testing/scripts/test_cross_browser.py).

### Lệnh chạy kiểm thử:
```powershell
python -m unittest scripts/test_cross_browser.py
```

Bộ kiểm thử sẽ tự động mô phỏng 5 profile trình duyệt (Chrome, Firefox, Edge, Mobile Safari, Android Chrome), kiểm tra tính tương thích của User-Agent, Viewport responsivity, Touch Capabilities và xuất báo cáo tổng hợp chi tiết (Test Execution Summary).
