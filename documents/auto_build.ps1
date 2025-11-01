# Topgrade Software App Auto Build Script (PowerShell)
# This script provides advanced automation for building and deploying your Android app

param(
    [switch]$Debug,
    [switch]$Release,
    [switch]$Clean,
    [switch]$Upload,
    [switch]$Notify,
    [string]$Email = ""
)

# Set execution policy for this script
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force

# Colors for output
$Green = "`e[32m"
$Yellow = "`e[33m"
$Red = "`e[31m"
$Reset = "`e[0m"

# Function to write colored output
function Write-ColorOutput {
    param([string]$Message, [string]$Color = $Reset)
    Write-Host "$Color$Message$Reset"
}

# Function to check prerequisites
function Test-Prerequisites {
    Write-ColorOutput "Checking prerequisites..." $Yellow
    
    # Check Java
    try {
        $javaVersion = java -version 2>&1 | Select-String "version"
        Write-ColorOutput "✓ Java found: $javaVersion" $Green
    }
    catch {
        Write-ColorOutput "✗ Java not found! Please install Java 17 or later." $Red
        return $false
    }
    
    # Check Android SDK
    if (-not $env:ANDROID_HOME) {
        Write-ColorOutput "✗ ANDROID_HOME not set! Please set Android SDK path." $Red
        return $false
    }
    else {
        Write-ColorOutput "✓ Android SDK found: $env:ANDROID_HOME" $Green
    }
    
    # Check Gradle wrapper
    if (-not (Test-Path "gradlew.bat")) {
        Write-ColorOutput "✗ Gradle wrapper not found!" $Red
        return $false
    }
    else {
        Write-ColorOutput "✓ Gradle wrapper found" $Green
    }
    
    return $true
}

# Function to create build directory
function Initialize-BuildDirectory {
    if (-not (Test-Path "builds")) {
        New-Item -ItemType Directory -Path "builds" | Out-Null
        Write-ColorOutput "✓ Created builds directory" $Green
    }
}

# Function to get timestamp
function Get-BuildTimestamp {
    return Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
}

# Function to build APK
function Build-APK {
    param([string]$BuildType)
    
    Write-ColorOutput "Building $BuildType APK..." $Yellow
    
    $command = ".\gradlew.bat assemble$BuildType"
    $result = Invoke-Expression $command
    
    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput "✓ $BuildType build successful" $Green
        return $true
    }
    else {
        Write-ColorOutput "✗ $BuildType build failed!" $Red
        return $false
    }
}

# Function to copy APKs
function Copy-APKs {
    param([string]$Timestamp)
    
    Write-ColorOutput "Copying APKs to builds directory..." $Yellow
    
    $debugSource = "app\build\outputs\apk\debug\app-debug.apk"
    $releaseSource = "app\build\outputs\apk\release\app-release.apk"
    
    $debugDest = "builds\Topgrade-Debug-v1.5-$Timestamp.apk"
    $releaseDest = "builds\Topgrade-Release-v1.5-$Timestamp.apk"
    
    if (Test-Path $debugSource) {
        Copy-Item $debugSource $debugDest
        Write-ColorOutput "✓ Debug APK copied: $debugDest" $Green
    }
    
    if (Test-Path $releaseSource) {
        Copy-Item $releaseSource $releaseDest
        Write-ColorOutput "✓ Release APK copied: $releaseDest" $Green
    }
}

# Function to generate build report
function Generate-BuildReport {
    param([string]$Timestamp, [bool]$Success)
    
    $reportPath = "builds\build-report-$Timestamp.txt"
    
    $report = @"
Build Report - $Timestamp
========================================
App Version: 1.5
Build Timestamp: $Timestamp
Build Status: $(if ($Success) { "SUCCESS" } else { "FAILED" })
Build Type: $(if ($Debug) { "Debug" } elseif ($Release) { "Release" } else { "Both" })

System Information:
- OS: $([System.Environment]::OSVersion)
- .NET Version: $([System.Environment]::Version)
- Java Version: $(java -version 2>&1 | Select-String "version" | Select-Object -First 1)

Generated APKs:
- Topgrade-Debug-v1.5-$Timestamp.apk
- Topgrade-Release-v1.5-$Timestamp.apk

Build Log:
$(Get-Content "build.log" -ErrorAction SilentlyContinue | Select-Object -Last 20)
"@
    
    $report | Out-File -FilePath $reportPath -Encoding UTF8
    Write-ColorOutput "✓ Build report generated: $reportPath" $Green
}

# Function to send email notification
function Send-EmailNotification {
    param([string]$Timestamp, [bool]$Success, [string]$Email)
    
    if (-not $Email) {
        Write-ColorOutput "No email provided, skipping notification" $Yellow
        return
    }
    
    $subject = "Topgrade Software App Build - $(if ($Success) { "SUCCESS" } else { "FAILED" })"
    $body = @"
Topgrade Software App Build Report

Build Timestamp: $Timestamp
Status: $(if ($Success) { "SUCCESS" } else { "FAILED" })
Version: 1.5

Generated APKs:
- Topgrade-Debug-v1.5-$Timestamp.apk
- Topgrade-Release-v1.5-$Timestamp.apk

Build completed on: $(Get-Date)
"@
    
    try {
        Send-MailMessage -From "noreply@topgrade.com" -To $Email -Subject $subject -Body $body -SmtpServer "localhost"
        Write-ColorOutput "✓ Email notification sent" $Green
    }
    catch {
        Write-ColorOutput "✗ Failed to send email notification" $Red
    }
}

# Main execution
Write-ColorOutput "========================================" $Green
Write-ColorOutput "    Topgrade Software App Auto Build Script" $Green
Write-ColorOutput "========================================" $Green
Write-Host ""

# Check prerequisites
if (-not (Test-Prerequisites)) {
    Write-ColorOutput "Prerequisites check failed. Exiting." $Red
    exit 1
}

# Initialize build directory
Initialize-BuildDirectory

# Get timestamp
$timestamp = Get-BuildTimestamp
Write-ColorOutput "Build timestamp: $timestamp" $Green
Write-Host ""

# Determine build types
$buildTypes = @()
if ($Debug) { $buildTypes += "Debug" }
if ($Release) { $buildTypes += "Release" }
if (-not $Debug -and -not $Release) { $buildTypes = @("Debug", "Release") }

# Clean if requested
if ($Clean) {
    Write-ColorOutput "Cleaning previous builds..." $Yellow
    .\gradlew.bat clean
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "Clean failed!" $Red
        exit 1
    }
}

# Build APKs
$buildSuccess = $true
foreach ($buildType in $buildTypes) {
    if (-not (Build-APK -BuildType $buildType)) {
        $buildSuccess = $false
        break
    }
}

# Copy APKs if build was successful
if ($buildSuccess) {
    Copy-APKs -Timestamp $timestamp
    Generate-BuildReport -Timestamp $timestamp -Success $true
    
    # Send notification if requested
    if ($Notify) {
        Send-EmailNotification -Timestamp $timestamp -Success $true -Email $Email
    }
    
    Write-ColorOutput "========================================" $Green
    Write-ColorOutput "           BUILD COMPLETED!" $Green
    Write-ColorOutput "========================================" $Green
    Write-Host ""
    Write-ColorOutput "Generated APKs:" $Green
    Write-ColorOutput "- Topgrade-Debug-v1.5-$timestamp.apk" $Green
    Write-ColorOutput "- Topgrade-Release-v1.5-$timestamp.apk" $Green
    Write-Host ""
    Write-ColorOutput "Build report saved to:" $Green
    Write-ColorOutput "- builds\build-report-$timestamp.txt" $Green
    Write-Host ""
    
    # Open builds directory
    $openFolder = Read-Host "Do you want to open the builds folder? (y/n)"
    if ($openFolder -eq "y" -or $openFolder -eq "Y") {
        Invoke-Item "builds"
    }
}
else {
    Write-ColorOutput "========================================" $Red
    Write-ColorOutput "           BUILD FAILED!" $Red
    Write-ColorOutput "========================================" $Red
    Generate-BuildReport -Timestamp $timestamp -Success $false
    
    if ($Notify) {
        Send-EmailNotification -Timestamp $timestamp -Success $false -Email $Email
    }
}

Write-ColorOutput "Build process completed!" $Green 