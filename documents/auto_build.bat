@echo off
echo ========================================
echo    Topgrade Software App Auto Build Script
echo ========================================
echo.

:: Set colors for better output
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "RESET=[0m"

:: Check if Java is available
echo %YELLOW%Checking Java installation...%RESET%
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo %RED%Error: Java not found! Please install Java 17 or later.%RESET%
    pause
    exit /b 1
)

:: Check if Android SDK is available
echo %YELLOW%Checking Android SDK...%RESET%
if not exist "%ANDROID_HOME%" (
    echo %RED%Error: ANDROID_HOME not set! Please set Android SDK path.%RESET%
    pause
    exit /b 1
)

:: Create build directory if it doesn't exist
if not exist "builds" mkdir builds

:: Get current timestamp for build naming
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "timestamp=%YYYY%-%MM%-%DD%_%HH%-%Min%-%Sec%"

echo %GREEN%Starting automated build process...%RESET%
echo Build timestamp: %timestamp%
echo.

:: Step 1: Clean previous builds
echo %YELLOW%Step 1: Cleaning previous builds...%RESET%
call gradlew clean
if %errorlevel% neq 0 (
    echo %RED%Build failed at cleaning step!%RESET%
    pause
    exit /b 1
)

:: Step 2: Build debug APK
echo %YELLOW%Step 2: Building debug APK...%RESET%
call gradlew assembleDebug
if %errorlevel% neq 0 (
    echo %RED%Build failed at debug build step!%RESET%
    pause
    exit /b 1
)

:: Step 3: Build release APK
echo %YELLOW%Step 3: Building release APK...%RESET%
call gradlew assembleRelease
if %errorlevel% neq 0 (
    echo %RED%Build failed at release build step!%RESET%
    pause
    exit /b 1
)

:: Step 4: Copy APKs to builds directory
echo %YELLOW%Step 4: Copying APKs to builds directory...%RESET%
copy "app\build\outputs\apk\debug\app-debug.apk" "builds\Topgrade-Debug-v1.5-%timestamp%.apk" >nul
copy "app\build\outputs\apk\release\app-release.apk" "builds\Topgrade-Release-v1.5-%timestamp%.apk" >nul

:: Step 5: Generate build report
echo %YELLOW%Step 5: Generating build report...%RESET%
echo Build Report - %timestamp% > "builds\build-report-%timestamp%.txt"
echo ======================================== >> "builds\build-report-%timestamp%.txt"
echo App Version: 1.5 >> "builds\build-report-%timestamp%.txt"
echo Build Timestamp: %timestamp% >> "builds\build-report-%timestamp%.txt"
echo Build Status: SUCCESS >> "builds\build-report-%timestamp%.txt"
echo. >> "builds\build-report-%timestamp%.txt"
echo Generated APKs: >> "builds\build-report-%timestamp%.txt"
echo - Topgrade-Debug-v1.5-%timestamp%.apk >> "builds\build-report-%timestamp%.txt"
echo - Topgrade-Release-v1.5-%timestamp%.apk >> "builds\build-report-%timestamp%.txt"

:: Step 6: Show build summary
echo.
echo %GREEN%========================================%RESET%
echo %GREEN%           BUILD COMPLETED!%RESET%
echo %GREEN%========================================%RESET%
echo.
echo %GREEN%Generated APKs:%RESET%
echo - Topgrade-Debug-v1.5-%timestamp%.apk
echo - Topgrade-Release-v1.5-%timestamp%.apk
echo.
echo %GREEN%Build report saved to:%RESET%
echo - builds\build-report-%timestamp%.txt
echo.

:: Step 7: Optional - Open builds directory
set /p "open_folder=Do you want to open the builds folder? (y/n): "
if /i "%open_folder%"=="y" (
    explorer "builds"
)

echo %GREEN%Build process completed successfully!%RESET%
pause 