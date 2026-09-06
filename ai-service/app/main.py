from fastapi import FastAPI, File, UploadFile, Query
from fastapi.responses import JSONResponse
from app.image_qa import analyze_image

app = FastAPI(
    title="AI Image QA Service & Mock Server",
    description="Dịch vụ AI kiểm duyệt ảnh sản phẩm và AI Mock Server phục vụ Testing",
    version="1.0.0",
)


@app.get("/")
def health_check():
    """Điểm cuối kiểm tra trạng thái dịch vụ AI."""
    return {"status": "AI Service is running perfectly", "version": "1.0.0"}


@app.post("/api/v1/analyze")
async def analyze_product_image(file: UploadFile = File(...)):
    """Phân tích ảnh thật bằng thuật toán AI Image QA."""
    try:
        contents = await file.read()
        result = analyze_image(image_bytes=contents, filename=file.filename or "unknown")
        return JSONResponse(content=result)
    except Exception as error:
        return JSONResponse(
            status_code=500,
            content={
                "approved": False,
                "status": "ERROR",
                "reason": f"Lỗi hệ thống AI: {error}",
                "filename": file.filename or "unknown",
            },
        )


# =================================================================
# TEST-13: AI MOCK SERVER ENDPOINTS FOR FAST AUTOMATED TESTING
# =================================================================

@app.post("/api/v1/mock/analyze")
async def mock_analyze_image(
    file: UploadFile = File(...),
    force_status: str = Query(None, description="Ép buộc trạng thái: 'PASS', 'REJECT', 'BLUR', 'NOT_SHOE'")
):
    """
    Mock AI Endpoint phản hồi tức thì (< 10ms) phục vụ Test Automation.
    - Nếu tên file chứa 'blur' hoặc 'invalid' -> Trả về REJECT.
    - Nếu query param force_status='REJECT' -> Trả về REJECT.
    - Mặc định -> Trả về PASS.
    """
    filename = (file.filename or "unknown").lower()
    
    # Kịch bản 1: Ép buộc trạng thái từ query param hoặc tên file
    if force_status == "REJECT" or "blur" in filename or "invalid" in filename:
        return JSONResponse(content={
            "approved": False,
            "status": "REJECTED",
            "quality_score": 0.35,
            "reason": "Ảnh bị mờ hoặc không đạt tiêu chuẩn độ phân giải.",
            "filename": file.filename,
            "is_mock": True
        })
    
    if force_status == "NOT_SHOE" or "not_shoe" in filename:
        return JSONResponse(content={
            "approved": False,
            "status": "REJECTED",
            "quality_score": 0.20,
            "reason": "Không phát hiện sản phẩm giày trong hình ảnh.",
            "filename": file.filename,
            "is_mock": True
        })

    # Kịch bản 2: Trả về PASS (Ảnh đạt chuẩn)
    return JSONResponse(content={
        "approved": True,
        "status": "APPROVED",
        "quality_score": 0.95,
        "reason": "Ảnh rõ nét, ánh sáng tốt và nhận diện đúng sản phẩm giày.",
        "filename": file.filename,
        "is_mock": True
    })
