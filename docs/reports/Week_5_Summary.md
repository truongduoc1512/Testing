# 📈 Week 5 Summary: Theory Alignment & Documentation

**Thành viên thực hiện:** Thịnh, Lĩnh, Phương, Được (Leader)  
**Mục tiêu Sprint:** Chuẩn hóa toàn bộ hệ thống code và tài liệu kiểm thử dự án ShoeShop bám sát 100% giáo trình lý thuyết hàn lâm (Black-box, White-box, Experience-based Testing).  
**Nhánh tích hợp chính:** `week/week-5-theory-alignment`

---

## 🚀 1. Tổng quan các kỹ thuật lý thuyết đã áp dụng

Trong Sprint 5, nhóm không phát triển thêm tính năng mới mà tập trung vào việc **nâng cấp học thuật** cho các Task đã hoàn thành ở Sprint 3 và Sprint 4. Mọi kỹ thuật do giảng viên yêu cầu đều được ánh xạ trực tiếp vào thực tiễn dự án:

### Phân hoạch lớp tương đương (Equivalence Partitioning) & Test Case chuẩn
- **Người thực hiện:** Lĩnh
- **Cập nhật:** Đã tạo tài liệu `docs/TEST-22.md` và `docs/TEST-23.md`.
- **Thực tiễn:** Kẻ bảng phân hoạch lớp hợp lệ / không hợp lệ cho form Authentication (Username/Password) và Checkout (Cart Status). Chuyển đổi kịch bản Selenium thành Bảng đặc tả Test Case chuẩn 7 cột.

### Phân tích giá trị biên (Boundary Value Analysis - BVA)
- **Người thực hiện:** Phương
- **Cập nhật:** Sửa file Java `CustomerFormValidatorTest.java`.
- **Thực tiễn:** Code các bài test tự động áp dụng chính xác công thức toán học **Standard BVA (4n+1)** và **Robustness BVA (6n+1)** bằng thư viện JUnit `@ParameterizedTest`.

### Kỹ thuật Worst-Case & Robust Worst-Case Testing ($5^n$)
- **Người thực hiện:** Thịnh
- **Cập nhật:** Sửa script Python `test_search_pagination_api.py` (TEST-20).
- **Thực tiễn:** Code 2 vòng lặp lồng nhau sinh ra ma trận $5^2 = 25$ test cases để vét cạn tổ hợp giá trị biên tệ nhất của 2 tham số `page` và `size`, đảm bảo API không bị crash (HTTP 500).

### Bảng chuyển đổi trạng thái (State Transition Testing)
- **Người thực hiện:** Thịnh
- **Cập nhật:** Bổ sung vào tài liệu `docs/TEST-19.md`.
- **Thực tiễn:** Thiết kế Bảng chuyển đổi trạng thái (Start State -> Event -> End State) cho cỗ máy trạng thái Bug Lifecycle Framework gồm 6 bước của dự án.

### Kỹ thuật Bảng quyết định (Decision Table Testing)
- **Người thực hiện:** Được (Leader)
- **Cập nhật:** Tạo file phân tích mới `docs/Blackbox_Decision_Table.md`.
- **Thực tiễn:** Xây dựng Bảng quyết định 8 Rules ($2^3$) cho logic thanh toán và áp dụng Voucher, sau đó rút gọn bảng cực tiểu xuống còn 4 Test Cases cốt lõi giúp bao phủ 100% logic IF-ELSE phức tạp của nghiệp vụ.

### Kiểm thử luồng điều khiển (CFG) & Độ phức tạp Cyclomatic $V(G)$
- **Người thực hiện:** Phương
- **Cập nhật:** Bổ sung vào tài liệu `docs/TEST-17.md` (White-box).
- **Thực tiễn:** Lấy hàm `validate()` của form validator làm mẫu, vẽ Đồ thị luồng điều khiển (CFG) bằng Mermaid (12 Node, 16 Cạnh), tính toán độ phức tạp $V(G) = E - N + 2 = 6$, và lập bảng map 6 đường đi cơ sở (Basis paths) tới các hàm test Java.

### Kỹ thuật kiểm thử dựa trên kinh nghiệm (Experience-Based Testing)
- **Người thực hiện:** Lĩnh
- **Cập nhật:** Bổ sung vào báo cáo `docs/test-results/TEST-18.md`.
- **Thực tiễn:** Đưa các khái niệm **Error Guessing** (Đoán lỗi để phá AI bằng file `.MOV`) và **Exploratory Testing** (Thăm dò ngưỡng blur_score, độ trễ và điều kiện ánh sáng) vào báo cáo Manual Test hệ thống AI Computer Vision.

---

## 🎯 2. Đánh giá chất lượng và Hoàn thành Sprint
Tất cả 4 thành viên đều hoàn thành 100% nhiệm vụ xuất sắc và đúng hạn. Quá trình làm việc nhóm tuân thủ tuyệt đối quy trình Git Flow chuyên nghiệp: chia nhỏ các nhánh độc lập để viết lý thuyết và merge gọn gàng vào nhánh `week-5` mà không xảy ra conflict.

**Kết quả cuối cùng:** 
Dự án Kiểm thử phần mềm ShoeShop của nhóm giờ đây không chỉ siêu việt về mặt tự động hóa (Selenium, Postman CLI, JaCoCo, CI/CD, Python) mà bộ tài liệu đi kèm đã trở nên cực kỳ hàn lâm và đạt tiêu chuẩn điểm tuyệt đối của bộ môn. Mọi câu chữ trong giáo trình của giảng viên đều được nhóm biến thành mã code và minh chứng thực tế!

Dự án đã sẵn sàng để nghiệm thu cuối kỳ. 🏆
