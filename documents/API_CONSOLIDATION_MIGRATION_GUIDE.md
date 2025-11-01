# API Consolidation Migration Guide

## Overview
This document provides guidance on migrating from the old scattered API constants to the new consolidated `ApiConstants.kt` file.

## What Changed

### 1. New Consolidated File
- **File**: `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Utils/ApiConstants.kt`
- **Purpose**: Single source of truth for all API endpoints, base URLs, and related constants

### 2. Updated Files
- `API.java` - Now uses `ApiConstants` for all endpoints
- `Constants.java` - Now uses `ApiConstants` for shared preferences keys
- `ModernNetworkClient.kt` - Now uses `ApiConstants.BASE_URL`

## How to Use the New ApiConstants

### 1. Accessing Base URLs
```kotlin
// Old way
val baseUrl = "https://topgradesoftware.com/"

// New way
val baseUrl = ApiConstants.BASE_URL
val campusBaseUrl = ApiConstants.CAMPUS_BASE_URL
```

### 2. Accessing API Endpoints
```kotlin
// Old way
val loginUrl = "https://topgradesoftware.com/api.php?page=parent/login"

// New way
val loginUrl = ApiConstants.buildApiUrl(ApiConstants.Parent.LOGIN)
```

### 3. Accessing Image URLs
```kotlin
// Old way
val imageUrl = "https://topgradesoftware.com/uploads/parent/" + filename

// New way
val imageUrl = ApiConstants.buildImageUrl(ApiConstants.PARENT_IMAGE_BASE_URL, filename)
```

### 4. Accessing Shared Preferences Keys
```kotlin
// Old way
val campusListKey = "campus_list"

// New way
val campusListKey = ApiConstants.SharedPrefs.CAMPUS_LIST
```

### 5. Building Complete URLs
```kotlin
// API URLs
val apiUrl = ApiConstants.buildApiUrl(ApiConstants.Parent.LOGIN)

// Campus API URLs
val campusApiUrl = ApiConstants.buildCampusApiUrl(ApiConstants.Campus.APP_VERSION)

// Play Store URLs
val playStoreUrl = ApiConstants.buildPlayStoreUrl(packageName)

// WhatsApp URLs
val whatsappUrl = ApiConstants.buildWhatsAppUrl(phoneNumber, message)
```

## Organization Structure

### Parent API Endpoints
```kotlin
ApiConstants.Parent.LOGIN
ApiConstants.Parent.LOAD_PROFILE
ApiConstants.Parent.UPDATE_PASSWORD
// ... etc
```

### Teacher API Endpoints
```kotlin
ApiConstants.Teacher.LOGIN
ApiConstants.Teacher.LOAD_ATTENDANCE
ApiConstants.Teacher.UPDATE_PROFILE
// ... etc
```

### Student API Endpoints
```kotlin
ApiConstants.Student.LOGOUT
```

### Campus API Endpoints
```kotlin
ApiConstants.Campus.APP_VERSION
```

### Shared Preferences Keys
```kotlin
ApiConstants.SharedPrefs.CAMPUS_LIST
ApiConstants.SharedPrefs.IS_LOGIN
ApiConstants.SharedPrefs.USER_TYPE
// ... etc
```

## Backward Compatibility

The existing `API.java` and `Constants.java` files maintain backward compatibility by:
- Keeping all existing public static fields
- Using the new `ApiConstants` internally
- No changes required in existing code that uses these classes

## Migration Steps for New Code

### 1. For New Java Files
```java
// Import the new constants
import topgrade.parent.com.parentseeks.Parent.Utils.ApiConstants;

// Use the constants
String loginUrl = ApiConstants.buildApiUrl(ApiConstants.Parent.LOGIN);
```

### 2. For New Kotlin Files
```kotlin
// Import the new constants
import topgrade.parent.com.parentseeks.Parent.Utils.ApiConstants

// Use the constants
val loginUrl = ApiConstants.buildApiUrl(ApiConstants.Parent.LOGIN)
```

## Benefits

1. **Single Source of Truth**: All API endpoints in one place
2. **Type Safety**: Compile-time checking for endpoint names
3. **Easy Maintenance**: Update URLs in one location
4. **Consistency**: Standardized URL building across the app
5. **Documentation**: Clear organization and comments
6. **Backward Compatibility**: Existing code continues to work

## Future Improvements

1. **Environment Support**: Add support for different environments (dev, staging, prod)
2. **API Versioning**: Add version management for API endpoints
3. **Dynamic Configuration**: Support for runtime URL configuration
4. **Security**: Add URL validation and security checks

## Troubleshooting

### Common Issues

1. **Import Errors**: Make sure to import `ApiConstants` in new files
2. **Build Errors**: Ensure the new file is included in the build
3. **Runtime Errors**: Check that all constants are properly defined

### Support

For questions or issues with the migration, refer to:
- This migration guide
- The `ApiConstants.kt` file comments
- The existing `API.java` and `Constants.java` files for examples
