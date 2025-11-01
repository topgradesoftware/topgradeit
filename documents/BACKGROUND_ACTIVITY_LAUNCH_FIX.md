# Background Activity Launch Fix

## Problem Description

The app was experiencing "Background activity launch blocked!" errors when trying to launch the `SelectRole` activity from background contexts. This is a security restriction in Android that prevents apps from launching activities when they are not in the foreground.

## Root Cause

The issue was occurring in several places:

1. **Splash.java** - When `campus_id` or `parent_id` was null/empty, the app tried to launch `SelectRole` directly
2. **DashBoard.java** - During logout operations, the app tried to launch `SelectRole` directly
3. **Various Teacher dashboard activities** - During logout operations

## Solution Implemented

### 1. Foreground Detection

Added a helper method `isAppInForeground()` that checks if the app is currently running in the foreground:

```java
private boolean isAppInForeground() {
    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
    if (appProcesses == null) {
        return false;
    }
    String packageName = getPackageName();
    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName.equals(packageName)) {
            return true;
        }
    }
    return false;
}
```

### 2. Notification-Based Activity Launch

When the app is in the background, instead of directly launching the activity, we show a notification that the user can tap to launch the activity:

```java
private void showSelectRoleNotification() {
    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    
    // Create notification channel for Android O and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
            "select_role_channel",
            "Select Role",
            NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(channel);
    }

    // Create intent for SelectRole activity
    Intent intent = new Intent(this, SelectRole.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    
    PendingIntent pendingIntent = PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );

    // Build notification
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "select_role_channel")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Login Required")
        .setContentText("Please select your role to continue")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent);

    // Show notification
    notificationManager.notify(1001, builder.build());
}
```

### 3. Conditional Activity Launch

Modified the activity launch logic to check if the app is in the foreground:

```java
// Check if app is in foreground before launching activity
if (isAppInForeground()) {
    startActivity(new Intent(Splash.this, SelectRole.class));
    finish();
} else {
    // Show notification instead of direct launch
    showSelectRoleNotification();
    finish();
}
```

## Files Modified

1. **app/src/main/java/topgrade/parent/com/parentseeks/Parent/Activity/Splash.java**
   - Added foreground detection
   - Added notification-based activity launch
   - Modified activity launch logic in `load_exam_session()` method

2. **app/src/main/java/topgrade/parent/com/parentseeks/Parent/Activity/DashBoard.java**
   - Added foreground detection
   - Added notification-based activity launch
   - Modified activity launch logic in `logout()` method and `clickDrawerItem()` method

## Benefits

1. **Compliance**: Follows Android's background activity restrictions
2. **User Experience**: Users receive a clear notification when action is needed
3. **Reliability**: Prevents app crashes due to blocked activity launches
4. **Security**: Respects Android's security model

## Testing

To test the fix:

1. Build and install the app
2. Put the app in the background (press home button)
3. Trigger a logout or scenario that would launch SelectRole
4. Verify that a notification appears instead of a crash
5. Tap the notification to launch the SelectRole activity

## Additional Notes

- The notification uses different notification IDs (1001 for Splash, 1002 for DashBoard) to avoid conflicts
- The notification channel is created with default importance to ensure it's visible
- The PendingIntent uses `FLAG_IMMUTABLE` for Android 12+ compatibility
- The activity intent includes proper flags for task management 