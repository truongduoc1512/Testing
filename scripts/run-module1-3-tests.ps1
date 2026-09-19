<#
.SYNOPSIS
    Runs Module 01 & 03 EP & BVA Postman Test Collection using Newman.
.DESCRIPTION
    Automates execution of granular Equivalence Partitioning (EP) and Robustness BVA (6n+1)
    test cases for Module 01 (Authentication) and Module 03 (Shopping Cart),
    generating an interactive HTML report via newman-reporter-htmlextra.
#>

$ErrorActionPreference = "Stop"

$CollectionPath = "docs/Shoeshop_Module1_3_EP_BVA_Collection.json"
$EnvironmentPath = "docs/Shoeshop_Postman_Environment.json"
$ReportDir = "target"
$ReportPath = "$ReportDir/module1_3_ep_bva_report.html"

if (-Not (Test-Path -Path $ReportDir)) {
    New-Item -ItemType Directory -Path $ReportDir | Out-Null
}

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " Running Module 01 & 03 EP/BVA Granular Test Suite" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Collection : $CollectionPath"
Write-Host "Environment: $EnvironmentPath"
Write-Host "Report Out : $ReportPath"
Write-Host "------------------------------------------------------------" -ForegroundColor Cyan

npx -p newman -p newman-reporter-htmlextra newman run $CollectionPath `
    -e $EnvironmentPath `
    -r cli,htmlextra `
    --reporter-htmlextra-export $ReportPath `
    --insecure

$ExitCode = $LASTEXITCODE

Write-Host "------------------------------------------------------------" -ForegroundColor Cyan
if ($ExitCode -eq 0) {
    Write-Host "All EP & BVA Tests completed SUCCESSFULLY (100% PASS)!" -ForegroundColor Green
    Write-Host "View interactive HTML report at: $ReportPath" -ForegroundColor Green
    Start-Process $ReportPath
} else {
    Write-Host "EP & BVA Tests completed with FAILURES." -ForegroundColor Red
}

exit $ExitCode
