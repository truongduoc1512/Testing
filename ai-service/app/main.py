from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse

from app.image_qa import analyze_image

app = FastAPI(
    title="AI Image QA Service",
    description="Dịch vụ AI kiểm duyệt ảnh sản phẩm tự động cho ShoeShop",
    version="1.0.0",
)


@app.get("/")
def health_check():
    """Điểm cuối kiểm tra trạng thái dành cho Docker và Spring Boot."""
    return {"status": "AI Service is running perfectly", "version": "1.0.0"}


@app.post("/api/v1/analyze")
async def analyze_product_image(file: UploadFile = File(...)):
    """Phân tích ảnh sản phẩm và trả về kết quả kiểm duyệt chất lượng."""
    try:
        contents = await file.read()
        result = analyze_image(image_bytes=contents, filename=file.filename or "unknown")
        return JSONResponse(content=result)
    except Exception as error:  # pylint: disable=broad-exception-caught
        return JSONResponse(
            status_code=500,
            content={
                "approved": False,
                "status": "ERROR",
                "reason": f"Lỗi hệ thống AI: {error}",
                "filename": file.filename or "unknown",
            },
        )
