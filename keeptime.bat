@echo off
setlocal

cd /d "%~dp0"

where javaw >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java was not found.
    echo.
    echo Please install Java.
    echo See installation requirements: https://github.com/doubleSlashde/KeepTime#requirements
    echo.
    pause
    exit /b 1
)

if not exist "keeptime.jar" (
    echo ERROR: keeptime.jar was not found in this folder.
    echo.
    echo Please download and extract KeepTime from the releases page.
    echo See installation instructions: https://github.com/doubleSlashde/KeepTime#install
    echo.
    pause
    exit /b 1
)

start "" "javaw" -Dprism.order=sw -jar keeptime.jar
