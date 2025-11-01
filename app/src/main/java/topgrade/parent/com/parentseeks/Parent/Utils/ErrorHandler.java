package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.os.Process;

import topgrade.parent.com.parentseeks.BuildConfig;

/**
 * Enhanced error handling utility for categorizing and handling different types of errors
 */
public class ErrorHandler {
    
    private static final String TAG = "ErrorHandler";
    
    /**
     * Categorize and handle errors based on the log output patterns
     * @param context Application context
     * @param errorMessage The error message from logcat
     */
    public static void handleLogError(Context context, String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return;
        }
        
        Log.w(TAG, "Processing error: " + errorMessage);
        
        // Categorize errors based on common patterns
        if (errorMessage.contains("NullPointerException")) {
            handleNullPointerError(context, errorMessage);
        } else if (errorMessage.contains("Looper") || errorMessage.contains("isPerfLogEnable")) {
            handleLooperError(context, errorMessage);
        } else if (errorMessage.contains("BtGatt") || errorMessage.contains("bluetooth")) {
            handleBluetoothError(context, errorMessage);
        } else if (errorMessage.contains("WindowManager") || errorMessage.contains("SurfaceFlinger")) {
            handleWindowManagerError(context, errorMessage);
        } else if (errorMessage.contains("libprotobuf") || errorMessage.contains("sns_std_sensor_event")) {
            handleSensorError(context, errorMessage);
        } else if (errorMessage.contains("Memory") || errorMessage.contains("OutOfMemory")) {
            handleMemoryError(context, errorMessage);
        } else if (errorMessage.contains("Permission") || errorMessage.contains("SecurityException")) {
            handlePermissionError(context, errorMessage);
        } else if (errorMessage.contains("Context not found") || errorMessage.contains("ContextMap")) {
            handleContextError(context, errorMessage);
        } else {
            handleGenericError(context, errorMessage);
        }
    }
    
    private static void handleNullPointerError(Context context, String errorMessage) {
        Log.w(TAG, "=== NULL POINTER EXCEPTION DETECTED ===");
        Log.w(TAG, "Error: " + errorMessage);
        Log.w(TAG, "Recommendations:");
        Log.w(TAG, "1. Check for null objects before method calls");
        Log.w(TAG, "2. Ensure proper initialization in onCreate/onResume");
        Log.w(TAG, "3. Add null checks in critical code paths");
        Log.w(TAG, "4. Review object lifecycle management");
        
        // Log app health for debugging
        AppHealthMonitor.logAppHealth(context);
    }
    
    private static void handleLooperError(Context context, String errorMessage) {
        Log.w(TAG, "=== LOOPER ERROR DETECTED ===");
        Log.w(TAG, "Error: " + errorMessage);
        Log.w(TAG, "Recommendations:");
        Log.w(TAG, "1. Ensure UI operations run on main thread");
        Log.w(TAG, "2. Check Handler/Looper initialization");
        Log.w(TAG, "3. Review async operation thread management");
        Log.w(TAG, "4. Use runOnUiThread() for UI updates from background threads");
        
        AppHealthMonitor.logAppHealth(context);
    }
    
    private static void handleBluetoothError(Context context, String errorMessage) {
        Log.w(TAG, "=== BLUETOOTH ERROR DETECTED ===");
        Log.w(TAG, "Error: " + errorMessage);
        Log.w(TAG, "Recommendations:");
        Log.w(TAG, "1. Check Bluetooth permissions in AndroidManifest.xml");
        Log.w(TAG, "2. Verify Bluetooth adapter availability");
        Log.w(TAG, "3. Handle Bluetooth state changes properly");
        Log.w(TAG, "4. Add proper error handling for Bluetooth operations");
        
        AppHealthMonitor.logAppHealth(context);
    }
    
    private static void handleWindowManagerError(Context context, String errorMessage) {
        Log.w(TAG, "=== WINDOW MANAGER ERROR DETECTED ===");
        Log.w(TAG, "Error: " + errorMessage);
        Log.w(TAG, "Recommendations:");
        Log.w(TAG, "1. Ensure UI operations on main thread");
        Log.w(TAG, "2. Check activity lifecycle management");
        Log.w(TAG, "3. Properly handle dialog/window cleanup");
        Log.w(TAG, "4. Review surface/drawing operations");
        
        AppHealthMonitor.logAppHealth(context);
    }
    
    private static void handleSensorError(Context context, String errorMessage) {
        Log.w(TAG, "=== SENSOR ERROR DETECTED ===");
        Log.w(TAG, "Error: " + errorMessage);
        Log.w(TAG, "Recommendations:");
        Log.w(TAG, "1. Check sensor permissions if using sensors");
        Log.w(TAG, "2. Handle sensor unavailability gracefully");
        Log.w(TAG, "3. Review sensor data parsing");
        Log.w(TAG, "4. This is usually a system-level error, not app-specific");
        
        AppHealthMonitor.logAppHealth(context);
    }
    
    private static void handleMemoryError(Context context, String errorMessage) {
        Log.w(TAG, "=== MEMORY ERROR DETECTED ===");
        Log.w(TAG, "Error: " + errorMessage);
        Log.w(TAG, "Recommendations:");
        Log.w(TAG, "1. Check for memory leaks in activities/fragments");
        Log.w(TAG, "2. Optimize bitmap handling and image loading");
        Log.w(TAG, "3. Implement proper resource cleanup");
        Log.w(TAG, "4. Use memory-efficient data structures");
        
        AppHealthMonitor.logAppHealth(context);
    }
    
    private static void handlePermissionError(Context context, String errorMessage) {
        Log.w(TAG, "=== PERMISSION ERROR DETECTED ===");
        Log.w(TAG, "Error: " + errorMessage);
        Log.w(TAG, "Recommendations:");
        Log.w(TAG, "1. Check runtime permissions for Android 6.0+");
        Log.w(TAG, "2. Verify manifest permissions");
        Log.w(TAG, "3. Handle permission denial gracefully");
        Log.w(TAG, "4. Request permissions at appropriate times");
        
        AppHealthMonitor.logAppHealth(context);
    }
    
    private static void handleContextError(Context context, String errorMessage) {
        Log.w(TAG, "=== CONTEXT ERROR DETECTED ===");
        Log.w(TAG, "Error: " + errorMessage);
        Log.w(TAG, "Recommendations:");
        Log.w(TAG, "1. Check context lifecycle management");
        Log.w(TAG, "2. Avoid using activity context in long-lived objects");
        Log.w(TAG, "3. Use ApplicationContext when appropriate");
        Log.w(TAG, "4. Review context usage in async operations");
        
        AppHealthMonitor.logAppHealth(context);
    }
    
    private static void handleGenericError(Context context, String errorMessage) {
        Log.w(TAG, "=== GENERIC ERROR DETECTED ===");
        Log.w(TAG, "Error: " + errorMessage);
        Log.w(TAG, "This appears to be a system-level error or from another app");
        Log.w(TAG, "Recommendations:");
        Log.w(TAG, "1. Check if error affects app functionality");
        Log.w(TAG, "2. Monitor app performance and stability");
        Log.w(TAG, "3. Report to system administrator if persistent");
        
        AppHealthMonitor.logAppHealth(context);
    }
    
    /**
     * Check if an error is app-specific or system-level
     * @param errorMessage The error message
     * @return true if app-specific, false if system-level
     */
    public static boolean isAppSpecificError(String errorMessage) {
        if (errorMessage == null) return false;
        
        // Check if error contains your app's package name
        String packageName = BuildConfig.APPLICATION_ID;
        return errorMessage.contains(packageName);
    }
    
    /**
     * Get error severity level
     * @param errorMessage The error message
     * @return Severity level (1=Low, 2=Medium, 3=High, 4=Critical)
     */
    public static int getErrorSeverity(String errorMessage) {
        if (errorMessage == null) return 1;
        
        if (errorMessage.contains("OutOfMemory") || errorMessage.contains("FATAL")) {
            return 4; // Critical
        } else if (errorMessage.contains("NullPointerException") || errorMessage.contains("SecurityException")) {
            return 3; // High
        } else if (errorMessage.contains("Permission") || errorMessage.contains("Context")) {
            return 2; // Medium
        } else {
            return 1; // Low
        }
    }
    
    /**
     * Log comprehensive error report
     * @param context Application context
     * @param errorMessage The error message
     */
    public static void logErrorReport(Context context, String errorMessage) {
        Log.e(TAG, "=== ERROR REPORT ===");
        Log.e(TAG, "Timestamp: " + System.currentTimeMillis());
        Log.e(TAG, "Error: " + errorMessage);
        Log.e(TAG, "App-specific: " + isAppSpecificError(errorMessage));
        Log.e(TAG, "Severity: " + getErrorSeverity(errorMessage));
        Log.e(TAG, "Device: " + Build.MANUFACTURER + " " + Build.MODEL);
        Log.e(TAG, "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        Log.e(TAG, "App Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
        Log.e(TAG, "Process ID: " + Process.myPid());
        Log.e(TAG, "=== END ERROR REPORT ===");
    }
} 