$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) { $env:PATH = "$env:JAVA_HOME\bin;$env:PATH" }
& '.\maven\bin\mvn.cmd' test
exit $LASTEXITCODE
