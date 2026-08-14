[CmdletBinding()]
param(
    [switch]$OpenReport,

    [ValidatePattern('^[A-Za-z0-9_.$,*?]+$')]
    [string]$TestSelector
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$ContainerName = 'shoeshop-mysql'
$ContainerMySqlPort = '3306/tcp'
$JaCoCoVersion = '0.8.15'
$RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$DatabaseNamePattern = '^shoeshop_cov_[0-9a-f]{32}$'
$DatabaseName = 'shoeshop_cov_' + [Guid]::NewGuid().ToString('N')
$DatasourceVariables = @(
    'SPRING_DATASOURCE_URL',
    'SPRING_DATASOURCE_USERNAME',
    'SPRING_DATASOURCE_PASSWORD'
)

function Assert-NativeCommandSucceeded {
    param(
        [Parameter(Mandatory = $true)]
        [int]$ExitCode,

        [Parameter(Mandatory = $true)]
        [string]$FailureMessage
    )

    if ($ExitCode -ne 0) {
        throw "$FailureMessage Exit code: $ExitCode."
    }
}

function Assert-SafeDatabaseName {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    # This exact allowlist is the safety boundary for CREATE/DROP DATABASE.
    if ($Name -notmatch $DatabaseNamePattern) {
        throw "Unsafe temporary database name: $Name"
    }
}

function Invoke-RootMySql {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Password,

        [Parameter(Mandatory = $true)]
        [string]$Statement,

        [Parameter(Mandatory = $true)]
        [string]$FailureMessage
    )

    & docker exec --env "MYSQL_PWD=$Password" $ContainerName `
        mysql -uroot --execute $Statement
    Assert-NativeCommandSucceeded $LASTEXITCODE $FailureMessage
}

function Save-EnvironmentVariables {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Names
    )

    $snapshot = @{}
    foreach ($name in $Names) {
        $exists = Test-Path "Env:$name"
        $snapshot[$name] = [pscustomobject]@{
            Exists = $exists
            Value = if ($exists) { (Get-Item "Env:$name").Value } else { $null }
        }
    }
    return $snapshot
}

function Restore-EnvironmentVariables {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Snapshot
    )

    foreach ($name in $Snapshot.Keys) {
        $savedVariable = $Snapshot[$name]
        if ($savedVariable.Exists) {
            Set-Item "Env:$name" $savedVariable.Value
        }
        else {
            Remove-Item "Env:$name" -ErrorAction SilentlyContinue
        }
    }
}

Assert-SafeDatabaseName $DatabaseName
foreach ($commandName in @('docker', 'mvn')) {
    if (-not (Get-Command $commandName -ErrorAction SilentlyContinue)) {
        throw "Required command '$commandName' was not found in PATH."
    }
}

$environmentSnapshot = Save-EnvironmentVariables $DatasourceVariables
$databaseCleanupRequired = $false
$locationPushed = $false
$runError = $null
$cleanupError = $null

try {
    Push-Location $RepositoryRoot
    $locationPushed = $true

    # Phase 1: ensure only the MySQL service needed by integration tests is running.
    $matchingContainers = @(
        & docker ps -a --filter "name=^/$ContainerName$" --format '{{.Names}}'
    )
    Assert-NativeCommandSucceeded $LASTEXITCODE 'Unable to inspect Docker containers.'

    if ($matchingContainers -notcontains $ContainerName) {
        Write-Host "Creating $ContainerName with Docker Compose..."
        & docker compose up -d $ContainerName
        Assert-NativeCommandSucceeded $LASTEXITCODE "Unable to create $ContainerName."
    }
    else {
        $isRunning = & docker inspect --format '{{.State.Running}}' $ContainerName
        Assert-NativeCommandSucceeded $LASTEXITCODE "Unable to inspect $ContainerName."

        if ($isRunning -ne 'true') {
            Write-Host "Starting $ContainerName..."
            & docker start $ContainerName | Out-Null
            Assert-NativeCommandSucceeded $LASTEXITCODE "Unable to start $ContainerName."
        }
    }

    Write-Host 'Waiting for the test database service to become healthy...'
    $healthDeadline = [DateTime]::UtcNow.AddSeconds(90)
    while ([DateTime]::UtcNow -lt $healthDeadline) {
        $healthStatus = & docker inspect `
            --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' `
            $ContainerName
        Assert-NativeCommandSucceeded $LASTEXITCODE `
            "Unable to read the health status of $ContainerName."

        if ($healthStatus -in @('healthy', 'running')) {
            break
        }
        if ($healthStatus -eq 'unhealthy') {
            throw "$ContainerName is unhealthy."
        }
        Start-Sleep -Seconds 2
    }
    if ($healthStatus -notin @('healthy', 'running')) {
        throw "Timed out after 90 seconds waiting for $ContainerName."
    }

    # Phase 2: discover connection details without hardcoding or printing secrets.
    $publishedPorts = @(& docker port $ContainerName $ContainerMySqlPort)
    Assert-NativeCommandSucceeded $LASTEXITCODE `
        "Unable to read the published MySQL port of $ContainerName."
    $databasePort = $publishedPorts |
        ForEach-Object { if ($_ -match ':(?<Port>[0-9]+)$') { $Matches.Port } } |
        Select-Object -First 1
    if (-not $databasePort) {
        throw "$ContainerName does not publish MySQL port $ContainerMySqlPort to the host."
    }

    $containerEnvironment = @(
        & docker inspect --format '{{range .Config.Env}}{{println .}}{{end}}' $ContainerName
    )
    Assert-NativeCommandSucceeded $LASTEXITCODE `
        "Unable to read the environment of $ContainerName."
    $passwordSetting = $containerEnvironment |
        Where-Object { $_ -like 'MYSQL_ROOT_PASSWORD=*' } |
        Select-Object -First 1
    if (-not $passwordSetting) {
        throw "MYSQL_ROOT_PASSWORD was not found in $ContainerName."
    }
    $databasePassword = $passwordSetting.Substring('MYSQL_ROOT_PASSWORD='.Length)

    # Phase 3: run only against a disposable schema with a validated UUID name.
    Write-Host "Creating isolated database $DatabaseName..."
    # DROP IF EXISTS is required even if Docker reports an ambiguous CREATE error.
    $databaseCleanupRequired = $true
    Invoke-RootMySql `
        -Password $databasePassword `
        -Statement "CREATE DATABASE $DatabaseName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" `
        -FailureMessage "Unable to create temporary database $DatabaseName."
    $env:SPRING_DATASOURCE_URL = `
        'jdbc:mysql://localhost:{0}/{1}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' `
        -f $databasePort, $DatabaseName
    $env:SPRING_DATASOURCE_USERNAME = 'root'
    $env:SPRING_DATASOURCE_PASSWORD = $databasePassword

    # Phase 4: clean stale output, execute the requested suite, and generate one report.
    $coverageStartedAt = [DateTime]::UtcNow
    $mavenArguments = @(
        '-B',
        '-ntp',
        'clean',
        "org.jacoco:jacoco-maven-plugin:${JaCoCoVersion}:prepare-agent"
    )
    if ([string]::IsNullOrWhiteSpace($TestSelector)) {
        Write-Host 'Running the complete test suite and generating the JaCoCo report...'
    }
    else {
        Write-Host "Running selected tests ($TestSelector) and generating the JaCoCo report..."
        $mavenArguments += "-Dtest=$TestSelector"
    }
    $mavenArguments += @(
        'test',
        "org.jacoco:jacoco-maven-plugin:${JaCoCoVersion}:report"
    )
    & mvn @mavenArguments
    Assert-NativeCommandSucceeded $LASTEXITCODE 'Maven test/coverage failed.'

    # Phase 5: reject missing, empty, or stale artifacts before reporting success.
    $relativeArtifacts = @(
        'target\jacoco.exec',
        'target\site\jacoco\index.html',
        'target\site\jacoco\jacoco.xml',
        'target\site\jacoco\jacoco.csv'
    )
    foreach ($relativeArtifact in $relativeArtifacts) {
        $artifactPath = Join-Path $RepositoryRoot $relativeArtifact
        if (-not (Test-Path -LiteralPath $artifactPath -PathType Leaf)) {
            throw "Coverage artifact was not generated: $relativeArtifact"
        }

        $artifact = Get-Item -LiteralPath $artifactPath
        if ($artifact.Length -eq 0) {
            throw "Coverage artifact is empty: $relativeArtifact"
        }
        if ($artifact.LastWriteTimeUtc -lt $coverageStartedAt.AddSeconds(-2)) {
            throw "Coverage artifact is stale: $relativeArtifact"
        }
    }

    $reportPath = Join-Path $RepositoryRoot 'target\site\jacoco\index.html'
    Write-Host "JaCoCo report: $reportPath"
    if ($OpenReport) {
        Start-Process $reportPath
    }
}
catch {
    $runError = $_
}
finally {
    if ($databaseCleanupRequired) {
        try {
            Assert-SafeDatabaseName $DatabaseName
            Write-Host "Dropping isolated database $DatabaseName..."
            Invoke-RootMySql `
                -Password $databasePassword `
                -Statement "DROP DATABASE IF EXISTS $DatabaseName;" `
                -FailureMessage "Unable to drop temporary database $DatabaseName."
        }
        catch {
            $cleanupError = $_
        }
    }

    Restore-EnvironmentVariables $environmentSnapshot
    if ($locationPushed) {
        Pop-Location
    }
}

if ($runError -and $cleanupError) {
    throw "Coverage run failed: $($runError.Exception.Message) Cleanup also failed: $($cleanupError.Exception.Message)"
}
if ($runError) {
    throw $runError
}
if ($cleanupError) {
    throw $cleanupError
}
