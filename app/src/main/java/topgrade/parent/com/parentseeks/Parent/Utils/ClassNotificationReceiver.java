package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * BroadcastReceiver to handle class notification broadcasts
 */
public class ClassNotificationReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ClassNotificationReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received class notification broadcast");
        
        // Log the action to understand what triggered this receiver
        String action = intent.getAction();
        Log.d(TAG, "Intent action: " + action);
        
        // Only process class notification intents, ignore other broadcasts
        if (action != null && !action.equals("android.intent.action.BOOT_COMPLETED") && 
            !action.equals("topgrade.parent.com.parentseeks.CLASS_NOTIFICATION")) {
            Log.d(TAG, "Ignoring non-class notification broadcast: " + action);
            return;
        }
        
        // Extract notification data from intent
        String subject = intent.getStringExtra("subject");
        String startTime = intent.getStringExtra("startTime");
        String endTime = intent.getStringExtra("endTime");
        String studentName = intent.getStringExtra("studentName");
        int notificationId = intent.getIntExtra("notificationId", 1000);
        
        // Log all extracted data for debugging
        Log.d(TAG, "Extracted data - Subject: " + subject + 
                   ", StartTime: " + startTime + 
                   ", EndTime: " + endTime + 
                   ", StudentName: " + studentName + 
                   ", NotificationId: " + notificationId);
        
        // Check for missing data with more detailed logging
        if (subject == null) {
            Log.e(TAG, "Missing notification data: subject is null");
            return;
        }
        if (startTime == null) {
            Log.e(TAG, "Missing notification data: startTime is null");
            return;
        }
        if (endTime == null) {
            Log.e(TAG, "Missing notification data: endTime is null");
            return;
        }
        if (studentName == null) {
            Log.e(TAG, "Missing notification data: studentName is null");
            return;
        }
        
        // Validate that the data is not empty
        if (subject.trim().isEmpty()) {
            Log.e(TAG, "Missing notification data: subject is empty");
            return;
        }
        if (startTime.trim().isEmpty()) {
            Log.e(TAG, "Missing notification data: startTime is empty");
            return;
        }
        if (endTime.trim().isEmpty()) {
            Log.e(TAG, "Missing notification data: endTime is empty");
            return;
        }
        if (studentName.trim().isEmpty()) {
            Log.e(TAG, "Missing notification data: studentName is empty");
            return;
        }
        
        try {
            // Show the notification
            NotificationUtils.showClassNotification(context, subject, startTime, endTime, studentName, notificationId);
            Log.d(TAG, "Successfully showed notification for " + subject + " class at " + startTime);
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }
} 