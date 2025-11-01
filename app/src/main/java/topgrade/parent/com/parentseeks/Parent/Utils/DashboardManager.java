package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Model.DashboardConfig;
import topgrade.parent.com.parentseeks.Parent.Model.DashboardMenuItem;
import topgrade.parent.com.parentseeks.Parent.Model.UserPreferences;
import topgrade.parent.com.parentseeks.R;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class DashboardManager {
    private static final String TAG = "DashboardManager";
    private static final String PREF_DASHBOARD_CONFIG = "dashboard_config";
    private static final String PREF_USER_PREFERENCES = "user_preferences";
    
    private final Context context;
    private DashboardConfig config;
    private UserPreferences userPreferences;
    private List<DashboardMenuItem> allMenuItems;
    private List<DashboardMenuItem> filteredMenuItems;
    
    public DashboardManager(Context context) {
        this.context = context;
        loadConfiguration();
        loadUserPreferences();
        initializeDefaultMenuItems();
    }
    
    private void loadConfiguration() {
        try {
            String configJson = Paper.book().read(PREF_DASHBOARD_CONFIG, "");
            if (configJson != null && !configJson.isEmpty()) {
                config = new Gson().fromJson(configJson, DashboardConfig.class);
            } else {
                config = new DashboardConfig();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading dashboard config", e);
            config = new DashboardConfig();
        }
    }
    
    private void loadUserPreferences() {
        try {
            String prefsJson = Paper.book().read(PREF_USER_PREFERENCES, "");
            if (prefsJson != null && !prefsJson.isEmpty()) {
                userPreferences = new Gson().fromJson(prefsJson, UserPreferences.class);
            } else {
                userPreferences = new UserPreferences();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user preferences", e);
            userPreferences = new UserPreferences();
        }
    }
    
    private void initializeDefaultMenuItems() {
        allMenuItems = new ArrayList<>();
        
        // Staff Profile
        allMenuItems.add(new DashboardMenuItem("staff_profile", "Staff Profile", 
            R.drawable.man, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffProfile",
            new String[]{"view_profile"}, new String[]{"teacher", "admin"}, true, true, 1, "profile"));
        
        // Salary
        allMenuItems.add(new DashboardMenuItem("salary", "Salary", 
            R.drawable.salary, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffSalary",
            new String[]{"view_salary"}, new String[]{"teacher", "admin"}, true, true, 2, "finance"));
        
        // Invoice
        allMenuItems.add(new DashboardMenuItem("invoice", "Invoice", 
            R.drawable.invoice, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffInvoice",
            new String[]{"view_invoice"}, new String[]{"teacher", "admin"}, true, true, 4, "finance"));
        
        // Ledger
        allMenuItems.add(new DashboardMenuItem("ledger", "Ledger", 
            R.drawable.ledger, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffLedger",
            new String[]{"view_ledger"}, new String[]{"teacher", "admin"}, true, true, 5, "finance"));
        
        // TimeTable
        allMenuItems.add(new DashboardMenuItem("timetable", "View TimeTable", 
            R.drawable.timetablee, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffTimeTable",
            new String[]{"view_timetable"}, new String[]{"teacher", "admin"}, true, true, 6, "academic"));
        
        // Assign Task
        allMenuItems.add(new DashboardMenuItem("assign_task", "View Assign Task", 
            R.drawable.assign_task, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffTaskMenu",
            new String[]{"view_tasks"}, new String[]{"teacher", "admin"}, true, true, 7, "academic"));
        
        // Complain Box
        allMenuItems.add(new DashboardMenuItem("complain_box", "Complain Box", 
                            R.drawable.ic_complaints, "topgrade.parent.com.parentseeks.Teacher.Activity.Complaint.StaffSubmitComplaint",
            new String[]{"submit_complaint"}, new String[]{"teacher", "admin"}, true, true, 8, "communication"));
        
        // Leave Application
        allMenuItems.add(new DashboardMenuItem("leave_application", "Leave Application", 
            R.drawable.leave_application, "topgrade.parent.com.parentseeks.Teacher.Activity.Application.StaffApplicationMenu",
            new String[]{"submit_leave"}, new String[]{"teacher", "admin"}, true, true, 9, "communication"));
        
        // Attendance (for admin/coordinator roles)
        allMenuItems.add(new DashboardMenuItem("attendance", "Attendance", 
                            R.drawable.attendence, "topgrade.parent.com.parentseeks.Teacher.Activity.Attendance.StaffAttendanceMenu",
            new String[]{"manage_attendance"}, new String[]{"admin", "coordinator"}, true, true, 10, "academic"));
        
        // Student List (for admin/coordinator roles)
        allMenuItems.add(new DashboardMenuItem("student_list", "Student List", 
            R.drawable.children, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffStudentList",
            new String[]{"view_students"}, new String[]{"admin", "coordinator"}, true, true, 11, "academic"));
        
        // Exam (for admin/coordinator roles)
        allMenuItems.add(new DashboardMenuItem("exam", "Exam", 
            R.drawable.exam, "topgrade.parent.com.parentseeks.Teacher.Exam.ExamManagementDashboard",
            new String[]{"manage_exams"}, new String[]{"admin", "coordinator"}, true, true, 12, "academic"));
        
        // Feedback
        allMenuItems.add(new DashboardMenuItem("feedback", "Feedback", 
            R.drawable.feedback, "topgrade.parent.com.parentseeks.Teacher.Activity.FeedbackMenu",
            new String[]{"view_feedback"}, new String[]{"teacher", "admin"}, true, true, 13, "communication"));
        
        // Progress Report
        allMenuItems.add(new DashboardMenuItem("progress_report", "Progress Report", 
                            R.drawable.clipboard, "topgrade.parent.com.parentseeks.Teacher.Activites.Activity.StaffProgress",
            new String[]{"view_progress"}, new String[]{"teacher", "admin"}, true, true, 14, "academic"));
        
        // Send Diary
        allMenuItems.add(new DashboardMenuItem("send_diary", "Send Diary", 
            R.drawable.feedback, "topgrade.parent.com.parentseeks.Teacher.Activity.AddDiaryActivity",
            new String[]{"send_diary"}, new String[]{"teacher", "admin"}, true, true, 15, "communication"));
        
        // View Diary
        allMenuItems.add(new DashboardMenuItem("view_diary", "View Diary", 
            R.drawable.feedback, "topgrade.parent.com.parentseeks.Teacher.Activity.ViewDiaryActivity",
            new String[]{"view_diary"}, new String[]{"teacher", "admin"}, true, true, 16, "communication"));
        
        // Announcements (Combined News & Events)
        allMenuItems.add(new DashboardMenuItem("announcements", "Announcements", 
            R.drawable.news, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffAnnouncements",
            new String[]{"view_news", "view_events"}, new String[]{"teacher", "admin"}, true, true, 17, "communication"));
        
        filterMenuItems();
    }
    
    public void filterMenuItems() {
        filteredMenuItems = new ArrayList<>();
        String userRole = getUserRole();
        List<String> userPermissions = getUserPermissions();
        
        for (DashboardMenuItem item : allMenuItems) {
            if (item.isVisible() && item.isEnabled()) {
                // Check role-based access
                if (item.hasRole(userRole)) {
                    // Check permissions
                    boolean hasPermission = true;
                    if (item.getPermissions() != null) {
                        hasPermission = false;
                        for (String permission : item.getPermissions()) {
                            if (userPermissions.contains(permission)) {
                                hasPermission = true;
                                break;
                            }
                        }
                    }
                    
                    if (hasPermission) {
                        // Check if item is hidden by user
                        if (!userPreferences.isHidden(item.getId())) {
                            filteredMenuItems.add(item);
                        }
                    }
                }
            }
        }
        
        // Sort by order and user preferences
        sortMenuItems();
    }
    
    private void sortMenuItems() {
        filteredMenuItems.sort((item1, item2) -> {
                // First, check if items are in user's custom order
                List<String> customOrder = userPreferences.getCustomOrder();
                if (customOrder != null) {
                    int index1 = customOrder.indexOf(item1.getId());
                    int index2 = customOrder.indexOf(item2.getId());
                    
                    if (index1 != -1 && index2 != -1) {
                        return Integer.compare(index1, index2);
                    } else if (index1 != -1) {
                        return -1; // item1 comes first
                    } else if (index2 != -1) {
                        return 1; // item2 comes first
                    }
                }
                
                // Then sort by default order
                return Integer.compare(item1.getOrder(), item2.getOrder());
        });
    }
    
    private String getUserRole() {
        // Get user role from Paper or API
        return Paper.book().read("user_role", "teacher");
    }
    
    private List<String> getUserPermissions() {
        // Get user permissions from Paper or API
        String permissionsJson = Paper.book().read("user_permissions", "[]");
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            return new Gson().fromJson(permissionsJson, listType);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public List<DashboardMenuItem> getMenuItems() {
        return filteredMenuItems;
    }
    
    public List<DashboardMenuItem> getFavoriteItems() {
        List<DashboardMenuItem> favorites = new ArrayList<>();
        for (DashboardMenuItem item : filteredMenuItems) {
            if (userPreferences.isFavorite(item.getId())) {
                favorites.add(item);
            }
        }
        return favorites;
    }
    
    public void toggleFavorite(String itemId) {
        if (userPreferences.isFavorite(itemId)) {
            userPreferences.removeFavorite(itemId);
        } else {
            userPreferences.addFavorite(itemId);
        }
        saveUserPreferences();
    }
    
    public void hideItem(String itemId) {
        userPreferences.hideItem(itemId);
        saveUserPreferences();
        filterMenuItems();
    }
    
    public void showItem(String itemId) {
        userPreferences.showItem(itemId);
        saveUserPreferences();
        filterMenuItems();
    }
    
    public void updateBadge(String itemId, int count) {
        for (DashboardMenuItem item : allMenuItems) {
            if (item.getId().equals(itemId)) {
                item.setBadgeCount(count);
                break;
            }
        }
    }
    
    public void updateBadgeText(String itemId, String text) {
        for (DashboardMenuItem item : allMenuItems) {
            if (item.getId().equals(itemId)) {
                item.setBadgeText(text);
                break;
            }
        }
    }
    
    public DashboardConfig getConfig() {
        return config;
    }
    
    public UserPreferences getUserPreferences() {
        return userPreferences;
    }
    
    public void saveUserPreferences() {
        try {
            String prefsJson = new Gson().toJson(userPreferences);
            Paper.book().write(PREF_USER_PREFERENCES, prefsJson);
        } catch (Exception e) {
            Log.e(TAG, "Error saving user preferences", e);
        }
    }
    
    public void saveConfiguration() {
        try {
            String configJson = new Gson().toJson(config);
            Paper.book().write(PREF_DASHBOARD_CONFIG, configJson);
        } catch (Exception e) {
            Log.e(TAG, "Error saving dashboard config", e);
        }
    }
    
    public void refreshFromServer() {
        // TODO: Implement server refresh logic
        // This would fetch updated menu items, permissions, and configuration from the server
    }
} 