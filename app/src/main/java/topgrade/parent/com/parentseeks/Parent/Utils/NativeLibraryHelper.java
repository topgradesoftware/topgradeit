package topgrade.parent.com.parentseeks.Parent.Utils;

import android.util.Log;

/**
 * Utility class to safely handle native library loading
 */
public class NativeLibraryHelper {
    
    private static final String TAG = "NativeLibraryHelper";
    
    /**
     * Safely load a native library
     * @param libraryName The name of the library to load
     * @return true if loaded successfully, false otherwise
     */
    public static boolean loadNativeLibrary(String libraryName) {
        try {
            System.loadLibrary(libraryName);
            Log.d(TAG, "Successfully loaded native library: " + libraryName);
            return true;
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "Failed to load native library: " + libraryName + ", Error: " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            Log.w(TAG, "Security exception loading native library: " + libraryName + ", Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.w(TAG, "Unexpected error loading native library: " + libraryName + ", Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a native library is available
     * @param libraryName The name of the library to check
     * @return true if available, false otherwise
     */
    public static boolean isNativeLibraryAvailable(String libraryName) {
        try {
            System.loadLibrary(libraryName);
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }
} 