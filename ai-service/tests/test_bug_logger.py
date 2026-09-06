"""
Unit tests for bug_logger module (Task TEST-19).
Uses Python built-in unittest framework.
"""

import json
import os
import tempfile
import unittest

from app.bug_logger import (
    BugReport,
    BugLogger,
    BugSeverity,
    BugPriority,
    BugCategory,
    BugStatus,
    ASSIGNEE_MAP,
)


class TestBugLogger(unittest.TestCase):
    """Test suite for BugReport and BugLogger classes."""

    def test_bug_report_initialization(self):
        """Kiểm tra khởi tạo BugReport với các giá trị mặc định và gán tự động."""
        report = BugReport(
            summary="[Test] AI Service Timeout",
            description="Chi tiết timeout xử lý ảnh",
            category=BugCategory.LOGIC_AI,
            severity=BugSeverity.CRITICAL,
            steps_to_reproduce=["1. Send large image", "2. Wait 30s"],
            expected_result="Trả về phản hồi trong < 5s",
            actual_result="Request bị timeout 30s",
        )

        self.assertEqual(report.summary, "[Test] AI Service Timeout")
        self.assertEqual(report.category, BugCategory.LOGIC_AI)
        self.assertEqual(report.severity, BugSeverity.CRITICAL)
        self.assertEqual(report.priority, BugPriority.P2_HIGH)
        self.assertEqual(report.assignee, "ai-team-lead")
        self.assertEqual(report.status, BugStatus.NEW)
        self.assertEqual(len(report.steps_to_reproduce), 2)

    def test_assignee_rules_mapping(self):
        """Kiểm tra quy tắc gán Assignee đúng theo phân vùng Category."""
        ui_report = BugReport(summary="UI Bug", description="", category=BugCategory.UI_UX)
        be_report = BugReport(summary="BE Bug", description="", category=BugCategory.BACKEND_API)
        db_report = BugReport(summary="DB Bug", description="", category=BugCategory.DATABASE)
        ai_report = BugReport(summary="AI Bug", description="", category=BugCategory.LOGIC_AI)

        self.assertEqual(ui_report.assignee, ASSIGNEE_MAP[BugCategory.UI_UX])
        self.assertEqual(be_report.assignee, ASSIGNEE_MAP[BugCategory.BACKEND_API])
        self.assertEqual(db_report.assignee, ASSIGNEE_MAP[BugCategory.DATABASE])
        self.assertEqual(ai_report.assignee, ASSIGNEE_MAP[BugCategory.LOGIC_AI])

    def test_bug_report_to_dict_and_jira_markdown(self):
        """Kiểm tra xuất dictionary và định dạng Jira markdown."""
        report = BugReport(
            summary="Sample Bug",
            description="Sample Description",
            category=BugCategory.BACKEND_API,
            severity=BugSeverity.BLOCKER,
        )
        report_dict = report.to_dict()

        self.assertEqual(report_dict["summary"], "Sample Bug")
        self.assertEqual(report_dict["severity"], BugSeverity.BLOCKER)
        self.assertEqual(report_dict["priority"], BugPriority.P1_HIGHEST)
        self.assertEqual(report_dict["assignee"], "be-tech-lead")

        markdown = report.to_jira_markdown()
        self.assertIn("h2. [Bug Report] Sample Bug", markdown)
        self.assertIn("*Category:* Backend/API", markdown)
        self.assertIn("*Severity:* Blocker", markdown)

    def test_bug_logger_capture_and_export(self):
        """Kiểm tra BugLogger bắt exception và xuất file JSON."""
        with tempfile.TemporaryDirectory() as tmp_dir:
            json_path = os.path.join(tmp_dir, "test_bugs.json")
            logger = BugLogger(output_file=json_path)

            try:
                # Tạo lỗi cố ý
                _ = 1 / 0
            except ZeroDivisionError as exc:
                report = logger.capture_exception(
                    exception=exc,
                    summary="[Test] ZeroDivisionError in calculation",
                    category=BugCategory.LOGIC_AI,
                    severity=BugSeverity.MAJOR,
                )

            self.assertIn("ZeroDivisionError", report.actual_result)

            exported_path = logger.export_to_json(report, file_path=json_path)
            self.assertTrue(os.path.exists(exported_path))

            with open(exported_path, "r", encoding="utf-8") as f:
                data = json.load(f)

            bugs = data.get("sample_bugs", [])
            self.assertEqual(len(bugs), 1)
            self.assertEqual(bugs[0]["summary"], "[Test] ZeroDivisionError in calculation")


if __name__ == "__main__":
    unittest.main()
