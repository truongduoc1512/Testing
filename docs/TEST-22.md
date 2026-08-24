# TEST-22 – Authentication UI Automation

## 1. Equivalence Partitioning

| Field | Lớp tương đương | Loại | Dữ liệu đại diện | Ý nghĩa |
|---|---|---|---|---|
| Username | Username hợp lệ | Valid | `employee1` | Tài khoản khách hàng hợp lệ |
| Username | Username admin hợp lệ | Valid | `manager1` | Tài khoản quản trị hợp lệ |
| Username | Username bỏ trống | Invalid | `""` | Không nhập username |
| Username | Username không tồn tại/sai | Invalid | `unknown_user` | Username không tồn tại |
| Username | Username sai định dạng | Invalid | `user@###` | Dữ liệu không phù hợp quy tắc username |
| Password | Password hợp lệ | Valid | `123456` | Mật khẩu đúng |
| Password | Password bỏ trống | Invalid | `""` | Không nhập password |
| Password | Password sai | Invalid | `wrongpassword` | Username đúng nhưng password sai |
| Password | Password sai định dạng | Invalid | `12` | Mật khẩu không đạt yêu cầu độ dài/định dạng |


## 2. Formal Test Cases – Authentication
| TC ID | Mô tả | Tiền điều kiện | Các bước | Dữ liệu đầu vào | Kết quả mong đợi | Trạng thái |
|---|---|---|---|---|---|---|
| TC-22-01 | Positive Test – Customer Login với thông tin hợp lệ | Hệ thống đang chạy; tài khoản `employee1` tồn tại | 1. Mở trang Login. 2. Nhập username. 3. Nhập password. 4. Click Login. | Username: `employee1`; Password: `123456` | Đăng nhập thành công, không xuất hiện lỗi đăng nhập. | Pass/Fail |
| TC-22-02 | Positive Test – Admin Login với thông tin hợp lệ | Hệ thống đang chạy; tài khoản `manager1` tồn tại | 1. Mở trang Login. 2. Nhập username. 3. Nhập password. 4. Click Login. | Username: `manager1`; Password: `123456` | Đăng nhập admin thành công, không xuất hiện lỗi đăng nhập. | Pass/Fail |
| TC-22-03 | Negative Test – Login với password sai | Tài khoản `employee1` tồn tại | 1. Mở trang Login. 2. Nhập username. 3. Nhập password sai. 4. Click Login. | Username: `employee1`; Password: `wrongpassword` | Hệ thống từ chối đăng nhập và hiển thị thông báo lỗi. | Pass/Fail |
| TC-22-04 | Negative Test – Login bỏ trống Username | Hệ thống đang ở trang Login | 1. Mở trang Login. 2. Không nhập Username. 3. Nhập Password. 4. Click Login. | Username: `""`; Password: `123456` | Không đăng nhập; người dùng vẫn ở trang Login. | Pass/Fail |
| TC-22-05 | Negative Test – Login bỏ trống Password | Hệ thống đang ở trang Login | 1. Mở trang Login. 2. Nhập Username. 3. Không nhập Password. 4. Click Login. | Username: `employee1`; Password: `""` | Không đăng nhập; người dùng vẫn ở trang Login. | Pass/Fail |
| TC-22-06 | Positive Test – Điều hướng sang Register | Hệ thống đang ở trang Login | 1. Mở trang Login. 2. Click Register/Đăng ký. | Không có | Hệ thống chuyển đến trang `/register`. | Pass/Fail |

## 3. Phân loại Test Case

| Loại | Test Case |
|---|---|
| Positive Test | TC-22-01 – Customer Login |
| Positive Test | TC-22-02 – Admin Login |
| Negative Test | TC-22-03 – Password sai |
| Negative Test | TC-22-04 – Username rỗng |
| Negative Test | TC-22-05 – Password rỗng |
| Positive Test | TC-22-06 – Register navigation |