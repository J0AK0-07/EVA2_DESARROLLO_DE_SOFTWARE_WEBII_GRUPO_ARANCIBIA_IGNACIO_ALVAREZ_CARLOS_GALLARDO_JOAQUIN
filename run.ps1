param([int]$Puerto = 8080)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw 'Instale Java 21 y vuelva a ejecutar.' }
if (-not (Test-Path 'dist\sabor-gourmet.jar')) {
    & '.\maven\bin\mvn.cmd' clean package
    if ($LASTEXITCODE -ne 0) { throw 'La compilación falló. Revise el mensaje anterior.' }
    New-Item -ItemType Directory -Path dist -Force | Out-Null
    Copy-Item 'target\sabor-gourmet.jar' 'dist\sabor-gourmet.jar' -Force
}
Write-Host "Sabor Gourmet: http://localhost:$Puerto"
Write-Host 'Abra la dirección después de ver Started SaborGourmetApplication. Ctrl+C para detener.'
& java '-Xms64m' '-Xmx512m' '-jar' 'dist\sabor-gourmet.jar' "--server.port=$Puerto"
exit $LASTEXITCODE
