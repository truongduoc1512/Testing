param (
    [string]$HostName = "localhost",
    [int]$Port = 8080,
    [int]$Threads = 100,
    [int]$RampUp = 10,
    [int]$Duration = 60,
    [switch]$SkipServerCheck = $false,
    [switch]$NoOpenReport = $false
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$JmxFile = Join-Path $ProjectRoot "docs\jmeter\Shoeshop_Load_Test.jmx"
$TargetDir = Join-Path $ProjectRoot "target\jmeter"
$ResultsFile = Join-Path $TargetDir "results_${Threads}vu.jtl"
$ReportDir = Join-Path $TargetDir "html_report_${Threads}vu"
$ToolsDir = Join-Path $ProjectRoot "tools"
$PortableJmeter = Join-Path $ToolsDir "apache-jmeter-5.6.3\bin\jmeter.bat"
$AppDataJmeter = Join-Path $env:LOCALAPPDATA "apache-jmeter-5.6.3\bin\jmeter.bat"

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host " ShoeShop Performance & Load Testing (TEST-32)" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Check if ShoeShop server is running
if (-not $SkipServerCheck) {
    Write-Host "[1/3] Checking ShoeShop server availability at http://${HostName}:${Port} ..." -ForegroundColor Cyan
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $connect = $tcpClient.BeginConnect($HostName, $Port, $null, $null)
        $wait = $connect.AsyncWaitHandle.WaitOne(2000, $false)
        if (-not $wait) {
            $tcpClient.Close()
            throw "Connection timed out."
        }
        $tcpClient.EndConnect($connect)
        $tcpClient.Close()
        Write-Host " [OK] Server is UP and listening on port ${Port}." -ForegroundColor Green
    } catch {
        Write-Host " [WARNING] ShoeShop server is NOT running on port ${Port}!" -ForegroundColor Yellow
        Write-Host " Load testing requires the server to be running so JMeter can send HTTP requests." -ForegroundColor Yellow
        Write-Host " Tip: Open another terminal and run 'mvn spring-boot:run', then re-run this script." -ForegroundColor Yellow
        Write-Host " (If you still want to proceed, pass -SkipServerCheck)" -ForegroundColor DarkGray
        Write-Host ""
        $choice = Read-Host "Do you want to continue anyway? (y/N)"
        if ($choice -notmatch '^[yY]') {
            exit 1
        }
    }
}

# 2. Locate or auto-download JMeter
Write-Host "[2/3] Locating Apache JMeter ..." -ForegroundColor Cyan
$jmeterCmd = ""
if (Get-Command "jmeter" -ErrorAction SilentlyContinue) {
    $jmeterCmd = "jmeter"
    Write-Host " [OK] Found 'jmeter' in system PATH." -ForegroundColor Green
} elseif (Test-Path $PortableJmeter) {
    $jmeterCmd = $PortableJmeter
    Write-Host " [OK] Found portable JMeter at $PortableJmeter." -ForegroundColor Green
} elseif (Test-Path $AppDataJmeter) {
    $jmeterCmd = $AppDataJmeter
    Write-Host " [OK] Found portable JMeter at $AppDataJmeter." -ForegroundColor Green
} else {
    Write-Host " [INFO] JMeter not detected on your system." -ForegroundColor Yellow
    Write-Host " Downloading official portable Apache JMeter 5.6.3 (clean zip, no admin needed) ..." -ForegroundColor Cyan
    
    if (-not (Test-Path $ToolsDir)) {
        New-Item -ItemType Directory -Path $ToolsDir | Out-Null
    }
    
    $zipUrl = "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.zip"
    $zipDest = Join-Path $ToolsDir "apache-jmeter-5.6.3.zip"
    
    Write-Host " Downloading from $zipUrl (approx. 85 MB) ..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri $zipUrl -OutFile $zipDest -UseBasicParsing
    
    Write-Host " Extracting to $ToolsDir ..." -ForegroundColor Cyan
    Expand-Archive -Path $zipDest -DestinationPath $ToolsDir -Force
    Remove-Item $zipDest -Force
    
    $jmeterCmd = $PortableJmeter
    Write-Host " [OK] JMeter 5.6.3 ready at $PortableJmeter!" -ForegroundColor Green
}

# 3. Clean previous results if exist
if (Test-Path $ResultsFile) { Remove-Item $ResultsFile -Force }
if (Test-Path $ReportDir) { Remove-Item $ReportDir -Recurse -Force }
if (-not (Test-Path $TargetDir)) { New-Item -ItemType Directory -Path $TargetDir | Out-Null }

# 4. Execute Load Test
Write-Host "[3/3] Executing Load Test (${Threads} VUs, Ramp-up: ${RampUp}s, Duration: ${Duration}s) ..." -ForegroundColor Cyan
Write-Host " Target: http://${HostName}:${Port}" -ForegroundColor Cyan
Write-Host " Test Plan: $JmxFile" -ForegroundColor Cyan
Write-Host ""

& $jmeterCmd -n -t "$JmxFile" `
    "-Jhost=$HostName" `
    "-Jport=$Port" `
    "-Jthreads=$Threads" `
    "-Jrampup=$RampUp" `
    "-Jduration=$Duration" `
    -l "$ResultsFile" `
    -e -o "$ReportDir"

$ExitCode = $LASTEXITCODE
$IndexHtml = Join-Path $ReportDir "index.html"

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
if (Test-Path $IndexHtml) {
    Write-Host " [SUCCESS] HTML Dashboard Report generated at:" -ForegroundColor Green
    Write-Host " $IndexHtml" -ForegroundColor Green
    if (-not $NoOpenReport) {
        Start-Process $IndexHtml
    }
} else {
    Write-Host " [WARNING] Could not find generated HTML dashboard." -ForegroundColor Yellow
}
Write-Host "=============================================" -ForegroundColor Cyan

exit $ExitCode
