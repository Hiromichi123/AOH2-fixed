$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$installer = Join-Path $root 'release\AoC2-Bugfix-1.0.0\Install.ps1'
$game = Join-Path $root 'release\tests\game'
function Check([bool]$Condition,[string]$Message) { if (-not $Condition) { throw $Message }; Write-Output ('PASS '+$Message) }
$originalHash = (Get-FileHash -LiteralPath (Join-Path $root 'original\AoC2-before-launcher-patch.exe')).Hash
$fixedHash = (Get-FileHash -LiteralPath (Join-Path $root 'dist\AoC2-fixed.exe')).Hash
New-Item -ItemType Directory -Force -Path $game | Out-Null
Copy-Item -LiteralPath (Join-Path $root 'original\AoC2-before-launcher-patch.exe') -Destination (Join-Path $game 'AoC2.exe') -Force
$result = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $installer -GameDirectory $game 2>&1
Check ($LASTEXITCODE -eq 0) 'installation completed'
Check ((Get-FileHash -LiteralPath (Join-Path $game 'AoC2.exe')).Hash -eq $fixedHash) 'installer rebuilt the exact tested EXE'
$result = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $installer -GameDirectory $game 2>&1
Check ($LASTEXITCODE -eq 0 -and ($result -match 'already installed')) 'repeated installation is idempotent'
$result = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $installer -GameDirectory $game -Mode Uninstall 2>&1
Check ($LASTEXITCODE -eq 0) 'uninstall completed'
Check ((Get-FileHash -LiteralPath (Join-Path $game 'AoC2.exe')).Hash -eq $originalHash) 'uninstall restored original EXE exactly'
$bad = Join-Path $root 'release\tests\unsupported'
New-Item -ItemType Directory -Force -Path $bad | Out-Null
$badExe = Join-Path $bad 'AoC2.exe'
[System.IO.File]::WriteAllBytes($badExe,[byte[]](1,2,3,4))
$badHash = (Get-FileHash -LiteralPath $badExe).Hash
$ErrorActionPreference = 'Continue'
$result = & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $installer -GameDirectory $bad 2>&1
$exitCode = $LASTEXITCODE
$ErrorActionPreference = 'Stop'
Check ($exitCode -ne 0) 'unsupported game versions are refused'
Check ((Get-FileHash -LiteralPath $badExe).Hash -eq $badHash) 'unsupported EXE remains unchanged'
Check (-not (Test-Path -LiteralPath (Join-Path $bad 'AoC2.exe.aoc2bugfix-original.bak'))) 'unsupported input creates no backup or replacement'
Write-Output 'ALL RELEASE INSTALLER TESTS PASSED'
