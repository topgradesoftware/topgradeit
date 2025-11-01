# PowerShell script to download searchablespinnerlibrary-1.3.1.aar

$url = "https://github.com/miteshpithadiya/SearchableSpinner/releases/download/v1.3.1/searchablespinnerlibrary-1.3.1.aar"
$outputPath = "app/libs/searchablespinnerlibrary-1.3.1.aar"

# Create libs directory if it doesn't exist
if (!(Test-Path "app/libs")) {
    New-Item -ItemType Directory -Path "app/libs" -Force
}

Write-Host "Downloading searchablespinnerlibrary-1.3.1.aar..."

try {
    # Use .NET WebClient for better compatibility
    $webClient = New-Object System.Net.WebClient
    $webClient.Headers.Add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    
    # Download the file
    $webClient.DownloadFile($url, $outputPath)
    
    # Check if download was successful
    $fileInfo = Get-Item $outputPath
    if ($fileInfo.Length -gt 1000) {
        Write-Host "Download successful! File size: $($fileInfo.Length) bytes"
    } else {
        Write-Host "Download may have failed. File size is too small: $($fileInfo.Length) bytes"
    }
}
catch {
    Write-Host "Error downloading file: $($_.Exception.Message)"
    
    # Try alternative URL
    Write-Host "Trying alternative download method..."
    try {
        $altUrl = "https://jitpack.io/com/github/miteshpithadiya/SearchableSpinner/v1.3.1/SearchableSpinner-v1.3.1.aar"
        $webClient.DownloadFile($altUrl, $outputPath)
        
        $fileInfo = Get-Item $outputPath
        if ($fileInfo.Length -gt 1000) {
            Write-Host "Alternative download successful! File size: $($fileInfo.Length) bytes"
        } else {
            Write-Host "Alternative download may have failed. File size: $($fileInfo.Length) bytes"
        }
    }
    catch {
        Write-Host "Alternative download also failed: $($_.Exception.Message)"
    }
}
finally {
    if ($webClient) {
        $webClient.Dispose()
    }
} 