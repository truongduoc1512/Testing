# Bảng Test Case: Chức năng 9 - Trí tuệ Nhân tạo (Computer Vision Inspection)
**Người thực hiện:** Lĩnh

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):**
  - **Phân hoạch lớp tương đương (EP):** Chia lớp định dạng ảnh hợp lệ (JPG, PNG) và không hợp lệ (PDF, TXT).
  - **Phân tích giá trị biên (BVA):** Ràng buộc dung lượng tối đa của file (5MB) và điểm độ nét (Blur Score ≥ 70.0).
  - **Dựa trên kinh nghiệm (Error Guessing):** Cố tình đưa các ảnh sai ngữ cảnh (ô tô, chó mèo) hoặc cố tình làm mờ ảnh để thử thách thuật toán nhận diện của Model AI.
  - **Bảng quyết định (Decision Table):** Ma trận quyết định 5 bước tuần tự từ chặn định dạng hệ thống đến chặn logic thuật toán AI.
- **Kỹ thuật Thực thi (Test Execution):** Kiểm thử API Tự động (Black-box API Testing) giao tiếp với vi dịch vụ AI FastAPI.
- **Endpoint API:** `POST /api/v1/analyze` (Tài liệu Swagger: `http://localhost:8000/docs`)

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)


### 2.1 Bảng Phân hoạch lớp tương đương (Equivalence Partitioning - EP)

| Biến đầu vào / Điều kiện | Lớp tương đương Hợp lệ | Tag | Lớp tương đương Không hợp lệ | Tag |
| :--- | :--- | :---: | :--- | :---: |
| **Định dạng file (`Content-Type`)**| `.jpg`, `.jpeg`, `.png` | **V1** | `.pdf`, `.mp4`, `.gif`, `.svg` (Sai MIME) | **X1** |
| **Chủ thể trong ảnh (`subject`)** | Giày thể thao, Sneaker, Boots | **V2** | Ô tô, Chó mèo, Phong cảnh (Sai vật thể)<br>Ảnh rỗng / 1x1 pixel (Không có dữ liệu) | **X2**<br>**X3** |
| **Độ nét của ảnh (`blurScore`)** | $Score \ge 70.0$ (Rõ nét, đủ chi tiết) | **V3** | $Score < 70.0$ (Bị mờ, rung tay, mất nét) | **X4** |
| **Dung lượng file (`fileSize`)** | $0 < Size \le 5.0$ MB | **V4** | $Size > 5.0$ MB (Quá dung lượng)<br>$Size = 0$ byte (File rỗng) | **X5**<br>**X6** |
| **Cơ chế chịu lỗi AI (`resilience`)**| AI phản hồi kết quả phân tích chuẩn | **V5** | AI Service gặp sự cố HTTP 500<br>AI trả về kết quả rỗng hoặc undecided | **X7**<br>**X8** |


### 2.2 Bảng Phân tích giá trị biên (Standard Boundary Value Analysis - BVA)

| Biến đầu vào | Miền hợp lệ | min | min+ | nominal | max- | max | Tag biên |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **Dung lượng tệp (`fileSize`)** | $[0.01, 5.0]$ MB | 0.01 | 0.1 | 2.5 | 4.9 | 5.0 | **B1, B2, B3, B4, B5** |
| **Độ nét hình ảnh (`blurScore`)** | $[70.0, 100.0]$ điểm | 70.0 | 70.1 | 85.0 | 99.9 | 100.0 | **B7, B8, B9, B10, B11** |

*Ghi chú mở rộng (Robustness BVA):*
- Ngoài biên trên dung lượng tệp: $Size = 6.0$ MB (Tag **B6** / max+1) -> Báo lỗi HTTP 413 Payload Too Large.
- Ngoài biên dưới điểm độ nét: $Score = 69.9$ điểm (Tag **B12** / min-1) -> Báo lỗi AI từ chối ảnh mờ nét.


### 2.3 Bảng Quyết định tổng hợp (Collapsed Decision Table)

| Condition/Action | R1 | R2 | R3 | R4 | R5 |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **C1: Định dạng JPG/PNG?** | N | Y | Y | Y | Y |
| **C2: Dung lượng ≤ 5MB?** | - | N | Y | Y | Y |
| **C3: Nhận diện đúng giày?** | - | - | N | Y | Y |
| **C4: Độ nét Score ≥ 70.0?** | - | - | - | N | Y |
| **A1: HTTP 422 (Định dạng file không hỗ trợ)** | X | - | - | - | - |
| **A2: HTTP 413 (Dung lượng quá lớn)** | - | X | - | - | - |
| **A3: HTTP 200, `REJECTED` (Không thấy giày)** | - | - | X | - | - |
| **A4: HTTP 200, `REJECTED` (Ảnh mờ nét)** | - | - | - | X | - |
| **A5: HTTP 200, `APPROVED` (Hợp lệ)** | - | - | - | - | X |
| **Test Case Tương ứng** | TC_AI_005 | TC_AI_004 | TC_AI_003 | TC_AI_002 | TC_AI_001 |

---


## 3. Bảng Test Case Chi Tiết

### 2. Danh sách Test Cases

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Tag được bao phủ | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- | :---: |
| **TC_AI_001** | Bảng quyết định (Rule 5) / EP / BVA | Kiểm định ảnh giày rõ nét hợp lệ | AI FastAPI Service đang chạy | 1. Gửi request POST `/api/v1/analyze`. 2. Đính kèm ảnh giày nét | File: `shoe_clear.jpg` | HTTP 200, `status: "APPROVED"`, `blur_score > 70` | **V1, V2, V3, V4, V5, B3, B10** | HTTP 200, `APPROVED`, `blur = 84.5` | Pass |
| **TC_AI_002** | Bảng quyết định (Rule 4) / Error Guessing / BVA | Từ chối ảnh giày bị mờ nét | AI FastAPI Service đang chạy | 1. Gửi request kèm ảnh chụp mờ | File: `shoe_blurred.jpg` | HTTP 200, `status: "REJECTED"`, báo lỗi mờ | **V1, V2, X4, V4, B12** | HTTP 200, `REJECTED` | Pass |
| **TC_AI_003** | Bảng quyết định (Rule 3) / Error Guessing | Từ chối ảnh không phải giày | AI FastAPI Service đang chạy | 1. Gửi request kèm ảnh ô tô | File: `car_photo.jpg` | HTTP 200, `status: "REJECTED"`, không nhận diện được giày | **V1, X2, X3** | HTTP 200, `REJECTED` | Pass |
| **TC_AI_004** | Bảng quyết định (Rule 2) / BVA | Chặn ảnh vượt dung lượng (> 5MB) | AI FastAPI Service đang chạy | 1. Gửi request đính kèm ảnh 6MB | File: `shoe_6MB.jpg` | HTTP 413 `Payload Too Large` | **V1, V2, X7** | HTTP 413 `Payload Too Large` | Pass |
| **TC_AI_005** | Bảng quyết định (Rule 1) / EP | Chặn file sai định dạng | AI FastAPI Service đang chạy | 1. Gửi request đính kèm file pdf | File: `document.pdf` | HTTP 422 `Unsupported File Format` | **X1, X8** | HTTP 422 `Unsupported format` | Pass |


---

## 4. Bảng Đối Chiếu & Ý Nghĩa Nhãn Tag (Tag Traceability Legend)

| Nhóm Tag | Mã Tag | Ý nghĩa nghiệp vụ | Trạng thái |
| :---: | :---: | :--- | :---: |
| **Valid EP** | **V1** | Định dạng hình ảnh hợp lệ (JPG, JPEG, PNG) | Hợp lệ |
| | **V2** | Chủ thể nhận diện trong ảnh đúng là sản phẩm giày | Hợp lệ |
| | **V3** | Điểm độ nét đạt ngưỡng chuẩn (blur_score >= 70.0) | Hợp lệ |
| | **V4** | Dung lượng tệp ảnh nằm trong giới hạn cho phép (<= 5.0 MB) | Hợp lệ |
| | **V5** | Dịch vụ AI hoạt động ổn định và phản hồi kết quả chuẩn | Hợp lệ |
| **Invalid EP**| **X1** | File sai định dạng hình ảnh (PDF, MP4, TXT) | Không hợp lệ |
| | **X2** | Ảnh chụp không chứa sản phẩm giày (ô tô, phong cảnh) | Không hợp lệ |
| | **X3** | Ảnh kích thước quá nhỏ (1x1 pixel) không đủ dữ liệu phân tích | Không hợp lệ |
| | **X4** | Ảnh chụp bị mờ nét, rung lắc (blur_score < 70.0) | Không hợp lệ |
| | **X5** | Dung lượng tệp vượt quá ngưỡng tối đa (> 5.0 MB) | Không hợp lệ |
| | **X6** | Tệp tin tải lên rỗng (0 bytes) | Không hợp lệ |
| | **X7** | Vi dịch vụ AI gặp sự cố kỹ thuật nội bộ (HTTP 500) | Không hợp lệ (Tự phục hồi) |
| | **X8** | Phản hồi AI bị khuyết thiếu hoặc không xác định (approved=null) | Không hợp lệ (Bảo toàn luồng) |
| **Boundary** | **B1 - B5**| Điểm biên dung lượng tệp: min (0.01MB), min+ (0.1MB), nom (2.5MB), max- (4.9MB), max (5.0MB) | Hợp lệ |
| | **B6** | Ngoài biên trên dung lượng tệp: 6.0 MB (HTTP 413) | Không hợp lệ |
| | **B7 - B11**| Điểm biên độ nét ảnh: min (70.0), min+ (70.1), nom (85.0), max- (99.9), max (100.0) | Hợp lệ |
| | **B12** | Ngoài biên dưới độ nét ảnh: 69.9 điểm (Bị từ chối) | Không hợp lệ |
