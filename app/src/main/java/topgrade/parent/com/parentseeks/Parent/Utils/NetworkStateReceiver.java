package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;

public class NetworkStateReceiver {
    
    private static final String TAG = "NetworkStateReceiver";
    private WeakReference<NetworkStateListener> listenerRef;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;
    
    public interface NetworkStateListener {
        void onNetworkAvailable();
        void onNetworkUnavailable();
    }
    
    public NetworkStateReceiver() {
        // Initialize network callback for modern API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    NetworkStateListener listener = listenerRef != null ? listenerRef.get() : null;
                    if (listener != null) {
                        try {
                            listener.onNetworkAvailable();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in network available callback", e);
                        }
                    }
                }

                @Override
                public void onLost(Network network) {
                    NetworkStateListener listener = listenerRef != null ? listenerRef.get() : null;
                    if (listener != null) {
                        try {
                            listener.onNetworkUnavailable();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in network lost callback", e);
                        }
                    }
                }
            };
        }
    }
    
    public void setListener(NetworkStateListener listener) {
        this.listenerRef = new WeakReference<>(listener);
    }
    
    /**
     * Register network callback for modern API (Android 7.0+)
     */
    public void registerNetworkCallback(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && networkCallback != null) {
            try {
                connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    NetworkRequest.Builder builder = new NetworkRequest.Builder();
                    builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                    builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                    builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
                    builder.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET);
                    
                    NetworkRequest networkRequest = builder.build();
                    connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
                    Log.d(TAG, "Network callback registered successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error registering network callback", e);
            }
        }
    }
    
    /**
     * Unregister network callback
     */
    public void unregisterNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && networkCallback != null && connectivityManager != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                Log.d(TAG, "Network callback unregistered successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering network callback", e);
            }
        }
    }
    
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                // minSdk is 26, so M (API 23) check is unnecessary
Network network = cm.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                        if (capabilities != null &&
                                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {
                            return true;
                    }
                } else {
                    // Fallback for older Android versions
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
     * Clear the listener reference to prevent memory leaks
     */
    public void clearListener() {
        if (listenerRef != null) {
            listenerRef.clear();
            listenerRef = null;
        }
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        unregisterNetworkCallback();
        clearListener();
    }
} 