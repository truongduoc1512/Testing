"""
Bug Logger & Lifecycle Management Helper Module (Task TEST-19)
Cung cấp công cụ chuẩn hóa ghi nhận lỗi (Bug Logging), tự động phân loại Severity,
gợi ý Assignee và xuất dữ liệu tương thích với Jira API & bugs_report_template.json.
"""

import json
import logging
import os
import traceback
from datetime import datetime, timezone
from typing import Dict, Any, List, Optional


class BugSeverity:
    BLOCKER = "Blocker"
    CRITICAL = "Critical"
    MAJOR = "Major"
    MINOR = "Minor"
    TRIVIAL = "Trivial"


class BugPriority:
    P1_HIGHEST = "P1 - Highest"
    P2_HIGH = "P2 - High"
    P3_MEDIUM = "P3 - Medium"
    P4_LOW = "P4 - Low"
    P5_LOWEST = "P5 - Lowest"


class BugCategory:
    LOGIC_AI = "Logic & AI"
    BACKEND_API = "Backend/API"
    DATABASE = "Database"
    UI_UX = "UI/UX"


class BugStatus:
    NEW = "New"
    OPEN = "Open"
    IN_PROGRESS = "In Progress"
    RESOLVED = "Resolved"
    RE_TESTING = "Re-testing"
    CLOSED = "Closed"
    RE_OPEN = "Re-open"


ASSIGNEE_MAP = {
    BugCategory.LOGIC_AI: "ai-team-lead",
    BugCategory.BACKEND_API: "be-tech-lead",
    BugCategory.DATABASE: "dba-lead",
    BugCategory.UI_UX: "fe-dev-team",
}

SEVERITY_PRIORITY_MAP = {
    BugSeverity.BLOCKER: BugPriority.P1_HIGHEST,
    BugSeverity.CRITICAL: BugPriority.P2_HIGH,
    BugSeverity.MAJOR: BugPriority.P3_MEDIUM,
    BugSeverity.MINOR: BugPriority.P4_LOW,
    BugSeverity.TRIVIAL: BugPriority.P5_LOWEST,
}


class BugReport:
    """Đại diện cho một bản báo cáo Bug chuẩn hóa."""

    def __init__(
        self,
        summary: str,
        description: str,
        category: str = BugCategory.LOGIC_AI,
        severity: str = BugSeverity.MAJOR,
        steps_to_reproduce: Optional[List[str]] = None,
        expected_result: str = "",
        actual_result: str = "",
        reporter: str = "qa_automation",
        jira_key: str = "",
        bug_id: str = "",
        environment: Optional[Dict[str, Any]] = None,
    ):
        self.bug_id = bug_id or f"BUG-{int(datetime.now(timezone.utc).timestamp())}"
        self.jira_key = jira_key or "TEST-PENDING"
        self.summary = summary
        self.issue_type = "Bug"
        self.category = category
        self.severity = severity
        self.priority = SEVERITY_PRIORITY_MAP.get(severity, BugPriority.P3_MEDIUM)
        self.status = BugStatus.NEW
        self.reporter = reporter
        self.assignee = ASSIGNEE_MAP.get(category, "unassigned")
        self.description = description
        self.steps_to_reproduce = steps_to_reproduce or []
        self.expected_result = expected_result
        self.actual_result = actual_result
        self.environment = environment or {
            "service": "ai-service",
            "os": os.name,
            "timestamp": datetime.now(timezone.utc).isoformat(),
        }
        self.created_at = datetime.now(timezone.utc).isoformat()
        self.updated_at = self.created_at

    def to_dict(self) -> Dict[str, Any]:
        """Chuyển đổi BugReport thành dict tương thích với bugs_report_template.json."""
        return {
            "bug_id": self.bug_id,
            "jira_key": self.jira_key,
            "summary": self.summary,
            "issue_type": self.issue_type,
            "severity": self.severity,
            "priority": self.priority,
            "category": self.category,
            "status": self.status,
            "reporter": self.reporter,
            "assignee": self.assignee,
            "environment": self.environment,
            "description": self.description,
            "steps_to_reproduce": self.steps_to_reproduce,
            "expected_result": self.expected_result,
            "actual_result": self.actual_result,
            "created_at": self.created_at,
            "updated_at": self.updated_at,
        }

    def to_jira_markdown(self) -> str:
        """Tạo chuỗi định dạng Markdown phục vụ paste trực tiếp vào Jira Issue Description."""
        steps_formatted = "\n".join(self.steps_to_reproduce)
        return (
            f"h2. [Bug Report] {self.summary}\n\n"
            f"* *Issue Type:* {self.issue_type}\n"
            f"* *Category:* {self.category}\n"
            f"* *Severity:* {self.severity} ({self.priority})\n"
            f"* *Suggested Assignee:* {self.assignee}\n"
            f"* *Reporter:* {self.reporter}\n\n"
            f"h3. 1. Description\n{self.description}\n\n"
            f"h3. 2. Steps to Reproduce\n{steps_formatted}\n\n"
            f"h3. 3. Expected Result\n{self.expected_result}\n\n"
            f"h3. 4. Actual Result\n{self.actual_result}\n\n"
            f"h3. 5. Environment\n{json.dumps(self.environment, indent=2)}\n"
        )


class BugLogger:
    """Quản lý việc tự động tạo và xuất log báo cáo lỗi."""

    def __init__(self, output_file: str = "bugs_report_template.json"):
        self.output_file = output_file
        self.logger = logging.getLogger("BugLogger")

    def capture_exception(
        self,
        exception: Exception,
        summary: str,
        category: str = BugCategory.LOGIC_AI,
        severity: str = BugSeverity.MAJOR,
        steps: Optional[List[str]] = None,
        expected: str = "Hệ thống hoạt động bình thường mà không xảy ra ngoại lệ",
    ) -> BugReport:
        """Tự động ghi nhận một Exception và đóng gói thành BugReport."""
        stack_trace = traceback.format_exc()
        actual_result = f"Xảy ra ngoại lệ ({type(exception).__name__}): {str(exception)}\nStacktrace:\n{stack_trace}"
        
        bug_report = BugReport(
            summary=summary,
            description=f"Hệ thống phát hiện lỗi runtime trong quá trình xử lý: {str(exception)}",
            category=category,
            severity=severity,
            steps_to_reproduce=steps or ["1. Gọi API hoặc kích hoạt hàm bị lỗi"],
            expected_result=expected,
            actual_result=actual_result,
        )
        return bug_report

    def export_to_json(self, bug_report: BugReport, file_path: Optional[str] = None) -> str:
        """Lưu hoặc cập nhật danh sách Bug report vào file JSON."""
        target_path = file_path or self.output_file
        data = {"sample_bugs": []}

        if os.path.exists(target_path):
            try:
                with open(target_path, "r", encoding="utf-8") as f:
                    data = json.load(f)
            except Exception:
                data = {"sample_bugs": []}

        bugs = data.get("sample_bugs", [])
        bugs.append(bug_report.to_dict())
        data["sample_bugs"] = bugs

        with open(target_path, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

        return target_path
