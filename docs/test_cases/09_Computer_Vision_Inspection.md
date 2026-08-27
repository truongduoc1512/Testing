# Bảng Test Case: Chức năng 9 - Trí tuệ Nhân tạo (Computer Vision Inspection)
**Người thực hiện:** Lĩnh

## 1. Thông tin Kỹ thuật & Thực thi
- **Kỹ thuật Thiết kế (Test Design):**
  - **Phân hoạch lớp tương đương (EP):** Chia lớp định dạng ảnh hợp lệ (JPG, PNG) và không hợp lệ (PDF, TXT).
  - **Phân tích giá trị biên (BVA):** Ràng buộc dung lượng tối đa của file (5MB) và điểm độ nét (Blur Score $\ge$ 70.0).
  - **Dựa trên kinh nghiệm (Error Guessing):** Cố tình đưa các ảnh sai ngữ cảnh (ô tô, chó mèo) hoặc cố tình làm mờ ảnh để thử thách thuật toán nhận diện của Model AI.
  - **Bảng quyết định (Decision Table):** Ma trận quyết định 5 bước tuần tự từ chặn định dạng hệ thống đến chặn logic thuật toán AI.
- **Kỹ thuật Thực thi (Test Execution):** Kiểm thử API Tự động (Black-box API Testing) giao tiếp với vi dịch vụ AI FastAPI.
- **Endpoint API:** `POST /api/v1/analyze` (Tài liệu Swagger: `http://localhost:8000/docs`)

---

## 2. Phân tích Kỹ thuật Thiết kế (Test Design Analysis)

### 2.1 Bảng Phân hoạch lớp (EP) & Đoán lỗi (Error Guessing)

| STT | Trường hợp của điều kiện đầu vào | Lớp tương đương Hợp lệ | Lớp tương đương Không hợp lệ |
| :---: | :--- | :--- | :--- |
| 1 | Định dạng File (`Content-Type`) | `.jpg`, `.jpeg`, `.png` | `.pdf`, `.mp4`, `.gif`, `.svg` |
| 2 | Chủ thể trong ảnh (Error Guessing) | Giày (Sneaker, Boots, Sandals) | Ô tô, Con mèo, Phong cảnh (Sai vật thể) |

### 2.2 Bảng Phân tích giá trị biên (BVA)
Áp dụng BVA để kiểm tra bộ lọc chống Spam File dung lượng lớn và thuật toán đánh giá Ảnh chất lượng kém (Blurry):

| Biến đầu vào | Ràng buộc logic | Các giá trị biên kiểm thử (BVA Points) |
| :--- | :--- | :--- |
| **Dung lượng File** | $Size \le 5 \text{ MB}$ | 4.9MB (nom), 5.0MB (max), 5.1MB (Lỗi max+) |
| **Độ nét (Blur Score)**| $Score \ge 70.0$ | 69.9 (Lỗi min-), 70.0 (min), 85.0 (nom) |

### 2.3 Bảng Quyết định tổng hợp (Collapsed Decision Table)

| Condition/Action | R1 | R2 | R3 | R4 | R5 |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **C1: Định dạng JPG/PNG?** | N | Y | Y | Y | Y |
| **C2: Dung lượng $\le$ 5MB?** | - | N | Y | Y | Y |
| **C3: Nhận diện đúng giày?** | - | - | N | Y | Y |
| **C4: Độ nét Score $\ge$ 70.0?** | - | - | - | N | Y |
| **A1: HTTP 422 (Định dạng file không hỗ trợ)** | X | - | - | - | - |
| **A2: HTTP 413 (Dung lượng quá lớn)** | - | X | - | - | - |
| **A3: HTTP 200, `REJECTED` (Không thấy giày)** | - | - | X | - | - |
| **A4: HTTP 200, `REJECTED` (Ảnh mờ nét)** | - | - | - | X | - |
| **A5: HTTP 200, `APPROVED` (Hợp lệ)** | - | - | - | - | X |
| **Test Case Tương ứng** | TC_AI_005 | TC_AI_004 | TC_AI_003 | TC_AI_002 | TC_AI_001 |

---

## 3. Bảng Test Case Chi Tiết

### 2. Danh sách Test Cases

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_AI_001** | Bảng quyết định (Rule 5) / EP / BVA | Kiểm định ảnh giày rõ nét hợp lệ | AI FastAPI Service đang chạy | 1. Gửi request POST `/api/v1/analyze`. 2. Đính kèm ảnh giày nét | File: `shoe_clear.jpg` | HTTP 200, `status: "APPROVED"`, `blur_score > 70` | HTTP 200, `APPROVED`, `blur = 84.5` | Pass |
| **TC_AI_002** | Bảng quyết định (Rule 4) / Error Guessing / BVA | Từ chối ảnh giày bị mờ nét | AI FastAPI Service đang chạy | 1. Gửi request kèm ảnh chụp mờ | File: `shoe_blurred.jpg` | HTTP 200, `status: "REJECTED"`, báo lỗi mờ | HTTP 200, `REJECTED` | Pass |
| **TC_AI_003** | Bảng quyết định (Rule 3) / Error Guessing | Từ chối ảnh không phải giày | AI FastAPI Service đang chạy | 1. Gửi request kèm ảnh ô tô | File: `car_photo.jpg` | HTTP 200, `status: "REJECTED"`, không nhận diện được giày | HTTP 200, `REJECTED` | Pass |
| **TC_AI_004** | Bảng quyết định (Rule 2) / BVA | Chặn ảnh vượt dung lượng (> 5MB) | AI FastAPI Service đang chạy | 1. Gửi request đính kèm ảnh 6MB | File: `shoe_6MB.jpg` | HTTP 413 `Payload Too Large` | HTTP 413 `Payload Too Large` | Pass |
| **TC_AI_005** | Bảng quyết định (Rule 1) / EP | Chặn file sai định dạng | AI FastAPI Service đang chạy | 1. Gửi request đính kèm file pdf | File: `document.pdf` | HTTP 422 `Unsupported File Format` | HTTP 422 `Unsupported format` | Pass |

 

