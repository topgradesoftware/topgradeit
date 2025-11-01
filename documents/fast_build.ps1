# Topgrade Software App Fast Build Script (PowerShell)
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Topgrade Software App Fast Build Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "Cleaning previous build cache..." -ForegroundColor Yellow
& .\gradlew clean

Write-Host ""
Write-Host "Starting fast build with optimizations..." -ForegroundColor Green
Write-Host ""

# Set environment variables for faster builds
$env:GRADLE_OPTS = "-Xmx8192m -XX:MaxMetaspaceSize=2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=50"
$env:JAVA_OPTS = "-Xmx8192m -XX:MaxMetaspaceSize=2048m"

# Build with optimizations
& .\gradlew assembleDebug --parallel --max-workers=16 --build-cache --configure-on-demand --daemon

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Build completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "To install on device:" -ForegroundColor Yellow
Write-Host ".\gradlew installDebug" -ForegroundColor White
Write-Host ""
