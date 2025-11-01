package topgrade.parent.com.parentseeks.Parent.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    
    private static final String TAG = "PermissionUtils";
    
    // Permission request codes
    public static final int REQUEST_CAMERA_PERMISSION = 100;
    public static final int REQUEST_STORAGE_PERMISSION = 101;
    public static final int REQUEST_LOCATION_PERMISSION = 102;
    public static final int REQUEST_MICROPHONE_PERMISSION = 103;
    public static final int REQUEST_CONTACTS_PERMISSION = 104;
    public static final int REQUEST_CALENDAR_PERMISSION = 105;
    public static final int REQUEST_PHONE_PERMISSION = 106;
    public static final int REQUEST_SMS_PERMISSION = 107;
    public static final int REQUEST_MULTIPLE_PERMISSIONS = 200;
    
    /**
     * Check if camera permission is granted
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if storage permission is granted (for Android < 10)
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ uses scoped storage, no storage permission needed for app-specific directories
            return true;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                   == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * Check if location permission is granted
     */
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
               == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if microphone permission is granted
     */
    public static boolean hasMicrophonePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if contacts permission is granted
     */
    public static boolean hasContactsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if calendar permission is granted
     */
    public static boolean hasCalendarPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if phone permission is granted
     */
    public static boolean hasPhonePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if SMS permission is granted
     */
    public static boolean hasSmsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Request camera permission
     */
    public static void requestCameraPermission(Activity activity) {
        if (activity == null) return;
        
        if (!hasCameraPermission(activity)) {
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.CAMERA}, 
                REQUEST_CAMERA_PERMISSION);
        }
    }
    
    /**
     * Request storage permission (for Android < 10)
     */
    public static void requestStoragePermission(Activity activity) {
        if (activity == null) return;
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasStoragePermission(activity)) {
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                REQUEST_STORAGE_PERMISSION);
        }
    }
    
    /**
     * Request location permission
     */
    public static void requestLocationPermission(Activity activity) {
        if (activity == null) return;
        
        if (!hasLocationPermission(activity)) {
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                REQUEST_LOCATION_PERMISSION);
        }
    }
    
    /**
     * Request microphone permission
     */
    public static void requestMicrophonePermission(Activity activity) {
        if (activity == null) return;
        
        if (!hasMicrophonePermission(activity)) {
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.RECORD_AUDIO}, 
                REQUEST_MICROPHONE_PERMISSION);
        }
    }
    
    /**
     * Request contacts permission
     */
    public static void requestContactsPermission(Activity activity) {
        if (activity == null) return;
        
        if (!hasContactsPermission(activity)) {
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.READ_CONTACTS}, 
                REQUEST_CONTACTS_PERMISSION);
        }
    }
    
    /**
     * Request calendar permission
     */
    public static void requestCalendarPermission(Activity activity) {
        if (activity == null) return;
        
        if (!hasCalendarPermission(activity)) {
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.READ_CALENDAR}, 
                REQUEST_CALENDAR_PERMISSION);
        }
    }
    
    /**
     * Request phone permission
     */
    public static void requestPhonePermission(Activity activity) {
        if (activity == null) return;
        
        if (!hasPhonePermission(activity)) {
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.CALL_PHONE}, 
                REQUEST_PHONE_PERMISSION);
        }
    }
    
    /**
     * Request SMS permission
     */
    public static void requestSmsPermission(Activity activity) {
        if (activity == null) return;
        
        if (!hasSmsPermission(activity)) {
            ActivityCompat.requestPermissions(activity, 
                new String[]{Manifest.permission.SEND_SMS}, 
                REQUEST_SMS_PERMISSION);
        }
    }
    
    /**
     * Request multiple permissions
     */
    public static void requestMultiplePermissions(Activity activity, String[] permissions) {
        if (activity == null || permissions == null || permissions.length == 0) return;
        
        List<String> permissionsToRequest = new ArrayList<>();
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(activity, 
                permissionsToRequest.toArray(new String[0]), 
                REQUEST_MULTIPLE_PERMISSIONS);
        }
    }
    
    /**
     * Check if permission should show rationale
     */
    public static boolean shouldShowPermissionRationale(Activity activity, String permission) {
        if (activity == null || permission == null) return false;
        
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
    
    /**
     * Check if all permissions are granted
     */
    public static boolean areAllPermissionsGranted(Context context, String[] permissions) {
        if (context == null || permissions == null) return false;
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get denied permissions
     */
    public static List<String> getDeniedPermissions(Context context, String[] permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        
        if (context == null || permissions == null) return deniedPermissions;
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }
        
        return deniedPermissions;
    }
    
    /**
     * Check if any permission is permanently denied
     */
    public static boolean isAnyPermissionPermanentlyDenied(Activity activity, String[] permissions) {
        if (activity == null || permissions == null) return false;
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                != PackageManager.PERMISSION_GRANTED &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get permission display name
     */
    public static String getPermissionDisplayName(String permission) {
        if (permission == null) return "Unknown Permission";
        
        switch (permission) {
            case Manifest.permission.CAMERA:
                return "Camera";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Storage";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Storage";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "Location";
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "Location";
            case Manifest.permission.RECORD_AUDIO:
                return "Microphone";
            case Manifest.permission.READ_CONTACTS:
                return "Contacts";
            case Manifest.permission.WRITE_CONTACTS:
                return "Contacts";
            case Manifest.permission.READ_CALENDAR:
                return "Calendar";
            case Manifest.permission.WRITE_CALENDAR:
                return "Calendar";
            case Manifest.permission.CALL_PHONE:
                return "Phone";
            case Manifest.permission.SEND_SMS:
                return "SMS";
            case Manifest.permission.READ_SMS:
                return "SMS";
            default:
                return "Unknown Permission";
        }
    }
    
    /**
     * Get permission description
     */
    public static String getPermissionDescription(String permission) {
        if (permission == null) return "This permission is required for app functionality.";
        
        switch (permission) {
            case Manifest.permission.CAMERA:
                return "Camera permission is needed to take photos and videos.";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Storage permission is needed to save files to your device.";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Storage permission is needed to access files on your device.";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "Location permission is needed to show your current location.";
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "Location permission is needed to show your approximate location.";
            case Manifest.permission.RECORD_AUDIO:
                return "Microphone permission is needed to record audio.";
            case Manifest.permission.READ_CONTACTS:
                return "Contacts permission is needed to access your contacts.";
            case Manifest.permission.WRITE_CONTACTS:
                return "Contacts permission is needed to save contact information.";
            case Manifest.permission.READ_CALENDAR:
                return "Calendar permission is needed to access your calendar events.";
            case Manifest.permission.WRITE_CALENDAR:
                return "Calendar permission is needed to create calendar events.";
            case Manifest.permission.CALL_PHONE:
                return "Phone permission is needed to make phone calls.";
            case Manifest.permission.SEND_SMS:
                return "SMS permission is needed to send text messages.";
            case Manifest.permission.READ_SMS:
                return "SMS permission is needed to read text messages.";
            default:
                return "This permission is required for app functionality.";
        }
    }
} 