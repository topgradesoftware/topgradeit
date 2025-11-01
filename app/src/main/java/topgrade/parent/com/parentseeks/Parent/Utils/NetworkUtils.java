package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

public class NetworkUtils {
    
    private static final String TAG = "NetworkUtils";
    
    /**
     * Check if device has internet connectivity using modern API
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                Network network = cm.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                        return capabilities != null && 
                               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                } else {
                    // Fallback for older Android versions (deprecated but still functional)
                    @SuppressWarnings("deprecation")
                    android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    return activeNetwork != null && activeNetwork.isConnected();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network availability", e);
        }
        return false;
    }
    
    /**
     * Check if device is connected to WiFi
     */
    public static boolean isWifiConnected(Context context) {
        if (context == null) return false;
        
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                Network network = cm.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                        return capabilities != null && 
                               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                } else {
                    // Fallback for older Android versions
                    @SuppressWarnings("deprecation")
                    android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    return activeNetwork != null && 
                           activeNetwork.isConnected() && 
                           activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking WiFi connection", e);
        }
        return false;
    }
    
    /**
     * Check if device is connected to mobile data
     */
    public static boolean isMobileDataConnected(Context context) {
        if (context == null) return false;
        
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                Network network = cm.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                        return capabilities != null && 
                               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                } else {
                    // Fallback for older Android versions
                    @SuppressWarnings("deprecation")
                    android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    return activeNetwork != null && 
                           activeNetwork.isConnected() && 
                           activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking mobile data connection", e);
        }
        return false;
    }
    
    /**
     * Get network type as string
     */
    public static String getNetworkType(Context context) {
        if (context == null) return "Unknown";
        
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                Network network = cm.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return "WiFi";
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return "Mobile";
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            return "Ethernet";
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
                            return "Bluetooth";
                        } else {
                            return "Other";
                        }
                    }
                }
                
                // Fallback for older Android versions
                @SuppressWarnings("deprecation")
                android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    switch (activeNetwork.getType()) {
                        case ConnectivityManager.TYPE_WIFI:
                            return "WiFi";
                        case ConnectivityManager.TYPE_MOBILE:
                            return "Mobile";
                        case ConnectivityManager.TYPE_ETHERNET:
                            return "Ethernet";
                        case ConnectivityManager.TYPE_BLUETOOTH:
                            return "Bluetooth";
                        default:
                            return "Other";
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting network type", e);
        }
        return "Unknown";
    }
} 