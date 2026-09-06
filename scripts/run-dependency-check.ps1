param (
    [switch]$AuditOnly = $false,
    [int]$FailOnCVSS = 8
)

$ErrorActionPreference = "Continue"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$EnvFile = Join-Path $ProjectRoot ".env"

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host " OWASP Dependency-Check (SCA Scanner)" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# Check if .env exists and parse NVD_API_KEY
if (Test-Path $EnvFile) {
    Get-Content $EnvFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -match '^NVD_API_KEY\s*=\s*(.+)$') {
            $env:NVD_API_KEY = $Matches[1].Trim(' "''')
        }
    }
}

if ([string]::IsNullOrWhiteSpace($env:NVD_API_KEY)) {
    Write-Host "[WARNING] NVD_API_KEY not found in .env file." -ForegroundColor Yellow
} else {
    Write-Host "[OK] Loaded NVD_API_KEY from .env successfully." -ForegroundColor Green
}

$cvssArg = if ($AuditOnly) { "-DfailBuildOnCVSS=11" } else { "-DfailBuildOnCVSS=$FailOnCVSS" }

Write-Host "Executing Maven Dependency-Check ($cvssArg) ..." -ForegroundColor Cyan
& mvn org.owasp:dependency-check-maven:check "-DnvdApiKey=$($env:NVD_API_KEY)" "$cvssArg"

$ExitCode = $LASTEXITCODE
$ReportPath = Join-Path $ProjectRoot "target\dependency-check-report.html"

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
if (Test-Path $ReportPath) {
    Write-Host "[SUCCESS] Report generated: $ReportPath" -ForegroundColor Green
}

if ($ExitCode -eq 0) {
    Write-Host "[PASS] Quality Gate Passed! No vulnerabilities above threshold." -ForegroundColor Green
} else {
    Write-Host "[QUALITY GATE TRIGGERED] Build failed because dependencies with CVSS >= $FailOnCVSS were found." -ForegroundColor Yellow
    Write-Host "This is EXPECTED behavior demonstrating that the Security Quality Gate works." -ForegroundColor Yellow
    Write-Host "Tip: Run '.\scripts\run-dependency-check.ps1 -AuditOnly' to generate report without failing build." -ForegroundColor Cyan
}
Write-Host "=============================================" -ForegroundColor Cyan

exit $ExitCode

