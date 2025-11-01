package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.os.Process;

import topgrade.parent.com.parentseeks.BuildConfig;

/**
 * Utility class to monitor app health and handle system-level issues
 */
public class AppHealthMonitor {
    
    private static final String TAG = "AppHealthMonitor";
    
    /**
     * Check if the app is running on a supported Android version
     * @return true if supported, false otherwise
     */
    public static boolean isAndroidVersionSupported() {
        return true; // minSdk is 26, so always supported
    }
    
    /**
     * Check if required permissions are granted
     * @param context Application context
     * @param permissions Array of permission strings
     * @return true if all permissions granted, false otherwise
     */
    public static boolean arePermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Permission not granted: " + permission);
                return false;
            }
        }
        return true;
    }
    
    /**
     * Log app health information
     * @param context Application context
     */
    public static void logAppHealth(Context context) {
        Log.i(TAG, "=== App Health Report ===");
        Log.i(TAG, "Android Version: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        Log.i(TAG, "Device: " + Build.MANUFACTURER + " " + Build.MODEL);
        Log.i(TAG, "App Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
        Log.i(TAG, "Process ID: " + Process.myPid());
        Log.i(TAG, "Supported Android Version: " + isAndroidVersionSupported());
        
        // Check critical permissions
        String[] criticalPermissions = {
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE"
        };
        Log.i(TAG, "Critical Permissions Granted: " + arePermissionsGranted(context, criticalPermissions));
        Log.i(TAG, "=== End Health Report ===");
    }
    
    /**
     * Handle system-level errors gracefully
     * @param context Application context
     * @param error Error message
     */
    public static void handleSystemError(Context context, String error) {
        Log.w(TAG, "System error detected: " + error);
        
        // Log app health for debugging
        logAppHealth(context);
        
        // Enhanced error categorization
        if (error.contains("Parcel") || error.contains("NULL string")) {
            Log.w(TAG, "Parcel-related system error - this is usually not app-specific");
        } else if (error.contains("Permission")) {
            Log.w(TAG, "Permission-related error detected");
        } else if (error.contains("Memory")) {
            Log.w(TAG, "Memory-related error detected");
        } else if (error.contains("NullPointerException")) {
            Log.w(TAG, "Null pointer exception detected - check for uninitialized objects");
        } else if (error.contains("Looper")) {
            Log.w(TAG, "Looper-related error - check thread management");
        } else if (error.contains("Bluetooth") || error.contains("BtGatt")) {
            Log.w(TAG, "Bluetooth-related error - check Bluetooth permissions and state");
        } else if (error.contains("WindowManager") || error.contains("SurfaceFlinger")) {
            Log.w(TAG, "Display/Window management error - check UI thread operations");
        }
    }
    
    /**
     * Check if the app is in a healthy state
     * @param context Application context
     * @return true if healthy, false otherwise
     */
    public static boolean isAppHealthy(Context context) {
        boolean healthy = true;
        
        if (!isAndroidVersionSupported()) {
            Log.w(TAG, "Unsupported Android version");
            healthy = false;
        }
        
        String[] criticalPermissions = {
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE"
        };
        
        if (!arePermissionsGranted(context, criticalPermissions)) {
            Log.w(TAG, "Critical permissions not granted");
            healthy = false;
        }
        
        return healthy;
    }
    
    /**
     * Handle specific error types from the log output
     * @param context Application context
     * @param errorType Type of error encountered
     * @param errorMessage Detailed error message
     */
    public static void handleSpecificError(Context context, String errorType, String errorMessage) {
        Log.w(TAG, "Handling specific error - Type: " + errorType + ", Message: " + errorMessage);
        
        switch (errorType.toLowerCase()) {
            case "nullpointerexception":
                handleNullPointerError(context, errorMessage);
                break;
            case "looper":
                handleLooperError(context, errorMessage);
                break;
            case "bluetooth":
                handleBluetoothError(context, errorMessage);
                break;
            case "windowmanager":
                handleWindowManagerError(context, errorMessage);
                break;
            case "memory":
                handleMemoryError(context, errorMessage);
                break;
            default:
                handleSystemError(context, errorMessage);
                break;
        }
    }
    
    private static void handleNullPointerError(Context context, String errorMessage) {
        Log.w(TAG, "Null pointer exception detected. Check for:");
        Log.w(TAG, "- Uninitialized objects before use");
        Log.w(TAG, "- Null checks before method calls");
        Log.w(TAG, "- Proper initialization in onCreate/onResume");
    }
    
    private static void handleLooperError(Context context, String errorMessage) {
        Log.w(TAG, "Looper-related error detected. Check for:");
        Log.w(TAG, "- UI operations on background threads");
        Log.w(TAG, "- Handler/Looper initialization");
        Log.w(TAG, "- Thread management in async operations");
    }
    
    private static void handleBluetoothError(Context context, String errorMessage) {
        Log.w(TAG, "Bluetooth error detected. Check for:");
        Log.w(TAG, "- Bluetooth permissions in manifest");
        Log.w(TAG, "- Bluetooth adapter availability");
        Log.w(TAG, "- Bluetooth state before operations");
    }
    
    private static void handleWindowManagerError(Context context, String errorMessage) {
        Log.w(TAG, "Window manager error detected. Check for:");
        Log.w(TAG, "- UI operations on main thread");
        Log.w(TAG, "- Proper activity lifecycle management");
        Log.w(TAG, "- Dialog/Window cleanup");
    }
    
    private static void handleMemoryError(Context context, String errorMessage) {
        Log.w(TAG, "Memory error detected. Check for:");
        Log.w(TAG, "- Memory leaks in activities/fragments");
        Log.w(TAG, "- Large bitmap handling");
        Log.w(TAG, "- Proper resource cleanup");
    }
} 