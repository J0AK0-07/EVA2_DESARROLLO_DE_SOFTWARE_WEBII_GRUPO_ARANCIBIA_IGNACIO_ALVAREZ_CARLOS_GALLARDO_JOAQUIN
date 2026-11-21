@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
if exist "%JAVA_HOME%\bin\java.exe" set "PATH=%JAVA_HOME%\bin;%PATH%"
call "maven\bin\mvn.cmd" clean verify
if errorlevel 1 (
    echo Compilacion o pruebas fallidas. El ejecutable anterior se conserva.
    pause
    exit /b 1
)
if not exist dist mkdir dist
copy /y "target\sabor-gourmet.jar" "dist\sabor-gourmet.jar" >nul
echo Ejecutable actualizado en dist\sabor-gourmet.jar
pause
