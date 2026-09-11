# 📊 BÁO CÁO TỔNG HỢP TIẾN ĐỘ - TUẦN 5
> **Dự án:** ShoeShop Testing & Quality Assurance System  
> **Sprint Jira:** Sprint 5 — Academic Theory Alignment & Testing Formalization  
> **Thời gian:** 24/08/2026 – 31/08/2026  
> **Người tổng hợp (Leader):** Trương Hoài Được  
> **Nhánh tích hợp chính:** `week/week-5-theory-alignment` ➔ `develop`  

---

## 🎯 1. MỤC TIÊU SPRINT (SPRINT OBJECTIVES)
- [x] **Mục tiêu 1:** Chuẩn hóa toàn bộ hệ thống kiểm thử hộp đen (Black-box Testing) bám sát 100% giáo trình lý thuyết kiểm thử phần mềm quốc tế (ISTQB & IEEE).
- [x] **Mục tiêu 2:** Triển khai kỹ thuật Phân tích giá trị biên chuẩn tắc (Standard BVA $4n+1$) và Giá trị biên mở rộng (Robustness BVA $6n+1$) trực tiếp vào mã nguồn Unit Test bằng JUnit 5 `@ParameterizedTest`.
- [x] **Mục tiêu 3:** Ứng dụng kỹ thuật kiểm thử tổ hợp biên xấu nhất (Worst-Case & Robust Worst-Case $5^n$) trên các API phân trang và tìm kiếm sản phẩm.
- [x] **Mục tiêu 4:** Thiết kế Bảng quyết định (Decision Table Testing) 8 quy tắc ($2^3$) cho phân hệ Checkout & Voucher, sau đó tối ưu rút gọn bảng cực tiểu xuống 4 ca kiểm thử thiết yếu.
- [x] **Mục tiêu 5:** Thiết kế Bảng chuyển đổi trạng thái (State Transition Testing) đầy đủ các cặp Trạng thái xuất phát ➔ Sự kiện ➔ Trạng thái kết thúc cho Vòng đời Bug và Vòng đời Đơn hàng.
- [x] **Mục tiêu 6:** Chuẩn hóa Kiểm thử Hộp trắng (White-box Testing): Xây dựng Đồ thị luồng điều khiển (Control Flow Graph - CFG) bằng Mermaid, tính toán thủ công Độ phức tạp Cyclomatic $V(G)$ và lập ma trận ánh xạ Đường đi cơ sở (Basis Paths) đối chiếu với JaCoCo.
- [x] **Mục tiêu 7:** Tài liệu hóa các kỹ thuật Kiểm thử dựa trên kinh nghiệm (Experience-Based Testing: Error Guessing & Exploratory Testing) ứng dụng trên phân hệ AI Computer Vision.

---

## 📋 2. BẢNG TỔNG HỢP THỰC THI TASK (JIRA & GITHUB)

| Mã Task Jira | Tên công việc (Summary) | Người thực hiện | Điểm SP | Trạng thái | Pull Request / Nhánh Git | Phân loại |
| :--- | :--- | :--- | :---: | :---: | :--- | :---: |
| `TEST-33` | Test review edit time limit (EP & BVA) | Hoàng Phương | 5 | ✅ Done | [PR test/w5-TEST-33-test-review-edit-time-limit](https://github.com/truongduoc1512/Testing/tree/test/w5-TEST-33-test-review-edit-time-limit) | `test` |
| `TEST-34` | Prepare performance summary | Ngọc Thịnh | 3 | ✅ Done | [PR test/w5-TEST-34-prepare-performance-summary](https://github.com/truongduoc1512/Testing/tree/test/w5-TEST-34-prepare-performance-summary) | `docs` |
| `TEST-35` | Standardize BVA ($4n+1$, $6n+1$, $5^n$) & CFG | Hoàng Phương | 8 | ✅ Done | [PR test/update-bva-cfg-implementation](https://github.com/truongduoc1512/Testing/tree/test/update-bva-cfg-implementation) | `test` |
| `TEST-36` | Formalize Decision Table for Checkout Voucher | Hoài Được | 5 | ✅ Done | [PR docs/update-decision-table-checkout](https://github.com/truongduoc1512/Testing/tree/docs/update-decision-table-checkout) | `docs` |
| `TEST-37` | Formalize EP, BVA & State Transition for 9 modules | Lĩnh | 8 | ✅ Done | [PR docs/update-formal-test-specs](https://github.com/truongduoc1512/Testing/tree/docs/update-formal-test-specs) | `docs` |
| `TEST-38` | Submit Week 5 summary report & merge branch | Tất cả | 2 | ✅ Done | [PR week/week-5-theory-alignment](https://github.com/truongduoc1512/Testing/tree/week/week-5-theory-alignment) | `docs` |

---

## 📈 3. THỐNG KÊ CHỈ SỐ SPRINT (METRICS & KPIS)

| Chỉ số đo lường (Metric) | Kế hoạch cam kết | Kết quả thực tế | Tỷ lệ hoàn thành | Đánh giá |
| :--- | :---: | :---: | :---: | :---: |
| **Tổng số Task Jira cam kết** | 6 tasks | 6 tasks | **100%** | 🟢 Đạt chuẩn |
| **Tổng Story Points hoàn thành** | 31 SP | 31 SP | **100%** | 🟢 Đạt chuẩn |
| **Số phân hệ chuẩn hóa ma trận ISTQB** | 9 phân hệ | **9 phân hệ cốt lõi (100%)** | **100%** | 🟢 Toàn diện |
| **Kỹ thuật BVA triển khai bằng code** | Standard $4n+1$ | **Standard ($4n+1$), Robustness ($6n+1$), Worst-case ($5^n$)** | **Vượt chuẩn** | 🟢 Rất sâu sắc |
| **Số Rule trong Bảng quyết định (Decision Table)**| $2^3 = 8$ Rules | **8 Rules gốc ➔ Tối ưu 4 ca kiểm thử rút gọn** | **100%** | 🟢 Chuẩn mực |
| **Đồ thị luồng điều khiển (CFG)** | 1 mô hình CFG | **12 Node, 16 Cạnh, $V(G) = 6$, 6 Basis Paths** | **100%** | 🟢 Chính xác |
| **Độ bao phủ White-box duy trì (JaCoCo)** | > 70% | **Line: 99.85%, Branch: 99.33%** | **Duy trì đỉnh cao** | 🟢 Bền vững |

---

## 🔍 4. CHI TIẾT SẢN PHẨM BÀN GIAO & MINH CHỨNG THEO THÀNH VIÊN

### 1. Trương Hoài Được (Leader) — Thực hiện `TEST-36`, `Quản lý chuẩn hóa học thuật`
- **Sản phẩm bàn giao:**
  * Xây dựng tài liệu chuyên sâu [`docs/Blackbox_Decision_Table.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/Blackbox_Decision_Table.md) phân tích Bảng quyết định (Decision Table) cho luồng Checkout áp dụng Voucher:
    - Định nghĩa 3 điều kiện (Conditions): $C_1$ (Mã hợp lệ), $C_2$ (Còn hạn sử dụng), $C_3$ (Tổng giá trị đơn hàng $\ge 500.000$ VNĐ).
    - Lập ma trận đầy đủ $2^3 = 8$ Quy tắc kết hợp với các Hành động (Actions): Áp dụng chiết khấu, Báo lỗi mã không tồn tại, Báo lỗi hết hạn, Báo lỗi chưa đủ giá trị tối thiểu.
    - Áp dụng nguyên tắc logic "Don't Care" (Rút gọn điều kiện phụ thuộc khi mã không hợp lệ), cô đọng từ 8 quy tắc xuống còn **4 Test Cases cốt lõi** bao phủ trọn vẹn logic nhánh rẽ.
  * Chỉ đạo rà soát chéo chất lượng học thuật trên toàn bộ 9 tài liệu đặc tả test case trong [`docs/test_cases/`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/).

### 2. Hoàng Phương — Thực hiện `TEST-33`, `TEST-35`
- **Sản phẩm bàn giao:**
  * **Hiện thực hóa BVA vào mã nguồn Java:** Cập nhật [`CustomerFormValidatorTest.java`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/validator/CustomerFormValidatorTest.java) và [`ProductReviewDAOTest.java`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/src/test/java/com/example/demo/dao/ProductReviewDAOTest.java) áp dụng công thức toán học:
    - **Standard BVA ($4n+1$):** Kiểm tra các điểm $\{min, min+, nom, max-, max\}$.
    - **Robustness BVA ($6n+1$):** Mở rộng kiểm tra ngoài biên $\{min-, max+\}$ với các giá trị biên âm, ký tự vượt ngưỡng tối đa.
  * **Xây dựng Đồ thị luồng điều khiển (CFG) & Cyclomatic Complexity:** Cập nhật tài liệu [`docs/TEST-17.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-17.md), phân tích hàm kiểm tra tính hợp lệ `validate()`:
    - Vẽ sơ đồ CFG dạng Mermaid gồm 12 Nodes và 16 Edges.
    - Tính toán độ phức tạp luồng: $V(G) = E - N + 2P = 16 - 12 + 2(1) = 6$ (hoặc số vùng đóng $Predicate + 1 = 5 + 1 = 6$).
    - Xác định tập 6 đường đi độc lập (Independent Basis Paths) và lập bảng ánh xạ từng đường đi tới phương thức test Java cụ thể.
  * Hoàn thành kiểm thử giới hạn thời gian chỉnh sửa đánh giá 5 phút (Edit Time Window) trong `TEST-33`.

### 3. Lĩnh — Thực hiện `TEST-37`
- **Sản phẩm bàn giao:**
  * **Chuẩn hóa 9 bộ tài liệu đặc tả kiểm thử theo chuẩn ISTQB** trong thư mục [`docs/test_cases/`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test_cases/):
    1. `01_Authentication.md`: Phân hoạch lớp tương đương (EP) & BVA cho Username, Password, Email.
    2. `02_Product_Search_Pagination.md`: Bảng BVA Worst-case $5^2$ cho `page` và `size`.
    3. `03_Shopping_Cart.md`: BVA số lượng sản phẩm, Session Cart.
    4. `04_Vouchers.md`: Bảng quyết định và BVA giá trị chiết khấu / đơn hàng tối thiểu.
    5. `05_Order_Placement_Payment.md`: Bảng quyết định thanh toán COD và trạng thái đơn hàng.
    6. `06_Review_Rating.md`: BVA điểm số đánh giá 1–5 sao, độ dài bình luận (1–2000 ký tự) và cửa sổ thời gian 5 phút.
    7. `07_Cancel_Return_Order.md`: Bảng chuyển đổi trạng thái hủy đơn và trả hàng.
    8. `08_Admin_Management.md`: Phân quyền RBAC, Bảng chuyển đổi trạng thái duyệt đơn.
    9. `09_AI_Quality_Gate.md`: Kiểm thử dựa trên kinh nghiệm cho AI.
  * Bổ sung kỹ thuật **Experience-Based Testing** vào [`docs/test-results/TEST-18.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/test-results/TEST-18.md): Đưa phương pháp Đoán lỗi (Error Guessing) và Thăm dò (Exploratory Testing) vào kiểm thử Cổng AI YOLOv8.

### 4. Ngọc Thịnh — Thực hiện `TEST-34`, `Hỗ trợ Worst-Case & State Transition`
- **Sản phẩm bàn giao:**
  * Hiện thực hóa kỹ thuật **Worst-Case Testing ($5^n$)**: Viết script Python [`scripts/test_search_pagination_api.py`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/scripts/test_search_pagination_api.py) sử dụng 2 vòng lặp lồng nhau sinh $5^2 = 25$ ca kiểm thử tổ hợp biên xấu nhất của 2 biến `page` và `size`, kiểm chứng API hoàn toàn không bị lỗi 500 Crash.
  * Bổ sung Bảng chuyển đổi trạng thái (State Transition Table) vào tài liệu [`docs/TEST-19.md`](file:///i:/Subjects/CloudComputing/project/shoeshop-testing/docs/TEST-19.md) cho Vòng đời Bug gồm 6 trạng thái.
  * Lập báo cáo tổng hợp hiệu năng sơ bộ (`TEST-34`) làm tiền đề cho việc xây dựng kịch bản kiểm thử tải ở Tuần 6.

---

## ⚠️ 5. VẤN ĐỀ PHÁT SINH & GIẢI PHÁP XỬ LÝ (BLOCKERS & RESOLUTIONS)

| STT | Vấn đề phát sinh (Blocker) | Nguyên nhân gốc rễ | Giải pháp kỹ thuật đã xử lý | Kết quả |
| :---: | :--- | :--- | :--- | :--- :---: |
| 1 | Bảng quyết định ban đầu có 8 rules gây dư thừa test case | Tổ hợp $2^3$ sinh ra các ca kiểm thử trùng lặp hành động khi mã voucher sai | Áp dụng kỹ thuật rút gọn bảng quyết định dựa trên điều kiện "Don't Care" để cô đọng còn 4 ca kiểm thử tối ưu | ✅ Tinh gọn |
| 2 | Khó khăn khi đối soát độ phức tạp Cyclomatic thủ công với công cụ JaCoCo | JaCoCo tính toán độ phức tạp dựa trên bytecode (nhánh rẽ `IF_ICMP`), khác với mã nguồn Java gốc | Vẽ CFG trực tiếp từ cấu trúc lệnh Java, phân tích song song cả 2 công thức $V(G) = E - N + 2$ và $V(G) = P + 1$ để giải trình thuyết phục | ✅ Minh bạch |
| 3 | Mất đồng bộ tên hàm test trong các tài liệu Markdown và mã nguồn | Quá trình refactor mã nguồn làm đổi tên method test nhưng chưa cập nhật doc | Rà soát toàn bộ codebase, cập nhật đồng bộ 100% tên method trong tài liệu | ✅ Khớp 100% |

---

## 🚀 6. KẾ HOẠCH BÀN GIAO SPRINT TIẾP THEO (SPRINT 6 - SPRINT CUỐI)
Trong Tuần 6 (Sprint 6), đội ngũ sẽ hoàn thiện các nội dung kiểm thử nâng cao cuối cùng và đóng gói toàn diện hệ thống:
- **Xây dựng CI/CD Pipeline trên GitHub Actions (`TEST-29`):** Tự động hóa build, test với CSDL MySQL container và kiểm soát JaCoCo Quality Gate (>70% Line, >65% Branch).
- **Tích hợp Newman vào CI/CD (`TEST-30`):** Chạy tự động 46 APIs Postman trên môi trường Docker của GitHub Actions.
- **Kiểm thử Bảo mật toàn diện (`TEST-31`):** Kiểm chứng OWASP Top 10 (SQLi, XSS, CSRF, IDOR), quét SCA với OWASP Dependency-Check và rà soát thông tin bí mật (Secret Audit).
- **Kiểm thử Tải và Sức chịu tải (`TEST-32`):** Thiết kế kịch bản Apache JMeter từ 100 đến 500 VUs, kiểm tra SLA và xác định điểm gãy (Breaking Point).
- **Nghiệm thu & Đóng gói toàn bộ dự án:** Hoàn thiện báo cáo đóng góp cá nhân (`TEST-39`) và phát hành phiên bản Release cuối cùng `v5.0.0`.
