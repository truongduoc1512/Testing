# Sử dụng image Python phiên bản rút gọn
FROM python:3.10-slim

# Thiết lập thư mục làm việc mặc định trong container
WORKDIR /app

# Cài đặt một số thư viện hệ thống tối thiểu cần thiết cho xử lý ảnh
RUN apt-get update && apt-get install -y \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Copy file requirements vào container trước để tận dụng Docker Cache
COPY requirements.txt .

# Cài đặt các thư viện Python
RUN pip install --no-cache-dir -r requirements.txt

# Copy toàn bộ mã nguồn vào container
COPY . .

# Mở port 8000 để ứng dụng Spring Boot giao tiếp
EXPOSE 8000

# Lệnh khởi chạy server FastAPI
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]