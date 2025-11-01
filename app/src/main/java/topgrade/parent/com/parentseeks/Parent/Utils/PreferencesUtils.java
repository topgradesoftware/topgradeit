package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;
import java.util.Set;

public class PreferencesUtils {
    
    private static final String TAG = "PreferencesUtils";
    private static final String DEFAULT_PREFERENCES_NAME = "app_preferences";
    
    /**
     * Get SharedPreferences instance
     */
    public static SharedPreferences getPreferences(Context context) {
        return getPreferences(context, DEFAULT_PREFERENCES_NAME);
    }
    
    /**
     * Get SharedPreferences instance with custom name
     */
    public static SharedPreferences getPreferences(Context context, String name) {
        if (context == null) return null;
        
        try {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE);
        } catch (Exception e) {
            Log.e(TAG, "Error getting SharedPreferences: " + name, e);
            return null;
        }
    }
    
    /**
     * Save string preference
     */
    public static boolean saveString(Context context, String key, String value) {
        return saveString(context, DEFAULT_PREFERENCES_NAME, key, value);
    }
    
    /**
     * Save string preference with custom preferences name
     */
    public static boolean saveString(Context context, String preferencesName, String key, String value) {
        if (context == null || key == null) return false;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(key, value);
                return editor.commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving string preference: " + key, e);
        }
        
        return false;
    }
    
    /**
     * Get string preference
     */
    public static String getString(Context context, String key, String defaultValue) {
        return getString(context, DEFAULT_PREFERENCES_NAME, key, defaultValue);
    }
    
    /**
     * Get string preference with custom preferences name
     */
    public static String getString(Context context, String preferencesName, String key, String defaultValue) {
        if (context == null || key == null) return defaultValue;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                return prefs.getString(key, defaultValue);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting string preference: " + key, e);
        }
        
        return defaultValue;
    }
    
    /**
     * Save integer preference
     */
    public static boolean saveInt(Context context, String key, int value) {
        return saveInt(context, DEFAULT_PREFERENCES_NAME, key, value);
    }
    
    /**
     * Save integer preference with custom preferences name
     */
    public static boolean saveInt(Context context, String preferencesName, String key, int value) {
        if (context == null || key == null) return false;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(key, value);
                return editor.commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving integer preference: " + key, e);
        }
        
        return false;
    }
    
    /**
     * Get integer preference
     */
    public static int getInt(Context context, String key, int defaultValue) {
        return getInt(context, DEFAULT_PREFERENCES_NAME, key, defaultValue);
    }
    
    /**
     * Get integer preference with custom preferences name
     */
    public static int getInt(Context context, String preferencesName, String key, int defaultValue) {
        if (context == null || key == null) return defaultValue;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                return prefs.getInt(key, defaultValue);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting integer preference: " + key, e);
        }
        
        return defaultValue;
    }
    
    /**
     * Save long preference
     */
    public static boolean saveLong(Context context, String key, long value) {
        return saveLong(context, DEFAULT_PREFERENCES_NAME, key, value);
    }
    
    /**
     * Save long preference with custom preferences name
     */
    public static boolean saveLong(Context context, String preferencesName, String key, long value) {
        if (context == null || key == null) return false;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(key, value);
                return editor.commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving long preference: " + key, e);
        }
        
        return false;
    }
    
    /**
     * Get long preference
     */
    public static long getLong(Context context, String key, long defaultValue) {
        return getLong(context, DEFAULT_PREFERENCES_NAME, key, defaultValue);
    }
    
    /**
     * Get long preference with custom preferences name
     */
    public static long getLong(Context context, String preferencesName, String key, long defaultValue) {
        if (context == null || key == null) return defaultValue;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                return prefs.getLong(key, defaultValue);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting long preference: " + key, e);
        }
        
        return defaultValue;
    }
    
    /**
     * Save float preference
     */
    public static boolean saveFloat(Context context, String key, float value) {
        return saveFloat(context, DEFAULT_PREFERENCES_NAME, key, value);
    }
    
    /**
     * Save float preference with custom preferences name
     */
    public static boolean saveFloat(Context context, String preferencesName, String key, float value) {
        if (context == null || key == null) return false;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat(key, value);
                return editor.commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving float preference: " + key, e);
        }
        
        return false;
    }
    
    /**
     * Get float preference
     */
    public static float getFloat(Context context, String key, float defaultValue) {
        return getFloat(context, DEFAULT_PREFERENCES_NAME, key, defaultValue);
    }
    
    /**
     * Get float preference with custom preferences name
     */
    public static float getFloat(Context context, String preferencesName, String key, float defaultValue) {
        if (context == null || key == null) return defaultValue;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                return prefs.getFloat(key, defaultValue);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting float preference: " + key, e);
        }
        
        return defaultValue;
    }
    
    /**
     * Save boolean preference
     */
    public static boolean saveBoolean(Context context, String key, boolean value) {
        return saveBoolean(context, DEFAULT_PREFERENCES_NAME, key, value);
    }
    
    /**
     * Save boolean preference with custom preferences name
     */
    public static boolean saveBoolean(Context context, String preferencesName, String key, boolean value) {
        if (context == null || key == null) return false;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(key, value);
                return editor.commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving boolean preference: " + key, e);
        }
        
        return false;
    }
    
    /**
     * Get boolean preference
     */
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getBoolean(context, DEFAULT_PREFERENCES_NAME, key, defaultValue);
    }
    
    /**
     * Get boolean preference with custom preferences name
     */
    public static boolean getBoolean(Context context, String preferencesName, String key, boolean defaultValue) {
        if (context == null || key == null) return defaultValue;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                return prefs.getBoolean(key, defaultValue);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting boolean preference: " + key, e);
        }
        
        return defaultValue;
    }
    
    /**
     * Save string set preference
     */
    public static boolean saveStringSet(Context context, String key, Set<String> value) {
        return saveStringSet(context, DEFAULT_PREFERENCES_NAME, key, value);
    }
    
    /**
     * Save string set preference with custom preferences name
     */
    public static boolean saveStringSet(Context context, String preferencesName, String key, Set<String> value) {
        if (context == null || key == null) return false;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putStringSet(key, value);
                return editor.commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving string set preference: " + key, e);
        }
        
        return false;
    }
    
    /**
     * Get string set preference
     */
    public static Set<String> getStringSet(Context context, String key, Set<String> defaultValue) {
        return getStringSet(context, DEFAULT_PREFERENCES_NAME, key, defaultValue);
    }
    
    /**
     * Get string set preference with custom preferences name
     */
    public static Set<String> getStringSet(Context context, String preferencesName, String key, Set<String> defaultValue) {
        if (context == null || key == null) return defaultValue;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                return prefs.getStringSet(key, defaultValue);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting string set preference: " + key, e);
        }
        
        return defaultValue;
    }
    
    /**
     * Remove preference
     */
    public static boolean removePreference(Context context, String key) {
        return removePreference(context, DEFAULT_PREFERENCES_NAME, key);
    }
    
    /**
     * Remove preference with custom preferences name
     */
    public static boolean removePreference(Context context, String preferencesName, String key) {
        if (context == null || key == null) return false;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(key);
                return editor.commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing preference: " + key, e);
        }
        
        return false;
    }
    
    /**
     * Clear all preferences
     */
    public static boolean clearAllPreferences(Context context) {
        return clearAllPreferences(context, DEFAULT_PREFERENCES_NAME);
    }
    
    /**
     * Clear all preferences with custom preferences name
     */
    public static boolean clearAllPreferences(Context context, String preferencesName) {
        if (context == null) return false;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                return editor.commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all preferences: " + preferencesName, e);
        }
        
        return false;
    }
    
    /**
     * Check if preference exists
     */
    public static boolean containsPreference(Context context, String key) {
        return containsPreference(context, DEFAULT_PREFERENCES_NAME, key);
    }
    
    /**
     * Check if preference exists with custom preferences name
     */
    public static boolean containsPreference(Context context, String preferencesName, String key) {
        if (context == null || key == null) return false;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                return prefs.contains(key);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking preference existence: " + key, e);
        }
        
        return false;
    }
    
    /**
     * Get all preferences
     */
    public static Map<String, ?> getAllPreferences(Context context) {
        return getAllPreferences(context, DEFAULT_PREFERENCES_NAME);
    }
    
    /**
     * Get all preferences with custom preferences name
     */
    public static Map<String, ?> getAllPreferences(Context context, String preferencesName) {
        if (context == null) return null;
        
        try {
            SharedPreferences prefs = getPreferences(context, preferencesName);
            if (prefs != null) {
                return prefs.getAll();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all preferences: " + preferencesName, e);
        }
        
        return null;
    }
    
    /**
     * Get preferences count
     */
    public static int getPreferencesCount(Context context) {
        return getPreferencesCount(context, DEFAULT_PREFERENCES_NAME);
    }
    
    /**
     * Get preferences count with custom preferences name
     */
    public static int getPreferencesCount(Context context, String preferencesName) {
        if (context == null) return 0;
        
        try {
            Map<String, ?> allPrefs = getAllPreferences(context, preferencesName);
            return allPrefs != null ? allPrefs.size() : 0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting preferences count: " + preferencesName, e);
        }
        
        return 0;
    }
} 