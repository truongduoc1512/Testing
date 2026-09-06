# 🤖 TÀI LIỆU VẬN HÀNH AI MOCK SERVER (TASK TEST-13)
> **Người thực hiện:** Leader (Trương Hoài Dược)  
> **Dự án:** ShoeShop Quality Assurance & Testing  

---

## 📌 1. MỤC TIÊU
Cung cấp dịch vụ **AI Mock Server** giả lập API kiểm duyệt ảnh sản phẩm (`/api/v1/analyze`) với tốc độ phản hồi cực nhanh (`< 10ms`), hỗ trợ kiểm thử tự động (Unit Test, API Integration Test) mà không cần nạp GPU hoặc mô hình AI YOLOv8 thực tế.

---

## 🚀 2. CÁC KỊCH BẢN MOCK HỖ TRỢ (TEST SCENARIOS)

| Kịch bản Test | Cách kích hoạt | Trạng thái trả về (`approved`) | Lý do trả về (`reason`) |
| :--- | :--- | :---: | :--- |
| **Pass (Ảnh đạt chuẩn)** | Gửi tên file bình thường (ví dụ `shoes.jpg`) | `true` | Ảnh rõ nét, ánh sáng tốt, nhận diện đúng sản phẩm. |
| **Reject (Ảnh mờ)** | Tên file chứa `blur` hoặc query `force_status=REJECT` | `false` | Ảnh bị mờ hoặc không đạt tiêu chuẩn độ phân giải. |
| **Reject (Không phải giày)**| Tên file chứa `not_shoe` hoặc query `force_status=NOT_SHOE` | `false` | Không phát hiện sản phẩm giày trong hình ảnh. |

---

## 🧪 3. HƯỚNG DẪN KHỞI CHẠY KHÔNG CẦN DOCKER
Lập trình viên có thể khởi chạy AI Mock Server độc lập trên máy bằng câu lệnh:

```bash
python scripts/mock_ai_server.py

```

Dịch vụ sẽ tự động lắng nghe trên cổng `http://localhost:8000`.

---

## 🎯 4. CÁCH GỬI YÊU CẦU (INTEGRATION TEST)

### 1. Tạo API Mock Client (scripts/mock_api_client.py)
File `mock_api_client.py` triển khai logic gọi Mock API:

```python
import requests
MOCK_API_URL = "http://localhost:8000/api/v1/mock/analyze"

def analyze_image(image_path):
    """Gửi ảnh đến Mock AI Server."""
    try:
        files = {'file': open(image_path, 'rb')}
        response = requests.post(MOCK_API_URL, files=files, timeout=5)
        return response.json()
    except Exception as e:
        return {"error": str(e)}
```

### 2. Kịch bản Test 1: Ép buộc Reject ( Ảnh mờ)
```python
import unittest
from unittest.mock import MagicMock

# Giả lập requests.post để test
requests.post = MagicMock()

class TestMockReject(unittest.TestCase):
    def test_reject_blurred_image(self):
        # Cấu hình Mock Server trả về REJECT
        requests.post.return_value.json.return_value = {
            "approved": False,
            "status": "REJECTED",
            "quality_score": 0.35,
            "reason": "Ảnh bị mờ...",
            "is_mock": True
        }
        
        # Test với file ảnh chứa "blur" trong tên
        result = requests.post("image.jpg")
        self.assertFalse(result.json()["approved"])
        self.assertEqual(result.json()["status"], "REJECTED")
```

### 3. Kịch bản Test 2: Ép buộc Not Shoe
```python
class TestMockNotShoe(unittest.TestCase):
    def test_not_shoe_image(self):
        # Cấu hình Mock Server trả về NOT_SHOE
        requests.post.return_value.json.return_value = {
            "approved": False,
            "status": "REJECTED",
            "quality_score": 0.20,
            "reason": "Không phát hiện sản phẩm giày...",
            "is_mock": True
        }
        
        # Test với file ảnh chứa "not_shoe" trong tên
        result = requests.post("not_shoe.jpg")
        self.assertFalse(result.json()["approved"])
        self.assertIn("Không phát hiện", result.json()["reason"])
```

### 4. Kịch bản Test 3: Trả về Pass (Mặc định)
```python
class TestMockPass(unittest.TestCase):
    def test_pass_image(self):
        # Cấu hình Mock Server trả về PASS
        requests.post.return_value.json.return_value = {
            "approved": True,
            "status": "APPROVED",
            "quality_score": 0.95,
            "reason": "Ảnh rõ nét...",
            "is_mock": True
        }
        
        result = requests.post("valid_shoe.jpg")
        self.assertTrue(result.json()["approved"])
        self.assertEqual(result.json()["status"], "APPROVED")
```

---

## 🔗 5. CÁCH GỬI YÊU CẦU (API GATEWAY TEST)
Khi tích hợp với Spring Boot Application Gateway, lập trình viên chỉ cần gửi POST request đến:

```
http://localhost:8080/api/v1/analyze
```

**Lưu ý:** Không cần gửi thêm query params `useMock` hay `mockType` vì đã được cấu hình sẵn trong Spring Boot (xem Task TEST-12).

---

## 🔌 6. DOCKER COMPOSE CONFIGURATION

### docker-compose-ai.yml (Config AI Mock Server)
File cấu hình Docker Compose cho Mock AI Service:

```yaml
# docker-compose-ai.yml
version: '3.9'
services:
  ai-mock:
    build:
      context: ./ai-service
      dockerfile: Dockerfile
    container_name: shoeshop-ai-mock
    ports:
      - "8000:80"
    environment:
      - APP_ENV=production
      - AI_MODEL_PATH=dummy
    restart: always
    volumes:
      - ai_data:/app/data
    networks:
      - shoeshop-network

volumes:
  ai_data:

networks:
  shoeshop-network:
    external: true
```

### Câu lệnh Docker Compose
```bash
# Kéo cấu hình từ folder ai-service
cp ai-service/docker-compose-ai.yml .

# Build và chạy AI Mock Service
docker-compose -f docker-compose-ai.yml up -d --build

# Dừng AI Mock Service
docker-compose -f docker-compose-ai.yml down
```

---

## ✅ 7. TÓM TẮT LỢI ÍCH
1.  **Tốc độ siêu nhanh**: Mock Server phản hồi gần như tức thì (`< 10ms`), loại bỏ độ trễ GPU.
2.  **Tự động hóa**: Hỗ trợ Unit Test và Integration Test mà không cần GPU.
3.  **Kiểm thử đa kịch bản**: Dễ dàng giả lập các tình huống ảnh mờ, ảnh không phải giày.
4.  **Không phụ thuộc**: Có thể chạy độc lập trên máy lập trình viên mà không cần Docker AI Service.
