from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse

# Khởi tạo ứng dụng FastAPI
app = FastAPI(title="AI Image QA Service")

@app.get("/")
def health_check():
    """API dùng để Docker kiểm tra xem service đã chạy lên thành công chưa"""
    return {"status": "AI Service is running perfectly"}

@app.post("/api/v1/analyze")
async def analyze_image(file: UploadFile = File(...)):
    """
    Endpoint nhận file ảnh từ backend Spring Boot.
    Logic xử lý bằng OpenCV và YOLO sẽ được cập nhật tại đây.
    """
    try:
        # Đọc nội dung file ảnh (dưới dạng bytes)
        contents = await file.read()
        
        # TODO: Chèn logic gọi hàm AI (OpenCV + YOLO) ở đây
        
        # Dữ liệu giả lập (Mock Data) trả về cho Spring Boot để test luồng
        mock_result = {
            "status": "REJECTED",
            "reason": "Ảnh giả lập báo lỗi: Quá mờ và vật thể nhỏ",
            "file_name": file.filename,
            "metrics": {
                "blur_score": 12.5,
                "object_ratio": 0.15
            }
        }
        
        return JSONResponse(content=mock_result)

    except Exception as e:
        return JSONResponse(status_code=500, content={"status": "ERROR", "reason": str(e)})