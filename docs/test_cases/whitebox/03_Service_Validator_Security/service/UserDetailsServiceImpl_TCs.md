# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `UserDetailsServiceImpl`
## Tầng: Service Layer | Phân hệ: Xác thực & Nạp Quyền hạn Người dùng (Spring Security)
* **Tổng số Test Case:** **3 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `UserDetailsServiceImpl.java` và các ca kiểm thử trong `UserDetailsServiceImplTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `UserDetailsServiceImpl.java`

Lớp `UserDetailsServiceImpl` triển khai interface `UserDetailsService` chuẩn của Spring Security để nạp người dùng từ CSDL vào Security Context:

| STT | Tên hàm trong `UserDetailsServiceImpl.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `loadUserByUsername(String username)` | Truy vấn Account, nạp GrantedAuthority và khởi tạo đối tượng `UserDetails` | **3** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 3 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_UDS_01` | `loadUserByUsername` | `loadUserByUsername_throwsForUnknownAccount` | `username = "missing"`<br>Mock: `accountDAO.findAccount("missing") = null` | Nhánh `if (account == null)` | Ném ngoại lệ `UsernameNotFoundException` chứa nội dung `"missing"` |
| `TC_UDS_02` | `loadUserByUsername` | `loadUserByUsername_addsRolePrefixForLegacyRoleValue` | `username = "alice"`, `userRole = "USER"` (chưa có prefix `ROLE_`)<br>Mock: `findAccount("alice")` trả về Account | Nhánh `normalizeRole`: chuỗi chưa bắt đầu bằng `ROLE_` ➔ gắn thêm prefix `ROLE_` | Trả về `UserDetails` với quyền duy nhất là `"ROLE_USER"` |
| `TC_UDS_03` | `loadUserByUsername` | `loadUserByUsername_preservesAlreadyPrefixedRoleValue` | `username = "admin"`, `userRole = "ROLE_ADMIN"` (đã có prefix `ROLE_`)<br>Mock: `findAccount("admin")` trả về Account | Nhánh `normalizeRole`: chuỗi đã bắt đầu bằng `ROLE_` ➔ giữ nguyên | Trả về `UserDetails` với quyền duy nhất là `"ROLE_ADMIN"` |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.service.UserDetailsServiceImpl`
* **Statement Coverage (Instructions):** **100.0%** (81/81 instructions)
* **Branch Coverage (Branches):** **100.0%** (4/4 branches)
* **Line Coverage:** **100.0%** (16/16 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, đảm bảo định danh người dùng và cơ chế chuẩn hóa tiền tố quyền (Role Prefix) luôn hoạt động ổn định.
