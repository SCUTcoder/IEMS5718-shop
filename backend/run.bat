@echo off
REM IEMS5718 Shop Backend Startup Script for Windows

echo ======================================
echo   IEMS5718 Shop Backend Startup
echo ======================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed!
    echo Please install Java 17 or higher.
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Maven is not installed!
    echo Please install Maven 3.6 or higher.
    pause
    exit /b 1
)

echo Java version:
java -version 2>&1 | findstr /C:"version"
echo.
echo Maven version:
mvn -version | findstr /C:"Apache Maven"
echo.

REM Build the project
echo Building project...
call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful!
echo.

REM Run the application
echo Starting IEMS5718 Shop Backend...
echo API will be available at: http://localhost:8080/api
echo Press Ctrl+C to stop the server
echo.

call mvn spring-boot:run
