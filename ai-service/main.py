"""
Entry point cho AI Service.
Khởi chạy uvicorn với module app.main
"""

# Re-exported for the Docker/Uvicorn entrypoint "main:app".
from app.main import app  # noqa: F401  # pylint: disable=unused-import
