# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `PaginationResult`
## Tầng: Pagination Layer | Phân hệ: Thuật toán Phân trang & Thanh điều hướng (Navigation Bar)
* **Tổng số Test Case:** **7 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa hàm nghiệp vụ trong `PaginationResult.java` và các ca kiểm thử trong `PaginationResultTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `PaginationResult.java`

Lớp generic `PaginationResult<E>` bọc kết quả phân trang Hibernate ScrollableResults và tính toán thanh điều hướng:

| STT | Tên hàm trong `PaginationResult.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `PaginationResult(Query<E>, int page, int maxResult, int maxNav)` | Khởi tạo phân trang, cuộn con trỏ DB và tính tổng số trang | **4** |
| 2 | `calcNavigationPages()` | Tính toán danh sách trang hiển thị kèm dấu ba chấm (`-1`) | **3** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 7 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_PGR_01` | Constructor, `calcNavigationPages` | `emptyResult_normalizesPageAndReturnsImmutableCollections` | `page = 0`, `maxResult = 10`, `maxNav = 5`<br>Mock: `resultScroll.getRowNumber() = -1` (0 bản ghi) | Nhánh CSDL rỗng: chuẩn hóa trang về 1, danh sách bất biến rỗng | `totalRecords = 0`, `totalPages = 0`, `currentPage = 1`, `navigationPages = [1, 0]`, ném `UnsupportedOperationException` nếu sửa list |
| `TC_PGR_02` | Constructor, `calcNavigationPages` | `populatedResult_collectsRecordsAndCalculatesNonDivisiblePages`| `totalRecords = 13`, `maxResult = 5` (13 không chia hết cho 5) | Nhánh `totalRecords % maxResult != 0` ➔ `totalPages = (13/5) + 1 = 3` | `totalPages = 3`, `list = ["first", "second"]`, `navigationPages = [1, 2, 3]` |
| `TC_PGR_03` | `calcNavigationPages` | `largeResult_capsNavigationAndAddsLeadingEllipsis` | `totalRecords = 100`, `maxResult = 10`, `currentPage = 8`, `maxNav = 5` | Nhánh trang hiện tại ở gần cuối: sinh dấu ba chấm đầu trang (`begin > 2`) | `totalPages = 10`, `navigationPages = [1, -1, 6, 7, 8, 9, 10]` |
| `TC_PGR_04` | `calcNavigationPages` | `largeResultAtStart_addsTrailingEllipsis` | `totalRecords = 100`, `currentPage = 1`, `maxNav = 5` | Nhánh trang hiện tại ở đầu: sinh dấu ba chấm cuối trang (`end < totalPages - 2`) | `navigationPages = [1, 2, -1, 10]` |
| `TC_PGR_05` | Constructor, `calcNavigationPages` | `pageBeyondLast_isClampedOnlyForNavigationAndMissingPageHasNoRows`| `page = 20`, nhưng CSDL chỉ có 20 bản ghi (`totalPages = 2`) | Nhánh trang yêu cầu vượt quá tổng số trang (`current = totalPages`) | `totalPages = 2`, `list` rỗng, `navigationPages = [1, 2]` |
| `TC_PGR_06` | Constructor (vòng lặp đọc bản ghi) | `iterationStopsWhenCursorFallsBeforeRequestedPage` | Con trỏ cuộn rơi vào vị trí nhỏ hơn `fromRecordIndex` | Nhánh điều kiện dừng vòng lặp `getRowNumber() >= fromRecordIndex` | Dừng nạp bản ghi khi lệch chỉ mục trang |
| `TC_PGR_07` | Constructor (vòng lặp đọc bản ghi) | `iterationStopsAtExclusivePageEnd` | Đã duyệt đủ số bản ghi của trang (`maxRecordIndex`) | Nhánh điều kiện dừng vòng lặp `getRowNumber() < maxRecordIndex` | Dừng nạp ngay khi đạt ngưỡng bản ghi tối đa của trang |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.pagination.PaginationResult`
* **Statement Coverage (Instructions):** **100.0%** (219/219 instructions)
* **Branch Coverage (Branches):** **100.0%** (28/28 branches)
* **Line Coverage:** **100.0%** (48/48 lines)
* **Đánh giá:** Đạt độ bao phủ tuyệt đối 100%, kiểm soát toàn bộ thuật toán tính toán thanh điều hướng phân trang và các điều kiện ngắt vòng lặp cuộn dữ liệu.
