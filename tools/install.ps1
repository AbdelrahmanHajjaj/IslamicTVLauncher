# Usage: ./tools/install.ps1 -Tv <address>:5555 [-Exclusive] [-SkipBuild]
param([string]$Tv = $env:TV, [switch]$Exclusive, [switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
if (-not $Tv) { throw 'Supply -Tv or set the TV environment variable.' }
$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not (Get-ChildItem -LiteralPath (Join-Path $repoRoot 'app/src/main/assets/adhan') -Filter '*.mp3')) {
    Write-Warning 'Adhan recordings are missing. Run tools/fetch-adhans.sh before building for audible calls.'
}
if (-not $SkipBuild) {
    Push-Location -LiteralPath $repoRoot
    try {
        & ./gradlew.bat :app:assembleDebug --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Build failed; TV unchanged.' }
    } finally { Pop-Location }
}
$deviceArgs = @((Join-Path $PSScriptRoot 'device.py'), '--tv', $Tv, 'install')
if ($Exclusive) { $deviceArgs += '--exclusive' }
& python -S -B @deviceArgs
exit $LASTEXITCODE
