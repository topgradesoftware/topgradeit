# 🚀 Topgrade Software App Automation

This directory contains automation scripts and configurations for building, testing, and deploying the Topgrade Software App automatically.

## 📁 Files Overview

### **Build Scripts**
- `auto_build.bat` - Windows batch script for automated builds
- `auto_build.ps1` - PowerShell script with advanced features
- `daily_build.bat` - Simple script for scheduled daily builds

### **CI/CD Configuration**
- `.github/workflows/android-build.yml` - GitHub Actions workflow
- `auto_config.json` - Configuration file for automation settings

## 🛠️ Quick Start

### **1. Simple Build (Windows)**
```bash
# Run the basic build script
auto_build.bat
```

### **2. Advanced Build (PowerShell)**
```powershell
# Build both debug and release
.\auto_build.ps1

# Build only debug
.\auto_build.ps1 -Debug

# Build only release
.\auto_build.ps1 -Release

# Clean before building
.\auto_build.ps1 -Clean

# Build with email notification
.\auto_build.ps1 -Notify -Email "your@email.com"
```

### **3. Daily Automated Build**
```bash
# Run daily build
daily_build.bat
```

## ⚙️ Configuration

Edit `auto_config.json` to customize automation settings:

```json
{
  "build": {
    "debug": true,
    "release": true,
    "clean_before_build": true
  },
  "automation": {
    "daily_build": true,
    "email_notifications": false
  }
}
```

## 📅 Scheduling

### **Windows Task Scheduler Setup**

1. Open Task Scheduler
2. Create Basic Task
3. Set trigger to daily at 9:00 AM
4. Action: Start a program
5. Program: `daily_build.bat`
6. Start in: Your project directory

### **GitHub Actions**

The workflow automatically runs on:
- Push to main/develop branches
- Pull requests to main
- Release creation

## 📊 Build Output

### **Generated Files**
- `builds/` - All APK files and reports
- `builds/Topgrade-Debug-v1.5-[timestamp].apk`
- `builds/Topgrade-Release-v1.5-[timestamp].apk`
- `builds/build-report-[timestamp].txt`

### **Build Report Example**
```
Build Report - 2024-01-15_14-30-25
========================================
App Version: 1.5
Build Timestamp: 2024-01-15_14-30-25
Build Status: SUCCESS
Build Type: Both

Generated APKs:
- Topgrade-Debug-v1.5-2024-01-15_14-30-25.apk
- Topgrade-Release-v1.5-2024-01-15_14-30-25.apk
```

## 🔧 Troubleshooting

### **Common Issues**

1. **Java not found**
   - Install Java 17 or later
   - Set JAVA_HOME environment variable

2. **Android SDK not found**
   - Set ANDROID_HOME environment variable
   - Install Android SDK

3. **Gradle daemon issues**
   ```bash
   ./gradlew --stop
   ./gradlew clean build
   ```

4. **Permission denied (PowerShell)**
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope CurrentUser
   ```

### **Environment Variables Required**
```bash
JAVA_HOME=C:\Program Files\Java\jdk-17
ANDROID_HOME=C:\Users\username\AppData\Local\Android\Sdk
```

## 📧 Notifications

### **Email Notifications**
Configure in `auto_config.json`:
```json
{
  "notifications": {
    "email": {
      "enabled": true,
      "smtp_server": "smtp.gmail.com",
      "smtp_port": 587,
      "from_email": "noreply@topgrade.com",
      "to_email": "admin@topgrade.com"
    }
  }
}
```

### **Slack Notifications**
```json
{
  "notifications": {
    "slack": {
      "enabled": true,
      "webhook_url": "https://hooks.slack.com/..."
    }
  }
}
```

## ☁️ Cloud Storage

### **Google Drive Upload**
```json
{
  "cloud_storage": {
    "google_drive": {
      "enabled": true,
      "folder_id": "your_folder_id"
    }
  }
}
```

### **Dropbox Upload**
```json
{
  "cloud_storage": {
    "dropbox": {
      "enabled": true,
      "access_token": "your_access_token"
    }
  }
}
```

## 🔄 GitHub Actions

### **Manual Trigger**
```bash
# Trigger workflow manually
gh workflow run "Android Build & Deploy"
```

### **Release Deployment**
1. Create a new release on GitHub
2. Tag: `v1.5`
3. Upload APK files
4. Publish release

## 📈 Monitoring

### **Build Logs**
- Check `daily_build.log` for scheduled builds
- GitHub Actions logs for CI/CD builds
- Build reports in `builds/` directory

### **Performance Metrics**
- Build time tracking
- APK size monitoring
- Test coverage reports

## 🛡️ Security

### **Signing Configuration**
- Release builds are automatically signed
- Keystore location: `app/Topgradeit`
- Keystore password: `Topgradeit`

### **Environment Variables**
- Never commit sensitive data
- Use GitHub Secrets for CI/CD
- Store credentials securely

## 📞 Support

For automation issues:
1. Check build logs
2. Verify environment setup
3. Test individual scripts
4. Review configuration files

## 🚀 Next Steps

1. **Set up daily builds** using Windows Task Scheduler
2. **Configure email notifications** for build status
3. **Enable cloud storage** for APK backup
4. **Set up GitHub Actions** for CI/CD
5. **Monitor build performance** and optimize

---

**Version:** 1.5  
**Last Updated:** January 2024  
**Maintainer:** Development Team 