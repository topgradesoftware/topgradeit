package topgrade.parent.com.parentseeks.Parent.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.install.model.ActivityResult;

/**
 * Utility class for managing app updates using Google Play In-App Updates
 */
public class AppUpdateHelper {
    private static final String TAG = "AppUpdateHelper";
    private static final int APP_UPDATE_REQUEST_CODE = 500;
    private static final int FLEXIBLE_UPDATE_REQUEST_CODE = 501;
    
    private static AppUpdateHelper instance;
    private com.google.android.play.core.appupdate.AppUpdateManager appUpdateManager;
    private Context context;
    
    private AppUpdateHelper(Context context) {
        this.context = context.getApplicationContext();
        this.appUpdateManager = AppUpdateManagerFactory.create(this.context);
    }
    
    public static synchronized AppUpdateHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AppUpdateHelper(context);
        }
        return instance;
    }
    
    /**
     * Check for app updates and handle them appropriately
     */
    public void checkForUpdates(Activity activity) {
        try {
            Log.d(TAG, "Checking for app updates...");
            
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                Log.d(TAG, "Update availability: " + appUpdateInfo.updateAvailability());
                Log.d(TAG, "Available version code: " + appUpdateInfo.availableVersionCode());
                Log.d(TAG, "Client version staleness days: " + appUpdateInfo.clientVersionStalenessDays());
                
                switch (appUpdateInfo.updateAvailability()) {
                    case UpdateAvailability.UPDATE_AVAILABLE:
                        handleUpdateAvailable(activity, appUpdateInfo);
                        break;
                    case UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS:
                        handleDeveloperTriggeredUpdate(activity, appUpdateInfo);
                        break;
                    case UpdateAvailability.UPDATE_NOT_AVAILABLE:
                        Log.d(TAG, "No updates available");
                        break;
                    default:
                        Log.d(TAG, "Update availability: " + appUpdateInfo.updateAvailability());
                        break;
                }
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Failed to check for updates", exception);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error checking for app updates", e);
        }
    }
    
    /**
     * Handle when an update is available
     */
    private void handleUpdateAvailable(Activity activity, AppUpdateInfo appUpdateInfo) {
        // Check if update is mandatory (stale for more than 7 days)
        Integer stalenessDays = appUpdateInfo.clientVersionStalenessDays();
        boolean isMandatory = stalenessDays != null && stalenessDays >= 7;
        
        if (isMandatory) {
            Log.d(TAG, "Mandatory update available - starting immediate update");
            startImmediateUpdate(activity, appUpdateInfo);
        } else {
            Log.d(TAG, "Optional update available - starting flexible update");
            startFlexibleUpdate(activity, appUpdateInfo);
        }
    }
    
    /**
     * Handle developer triggered update
     */
    private void handleDeveloperTriggeredUpdate(Activity activity, AppUpdateInfo appUpdateInfo) {
        Log.d(TAG, "Developer triggered update in progress");
        // Resume the update
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                activity,
                APP_UPDATE_REQUEST_CODE
            );
        } catch (Exception e) {
            Log.e(TAG, "Error starting developer triggered update", e);
        }
    }
    
    /**
     * Start immediate update (mandatory)
     */
    private void startImmediateUpdate(Activity activity, AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                activity,
                APP_UPDATE_REQUEST_CODE
            );
        } catch (Exception e) {
            Log.e(TAG, "Error starting immediate update", e);
        }
    }
    
    /**
     * Start flexible update (optional)
     */
    private void startFlexibleUpdate(Activity activity, AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                activity,
                FLEXIBLE_UPDATE_REQUEST_CODE
            );
        } catch (Exception e) {
            Log.e(TAG, "Error starting flexible update", e);
        }
    }
    
    /**
     * Handle update result
     */
    public void handleUpdateResult(int requestCode, int resultCode, Activity activity) {
        if (requestCode == APP_UPDATE_REQUEST_CODE || requestCode == FLEXIBLE_UPDATE_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.d(TAG, "App update completed successfully");
                    Toast.makeText(activity, "App updated successfully!", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Log.d(TAG, "App update was cancelled");
                    if (requestCode == APP_UPDATE_REQUEST_CODE) {
                        // For immediate updates, cancellation should be handled
                        Toast.makeText(activity, "Update cancelled", Toast.LENGTH_SHORT).show();
                    }
                    // For flexible updates, cancellation is normal - user can continue using the app
                    break;
                default:
                    Log.d(TAG, "App update failed with result code: " + resultCode);
                    Toast.makeText(activity, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    
    /**
     * Check if update is in progress
     */
    public boolean isUpdateInProgress() {
        try {
            return appUpdateManager != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking update status", e);
            return false;
        }
    }
    
    /**
     * Get update manager instance
     */
    public com.google.android.play.core.appupdate.AppUpdateManager getUpdateManager() {
        return appUpdateManager;
    }
}