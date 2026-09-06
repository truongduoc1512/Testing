<#
.SYNOPSIS
    Runs the Shoeshop API Postman Collection using Newman.
.DESCRIPTION
    This script automates the execution of the Shoeshop API Postman collection using the Newman CLI.
    It utilizes the environment file docs/Shoeshop_Postman_Environment.json and generates
    both CLI and HTML reports using newman-reporter-htmlextra.
#>

$ErrorActionPreference = "Stop"

# Define Paths
$CollectionPath = "docs/Shoeshop_API_Collection.json"
$EnvironmentPath = "docs/Shoeshop_Postman_Environment.json"
$ReportDir = "target"
$ReportPath = "$ReportDir/newman-report.html"

# Ensure the target directory exists for reports
if (-Not (Test-Path -Path $ReportDir)) {
    Write-Host "Creating report directory: $ReportDir" -ForegroundColor Cyan
    New-Item -ItemType Directory -Path $ReportDir | Out-Null
}

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host " Starting API Test Execution via Newman" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Collection: $CollectionPath"
Write-Host "Environment: $EnvironmentPath"
Write-Host "Report output: $ReportPath"
Write-Host "---------------------------------------------" -ForegroundColor Cyan

# Execute newman via npx. npx will download the required packages dynamically if not installed.
# We use --yes to automatically confirm the download prompt.
npx --yes newman run $CollectionPath `
    -e $EnvironmentPath `
    -r cli,htmlextra `
    --reporter-htmlextra-export $ReportPath `
    --insecure

$ExitCode = $LASTEXITCODE

Write-Host "---------------------------------------------" -ForegroundColor Cyan
if ($ExitCode -eq 0) {
    Write-Host "API Tests completed SUCCESSFULLY." -ForegroundColor Green
    Write-Host "View the HTML report at: $ReportPath" -ForegroundColor Green
} else {
    Write-Host "API Tests completed with ERRORS/FAILURES." -ForegroundColor Red
    Write-Host "View the HTML report at: $ReportPath" -ForegroundColor Red
}

exit $ExitCode
