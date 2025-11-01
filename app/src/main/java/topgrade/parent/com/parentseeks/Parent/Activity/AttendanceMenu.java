package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.R;

public class AttendanceMenu extends AppCompatActivity {
    
    private static final String TAG = "AttendanceMenu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AttendanceMenu onCreate - Loading layout");
        
        // Check user type and load appropriate layout
        String userType = Paper.book().read("User_Type", "");
        if ("STUDENT".equalsIgnoreCase(userType)) {
            setContentView(R.layout.activity_student_attendance_menu);
            Log.d(TAG, "AttendanceMenu onCreate - Student layout loaded");
            // Apply student theme
            ThemeHelper.applyStudentTheme(this);
            Toast.makeText(this, "STUDENT Attendance Menu Loaded", Toast.LENGTH_LONG).show();
        } else {
            setContentView(R.layout.activity_view_attendance_parent);
            Log.d(TAG, "AttendanceMenu onCreate - Parent layout loaded");
            // Apply unified parent theme
            ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
            ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for attendance menu
            ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for attendance menu
            ParentThemeHelper.setFooterVisibility(this, true); // Show footer
            ParentThemeHelper.setHeaderTitle(this, "Attendance Menu");
            Toast.makeText(this, "PARENT Attendance Menu Loaded", Toast.LENGTH_LONG).show();
        }
        
        Log.d(TAG, "AttendanceMenu onCreate - Layout loaded successfully");
    }

    public void onBackPressed(View view) {
        Log.d(TAG, "Back button clicked");
        finish();
    }

    public void View_Attendance_Class(View view) {
        Log.d(TAG, "View_Attendance_Class clicked");
        try {
            Toast.makeText(this, "Opening Class Attendance...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AttendanceMenu.this, AttendanceClassWise.class));
        } catch (Exception e) {
            Log.e(TAG, "Error opening Class Attendance", e);
            Toast.makeText(this, "Error opening Class Attendance", Toast.LENGTH_SHORT).show();
        }
    }


    public void View_Attendance_Subject_Improved(View view) {
        Log.d(TAG, "View_Attendance_Subject_Improved clicked");
        try {
            Toast.makeText(this, "Opening Improved Subject Attendance...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AttendanceMenu.this, topgrade.parent.com.parentseeks.Parent.Activity.AttendanceSubjectWiseImproved.class));
        } catch (Exception e) {
            Log.e(TAG, "Error opening Improved Subject Attendance", e);
            Toast.makeText(this, "Error opening Improved Subject Attendance", Toast.LENGTH_SHORT).show();
        }
    }

    // Student-specific attendance methods (matching parent layout structure exactly)
    public void StudentClass(View view) {
        Log.d(TAG, "StudentClass clicked");
        try {
            Toast.makeText(this, "Opening Student Class Attendance...", Toast.LENGTH_SHORT).show();
            // Use the same AttendanceClassWise activity as parent - it will work for students too
            Intent intent = new Intent(AttendanceMenu.this, AttendanceClassWise.class);
            // Pass student context flag if needed
            intent.putExtra("USER_TYPE", "STUDENT");
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening Student Class Attendance", e);
            Toast.makeText(this, "Error opening Student Class Attendance", Toast.LENGTH_SHORT).show();
        }
    }

    public void StudentSection(View view) {
        Log.d(TAG, "StudentSection clicked");
        try {
            Toast.makeText(this, "Opening Student Subject Attendance...", Toast.LENGTH_SHORT).show();
            // Use the same AttendanceSubjectWiseImproved activity as parent - it will work for students too
            Intent intent = new Intent(AttendanceMenu.this, topgrade.parent.com.parentseeks.Parent.Activity.AttendanceSubjectWiseImproved.class);
            // Pass student context flag if needed
            intent.putExtra("USER_TYPE", "STUDENT");
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening Student Subject Attendance", e);
            Toast.makeText(this, "Error opening Student Subject Attendance", Toast.LENGTH_SHORT).show();
        }
    }


}
