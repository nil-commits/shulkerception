param(
    [string[]]$Minecraft = @(),
    [ValidateSet('all', 'bukkit', 'paper')][string]$Edition = 'all',
    [switch]$Offline,
    [switch]$Resume
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Push-Location $PSScriptRoot
try {
    [xml]$project = Get-Content -LiteralPath 'pom.xml' -Raw
    $version = [string]$project.project.version
    $author = [string]$project.project.properties.'plugin.author'
    if ($version -notmatch '^\d+\.\d+\.\d+$') { throw 'Expected a numeric release version.' }
    $maven = Get-Command mvn -ErrorAction SilentlyContinue
    $mavenPath = if ($maven) { $maven.Source } else { Join-Path $PSScriptRoot '.tools/apache-maven-3.9.11/bin/mvn.cmd' }
    if (!(Test-Path -LiteralPath $mavenPath)) { throw 'Install Maven 3.9+ and JDK 25 first.' }
    $targets = @(Get-Content -LiteralPath 'release-targets.json' -Raw | ConvertFrom-Json)
    $selected = @($targets | Where-Object {
        ($Minecraft.Count -eq 0 -or $Minecraft -contains $_.minecraft) -and
        ($Edition -eq 'all' -or $_.edition -eq $Edition)
    })
    if ($selected.Count -eq 0) { throw 'No published API targets match the selection.' }
    $output = Join-Path $PSScriptRoot "dist/v$version"
    $logs = Join-Path $PSScriptRoot 'target/release-logs'
    New-Item -ItemType Directory -Path $output, $logs -Force | Out-Null
    Add-Type -AssemblyName System.IO.Compression.FileSystem

    # Resume is valid only for the exact source, tests, POM and API lock used before.
    $inputs = @('pom.xml', 'release-targets.json') + @(Get-ChildItem -LiteralPath src -File -Recurse | Sort-Object FullName | ForEach-Object FullName)
    $fingerprint = (($inputs | ForEach-Object { (Get-FileHash -LiteralPath $_ -Algorithm SHA256).Hash }) -join ':')
    $records = @()
    $failed = @()
    $index = 0
    foreach ($target in $selected) {
        $index++
        $id = "$($target.edition)-$($target.minecraft)"
        $name = "shulkerception-$version-mc$($target.minecraft)-$($target.edition).jar"
        $destination = Join-Path $output $name
        $receiptPath = Join-Path $logs "$id.receipt.json"
        $legacySettings = if ($id -eq 'paper-1.20.5') { Join-Path $PSScriptRoot 'compatibility/paper-1.20.5-settings.xml' } else { $null }
        $targetFingerprint = $fingerprint
        if ($legacySettings) { $targetFingerprint += ':' + (Get-FileHash -LiteralPath $legacySettings -Algorithm SHA256).Hash }
        if ($Resume -and (Test-Path -LiteralPath $receiptPath) -and (Test-Path -LiteralPath $destination)) {
            $receipt = Get-Content -LiteralPath $receiptPath -Raw | ConvertFrom-Json
            if ($receipt.fingerprint -eq $targetFingerprint -and $receipt.record.sha256 -eq (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash.ToLowerInvariant()) {
                $records += $receipt.record
                Write-Host "[$index/$($selected.Count)] Verified cached $id"
                continue
            }
        }
        $log = Join-Path $logs "$id.log"
        Write-Host "[$index/$($selected.Count)] Building $id (Java $($target.java))"
        $arguments = @('-B', '-ntp', '-P', $target.edition, '-Dmaven.repo.local=.m2',
            "-Dminecraft.version=$($target.minecraft)", "-Dserver.api.group=$($target.group)",
            "-Dserver.api.artifact=$($target.artifact)", "-Dserver.api.version=$($target.api)",
            "-Dmaven.compiler.release=$($target.java)", "-Dplugin.api.declaration=$($target.apiDeclaration)")
        if ($Offline) { $arguments += '-o' }
        if ($legacySettings) { $arguments += @('-s', $legacySettings, '-P', 'paper,paper-1.20.5-archived-adventure') }
        & $mavenPath @arguments package *> $log
        if ($LASTEXITCODE -ne 0) {
            $failed += $id
            Write-Host "FAILED $id; see $log"
            continue
        }
        $targetDir = Join-Path $PSScriptRoot "target/$($target.edition)/$($target.minecraft)"
        $tests = 0
        $failures = 0
        $reports = @(Get-ChildItem -Path (Join-Path $targetDir 'surefire-reports/TEST-*.xml'))
        foreach ($report in $reports) {
            [xml]$xml = Get-Content -LiteralPath $report.FullName
            $tests += [int]$xml.testsuite.tests
            $failures += [int]$xml.testsuite.failures + [int]$xml.testsuite.errors
        }
        if ($tests -lt 1 -or $failures -gt 0) { throw "$id has no passing test report." }
        $built = Join-Path $targetDir $name
        $archive = [System.IO.Compression.ZipFile]::OpenRead($built)
        try {
            $reader = [System.IO.StreamReader]::new($archive.GetEntry('plugin.yml').Open())
            try { $metadata = $reader.ReadToEnd() } finally { $reader.Dispose() }
            if (!$metadata.Contains("version: '$version'") -or !$metadata.Contains("author: '$author'")) { throw "$id has incorrect plugin metadata." }
            if ($target.apiDeclaration -and !$metadata.Contains($target.apiDeclaration)) { throw "$id has incorrect api-version." }
            if (!$target.apiDeclaration -and $metadata.Contains('api-version:')) { throw "$id must not declare a modern api-version." }
            foreach ($entry in $archive.Entries | Where-Object { $_.FullName.EndsWith('.class') }) {
                $stream = $entry.Open()
                try {
                    $header = New-Object byte[] 8
                    if ($stream.Read($header, 0, 8) -ne 8) { throw 'Invalid class header.' }
                    $major = $header[6] * 256 + $header[7]
                    if ($major -ne 44 + $target.java) { throw "$id has incorrect Java bytecode level: $major" }
                } finally { $stream.Dispose() }
            }
        } finally { $archive.Dispose() }
        Copy-Item -LiteralPath $built -Destination $destination -Force
        $record = [ordered]@{
            minecraft=$target.minecraft; edition=$target.edition; java=$target.java
            api="$($target.group):$($target.artifact):$($target.api)"
            tests=$tests; file=$name
            sha256=(Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash.ToLowerInvariant()
        }
        $records += $record
        @{fingerprint=$targetFingerprint;record=$record} | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $receiptPath -Encoding utf8
        Write-Host "PASS $id ($tests tests)"
    }
    if ($failed.Count -gt 0) { throw "Builds failed: $($failed -join ', '). Fix failures and use -Resume." }
    $records | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $output 'BUILD_REPORT.json') -Encoding utf8
    $releaseFiles = @($records | ForEach-Object { Join-Path $output $_.file })
    foreach ($document in @('README.md', 'RELEASE_NOTES.md', 'COMPATIBILITY.md', 'release-targets.json')) {
        Copy-Item -LiteralPath $document -Destination $output -Force
        $releaseFiles += Join-Path $output $document
    }
    $releaseFiles += Join-Path $output 'BUILD_REPORT.json'
    $checksums = foreach ($file in $releaseFiles) {
        "$((Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant())  $([System.IO.Path]::GetFileName($file))"
    }
    $checksumFile = Join-Path $output 'SHA256SUMS.txt'
    $checksums | Set-Content -LiteralPath $checksumFile -Encoding ascii
    $releaseFiles += $checksumFile
    $suffix = if ($selected.Count -eq $targets.Count) { 'all-versions' } else { 'selected-versions' }
    $bundle = Join-Path $PSScriptRoot "dist/shulkerception-$version-$suffix.zip"
    Compress-Archive -LiteralPath $releaseFiles -DestinationPath $bundle -Force
    Write-Host "Prepared $($records.Count) verified artifacts: $bundle"
} finally { Pop-Location }
