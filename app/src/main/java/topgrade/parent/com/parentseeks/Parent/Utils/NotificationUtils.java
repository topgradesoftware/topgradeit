package topgrade.parent.com.parentseeks.Parent.Utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.content.ContextCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import topgrade.parent.com.parentseeks.Parent.Activity.ModernStudentTimeTable;
import topgrade.parent.com.parentseeks.R;

/**
 * Utility class for handling class notifications and timetable-related notifications
 */
public class NotificationUtils {
    
    private static final String TAG = "NotificationUtils";
    
    // Notification channel IDs
    public static final String CHANNEL_ID_GENERAL = "general";
    public static final String CHANNEL_ID_IMPORTANT = "important";
    public static final String CHANNEL_ID_UPDATES = "updates";
    public static final String CHANNEL_ID_DOWNLOADS = "downloads";
    public static final String CHANNEL_ID_BACKUP = "backup";
    private static final String CHANNEL_ID_CLASS = "class_notifications";
    private static final String CHANNEL_NAME_CLASS = "Class Reminders";
    
    // Notification IDs
    public static final int NOTIFICATION_ID_GENERAL = 1000;
    public static final int NOTIFICATION_ID_IMPORTANT = 1001;
    public static final int NOTIFICATION_ID_UPDATES = 1002;
    public static final int NOTIFICATION_ID_DOWNLOADS = 1003;
    public static final int NOTIFICATION_ID_BACKUP = 1004;
    private static final int NOTIFICATION_ID_BASE = 1000;
    
    /**
     * Create notification channels (required for Android 8.0+)
     */
    public static void createNotificationChannels(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot create notification channels");
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            
            if (notificationManager == null) {
                Log.e(TAG, "NotificationManager is null, cannot create channels");
                return;
            }
            
            try {
                // General channel
                NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                generalChannel.setDescription("General notifications");
                notificationManager.createNotificationChannel(generalChannel);
                
                // Important channel
                NotificationChannel importantChannel = new NotificationChannel(
                    CHANNEL_ID_IMPORTANT,
                    "Important",
                    NotificationManager.IMPORTANCE_HIGH
                );
                importantChannel.setDescription("Important notifications");
                notificationManager.createNotificationChannel(importantChannel);
                
                // Updates channel
                NotificationChannel updatesChannel = new NotificationChannel(
                    CHANNEL_ID_UPDATES,
                    "Updates",
                    NotificationManager.IMPORTANCE_LOW
                );
                updatesChannel.setDescription("App updates and maintenance");
                notificationManager.createNotificationChannel(updatesChannel);
                
                // Downloads channel
                NotificationChannel downloadsChannel = new NotificationChannel(
                    CHANNEL_ID_DOWNLOADS,
                    "Downloads",
                    NotificationManager.IMPORTANCE_LOW
                );
                downloadsChannel.setDescription("Download progress and completion");
                notificationManager.createNotificationChannel(downloadsChannel);
                
                // Backup channel
                NotificationChannel backupChannel = new NotificationChannel(
                    CHANNEL_ID_BACKUP,
                    "Backup",
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                backupChannel.setDescription("Backup and restore notifications");
                notificationManager.createNotificationChannel(backupChannel);
                
                // Class channel
                NotificationChannel classChannel = new NotificationChannel(
                    CHANNEL_ID_CLASS,
                    CHANNEL_NAME_CLASS,
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                classChannel.setDescription("Notifications for class start times");
                classChannel.enableLights(true);
                classChannel.setLightColor(android.graphics.Color.BLUE);
                classChannel.enableVibration(true);
                classChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
                classChannel.setShowBadge(true);
                notificationManager.createNotificationChannel(classChannel);
                
                Log.d(TAG, "All notification channels created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channels", e);
            }
        } else {
            Log.d(TAG, "Android version < 8.0, notification channels not required");
        }
    }
    
    /**
     * Check if notification permission is granted
     */
    private static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission not required for older Android versions
    }
    
    /**
     * Show simple notification
     */
    public static void showNotification(Context context, String title, String message, String channelId, int notificationId) {
        if (context == null || title == null || message == null) {
            Log.e(TAG, "Invalid parameters for showing notification");
            return;
        }
        
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }
        
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
            
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            
            Log.d(TAG, "Notification shown: " + title);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }
    
    /**
     * Show notification with intent
     */
    public static void showNotificationWithIntent(Context context, String title, String message, 
                                                 String channelId, int notificationId, Intent intent) {
        if (context == null || title == null || message == null || intent == null) {
            Log.e(TAG, "Invalid parameters for showing notification with intent");
            return;
        }
        
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }
        
        try {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
            
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            
            Log.d(TAG, "Notification with intent shown: " + title);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification with intent", e);
        }
    }
    
    /**
     * Show progress notification
     */
    public static void showProgressNotification(Context context, String title, String message, 
                                               String channelId, int notificationId, int progress, int max) {
        if (context == null || title == null || message == null) {
            Log.e(TAG, "Invalid parameters for showing progress notification");
            return;
        }
        
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }
        
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setProgress(max, progress, false);
            
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            
            Log.d(TAG, "Progress notification shown: " + title + " (" + progress + "/" + max + ")");
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing progress notification", e);
        }
    }
    
    /**
     * Show indeterminate progress notification
     */
    public static void showIndeterminateProgressNotification(Context context, String title, String message, 
                                                            String channelId, int notificationId) {
        if (context == null || title == null || message == null) {
            Log.e(TAG, "Invalid parameters for showing indeterminate progress notification");
            return;
        }
        
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }
        
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setProgress(0, 0, true);
            
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            
            Log.d(TAG, "Indeterminate progress notification shown: " + title);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing indeterminate progress notification", e);
        }
    }
    
    /**
     * Update progress notification
     */
    public static void updateProgressNotification(Context context, String title, String message, 
                                                 String channelId, int notificationId, int progress, int max) {
        if (context == null || title == null || message == null) {
            Log.e(TAG, "Invalid parameters for updating progress notification");
            return;
        }
        
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }
        
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setProgress(max, progress, false);
            
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            
            Log.d(TAG, "Progress notification updated: " + title + " (" + progress + "/" + max + ")");
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating progress notification", e);
        }
    }
    
    /**
     * Complete progress notification
     */
    public static void completeProgressNotification(Context context, String title, String message, 
                                                   String channelId, int notificationId) {
        if (context == null || title == null || message == null) {
            Log.e(TAG, "Invalid parameters for completing progress notification");
            return;
        }
        
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }
        
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setProgress(0, 0, false);
            
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            
            Log.d(TAG, "Progress notification completed: " + title);
            
        } catch (Exception e) {
            Log.e(TAG, "Error completing progress notification", e);
        }
    }
    
    /**
     * Cancel notification
     */
    public static void cancelNotification(Context context, int notificationId) {
        if (context == null) {
            Log.e(TAG, "Context is null for canceling notification");
            return;
        }
        
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(notificationId);
            
            Log.d(TAG, "Notification canceled: " + notificationId);
            
        } catch (Exception e) {
            Log.e(TAG, "Error canceling notification", e);
        }
    }
    
    /**
     * Cancel all notifications
     */
    public static void cancelAllNotifications(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null for canceling all notifications");
            return;
        }
        
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancelAll();
            
            Log.d(TAG, "All notifications canceled");
            
        } catch (Exception e) {
            Log.e(TAG, "Error canceling all notifications", e);
        }
    }
    
    /**
     * Show download completion notification
     */
    public static void showDownloadCompletionNotification(Context context, String fileName) {
        if (context == null || fileName == null) {
            Log.e(TAG, "Invalid parameters for download completion notification");
            return;
        }
        
        String title = "Download Complete";
        String message = "File downloaded successfully: " + fileName;
        
        showNotification(context, title, message, CHANNEL_ID_DOWNLOADS, NOTIFICATION_ID_DOWNLOADS);
    }
    
    /**
     * Show backup completion notification
     */
    public static void showBackupCompletionNotification(Context context, String backupName) {
        if (context == null || backupName == null) {
            Log.e(TAG, "Invalid parameters for backup completion notification");
            return;
        }
        
        String title = "Backup Complete";
        String message = "Backup created successfully: " + backupName;
        
        showNotification(context, title, message, CHANNEL_ID_BACKUP, NOTIFICATION_ID_BACKUP);
    }
    
    /**
     * Show error notification
     */
    public static void showErrorNotification(Context context, String title, String errorMessage) {
        if (context == null || title == null || errorMessage == null) {
            Log.e(TAG, "Invalid parameters for error notification");
            return;
        }
        
        showNotification(context, title, errorMessage, CHANNEL_ID_IMPORTANT, NOTIFICATION_ID_IMPORTANT);
    }
    
    /**
     * Show update notification
     */
    public static void showUpdateNotification(Context context, String updateMessage) {
        if (context == null || updateMessage == null) {
            Log.e(TAG, "Invalid parameters for update notification");
            return;
        }
        
        String title = "App Update";
        
        showNotification(context, title, updateMessage, CHANNEL_ID_UPDATES, NOTIFICATION_ID_UPDATES);
    }
    
    /**
     * Schedule a notification for class start time
     */
    public static void scheduleClassNotification(Context context, String subject, String startTime, String endTime, String studentName) {
        try {
            // Parse the start time (assuming format like "08:00" or "08:00 AM")
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date classTime = timeFormat.parse(startTime);
            
            if (classTime == null) {
                Log.w(TAG, "Could not parse start time: " + startTime);
                return;
            }
            
            // Get current date and set the class time
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, classTime.getHours());
            calendar.set(Calendar.MINUTE, classTime.getMinutes());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            // If the time has already passed today, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            // Schedule the notification
            scheduleNotification(context, subject, startTime, endTime, studentName, calendar.getTimeInMillis());
            
            Log.d(TAG, "Scheduled notification for " + subject + " at " + startTime + " for " + studentName);
            
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing time: " + startTime, e);
        }
    }
    
    /**
     * Schedule a notification at a specific time
     */
    private static void scheduleNotification(Context context, String subject, String startTime, String endTime, String studentName, long triggerTime) {
        // Validate input parameters
        if (context == null) {
            Log.e(TAG, "Context is null, cannot schedule notification");
            return;
        }
        if (subject == null || subject.trim().isEmpty()) {
            Log.e(TAG, "Subject is null or empty, cannot schedule notification");
            return;
        }
        if (startTime == null || startTime.trim().isEmpty()) {
            Log.e(TAG, "Start time is null or empty, cannot schedule notification");
            return;
        }
        if (endTime == null || endTime.trim().isEmpty()) {
            Log.e(TAG, "End time is null or empty, cannot schedule notification");
            return;
        }
        if (studentName == null || studentName.trim().isEmpty()) {
            Log.e(TAG, "Student name is null or empty, cannot schedule notification");
            return;
        }
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null, cannot schedule notification");
            return;
        }
        
        try {
            // Create unique notification ID based on subject and time
            int notificationId = NOTIFICATION_ID_BASE + subject.hashCode() + startTime.hashCode();
            
            Log.d(TAG, "Creating notification intent with ID: " + notificationId);
            
            // Create intent for the notification
            Intent intent = new Intent(context, ClassNotificationReceiver.class);
            intent.setAction("topgrade.parent.com.parentseeks.CLASS_NOTIFICATION");
            intent.putExtra("subject", subject);
            intent.putExtra("startTime", startTime);
            intent.putExtra("endTime", endTime);
            intent.putExtra("studentName", studentName);
            intent.putExtra("notificationId", notificationId);
            
            // Log the intent data for debugging
            Log.d(TAG, "Intent data - Subject: " + subject + 
                       ", StartTime: " + startTime + 
                       ", EndTime: " + endTime + 
                       ", StudentName: " + studentName + 
                       ", NotificationId: " + notificationId);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            if (pendingIntent == null) {
                Log.e(TAG, "Failed to create PendingIntent for notification");
                return;
            }
            
            // Schedule the alarm using inexact alarms for better compatibility
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            
            Log.d(TAG, "Successfully scheduled notification for " + subject + " at " + startTime + " (trigger time: " + triggerTime + ")");
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification", e);
        }
    }
    
    /**
     * Create notification channel for class notifications (Android 8.0+)
     */
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager == null) {
                Log.e(TAG, "NotificationManager is null");
                return;
            }
            
            // Check if channel already exists
            NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID_CLASS);
            if (existingChannel != null) {
                return; // Channel already exists
            }
            
            // Create the notification channel
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID_CLASS,
                CHANNEL_NAME_CLASS,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            
            // Configure the channel
            channel.setDescription("Notifications for class reminders");
            channel.enableLights(true);
            channel.setLightColor(android.graphics.Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setShowBadge(true);
            
            // Create the channel
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Created notification channel: " + CHANNEL_ID_CLASS);
        }
    }
    
    /**
     * Show class notification
     */
    public static void showClassNotification(Context context, String subject, String startTime, String endTime, String studentName, int notificationId) {
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }
        
        createNotificationChannel(context);
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager == null) {
            Log.e(TAG, "NotificationManager is null");
            return;
        }
        
        // Create intent to open timetable when notification is tapped
        Intent intent = new Intent(context, ModernStudentTimeTable.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_CLASS)
            .setSmallIcon(R.drawable.ic_error)
            .setContentTitle("Class Starting Soon")
            .setContentText(subject + " - " + startTime + " to " + endTime)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(subject + " class is starting at " + startTime + " for " + studentName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);
        
        // Show the notification
        notificationManager.notify(notificationId, builder.build());
        
        Log.d(TAG, "Showed notification for " + subject + " class");
    }
    
    /**
     * Cancel all class notifications
     */
    public static void cancelClassNotifications(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (alarmManager == null || notificationManager == null) {
            Log.e(TAG, "AlarmManager or NotificationManager is null");
            return;
        }
        
        // Cancel all pending alarms for class notifications
        for (int i = 0; i < 100; i++) { // Cancel notifications with IDs 1000-1099
            int notificationId = NOTIFICATION_ID_BASE + i;
            
            Intent intent = new Intent(context, ClassNotificationReceiver.class);
            intent.setAction("topgrade.parent.com.parentseeks.CLASS_NOTIFICATION");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
        
        // Cancel all displayed notifications
        notificationManager.cancelAll();
        
        Log.d(TAG, "Cancelled all class notifications");
    }
    
    /**
     * Schedule daily recurring notifications for all classes
     */
    public static void scheduleDailyClassNotifications(Context context, String subject, String startTime, String endTime, String studentName) {
        try {
            // Parse the start time
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date classTime = timeFormat.parse(startTime);
            
            if (classTime == null) {
                Log.w(TAG, "Could not parse start time: " + startTime);
                return;
            }
            
            // Get current date and set the class time
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, classTime.getHours());
            calendar.set(Calendar.MINUTE, classTime.getMinutes());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            // If the time has already passed today, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            // Schedule daily recurring notification
            scheduleDailyNotification(context, subject, startTime, endTime, studentName, calendar.getTimeInMillis());
            
            Log.d(TAG, "Scheduled daily notification for " + subject + " at " + startTime + " for " + studentName);
            
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing time: " + startTime, e);
        }
    }
    
    /**
     * Schedule a daily recurring notification
     */
    private static void scheduleDailyNotification(Context context, String subject, String startTime, String endTime, String studentName, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }
        
        // Create unique notification ID based on subject and time
        int notificationId = NOTIFICATION_ID_BASE + subject.hashCode() + startTime.hashCode();
        
        // Create intent for the notification
        Intent intent = new Intent(context, ClassNotificationReceiver.class);
        intent.setAction("topgrade.parent.com.parentseeks.CLASS_NOTIFICATION");
        intent.putExtra("subject", subject);
        intent.putExtra("startTime", startTime);
        intent.putExtra("endTime", endTime);
        intent.putExtra("studentName", studentName);
        intent.putExtra("notificationId", notificationId);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Schedule the daily recurring alarm
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, AlarmManager.INTERVAL_DAY, pendingIntent);
    }
    
    /**
     * Check if notifications are enabled
     */
    public static boolean areNotificationsEnabled(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager == null) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID_CLASS);
            return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
        } else {
            return true; // For older versions, assume notifications are enabled
        }
    }
    
    /**
     * Request notification permissions (for Android 13+)
     */
    public static void requestNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // This would typically be called from an activity
            // The actual permission request should be handled in the activity
            Log.d(TAG, "Notification permission request should be handled in activity");
        }
    }
    
    /**
     * Test method to verify notification system is working
     */
    public static void testNotificationSystem(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null for testing notification system");
            return;
        }
        
        Log.d(TAG, "Testing notification system...");
        
        try {
            // Create notification channels
            createNotificationChannels(context);
            
            // Test immediate notification
            showNotification(context, "Test Notification", "This is a test notification", 
                           CHANNEL_ID_GENERAL, NOTIFICATION_ID_GENERAL);
            
            // Test class notification
            showClassNotification(context, "Test Subject", "09:00", "10:00", "Test Student", 9999);
            
            Log.d(TAG, "Notification system test completed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error testing notification system", e);
        }
    }
} 