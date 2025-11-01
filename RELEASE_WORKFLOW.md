# 🚀 Enhanced Release Workflow Guide

This guide covers the complete automated release workflow for your Android app, from version bumping to Play Store upload.

## 📋 Quick Reference

### Basic Version Update
```bash
# Simple patch update
python update_version.py --patch

# With Git integration
python update_version.py --patch --git --changelog

# With build automation
python update_version.py --patch --git --changelog --build
```

### Advanced Release Workflow
```bash
# Full automated release
python update_version.py --minor --git --changelog --build --validate-build
```

## 🔄 Complete Release Workflow

### 1. Pre-Release Validation

Before starting any release process:

```bash
# Validate everything is ready
python update_version.py --validate-only --validate-build

# Check current version
python update_version.py --show

# Ensure git is clean
git status
```

### 2. Version Bump Strategy

Choose the appropriate version bump based on your changes:

#### Patch Release (Bug Fixes)
```bash
# For bug fixes and minor improvements
python update_version.py --patch --git --changelog

# Example: 1.1.0 -> 1.1.1
```

#### Minor Release (New Features)
```bash
# For new features (backward compatible)
python update_version.py --minor --git --changelog

# Example: 1.1.0 -> 1.2.0
```

#### Major Release (Breaking Changes)
```bash
# For breaking changes
python update_version.py --major --git --changelog

# Example: 1.1.0 -> 2.0.0
```

### 3. Build and Test

```bash
# Build release bundle
python update_version.py --build --build-type release

# Or build debug for testing
python update_version.py --build --build-type debug
```

### 4. CI/CD Integration

#### GitHub Actions
Your `.github/workflows/release.yml` will automatically:
- Validate builds on pull requests
- Auto-bump versions on main branch pushes
- Build and release on tags
- Upload to GitHub Releases

#### GitLab CI
Your `.gitlab-ci.yml` provides:
- Validation pipeline
- Manual version bumping
- Security scanning
- Release automation

### 5. Play Store Upload

#### Manual Upload
```bash
# Build release bundle
./gradlew bundleRelease

# Upload manually to Play Console
```

#### Automated Upload (with Gradle Play Publisher)
```bash
# Add to app/build.gradle
plugins {
    id 'com.github.triplet.play' version '3.8.4'
}

play {
    serviceAccountCredentials = file('google-services.json')
    track = 'internal'  // or 'production'
}

# Then upload automatically
./gradlew publishRelease
```

## 🛠️ Advanced Features

### Custom Git Messages
```bash
python update_version.py --patch --git --git-message "Fix critical login bug"
```

### Custom Version Codes
```bash
python update_version.py --version 2.0.0 --version-code 10
```

### Dry Run Mode
```bash
# See what would change without making changes
python update_version.py --patch --git --changelog --dry-run
```

### Skip Validations
```bash
# Skip all validation checks (use with caution)
python update_version.py --patch --no-validation
```

### Validation Only
```bash
# Run validations without updating version
python update_version.py --validate-only --validate-build
```

## 📁 File Structure

```
project-root/
├── update_version.py          # Main version manager
├── update_version.bat         # Windows launcher
├── update_version.sh          # Unix launcher
├── CHANGELOG.md               # Auto-generated changelog
├── .github/workflows/         # GitHub Actions
│   └── release.yml
├── .gitlab-ci.yml             # GitLab CI
├── app/build.gradle           # Android build config
└── RELEASE_WORKFLOW.md        # This guide
```

## 🔧 Configuration

### Environment Variables

For CI/CD environments, set these variables:

#### GitHub Actions Secrets
- `KEYSTORE_BASE64`: Base64 encoded keystore
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

#### GitLab CI Variables
- `KEYSTORE_BASE64`: Base64 encoded keystore
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

### Gradle Configuration

Add to your `app/build.gradle` for Play Store automation:

```gradle
plugins {
    id 'com.github.triplet.play' version '3.8.4'
}

play {
    serviceAccountCredentials = file('google-services.json')
    track = 'internal'
    releaseStatus = 'draft'
}
```

## 📊 Release Checklist

### Before Release
- [ ] All tests passing
- [ ] Code review completed
- [ ] Documentation updated
- [ ] Version bump strategy decided
- [ ] Git repository is clean

### During Release
- [ ] Run version update script
- [ ] Verify build.gradle changes
- [ ] Check Git commit and tag
- [ ] Validate build process
- [ ] Test release bundle

### After Release
- [ ] Push changes to remote
- [ ] Push tags to remote
- [ ] Upload to Play Store
- [ ] Update release notes
- [ ] Notify team of release
- [ ] Monitor for issues

## 🚨 Troubleshooting

### Common Issues

#### Git Not Clean
```bash
# Check what's uncommitted
git status

# Commit or stash changes
git add .
git commit -m "WIP: preparing for release"
```

#### Build Failures
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# Check for dependency issues
./gradlew dependencies
```

#### Version Conflicts
```bash
# Check current version
python update_version.py --show

# Force specific version
python update_version.py --version 1.2.0 --version-code 5
```

### Error Messages

#### "Application ID mismatch"
- Check that `applicationId` in `build.gradle` matches expected value
- Update the expected value in `update_version.py` if needed

#### "Git repository not clean"
- Commit or stash uncommitted changes
- Use `--no-git-validation` to skip (not recommended)

#### "Gradle build failed"
- Check for compilation errors
- Verify all dependencies are available
- Use `--no-validation` to skip build validation

## 🎯 Best Practices

### Version Management
1. **Use semantic versioning**: Always follow `major.minor.patch`
2. **Increment consistently**: Don't skip version codes
3. **Document changes**: Use descriptive commit messages
4. **Tag releases**: Always create Git tags for releases

### Release Process
1. **Test thoroughly**: Always test before releasing
2. **Use staging**: Test releases internally first
3. **Monitor metrics**: Track app performance after release
4. **Rollback plan**: Have a plan for quick rollbacks

### Team Workflow
1. **Coordinate releases**: Don't release simultaneously
2. **Code review**: Always review changes before release
3. **Communication**: Notify team of releases
4. **Documentation**: Keep release notes updated

## 📈 Monitoring and Metrics

### Post-Release Monitoring
- Monitor crash reports
- Track user feedback
- Check performance metrics
- Watch download statistics

### Rollback Strategy
```bash
# If issues are found, quickly rollback
git checkout v1.1.0  # Previous stable version
python update_version.py --version 1.1.1 --version-code 3
python update_version.py --git --build
```

## 🔄 Continuous Improvement

### Regular Reviews
- Review release process monthly
- Update automation scripts
- Improve validation checks
- Enhance documentation

### Feedback Integration
- Collect team feedback
- Monitor CI/CD performance
- Optimize build times
- Improve error handling

Your enhanced release workflow is now ready for production use! 🎉

## 📞 Support

If you encounter issues:
1. Check this guide first
2. Review error messages carefully
3. Use `--dry-run` to test changes
4. Consult team members for help
5. Document new issues and solutions
