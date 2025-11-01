# App Versioning Guide

## Current Version
- **versionCode**: 2
- **versionName**: "1.1.0"

## Versioning Strategy

### versionCode (Internal Version)
- **Purpose**: Used by the Android system to determine which version is newer
- **Format**: Integer that must be incremented for each release
- **Rules**:
  - Must be a positive integer
  - Must be higher than the previous release
  - Typically increments by 1 for each release

### versionName (User-Visible Version)
- **Purpose**: User-facing version string shown in app stores and settings
- **Format**: String following semantic versioning (Major.Minor.Patch)
- **Rules**:
  - Follow semantic versioning: `MAJOR.MINOR.PATCH`
  - MAJOR: Breaking changes
  - MINOR: New features, backward compatible
  - PATCH: Bug fixes, backward compatible

## Release Workflow

### Before Each Release:

1. **Update versionCode**:
   ```gradle
   versionCode 2  // Increment by 1
   ```

2. **Update versionName**:
   ```gradle
   versionName "1.1.0"  // Follow semantic versioning
   ```

### Example Release Sequence:

| Release | versionCode | versionName | Description |
|---------|-------------|-------------|-------------|
| Initial | 1 | "1.0.0" | Initial release |
| Feature | 2 | "1.1.0" | New features added |
| Bug Fix | 3 | "1.1.1" | Critical bug fixes |
| Feature | 4 | "1.2.0" | Additional features |
| Bug Fix | 5 | "1.2.1" | Minor bug fixes |
| Major | 6 | "2.0.0" | Breaking changes |

## Build Configuration Location

File: `app/build.gradle`

```gradle
android {
    defaultConfig {
        applicationId "topgrade.parent.com.parentseeks"
        minSdk 26
        targetSdk 35
        versionCode 2        // ← Update this
        versionName "1.1.0"  // ← Update this
        // ... other config
    }
}
```

## Automated Versioning (Optional)

For automated versioning, you can use:

```gradle
defaultConfig {
    versionCode project.hasProperty('versionCode') ? project.versionCode.toInteger() : 1
    versionName project.hasProperty('versionName') ? project.versionName : "1.0"
}
```

Then build with:
```bash
./gradlew assembleRelease -PversionCode=2 -PversionName="1.1.0"
```

## Best Practices

1. **Always increment versionCode** for each release
2. **Use semantic versioning** for versionName
3. **Test thoroughly** before updating versions
4. **Document changes** in release notes
5. **Tag releases** in version control
6. **Keep version history** for rollback purposes

## Pre-release Versions

For pre-release versions, use suffixes:

```gradle
versionName "1.1.0-beta.1"  // Beta release
versionName "1.1.0-alpha.1" // Alpha release
versionName "1.1.0-rc.1"    // Release candidate
```

## 🚀 Automated Version Management

We now have an automated version management system that makes updating versions easy and error-free.

### Quick Start

#### Windows Users:
```cmd
# Run the interactive version manager
update_version.bat

# Or use direct commands
python update_version.py --show                    # Show current version
python update_version.py --patch                   # Increment patch (1.1.0 -> 1.1.1)
python update_version.py --minor                   # Increment minor (1.1.0 -> 1.2.0)
python update_version.py --major                   # Increment major (1.1.0 -> 2.0.0)
python update_version.py --version 2.0.0           # Set specific version
```

#### Unix/Linux/macOS Users:
```bash
# Make script executable (first time only)
chmod +x update_version.sh

# Run the interactive version manager
./update_version.sh

# Or use direct commands
python3 update_version.py --show                   # Show current version
python3 update_version.py --patch                  # Increment patch (1.1.0 -> 1.1.1)
python3 update_version.py --minor                  # Increment minor (1.1.0 -> 1.2.0)
python3 update_version.py --major                  # Increment major (1.1.0 -> 2.0.0)
python3 update_version.py --version 2.0.0          # Set specific version
```

### Advanced Usage

```bash
# Dry run to see what would change
python update_version.py --patch --dry-run

# Set custom version code
python update_version.py --version 2.0.0 --version-code 10

# Show help
python update_version.py --help
```

### What the Script Does

1. **Reads** current version from `app/build.gradle`
2. **Validates** version format and increments
3. **Updates** both `versionCode` and `versionName`
4. **Confirms** changes before applying
5. **Provides** clear feedback on what was updated

## Legacy Manual Update Commands

If you prefer manual updates, you can still use these commands:

```bash
# Update to version 1.2.0
sed -i 's/versionCode 2/versionCode 3/' app/build.gradle
sed -i 's/versionName "1.1.0"/versionName "1.2.0"/' app/build.gradle
```

