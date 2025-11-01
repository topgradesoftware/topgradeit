package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.R;

/**
 * Helper class to verify and test dynamic theme support
 * This class provides methods to test all three themes programmatically
 */
public class ThemeVerificationHelper {
    
    private static final String TAG = "ThemeVerificationHelper";
    
    /**
     * Test all themes and log the results
     */
    public static void testAllThemes(Context context) {
        Log.d(TAG, "=== STARTING THEME VERIFICATION TEST ===");
        
        // Test Student Theme
        testStudentTheme(context);
        
        // Test Staff Theme  
        testStaffTheme(context);
        
        // Test Parent Theme
        testParentTheme(context);
        
        Log.d(TAG, "=== THEME VERIFICATION TEST COMPLETED ===");
    }
    
    /**
     * Test Student Theme (Teal)
     */
    public static void testStudentTheme(Context context) {
        Log.d(TAG, "Testing Student Theme (Teal)");
        
        try {
            // Set user type
            Paper.book().write(Constants.User_Type, "STUDENT");
            
            // Verify color resources exist
            int studentPrimary = ContextCompat.getColor(context, R.color.student_primary);
            int expectedTeal = Color.parseColor("#004d40");
            
            if (studentPrimary == expectedTeal) {
                Log.d(TAG, "✅ Student theme color correct: #004d40");
            } else {
                Log.e(TAG, "❌ Student theme color incorrect. Expected: #004d40, Got: " + 
                    String.format("#%06X", (0xFFFFFF & studentPrimary)));
            }
            
            // Test drawable resources
            testDrawableResource(context, R.drawable.bg_wave_teal, "Student header wave");
            testDrawableResource(context, R.drawable.footer_background_teal, "Student footer");
            
            Toast.makeText(context, "Student Theme (Teal) - Color: #004d40", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error testing Student theme", e);
        }
    }
    
    /**
     * Test Staff Theme (Navy Blue)
     */
    public static void testStaffTheme(Context context) {
        Log.d(TAG, "Testing Staff Theme (Navy Blue)");
        
        try {
            // Set user type
            Paper.book().write(Constants.User_Type, "STAFF");
            
            // Verify color resources exist
            int navyBlue = ContextCompat.getColor(context, R.color.navy_blue);
            int expectedNavy = Color.parseColor("#000064");
            
            if (navyBlue == expectedNavy) {
                Log.d(TAG, "✅ Staff theme color correct: #000064");
            } else {
                Log.e(TAG, "❌ Staff theme color incorrect. Expected: #000064, Got: " + 
                    String.format("#%06X", (0xFFFFFF & navyBlue)));
            }
            
            // Test drawable resources
            testDrawableResource(context, R.drawable.bg_wave_navy_blue, "Staff header wave");
            testDrawableResource(context, R.drawable.footer_background_staff_navy, "Staff footer");
            
            Toast.makeText(context, "Staff Theme (Navy Blue) - Color: #000064", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error testing Staff theme", e);
        }
    }
    
    /**
     * Test Parent Theme (Brown)
     */
    public static void testParentTheme(Context context) {
        Log.d(TAG, "Testing Parent Theme (Brown)");
        
        try {
            // Set user type
            Paper.book().write(Constants.User_Type, "PARENT");
            
            // Verify color resources exist
            int parentPrimary = ContextCompat.getColor(context, R.color.parent_primary);
            int expectedBrown = Color.parseColor("#693e02");
            
            if (parentPrimary == expectedBrown) {
                Log.d(TAG, "✅ Parent theme color correct: #693e02");
            } else {
                Log.e(TAG, "❌ Parent theme color incorrect. Expected: #693e02, Got: " + 
                    String.format("#%06X", (0xFFFFFF & parentPrimary)));
            }
            
            // Test drawable resources
            testDrawableResource(context, R.drawable.bg_wave_dark_brown, "Parent header wave");
            testDrawableResource(context, R.drawable.footer_background_brown, "Parent footer");
            
            Toast.makeText(context, "Parent Theme (Brown) - Color: #693e02", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error testing Parent theme", e);
        }
    }
    
    /**
     * Test if a drawable resource exists
     */
    private static void testDrawableResource(Context context, int drawableRes, String resourceName) {
        try {
            context.getResources().getDrawable(drawableRes);
            Log.d(TAG, "✅ " + resourceName + " exists");
        } catch (Exception e) {
            Log.e(TAG, "❌ " + resourceName + " missing or invalid", e);
        }
    }
    
    /**
     * Verify current theme setup
     */
    public static void verifyCurrentTheme(Context context) {
        try {
            String userType = Paper.book().read(Constants.User_Type, "PARENT");
            Log.d(TAG, "Current user type: " + userType);
            
            switch (userType) {
                case "STUDENT":
                    Log.d(TAG, "Current theme: Student (Teal)");
                    break;
                case "STAFF":
                case "TEACHER":
                    Log.d(TAG, "Current theme: Staff (Navy Blue)");
                    break;
                case "PARENT":
                default:
                    Log.d(TAG, "Current theme: Parent (Brown)");
                    break;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error verifying current theme", e);
        }
    }
    
    /**
     * Quick test method - call this from any activity
     */
    public static void quickTest(Context context) {
        Log.d(TAG, "=== QUICK THEME TEST ===");
        
        // Test each theme quickly
        testStudentTheme(context);
        
        // Wait a bit, then test staff
        new android.os.Handler().postDelayed(() -> {
            testStaffTheme(context);
            
            // Wait a bit, then test parent
            new android.os.Handler().postDelayed(() -> {
                testParentTheme(context);
                
                // Final verification
                new android.os.Handler().postDelayed(() -> {
                    verifyCurrentTheme(context);
                    Toast.makeText(context, "Theme testing completed! Check logs for results.", Toast.LENGTH_LONG).show();
                }, 1000);
                
            }, 1000);
        }, 1000);
    }
}
