[CmdletBinding()]
param(
    [string]$GameDirectory,
    [ValidateSet('Check','Install','Uninstall')][string]$Mode = 'Install'
)
$ErrorActionPreference = 'Stop'
function Hash([string]$Path) { (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
function Find-Game {
    $roots = New-Object 'System.Collections.Generic.List[string]'
    $steam = Get-ItemProperty -LiteralPath 'HKCU:\Software\Valve\Steam' -ErrorAction SilentlyContinue
    if ($steam -and $steam.SteamPath) {
        $roots.Add([string]$steam.SteamPath)
        $libraries = Join-Path $steam.SteamPath 'steamapps\libraryfolders.vdf'
        if (Test-Path -LiteralPath $libraries) {
            $vdf = Get-Content -LiteralPath $libraries -Raw
            foreach ($match in [regex]::Matches($vdf, '"path"\s+"([^"]+)"')) { $roots.Add($match.Groups[1].Value.Replace('\\','\')) }
        }
    }
    foreach ($root in $roots) {
        $candidate = Join-Path $root 'steamapps\common\AoCII'
        if (Test-Path -LiteralPath (Join-Path $candidate 'AoC2.exe')) { return $candidate }
    }
    return $null
}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead((Join-Path $PSScriptRoot 'patch-data.zip'))
try {
    $entry = $archive.GetEntry('manifest.json')
    if (-not $entry) { throw 'Patch manifest missing.' }
    $reader = New-Object System.IO.StreamReader($entry.Open())
    try { $manifest = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
    if ($manifest.format -ne 'aoc2-copy-literal-v1') { throw 'Unsupported patch format.' }
    if (-not $GameDirectory) { $GameDirectory = Find-Game }
    if (-not $GameDirectory) { $GameDirectory = Read-Host 'Enter the folder containing AoC2.exe' }
    $gameRoot = (Resolve-Path -LiteralPath $GameDirectory).ProviderPath
    $exe = Join-Path $gameRoot 'AoC2.exe'
    $backup = Join-Path $gameRoot 'AoC2.exe.aoc2bugfix-original.bak'
    $temporary = Join-Path $gameRoot 'AoC2.exe.aoc2bugfix.tmp'
    $currentHash = Hash $exe
    if ($Mode -eq 'Check') {
        if ($currentHash -eq $manifest.target_sha256) { Write-Output 'Compatible: this patch is already installed.'; return }
        if ($currentHash -eq $manifest.source_sha256) { Write-Output 'Compatible: supported original EXE.'; return }
        throw 'Unsupported EXE version or another code mod. Nothing was changed.'
    }
    if (Get-Process -Name AoC2 -ErrorAction SilentlyContinue) { throw 'Close Age of History II before installation or removal.' }
    if ($Mode -eq 'Uninstall') {
        if ($currentHash -eq $manifest.source_sha256) { Write-Output 'Original EXE is already installed.'; return }
        if ($currentHash -ne $manifest.target_sha256) { throw 'EXE changed since installation. Refusing to overwrite it.' }
        if (-not (Test-Path -LiteralPath $backup)) { throw 'Original EXE backup is missing.' }
        if ((Hash $backup) -ne $manifest.source_sha256) { throw 'Original backup checksum mismatch.' }
        Copy-Item -LiteralPath $backup -Destination $exe -Force
        if ((Hash $exe) -ne $manifest.source_sha256) { throw 'Restore verification failed.' }
        Write-Output 'Original EXE restored. Saves were not changed.'
        return
    }
    if ($currentHash -eq $manifest.target_sha256) { Write-Output 'This patch is already installed. Nothing was changed.'; return }
    if ($currentHash -ne $manifest.source_sha256) { throw 'Unsupported EXE version or another code mod. Nothing was changed.' }
    if (Test-Path -LiteralPath $temporary) { throw 'A previous temporary patch file exists. Review it before retrying.' }
    if (Test-Path -LiteralPath $backup) {
        if ((Hash $backup) -ne $manifest.source_sha256) { throw 'Existing backup differs. Refusing to overwrite it.' }
    } else { Copy-Item -LiteralPath $exe -Destination $backup }
    $literalEntry = $archive.GetEntry('literal.bin')
    if (-not $literalEntry) { throw 'Patch payload missing.' }
    $literalStream = New-Object System.IO.MemoryStream
    $compressed = $literalEntry.Open()
    try { $compressed.CopyTo($literalStream) } finally { $compressed.Dispose() }
    $source = [System.IO.File]::OpenRead($exe)
    $output = [System.IO.File]::Open($temporary, [System.IO.FileMode]::CreateNew, [System.IO.FileAccess]::Write)
    try {
        $buffer = New-Object byte[] 65536
        foreach ($op in $manifest.operations) {
            $offset = [long]$op[1]; $remaining = [long]$op[2]
            if ($offset -lt 0 -or $remaining -lt 0) { throw 'Invalid patch bounds.' }
            if ($op[0] -eq 'c') { $inputStream = $source }
            elseif ($op[0] -eq 'd') { $inputStream = $literalStream }
            else { throw 'Invalid patch operation.' }
            if ($offset -gt $inputStream.Length -or $remaining -gt ($inputStream.Length - $offset)) { throw 'Patch data exceeds input bounds.' }
            $inputStream.Position = $offset
            while ($remaining -gt 0) {
                $wanted = [int][Math]::Min([long]$buffer.Length,$remaining)
                $received = $inputStream.Read($buffer,0,$wanted)
                if ($received -le 0) { throw 'Unexpected end of patch input.' }
                $output.Write($buffer,0,$received); $remaining -= $received
            }
        }
        if ($output.Length -ne [long]$manifest.target_size) { throw 'Rebuilt EXE size mismatch.' }
    } finally { $output.Dispose(); $source.Dispose(); $literalStream.Dispose() }
    if ((Hash $temporary) -ne $manifest.target_sha256) {
        Remove-Item -LiteralPath $temporary
        throw 'Rebuilt EXE checksum mismatch. Original EXE retained.'
    }
    if ((Hash $exe) -ne $manifest.source_sha256) { throw 'EXE changed while building the patch. Original file was not replaced.' }
    Move-Item -LiteralPath $temporary -Destination $exe -Force
    if ((Hash $exe) -ne $manifest.target_sha256) { throw 'Installed EXE checksum mismatch. Original backup retained.' }
    Write-Output 'Patch installed. Start the game normally through Steam or AoC2.exe. Saves were not changed.'
} finally { $archive.Dispose() }
