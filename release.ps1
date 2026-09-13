param([switch]$Offline)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Push-Location $PSScriptRoot
try {
    [xml]$project = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'pom.xml') -Raw
    $version = [string]$project.project.version
    $author = [string]$project.project.properties.'plugin.author'
    if ($version -notmatch '^\d+\.\d+\.\d+$') {
        throw 'A release must have a numeric major.minor.patch version without a snapshot suffix.'
    }
    $maven = Get-Command mvn -ErrorAction SilentlyContinue
    $mavenPath = if ($maven) { $maven.Source } else {
        Join-Path $PSScriptRoot '.tools/apache-maven-3.9.11/bin/mvn.cmd'
    }
    if (!(Test-Path -LiteralPath $mavenPath)) { throw 'Install Maven 3.9+ and Java 25 first.' }

    foreach ($edition in @('paper', 'bukkit')) {
        $mavenArgs = @('-B', '-ntp', '-P', $edition, '-Dmaven.repo.local=.m2')
        if ($Offline) { $mavenArgs += '-o' }
        & $mavenPath @mavenArgs package
        if ($LASTEXITCODE -ne 0) { throw "$edition build or tests failed." }
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $releaseDir = Join-Path $PSScriptRoot "dist/v$version"
    New-Item -ItemType Directory -Path $releaseDir -Force | Out-Null
    $releaseFiles = @()
    foreach ($edition in @('paper', 'bukkit')) {
        $name = "shulkerception-$version-$edition.jar"
        $builtJar = Join-Path $PSScriptRoot "target/$edition/$name"
        $archive = [System.IO.Compression.ZipFile]::OpenRead($builtJar)
        try {
            $entry = $archive.GetEntry('plugin.yml')
            if (!$entry) { throw "$name is missing plugin.yml." }
            $reader = [System.IO.StreamReader]::new($entry.Open())
            try { $metadata = $reader.ReadToEnd() } finally { $reader.Dispose() }
            if (!$metadata.Contains("version: '$version'") -or !$metadata.Contains("author: '$author'")) {
                throw "$name contains incorrect release metadata."
            }
        } finally { $archive.Dispose() }
        Copy-Item -LiteralPath $builtJar -Destination (Join-Path $releaseDir $name) -Force
        Copy-Item -LiteralPath $builtJar -Destination (Join-Path $PSScriptRoot "dist/$name") -Force
        $releaseFiles += Join-Path $releaseDir $name
    }
    Copy-Item -LiteralPath (Join-Path $releaseDir "shulkerception-$version-paper.jar") -Destination (Join-Path $PSScriptRoot "dist/shulkerception-$version.jar") -Force
    foreach ($document in @('README.md', 'RELEASE_NOTES.md')) {
        Copy-Item -LiteralPath (Join-Path $PSScriptRoot $document) -Destination $releaseDir -Force
        $releaseFiles += Join-Path $releaseDir $document
    }
    $checksums = foreach ($file in $releaseFiles) {
        $hash = (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant()
        "$hash  $([System.IO.Path]::GetFileName($file))"
    }
    $checksumFile = Join-Path $releaseDir 'SHA256SUMS.txt'
    $checksums | Set-Content -LiteralPath $checksumFile -Encoding ascii
    $releaseFiles += $checksumFile
    $bundle = Join-Path $PSScriptRoot "dist/shulkerception-$version-release.zip"
    Compress-Archive -LiteralPath $releaseFiles -DestinationPath $bundle -Force
    Write-Host "Release $version by $author prepared: $bundle"
} finally {
    Pop-Location
}
