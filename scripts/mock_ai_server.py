"""
TEST-13: Standalone AI Mock Server (Python HTTP Server)
Khởi chạy độc lập trên port 8000 khi không muốn bật Docker AI Service.
Cách chạy: python scripts/mock_ai_server.py
"""

from http.server import HTTPServer, BaseHTTPRequestHandler
import json

class MockAIHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        response = {"status": "Standalone Mock AI Server is running", "port": 8000}
        self.wfile.write(json.dumps(response).encode('utf-8'))

    def do_POST(self):
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        response = {
            "approved": True,
            "status": "APPROVED",
            "quality_score": 0.98,
            "reason": "Mock AI Response: Image passed QA inspection.",
            "is_standalone_mock": True
        }
        self.wfile.write(json.dumps(response).encode('utf-8'))

def run(port=8000):
    server_address = ('', port)
    httpd = HTTPServer(server_address, MockAIHandler)
    print(f"🚀 AI Standalone Mock Server running on http://localhost:{port}")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n🛑 AI Mock Server stopped.")

if __name__ == '__main__':
    run()
