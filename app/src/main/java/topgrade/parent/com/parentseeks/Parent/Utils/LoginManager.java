package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.util.Log;

import io.paperdb.Paper;

/**
 * Utility class to manage login state and provide methods to clear login data
 */
public class LoginManager {
    
    /**
     * Clear all stored login data
     */
    public static void clearLoginData(Context context) {
        try {
            Paper.init(context);
            Paper.book().delete(Constants.is_login);
            Paper.book().delete(Constants.User_Type);
            Paper.book().delete("parent_id");
            Paper.book().delete("campus_id");
            Paper.book().delete("students");
            Paper.book().delete("campus_name");
            Log.d("LoginManager", "Cleared all stored login data");
        } catch (Exception e) {
            Log.e("LoginManager", "Error clearing login data", e);
        }
    }
    
    /**
     * Check if user is logged in
     */
    public static boolean isLoggedIn(Context context) {
        try {
            Paper.init(context);
            boolean isLoggedIn = Paper.book().read(Constants.is_login, false);
            String userType = Paper.book().read(Constants.User_Type, "");
            return isLoggedIn && !userType.isEmpty();
        } catch (Exception e) {
            Log.e("LoginManager", "Error checking login status", e);
            return false;
        }
    }
    
    /**
     * Get current user type
     */
    public static String getUserType(Context context) {
        try {
            Paper.init(context);
            return Paper.book().read(Constants.User_Type, "");
        } catch (Exception e) {
            Log.e("LoginManager", "Error getting user type", e);
            return "";
        }
    }
    
    /**
     * Logout user (clear login data)
     */
    public static void logout(Context context) {
        clearLoginData(context);
        Log.d("LoginManager", "User logged out");
    }
}
