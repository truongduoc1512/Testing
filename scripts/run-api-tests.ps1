<#
.SYNOPSIS
    Runs Black-box Postman Collections for ShoeShop using Newman CLI.
.DESCRIPTION
    Automates the execution of Postman test collections located in docs/test_cases/black_box.
    Supports running all modules, filtering by module name, running legacy collections,
    and generating interactive HTML reports with an aggregated summary dashboard.
.PARAMETER Module
    Filter which module to run. Default is "all".
    Examples: -Module "all", -Module "checkout", -Module "voucher", -Module "address"
.PARAMETER BaseUrl
    Override the base_url environment variable (e.g. "http://localhost:8080" or "http://localhost").
.PARAMETER Legacy
    Run the legacy monolithic collection (docs/Shoeshop_API_Collection.json).
.PARAMETER OpenReport
    Automatically open the HTML report dashboard in the default browser upon completion.
.PARAMETER Bail
    Stop execution on the first collection failure.
.PARAMETER List
    List all discovered black-box modules without running tests.
.EXAMPLE
    .\scripts\run-api-tests.ps1
    # Runs all 14 Black-box modules

.EXAMPLE
    .\scripts\run-api-tests.ps1 -Module "checkout"
    # Runs only the Checkout module

.EXAMPLE
    .\scripts\run-api-tests.ps1 -Module "voucher" -OpenReport
    # Runs the Voucher module and opens HTML report
#>

[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [string]$Module = "all",

    [Parameter()]
    [string]$BaseUrl = "",

    [Parameter()]
    [switch]$Legacy,

    [Parameter()]
    [switch]$OpenReport,

    [Parameter()]
    [switch]$Bail,

    [Parameter()]
    [switch]$List
)

$ErrorActionPreference = "Continue"

# Resolve Project Root Directory
$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

$ReportBaseDir = Join-Path $RootDir "target\blackbox-reports"
if (-Not (Test-Path -Path $ReportBaseDir)) {
    New-Item -ItemType Directory -Path $ReportBaseDir -Force | Out-Null
}

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "         SHOESHOP - AUTOMATED BLACK-BOX API TEST RUNNER (NEWMAN CLI)           " -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan

# -----------------------------------------------------------------------------
# Case 1: Legacy Collection Run
# -----------------------------------------------------------------------------
if ($Legacy) {
    $LegacyCol = "docs/Shoeshop_API_Collection.json"
    $LegacyEnv = "docs/Shoeshop_Postman_Environment.json"
    $LegacyReport = Join-Path $ReportBaseDir "legacy-api-report.html"

    Write-Host "[LEGACY MODE] Running Monolithic Collection: $LegacyCol" -ForegroundColor Yellow
    Write-Host "Environment: $LegacyEnv"
    Write-Host "Report Output: $LegacyReport"
    Write-Host "--------------------------------------------------------------------------------" -ForegroundColor Cyan

    $cmdArgs = @(
        "-p", "newman",
        "-p", "newman-reporter-htmlextra",
        "newman", "run", $LegacyCol,
        "-e", $LegacyEnv,
        "-r", "cli,htmlextra",
        "--reporter-htmlextra-export", $LegacyReport,
        "--insecure"
    )
    if ($BaseUrl) {
        $cmdArgs += @("--env-var", "base_url=$BaseUrl")
    }
    if ($Bail) {
        $cmdArgs += "--bail"
    }

    & npx @cmdArgs
    $exitCode = $LASTEXITCODE

    if ($exitCode -eq 0) {
        Write-Host "`nLegacy API Tests completed SUCCESSFULLY!" -ForegroundColor Green
    } else {
        Write-Host "`nLegacy API Tests FAILED with exit code $exitCode." -ForegroundColor Red
    }
    if ($OpenReport -and (Test-Path $LegacyReport)) {
        Start-Process $LegacyReport
    }
    exit $exitCode
}

# -----------------------------------------------------------------------------
# Case 2: Discover all Black-box modules in docs/test_cases/black_box
# -----------------------------------------------------------------------------
$BlackBoxDir = Join-Path $RootDir "docs\test_cases\black_box"

if (-Not (Test-Path $BlackBoxDir)) {
    Write-Host "ERROR: Directory not found: $BlackBoxDir" -ForegroundColor Red
    exit 1
}

$DefaultEnv = Join-Path $RootDir "docs\Shoeshop_Postman_Environment.json"

# Scan subfolders
$SubDirs = Get-ChildItem -Path $BlackBoxDir -Directory | Sort-Object Name
$ModuleList = @()

foreach ($dir in $SubDirs) {
    $colFile = Get-ChildItem -Path $dir.FullName -Filter "*_Collection.json" -File | Select-Object -First 1
    if (-not $colFile) {
        continue
    }

    $envFile = Get-ChildItem -Path $dir.FullName -Filter "*_Environment.json" -File | Select-Object -First 1
    $envPath = if ($envFile) { $envFile.FullName } else { $DefaultEnv }

    $ModuleList += [PSCustomObject]@{
        Name           = $dir.Name
        Folder         = $dir.Name
        CollectionPath = $colFile.FullName
        CollectionFile = $colFile.Name
        EnvironmentPath= $envPath
        EnvironmentFile= if ($envFile) { $envFile.Name } else { "Shoeshop_Postman_Environment.json (Default)" }
    }
}

if ($ModuleList.Count -eq 0) {
    Write-Host "ERROR: No Postman Collections found in $BlackBoxDir" -ForegroundColor Red
    exit 1
}

# If -List switch was passed, display modules and exit
if ($List) {
    Write-Host "Discovered $($ModuleList.Count) Black-box Test Modules in docs/test_cases/black_box:`n" -ForegroundColor Yellow
    $idx = 1
    foreach ($m in $ModuleList) {
        Write-Host ("  {0,2}. {1,-32} -> {2}" -f $idx, $m.Name, $m.CollectionFile)
        $idx++
    }
    Write-Host "`nUse: .\scripts\run-api-tests.ps1 -Module <Tên_Module> để chạy riêng một module." -ForegroundColor Cyan
    exit 0
}

# Filter by -Module parameter
$TargetModules = @()
if ($Module -and $Module -ne "all") {
    $filterPattern = $Module.ToLower()
    $TargetModules = $ModuleList | Where-Object { $_.Name.ToLower() -like "*$filterPattern*" }

    if ($TargetModules.Count -eq 0) {
        Write-Host "ERROR: No module matches filter: '$Module'" -ForegroundColor Red
        Write-Host "`nAvailable modules:" -ForegroundColor Yellow
        foreach ($m in $ModuleList) {
            Write-Host "  - $($m.Name)"
        }
        exit 1
    }
} else {
    $TargetModules = $ModuleList
}

Write-Host "Target Modules to Execute: $($TargetModules.Count) / $($ModuleList.Count) modules" -ForegroundColor Green
if ($BaseUrl) {
    Write-Host "Base URL Override        : $BaseUrl" -ForegroundColor Yellow
}
Write-Host "HTML Reports Directory   : $ReportBaseDir"
Write-Host "--------------------------------------------------------------------------------" -ForegroundColor Cyan

# -----------------------------------------------------------------------------
# Execution Loop
# -----------------------------------------------------------------------------
$Results = @()
$OverallStartTime = Get-Date
$ModuleIdx = 1

foreach ($mod in $TargetModules) {
    Write-Host "`n[$ModuleIdx/$($TargetModules.Count)] RUNNING MODULE: $($mod.Name)" -ForegroundColor Yellow
    Write-Host "  Collection : $($mod.CollectionPath)"
    Write-Host "  Environment: $($mod.EnvironmentPath)"

    # Reset baseline test accounts in MySQL to guarantee isolation and prevent cross-module side effects
    try {
        docker exec -i shoeshop-mysql mysql -uroot -ptruonghoaiduoc5 shoe_shopdb -e "UPDATE Accounts SET USER_ROLE='ADMIN', ACTIVE=1, ACCOUNT_NON_LOCKED=1, FAILED_ATTEMPTS=0 WHERE USER_NAME IN ('manager1', 'admin2'); UPDATE Accounts SET USER_ROLE='USER', ACTIVE=1, ACCOUNT_NON_LOCKED=1, FAILED_ATTEMPTS=0 WHERE USER_NAME='employee1'; UPDATE Accounts SET USER_ROLE='ROLE_USER' WHERE USER_NAME='testadmin';" 2>$null
    } catch {
        # ignore if container is unreachable
    }

    $ReportFileName = "$($mod.Folder)-report.html"
    $ReportFilePath = Join-Path $ReportBaseDir $ReportFileName

    $cmdArgs = @(
        "-p", "newman",
        "-p", "newman-reporter-htmlextra",
        "newman", "run", $mod.CollectionPath,
        "-e", $mod.EnvironmentPath,
        "-r", "cli,htmlextra",
        "--reporter-htmlextra-export", $ReportFilePath,
        "--insecure"
    )

    if ($BaseUrl) {
        $cmdArgs += @("--env-var", "base_url=$BaseUrl")
    }
    if ($Bail) {
        $cmdArgs += "--bail"
    }

    $modStartTime = Get-Date
    & npx @cmdArgs
    $modExitCode = $LASTEXITCODE
    $modDuration = ((Get-Date) - $modStartTime).TotalSeconds

    $isPassed = ($modExitCode -eq 0)
    $statusText = if ($isPassed) { "PASS" } else { "FAIL" }
    $statusColor = if ($isPassed) { "Green" } else { "Red" }

    Write-Host "  Result: $statusText (Exit Code: $modExitCode, Duration: $([math]::Round($modDuration, 1))s)" -ForegroundColor $statusColor
    Write-Host "  Report: $ReportFilePath"

    $Results += [PSCustomObject]@{
        Index       = $ModuleIdx
        Module      = $mod.Name
        Collection  = $mod.CollectionFile
        Passed      = $isPassed
        Status      = $statusText
        ExitCode    = $modExitCode
        DurationSec = [math]::Round($modDuration, 1)
        ReportFile  = $ReportFileName
        ReportPath  = $ReportFilePath
    }

    if ($Bail -and (-not $isPassed)) {
        Write-Host "`nExecution stopped early due to -Bail on failure in $($mod.Name)." -ForegroundColor Red
        break
    }

    $ModuleIdx++
}

$TotalDuration = ((Get-Date) - $OverallStartTime).TotalSeconds
$TotalModules = $Results.Count
$PassedModules = @($Results | Where-Object { $_.Passed }).Count
$FailedModules = @($Results | Where-Object { -not $_.Passed }).Count

# -----------------------------------------------------------------------------
# Generate Aggregate HTML Dashboard (index.html)
# -----------------------------------------------------------------------------
$DashboardPath = Join-Path $ReportBaseDir "index.html"
$SummaryColor = if ($FailedModules -eq 0) { "#22c55e" } else { "#ef4444" }
$SummaryStatus = if ($FailedModules -eq 0) { "ALL TESTS PASSED" } else { "$FailedModules MODULE(S) FAILED" }

$RowsHtml = ""
foreach ($res in $Results) {
    $badgeBg = if ($res.Passed) { "#16a34a" } else { "#dc2626" }
    $linkHtml = if (Test-Path $res.ReportPath) {
        "<a href='./$($res.ReportFile)' target='_blank' style='color:#38bdf8;text-decoration:none;font-weight:600;'>View HTML Report &rarr;</a>"
    } else {
        "<span style='color:#94a3b8;'>Report not generated</span>"
    }

    $RowsHtml += @"
        <tr style="border-bottom: 1px solid #334155;">
            <td style="padding: 12px 16px; text-align: center;">$($res.Index)</td>
            <td style="padding: 12px 16px; font-weight: 600;">$($res.Module)</td>
            <td style="padding: 12px 16px; font-family: monospace; font-size: 13px; color: #94a3b8;">$($res.Collection)</td>
            <td style="padding: 12px 16px; text-align: center;">
                <span style="background: $badgeBg; color: white; padding: 4px 12px; border-radius: 9999px; font-size: 12px; font-weight: bold;">$($res.Status)</span>
            </td>
            <td style="padding: 12px 16px; text-align: center; color: #cbd5e1;">$($res.DurationSec)s</td>
            <td style="padding: 12px 16px; text-align: center;">$linkHtml</td>
        </tr>
"@
}

$HtmlTemplate = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ShoeShop - Black-Box API Test Dashboard</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            background-color: #0f172a;
            color: #f8fafc;
            margin: 0;
            padding: 32px 20px;
        }
        .container {
            max-width: 1100px;
            margin: 0 auto;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 2px solid #334155;
            padding-bottom: 20px;
            margin-bottom: 28px;
        }
        h1 { margin: 0; font-size: 26px; color: #f8fafc; }
        .subtitle { color: #94a3b8; font-size: 14px; margin-top: 6px; }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 16px;
            margin-bottom: 28px;
        }
        .stat-card {
            background: #1e293b;
            border: 1px solid #334155;
            border-radius: 10px;
            padding: 18px;
            text-align: center;
        }
        .stat-val { font-size: 30px; font-weight: bold; margin-top: 6px; }
        table {
            width: 100%;
            border-collapse: collapse;
            background: #1e293b;
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3);
        }
        th {
            background: #0f172a;
            color: #94a3b8;
            text-transform: uppercase;
            font-size: 12px;
            letter-spacing: 0.05em;
            padding: 14px 16px;
        }
        tr:hover { background-color: #273549; }
        .footer {
            text-align: center;
            margin-top: 32px;
            color: #64748b;
            font-size: 13px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div>
                <h1>👟 ShoeShop Black-Box API Test Execution Dashboard</h1>
                <div class="subtitle">Generated by Newman &amp; scripts/run-api-tests.ps1 | $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")</div>
            </div>
            <div>
                <span style="background: $SummaryColor; color: white; padding: 8px 18px; border-radius: 8px; font-weight: bold; font-size: 14px;">
                    $SummaryStatus
                </span>
            </div>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div style="color: #94a3b8; font-size: 13px; font-weight: 500;">TOTAL MODULES</div>
                <div class="stat-val" style="color: #38bdf8;">$TotalModules</div>
            </div>
            <div class="stat-card">
                <div style="color: #94a3b8; font-size: 13px; font-weight: 500;">PASSED MODULES</div>
                <div class="stat-val" style="color: #22c55e;">$PassedModules</div>
            </div>
            <div class="stat-card">
                <div style="color: #94a3b8; font-size: 13px; font-weight: 500;">FAILED MODULES</div>
                <div class="stat-val" style="color: #ef4444;">$FailedModules</div>
            </div>
            <div class="stat-card">
                <div style="color: #94a3b8; font-size: 13px; font-weight: 500;">TOTAL DURATION</div>
                <div class="stat-val" style="color: #f59e0b;">$([math]::Round($TotalDuration, 1))s</div>
            </div>
        </div>

        <table>
            <thead>
                <tr>
                    <th style="width: 50px;">#</th>
                    <th>Module Name</th>
                    <th>Postman Collection File</th>
                    <th style="width: 100px;">Status</th>
                    <th style="width: 100px;">Duration</th>
                    <th style="width: 180px;">Report</th>
                </tr>
            </thead>
            <tbody>
                $RowsHtml
            </tbody>
        </table>

        <div class="footer">
            ShoeShop Quality Assurance &bull; Automated Test Execution
        </div>
    </div>
</body>
</html>
"@

Set-Content -Path $DashboardPath -Value $HtmlTemplate -Encoding UTF8

# -----------------------------------------------------------------------------
# Console Final Summary
# -----------------------------------------------------------------------------
Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "                         FINAL EXECUTION SUMMARY                                " -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host ("{0,-4} | {1,-32} | {2,-8} | {3,-10} | {4}" -f "#", "Module", "Status", "Duration", "Report")
Write-Host "--------------------------------------------------------------------------------"

foreach ($res in $Results) {
    $stColor = if ($res.Passed) { "Green" } else { "Red" }
    Write-Host ("{0,-4} | {1,-32} | " -f $res.Index, $res.Module) -NoNewline
    Write-Host ("{0,-8}" -f $res.Status) -ForegroundColor $stColor -NoNewline
    Write-Host (" | {0,-10} | {1}" -f "$($res.DurationSec)s", $res.ReportFile)
}

Write-Host "--------------------------------------------------------------------------------"
Write-Host "Total Modules Executed : $TotalModules"
Write-Host "Passed Modules         : $PassedModules" -ForegroundColor Green
if ($FailedModules -gt 0) {
    Write-Host "Failed Modules         : $FailedModules" -ForegroundColor Red
}
Write-Host "Total Execution Time   : $([math]::Round($TotalDuration, 1)) seconds"
Write-Host "Aggregated Dashboard   : $DashboardPath" -ForegroundColor Cyan

if ($OpenReport) {
    if (Test-Path $DashboardPath) {
        Start-Process $DashboardPath
    }
}

if ($FailedModules -gt 0) {
    exit 1
} else {
    exit 0
}
