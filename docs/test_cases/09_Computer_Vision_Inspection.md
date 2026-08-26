# Module 09: Computer Vision Inspection (FastAPI AI Service)

### 1. Biến điều kiện (Conditions)
- **C1:** File tải lên có đúng định dạng ảnh hỗ trợ (JPG/PNG)? (Yes/No)
- **C2:** Dung lượng file có hợp lệ (<= 5MB)? (Yes/No)
- **C3:** AI Model có phát hiện được đối tượng Giày (Shoe Detection)? (Yes/No)
- **C4:** Độ nét của ảnh (Blur Score) có đạt chuẩn (>= 70.0)? (Yes/No)

### 2. Danh sách Test Cases

| Mã kiểm thử | Kỹ thuật áp dụng | Tiêu đề | Điều kiện tiên quyết | Các bước kiểm tra | Dữ liệu kiểm thử | Kết quả dự kiến | Kết quả thực tế | Trạng thái |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_AI_001** | Bảng quyết định (Rule 5) | Kiểm định ảnh giày rõ nét hợp lệ | AI FastAPI Service đang chạy | 1. Gửi request POST `/api/v1/analyze`<br>2. Đính kèm ảnh giày nét | File: `shoe_clear.jpg` | HTTP 200, `status: "APPROVED"`, `blur_score > 70` | HTTP 200, `APPROVED`, `blur = 84.5` | Pass |
| **TC_AI_002** | Bảng quyết định (Rule 4) | Từ chối ảnh giày bị mờ nét | AI FastAPI Service đang chạy | 1. Gửi request kèm ảnh chụp mờ | File: `shoe_blurred.jpg` | HTTP 200, `status: "REJECTED"`, báo lỗi mờ | HTTP 200, `REJECTED` | Pass |
| **TC_AI_003** | Bảng quyết định (Rule 3) | Từ chối ảnh không phải giày | AI FastAPI Service đang chạy | 1. Gửi request kèm ảnh ô tô | File: `car_photo.jpg` | HTTP 200, `status: "REJECTED"`, không nhận diện được giày | HTTP 200, `REJECTED` | Pass |
| **TC_AI_004** | Bảng quyết định (Rule 2) | Chặn ảnh vượt dung lượng (> 5MB) | AI FastAPI Service đang chạy | 1. Gửi request đính kèm ảnh 6MB | File: `shoe_6MB.jpg` | HTTP 413 `Payload Too Large` | HTTP 413 `Payload Too Large` | Pass |
| **TC_AI_005** | Bảng quyết định (Rule 1) | Chặn file sai định dạng | AI FastAPI Service đang chạy | 1. Gửi request đính kèm file pdf | File: `document.pdf` | HTTP 422 `Unsupported File Format` | HTTP 422 `Unsupported format` | Pass |

### 3. Bảng quyết định (Decision Table)

| Quy tắc (Rules) | C1: Định dạng JPG/PNG? | C2: Dung lượng <= 5MB? | C3: Nhận diện đúng giày? | C4: Độ nét >= 70.0? | Kết quả / Hành động hệ thống | Số kịch bản gộp | Test Case tương ứng |
| :--- | :---: | :---: | :---: | :---: | :--- | :---: | :---: |
| **Rule 1 (Sai định dạng)** | No | - | - | - | HTTP 422: Định dạng file không hỗ trợ | 8 | `TC_AI_005` |
| **Rule 2 (Vượt dung lượng)** | Yes | No | - | - | HTTP 413: Dung lượng vượt quá 5MB | 4 | `TC_AI_004` |
| **Rule 3 (Sai vật thể)** | Yes | Yes | No | - | HTTP 200: `REJECTED` (Không phát hiện giày) | 2 | `TC_AI_003` |
| **Rule 4 (Ảnh mờ)** | Yes | Yes | Yes | No | HTTP 200: `REJECTED` (Ảnh mờ nét, `score < 70`) | 1 | `TC_AI_002` |
| **Rule 5 (Duyệt thành công)** | Yes | Yes | Yes | Yes | HTTP 200: `APPROVED` (Ảnh hợp lệ, đạt độ nét) | 1 | `TC_AI_001` |

- **Endpoint:** `POST /api/v1/analyze` (Swagger UI: `http://localhost:8000/docs`)
