# MA TRẬN TRUY XUẤT YÊU CẦU (REQUIREMENT TRACEABILITY MATRIX - RTM)

**Dự án:** ShoeShop Testing & Quality Assurance System  
**Mã tài liệu:** RTM-SHOESHOP-v1.0  
**Người thực hiện:** Thịnh  
**Ngày cập nhật:** 03/08/2026  
**Dự án Jira:** ShoeShop Testing & Development (TEST)  

---

## 1. MỤC TIÊU CỦA MA TRẬN RTM
Tài liệu Ma trận truy xuất yêu cầu (RTM) được xây dựng nhằm theo dõi, đối chiếu toàn bộ các Yêu cầu Chức năng (Functional Requirements) và Phi chức năng của dự án ShoeShop với các Test Case tương ứng. RTM đảm bảo 100% yêu cầu hệ thống được kiểm thử đầy đủ (Test Coverage), không bỏ sót tính năng và hỗ trợ quản lý vết thay đổi trong suốt vòng đời dự án.

---

## 2. BẢNG MA TRẬN TRUY XUẤT YÊU CẦU CHI TIẾT (RTM TABLE)

| Mã Yêu cầu (REQ ID) | Mô tả Yêu cầu Chức năng | Phân hệ (Module) | Mức độ Ưu tiên | Mã Test Case Dự kiến (TC ID) | Phương pháp Kiểm thử | Người phụ trách | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **REQ-AUTH-01** | Đăng ký, Đăng nhập, Đăng xuất và Quản lý phiên làm việc với JWT Token | Xác thực (Auth) | High | TC_AUTH_01 | API / UI | Leader | Planned |
| **REQ-AUTH-02** | Khôi phục mật khẩu, Quên mật khẩu qua Email OTP | Xác thực (Auth) | High | TC_AUTH_02 | API / UI | Leader | Planned |
| **REQ-ADDR-01** | Thêm, sửa, xóa, thiết lập địa chỉ giao hàng mặc định cho tài khoản | Sổ địa chỉ (Address) | Medium | TC_ADDR_01 | API / UI | Phương | Planned |
| **REQ-PROD-01** | Xem danh sách sản phẩm, lọc theo danh mục, giá, kích thước và tìm kiếm | Sản phẩm (Product) | High | TC_PROD_01 | API / UI | Lĩnh | Planned |
| **REQ-AI-01** | Cổng AI Gate hỗ trợ tư vấn size giày và trả lời câu hỏi tự động qua hình ảnh/text | Cổng AI Gate | Medium | TC_AI_01 | API / Unit | Thịnh | Planned |
| **REQ-CART-01** | Thêm sản phẩm vào giỏ, cập nhật số lượng, xóa sản phẩm và tính tổng tiền | Giỏ hàng (Cart) | High | TC_CART_01 | API / UI | Lĩnh | Planned |
| **REQ-ORDER-01** | Đặt hàng, chọn phương thức thanh toán, áp mã giảm giá và theo dõi đơn | Đơn hàng (Order) | High | TC_ORDER_01 | API / UI | Phương | Planned |
| **REQ-VOUCH-01** | Quản lý, áp dụng mã voucher khuyến mãi hợp lệ vào giỏ hàng/đơn hàng | Khuyến mãi (Voucher) | Medium | TC_VOUCH_01 | API / Unit | Thịnh | Planned |
| **REQ-SEC-01** | Đảm bảo an toàn thông tin, chống SQL Injection, XSS và bảo mật API Token | Bảo mật (Security) | High | TC_SEC_01 | Security | Leader | Planned |
| **REQ-PERF-01** | Đảm bảo hệ thống chịu tải tốt trong giờ cao điểm và phản hồi API < 500ms | Hiệu năng (Performance) | Medium | TC_PERF_01 | Load | Thịnh | Planned |

---

## 3. THỐNG KÊ TỶ LỆ BAO PHỦ YÊU CẦU (COVERAGE SUMMARY)

* **Tổng số Yêu cầu Chức năng & Hệ thống:** 10 Yêu cầu.
* **Số Yêu cầu đã được phân công Test Case:** 10 / 10 (Đạt **100%**).
* **Thống kê theo Phương pháp Kiểm thử:**
  * **API Automation Test:** 8 REQs
  * **Unit & Integration Test:** 2 REQs
  * **Selenium UI Automation:** 6 REQs
  * **Manual & Bug Lifecycle Test:** 10 REQs
  * **Security & Performance Test:** 2 REQs
  
