@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
if exist "%JAVA_HOME%\bin\java.exe" set "PATH=%JAVA_HOME%\bin;%PATH%"
call "maven\bin\mvn.cmd" test
set "SG_EXIT=%ERRORLEVEL%"
pause
exit /b %SG_EXIT%
