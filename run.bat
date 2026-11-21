@echo off
setlocal
chcp 65001 > nul
cd /d "%~dp0"
if exist "%JAVA_HOME%\bin\java.exe" set "PATH=%JAVA_HOME%\bin;%PATH%"
where java >nul 2>nul
if errorlevel 1 (
    echo Instale Java 21 y vuelva a ejecutar este archivo.
    pause
    exit /b 1
)
if not exist "dist\sabor-gourmet.jar" (
    echo Compilando el proyecto. La primera compilacion requiere internet y JDK 21.
    call "maven\bin\mvn.cmd" clean package
    if errorlevel 1 (
        echo No se pudo compilar. Revise el mensaje anterior.
        pause
        exit /b 1
    )
    if not exist dist mkdir dist
    copy /y "target\sabor-gourmet.jar" "dist\sabor-gourmet.jar" >nul
)
echo.
echo SABOR GOURMET - Sistema de reservas
echo Abra http://localhost:8080 cuando aparezca Started SaborGourmetApplication.
echo Mantenga esta ventana abierta. Para detener, presione Ctrl+C.
echo.
java -Xms64m -Xmx512m -jar "dist\sabor-gourmet.jar"
set "SG_EXIT=%ERRORLEVEL%"
pause
exit /b %SG_EXIT%
