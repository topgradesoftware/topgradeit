# Constant Loading Fix

## Problem Identified
The logs showed that API calls were being made with empty parameters:
```
API parameters: {parent_id=, staff_id=}
JSON request body: {"parent_id":"","staff_id":""}
```

This was causing the exam session API to return empty results, leading to the "No exam sessions returned from API" error.

## Root Cause
The `Constant` class static variables (`staff_id`, `campus_id`, `parent_id`, `current_session`) were initialized as empty strings and were not being loaded from the Paper database when the app started.

## Solution Implemented

### 1. Added `loadFromPaper()` Method to Main Constant Class
- Added the `loadFromPaper()` method to `app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Utils/Constant.java`
- This method loads all constants from Paper DB and logs the values for debugging

### 2. Integrated Constant Loading in App Initialization
- **TopgradeApplication**: Added constant loading during app startup
- **Splash Activity**: Added constant loading to ensure constants are available during splash screen

### 3. Files Modified

#### `app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Utils/Constant.java`
```java
public static void loadFromPaper() {
    try {
        staff_id = Paper.book().read("staff_id", "");
        campus_id = Paper.book().read("campus_id", "");
        current_session = Paper.book().read("current_session", "");
        parent_id = Paper.book().read("parent_id", "");
        
        android.util.Log.d("Constant", "Constants loaded from Paper:");
        android.util.Log.d("Constant", "staff_id: " + staff_id);
        android.util.Log.d("Constant", "campus_id: " + campus_id);
        android.util.Log.d("Constant", "current_session: " + current_session);
        android.util.Log.d("Constant", "parent_id: " + parent_id);
    } catch (Exception e) {
        android.util.Log.e("Constant", "Error loading constants from Paper: " + e.getMessage());
    }
}
```

#### `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Utils/YourAppClass.kt`
```kotlin
// Load constants from Paper DB
topgrade.parent.com.parentseeks.Teacher.Utils.Constant.loadFromPaper()
Log.d(TAG, "Constants loaded from Paper DB")
```

#### `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Activity/Splash.java`
```java
// Load constants from Paper DB to ensure they're available
topgrade.parent.com.parentseeks.Teacher.Utils.Constant.loadFromPaper();
Log.d("Splash", "Constants loaded from Paper DB");
```

## Expected Results

1. **Proper API Parameters**: API calls will now include the correct `staff_id` and `campus_id` values
2. **Successful Exam Session Loading**: The exam session API should return actual data instead of empty results
3. **Better Debugging**: Logs will show the loaded constant values for troubleshooting
4. **Consistent Data Access**: All activities will have access to properly loaded constants

## Testing

After this fix, you should see in the logs:
```
Constants loaded from Paper:
staff_id: [actual_staff_id]
campus_id: [actual_campus_id]
current_session: [actual_session]
parent_id: [actual_parent_id]
```

And API calls should show:
```
API parameters: {parent_id=[actual_campus_id], staff_id=[actual_staff_id]}
```

## Additional Notes

- The constants are loaded both during app initialization and splash screen to ensure they're always available
- This fix addresses the core issue where user data wasn't being properly loaded into static variables
- The fix maintains backward compatibility with existing code that uses these constants
