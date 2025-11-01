@echo off
REM Enhanced Release Script for Android App (Windows)
REM This script provides a comprehensive release workflow

setlocal enabledelayedexpansion

REM Configuration
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
set "VERSION_SCRIPT=%PROJECT_ROOT%\update_version.py"

REM Default values
set "VERSION_TYPE="
set "VERSION="
set "VERSION_CODE="
set "GIT_ENABLED=false"
set "BUILD_ENABLED=false"
set "CHANGELOG_ENABLED=false"
set "DRY_RUN=false"
set "SKIP_VALIDATION=false"
set "VALIDATE_ONLY=false"
set "BUILD_TYPE=release"

REM Functions
:show_help
echo Enhanced Release Script for Android App
echo.
echo Usage: %0 [OPTIONS]
echo.
echo OPTIONS:
echo     -t, --type TYPE         Version bump type (patch^|minor^|major)
echo     -v, --version VERSION   Set specific version (e.g., 1.2.0)
echo     -c, --code CODE         Set specific version code
echo     -g, --git              Enable Git integration (commit + tag)
echo     -b, --build            Enable build automation
echo     -l, --changelog        Update changelog
echo     -d, --dry-run          Show what would be done without making changes
echo     -s, --skip-validation  Skip validation checks
echo     --validate-only        Run validations only
echo     --build-type TYPE      Build type (release^|debug)
echo     --help                 Show this help message
echo.
echo EXAMPLES:
echo     %0 --type patch --git --build --changelog
echo     %0 --version 2.0.0 --git --build
echo     %0 --validate-only
echo     %0 --type minor --dry-run
echo.
echo WORKFLOW:
echo     1. Validate current state
echo     2. Update version in build.gradle
echo     3. Update changelog (if enabled)
echo     4. Create git commit and tag (if enabled)
echo     5. Build release bundle (if enabled)
echo     6. Show next steps
echo.
goto :eof

:log_info
echo ℹ️  %~1
goto :eof

:log_success
echo ✅ %~1
goto :eof

:log_warning
echo ⚠️  %~1
goto :eof

:log_error
echo ❌ %~1
goto :eof

REM Parse command line arguments
:parse_args
if "%~1"=="" goto :args_done
if "%~1"=="-t" (
    set "VERSION_TYPE=%~2"
    shift
    shift
    goto :parse_args
)
if "%~1"=="--type" (
    set "VERSION_TYPE=%~2"
    shift
    shift
    goto :parse_args
)
if "%~1"=="-v" (
    set "VERSION=%~2"
    shift
    shift
    goto :parse_args
)
if "%~1"=="--version" (
    set "VERSION=%~2"
    shift
    shift
    goto :parse_args
)
if "%~1"=="-c" (
    set "VERSION_CODE=%~2"
    shift
    shift
    goto :parse_args
)
if "%~1"=="--code" (
    set "VERSION_CODE=%~2"
    shift
    shift
    goto :parse_args
)
if "%~1"=="-g" (
    set "GIT_ENABLED=true"
    shift
    goto :parse_args
)
if "%~1"=="--git" (
    set "GIT_ENABLED=true"
    shift
    goto :parse_args
)
if "%~1"=="-b" (
    set "BUILD_ENABLED=true"
    shift
    goto :parse_args
)
if "%~1"=="--build" (
    set "BUILD_ENABLED=true"
    shift
    goto :parse_args
)
if "%~1"=="-l" (
    set "CHANGELOG_ENABLED=true"
    shift
    goto :parse_args
)
if "%~1"=="--changelog" (
    set "CHANGELOG_ENABLED=true"
    shift
    goto :parse_args
)
if "%~1"=="-d" (
    set "DRY_RUN=true"
    shift
    goto :parse_args
)
if "%~1"=="--dry-run" (
    set "DRY_RUN=true"
    shift
    goto :parse_args
)
if "%~1"=="-s" (
    set "SKIP_VALIDATION=true"
    shift
    goto :parse_args
)
if "%~1"=="--skip-validation" (
    set "SKIP_VALIDATION=true"
    shift
    goto :parse_args
)
if "%~1"=="--validate-only" (
    set "VALIDATE_ONLY=true"
    shift
    goto :parse_args
)
if "%~1"=="--build-type" (
    set "BUILD_TYPE=%~2"
    shift
    shift
    goto :parse_args
)
if "%~1"=="--help" (
    call :show_help
    exit /b 0
)
call :log_error "Unknown option: %~1"
call :show_help
exit /b 1

:args_done

REM Validate arguments
if "%VERSION_TYPE%"=="" if "%VERSION%"=="" if "%VALIDATE_ONLY%"=="false" (
    call :log_error "Version type or specific version must be specified"
    call :show_help
    exit /b 1
)

if "%VALIDATE_ONLY%"=="true" if not "%VERSION_TYPE%"=="" (
    call :log_error "Validate-only mode cannot be used with version updates"
    exit /b 1
)

if "%VALIDATE_ONLY%"=="true" if not "%VERSION%"=="" (
    call :log_error "Validate-only mode cannot be used with version updates"
    exit /b 1
)

REM Change to project root
cd /d "%PROJECT_ROOT%"

REM Check if Python and required files exist
python --version >nul 2>&1
if errorlevel 1 (
    call :log_error "Python is required but not installed"
    exit /b 1
)

if not exist "%VERSION_SCRIPT%" (
    call :log_error "Version script not found: %VERSION_SCRIPT%"
    exit /b 1
)

if not exist "app\build.gradle" (
    call :log_error "build.gradle not found in app/ directory"
    exit /b 1
)

REM Build command arguments
set "PYTHON_ARGS="

if "%VALIDATE_ONLY%"=="true" (
    set "PYTHON_ARGS=--validate-only"
    if "%SKIP_VALIDATION%"=="true" (
        set "PYTHON_ARGS=!PYTHON_ARGS! --no-validation"
    )
) else (
    REM Version update arguments
    if not "%VERSION_TYPE%"=="" (
        if "%VERSION_TYPE%"=="patch" (
            set "PYTHON_ARGS=--patch"
        ) else if "%VERSION_TYPE%"=="minor" (
            set "PYTHON_ARGS=--minor"
        ) else if "%VERSION_TYPE%"=="major" (
            set "PYTHON_ARGS=--major"
        ) else (
            call :log_error "Invalid version type: %VERSION_TYPE%"
            exit /b 1
        )
    )
    
    if not "%VERSION%"=="" (
        if "%PYTHON_ARGS%"=="" (
            set "PYTHON_ARGS=--version %VERSION%"
        ) else (
            set "PYTHON_ARGS=!PYTHON_ARGS! --version %VERSION%"
        )
    )
    
    if not "%VERSION_CODE%"=="" (
        set "PYTHON_ARGS=!PYTHON_ARGS! --version-code %VERSION_CODE%"
    )
    
    REM Feature flags
    if "%GIT_ENABLED%"=="true" (
        set "PYTHON_ARGS=!PYTHON_ARGS! --git"
    )
    
    if "%BUILD_ENABLED%"=="true" (
        set "PYTHON_ARGS=!PYTHON_ARGS! --build"
        set "PYTHON_ARGS=!PYTHON_ARGS! --build-type %BUILD_TYPE%"
    )
    
    if "%CHANGELOG_ENABLED%"=="true" (
        set "PYTHON_ARGS=!PYTHON_ARGS! --changelog"
    )
    
    if "%DRY_RUN%"=="true" (
        set "PYTHON_ARGS=!PYTHON_ARGS! --dry-run"
    )
    
    if "%SKIP_VALIDATION%"=="true" (
        set "PYTHON_ARGS=!PYTHON_ARGS! --no-validation"
    )
)

REM Show what will be executed
call :log_info "Executing release workflow..."
echo Command: python %VERSION_SCRIPT% !PYTHON_ARGS!
echo.

REM Execute the version script
python "%VERSION_SCRIPT%" !PYTHON_ARGS!
if errorlevel 1 (
    call :log_error "Release workflow failed!"
    exit /b 1
)

call :log_success "Release workflow completed successfully!"

if not "%VALIDATE_ONLY%"=="true" if not "%DRY_RUN%"=="true" (
    echo.
    call :log_info "Next steps:"
    if "%GIT_ENABLED%"=="true" (
        echo   1. Push changes: git push
        echo   2. Push tags: git push --tags
    )
    if "%BUILD_ENABLED%"=="true" (
        echo   3. Test the build: %BUILD_TYPE% bundle created
        echo   4. Upload to Play Store: gradlew publishRelease
    )
    if not "%GIT_ENABLED%"=="true" if not "%BUILD_ENABLED%"=="true" (
        echo   1. Test the changes
        echo   2. Commit to git manually
        echo   3. Build release: gradlew bundleRelease
    )
)

exit /b 0
