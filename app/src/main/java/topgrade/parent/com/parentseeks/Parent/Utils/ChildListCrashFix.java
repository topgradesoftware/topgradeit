package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to fix child list crashes in parent activities
 * Provides safe data loading methods and proper null checks
 */
public class ChildListCrashFix {
    
    private static final String TAG = "ChildListCrashFix";
    
    /**
     * Safely load student list from Paper DB with comprehensive error handling
     */
    public static List<SharedStudent> safeLoadStudentList(Context context) {
        try {
            Log.d(TAG, "Attempting to load student list from Paper DB");
            
            // Initialize Paper if not already done
            try {
                Paper.init(context);
            } catch (Exception e) {
                // Paper might already be initialized, ignore error
                Log.d(TAG, "Paper initialization: " + e.getMessage());
            }
            
            // Try to read student list with fallback
            List<SharedStudent> studentList = null;
            try {
                studentList = Paper.book().read("students");
                Log.d(TAG, "Student list loaded from Paper DB: " + (studentList != null ? studentList.size() : "null") + " students");
            } catch (Exception e) {
                Log.e(TAG, "Error reading students from Paper DB, clearing corrupted data", e);
                // Clear corrupted data
                Paper.book().delete("students");
                studentList = null;
            }
            
            // Validate student list
            if (studentList == null) {
                Log.w(TAG, "Student list is null, creating empty list");
                studentList = new ArrayList<>();
            }
            
            // Validate individual student objects
            List<SharedStudent> validStudents = new ArrayList<>();
            for (int i = 0; i < studentList.size(); i++) {
                SharedStudent student = studentList.get(i);
                if (student != null && isValidStudent(student)) {
                    validStudents.add(student);
                    Log.d(TAG, "Valid student found: " + student.getFullName());
                } else {
                    Log.w(TAG, "Invalid student at position " + i + ", skipping");
                }
            }
            
            // If no valid students, try alternative data sources
            if (validStudents.isEmpty()) {
                Log.w(TAG, "No valid students found, trying alternative sources");
                validStudents = tryAlternativeStudentSources(context);
            }
            
            // Save validated list back to Paper DB
            if (!validStudents.isEmpty()) {
                try {
                    Paper.book().write("students", validStudents);
                    Log.d(TAG, "Validated student list saved back to Paper DB: " + validStudents.size() + " students");
                } catch (Exception e) {
                    Log.e(TAG, "Error saving validated student list", e);
                }
            }
            
            Log.d(TAG, "Safe student list loading completed: " + validStudents.size() + " valid students");
            return validStudents;
            
        } catch (Exception e) {
            Log.e(TAG, "Critical error in safeLoadStudentList", e);
            // Return empty list as last resort
            return new ArrayList<>();
        }
    }
    
    /**
     * Validate if a student object is valid and complete
     */
    private static boolean isValidStudent(SharedStudent student) {
        if (student == null) {
            return false;
        }
        
        // Check essential fields
        if (student.getUniqueId() == null || student.getUniqueId().isEmpty()) {
            Log.w(TAG, "Student has null/empty unique ID");
            return false;
        }
        
        if (student.getFullName() == null || student.getFullName().isEmpty()) {
            Log.w(TAG, "Student has null/empty full name");
            return false;
        }
        
        // Additional validation can be added here
        return true;
    }
    
    /**
     * Try alternative sources for student data
     */
    private static List<SharedStudent> tryAlternativeStudentSources(Context context) {
        List<SharedStudent> alternativeStudents = new ArrayList<>();
        
        try {
            // Try reading from alternative keys
            String[] alternativeKeys = {
                "student_list", "children", "child_list", "parent_students"
            };
            
            for (String key : alternativeKeys) {
                try {
                    Object data = Paper.book().read(key);
                    if (data instanceof List) {
                        List<?> list = (List<?>) data;
                        for (Object item : list) {
                            if (item instanceof SharedStudent) {
                                SharedStudent student = (SharedStudent) item;
                                if (isValidStudent(student)) {
                                    alternativeStudents.add(student);
                                    Log.d(TAG, "Found student in alternative key '" + key + "': " + student.getFullName());
                                }
                            }
                        }
                        if (!alternativeStudents.isEmpty()) {
                            Log.d(TAG, "Successfully loaded " + alternativeStudents.size() + " students from alternative key: " + key);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Alternative key '" + key + "' failed: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error trying alternative student sources", e);
        }
        
        return alternativeStudents;
    }
    
    /**
     * Safely get student names for UI display
     */
    public static List<String> safeGetStudentNames(List<SharedStudent> studentList) {
        List<String> studentNames = new ArrayList<>();
        
        try {
            if (studentList != null && !studentList.isEmpty()) {
                for (SharedStudent student : studentList) {
                    if (student != null && student.getFullName() != null && !student.getFullName().isEmpty()) {
                        studentNames.add(student.getFullName());
                    } else {
                        // Add fallback name for invalid students
                        studentNames.add("Student " + (studentNames.size() + 1));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting student names", e);
            // Add fallback names
            studentNames.add("Student 1");
            studentNames.add("Student 2");
        }
        
        // Ensure we always have at least one name
        if (studentNames.isEmpty()) {
            studentNames.add("No Students Available");
        }
        
        return studentNames;
    }
    
    /**
     * Check if student list is safe to use
     */
    public static boolean isStudentListSafe(List<SharedStudent> studentList) {
        return studentList != null && !studentList.isEmpty();
    }
    
    /**
     * Get safe student at position with bounds checking
     */
    public static SharedStudent getSafeStudentAt(List<SharedStudent> studentList, int position) {
        try {
            if (isStudentListSafe(studentList) && position >= 0 && position < studentList.size()) {
                SharedStudent student = studentList.get(position);
                if (isValidStudent(student)) {
                    return student;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting student at position " + position, e);
        }
        return null;
    }
    
    /**
     * Show appropriate message based on student list state
     */
    public static void showStudentListStatus(Context context, List<SharedStudent> studentList) {
        try {
            if (!isStudentListSafe(studentList)) {
                Toast.makeText(context, "No students available. Please check your account.", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Student list is safe with " + studentList.size() + " students");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing student list status", e);
            Toast.makeText(context, "Error loading student information.", Toast.LENGTH_SHORT).show();
        }
    }
}
