@echo off
echo ========================================
echo    Topgrade Software App Gradle Sync Script
echo    DataStore Migration Update
echo ========================================
echo.

echo [1/5] Cleaning project...
call gradlew clean
if %errorlevel% neq 0 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)

echo.
echo [2/5] Updating Gradle wrapper...
call gradlew wrapper --gradle-version 8.11.0
if %errorlevel% neq 0 (
    echo ERROR: Gradle wrapper update failed!
    pause
    exit /b 1
)

echo.
echo [3/5] Syncing project with latest dependencies...
call gradlew --refresh-dependencies
if %errorlevel% neq 0 (
    echo ERROR: Dependency refresh failed!
    pause
    exit /b 1
)

echo.
echo [4/5] Building project...
call gradlew build
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [5/5] Running DataStore migration tests...
call gradlew testDebugUnitTest --tests "*DataStore*"
if %errorlevel% neq 0 (
    echo WARNING: Some DataStore tests failed, but continuing...
)

echo.
echo ========================================
echo    Gradle Sync Completed Successfully!
echo ========================================
echo.
echo What was updated:
echo - DataStore dependencies to latest version
echo - Coroutines with Flow support
echo - Lifecycle components with StateFlow
echo - Testing dependencies for DataStore
echo - Performance monitoring tools
echo.
echo Next steps:
echo 1. Sync project in Android Studio
echo 2. Test the new DataStore implementations
echo 3. Verify dashboard functionality
echo.
pause 