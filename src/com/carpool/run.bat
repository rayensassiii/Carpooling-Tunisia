@echo off
REM ============================================
REM CarPool Platform - Build and Run Script
REM ============================================

echo.
echo ========================================
echo   CarPool Platform Build Script
echo ========================================
echo.

REM Check if JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set!
    echo Please set JAVA_HOME to your JDK installation path.
    pause
    exit /b 1
)

REM Check if JAVAFX_HOME is set
if "%JAVAFX_HOME%"=="" (
    echo WARNING: JAVAFX_HOME is not set!
    echo Please set JAVAFX_HOME to your JavaFX SDK path.
    echo Example: set JAVAFX_HOME=C:\javafx-sdk-17
    echo.
    set /p JAVAFX_HOME="Enter JavaFX SDK path: "
)

echo Using Java: %JAVA_HOME%
echo Using JavaFX: %JAVAFX_HOME%
echo.

REM Create output directory
if not exist "out" mkdir out

echo [1/2] Compiling Java files...
"%JAVA_HOME%\bin\javac" ^
    --module-path "%JAVAFX_HOME%\lib" ^
    --add-modules javafx.controls ^
    -d out ^
    *.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo [2/2] Running application...
echo.
echo ========================================
echo   Starting CarPool Application
echo ========================================
echo.

"%JAVA_HOME%\bin\java" ^
    --module-path "%JAVAFX_HOME%\lib" ^
    --add-modules javafx.controls ^
    -cp out ^
    com.carpool.CarpoolApp

pause
