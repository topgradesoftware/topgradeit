package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.install.model.ActivityResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import topgrade.parent.com.parentseeks.Parent.Model.ExamSession;
import topgrade.parent.com.parentseeks.Parent.Model.SessionModel;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Parent.Model.Subject;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.MemoryLeakDetector;
import topgrade.parent.com.parentseeks.Parent.Utils.AutoMigrationHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.PerformanceOptimizer;
import topgrade.parent.com.parentseeks.Parent.Utils.PerformanceMonitor;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffMainDashboard;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.Student.Activity.StudentMainDashboard;


public class Splash extends AppCompatActivity {
    Context context;
    String parent_id;
    String campus_id;
    
    // App Update Manager
    private AppUpdateManager appUpdateManager;
    private static final int APP_UPDATE_REQUEST_CODE = 500;
    List<SharedStudent> studentList = new ArrayList<>();
    String child_id = "";
    
    // Add lifecycle management variables
    private boolean isActivityDestroyed = false;
    private boolean isActivityPaused = false;
    private RequestQueue requestQueue;
    private StringRequest currentRequest;
    private android.os.Handler handler;
    private Runnable delayedRunnable;
    private Runnable timeoutRunnable;
    
    // Add executor for background operations
    private ExecutorService executor;
    private boolean isNavigationInProgress = false;
    
    // Add timeout for splash screen
    private static final int SPLASH_TIMEOUT = 5000; // Reduced to 5 seconds for faster app loading

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set basic window background to prevent black flash
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        
        // Apply anti-flickering flags before setContentView
        ActivityTransitionHelper.applyAntiFlickeringFlags(this);
        
        setContentView(R.layout.activity_splash);
        
        // Apply theme based on user type (if logged in)
        applyThemeBasedOnUserType();
        
        // System bar appearance is handled by SplashTheme in styles.xml
        
        // Register activity for memory leak detection
        MemoryLeakDetector.registerActivity(this);
        
        try {
            context = Splash.this;
            Paper.init(context);
            
            // Load constants from Paper DB to ensure they're available
            topgrade.parent.com.parentseeks.Teacher.Utils.Constant.loadFromPaper();
            Log.d("Splash", "Constants loaded from Paper DB");
            
            // Initialize performance optimizations (lightweight version)
            PerformanceOptimizer.initialize(context);
            
            // DISABLED: Performance monitoring to prevent high CPU usage
            // PerformanceMonitor.startMonitoring(context);
            
            // Initialize executor for background operations
            executor = Executors.newCachedThreadPool();
            
            // Initialize handler for delayed operations
            handler = new android.os.Handler(android.os.Looper.getMainLooper());
            
            // Initialize App Update Manager
            appUpdateManager = AppUpdateManagerFactory.create(this);
            
            // Set up timeout to prevent infinite hanging
            setupTimeout();
            
            // Check if user is logged in first - moved to background thread
            loadLoginDataInBackground();
            
            // Defer app update checks to avoid blocking startup
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isActivityDestroyed) {
                        checkForAppUpdates();
                    }
                }
            }, 2000); // Check for updates after 2 seconds
            
            // Initialize request queue with optimized settings
            requestQueue = Volley.newRequestQueue(context);
            
            // Start auto migration in background (non-blocking)
            startBackgroundMigration();
            
            // Add a minimal delay to ensure smooth rendering
            delayedRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!isActivityDestroyed && !isNavigationInProgress) {
                        load_exam_session(campus_id);
                    }
                }
            };
            handler.postDelayed(delayedRunnable, 10); // Minimal delay for faster loading
            
        } catch (Exception e) {
            Log.e("Splash", "Error in onCreate", e);
            if (!isActivityDestroyed) {
                Toast.makeText(this, "Error initializing splash", Toast.LENGTH_SHORT).show();
                navigateToRoleSelection();
            }
        }
    }

    /**
     * Set up timeout to prevent infinite hanging
     */
    private void setupTimeout() {
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isActivityDestroyed && !isNavigationInProgress) {
                    Log.w("Splash", "Splash timeout reached, forcing navigation");
                    forceNavigation();
                }
            }
        };
        handler.postDelayed(timeoutRunnable, SPLASH_TIMEOUT);
    }

    /**
     * Force navigation when timeout is reached
     * Reduced timeout to 5 seconds for faster app loading
     */
    private void forceNavigation() {
        try {
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("Splash", "Force navigation for user type: " + userType);
            
            isNavigationInProgress = true;
            
            if ("Teacher".equals(userType)) {
                ActivityTransitionHelper.startActivityAndFinishSmooth(
                    this, 
                    new Intent(Splash.this, StaffMainDashboard.class)
                );
            } else if ("STUDENT".equals(userType)) {
                ActivityTransitionHelper.startActivityAndFinishSmooth(
                    this, 
                    new Intent(Splash.this, StudentMainDashboard.class)
                );
            } else {
                ActivityTransitionHelper.startActivityAndFinishSmooth(
                    this, 
                    new Intent(Splash.this, ParentMainDashboard.class)
                );
            }
        } catch (Exception e) {
            Log.e("Splash", "Error in force navigation", e);
            isNavigationInProgress = false;
            navigateToRoleSelection();
        }
    }

    /**
     * Start background migration without blocking the UI
     */
    private void startBackgroundMigration() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Splash", "Starting background migration...");
                    AutoMigrationHelper.startAutoMigration(context);
                } catch (Exception e) {
                    Log.e("Splash", "Background migration error", e);
                }
            }
        });
    }

    /**
     * Check for app updates using Google Play In-App Updates
     */
    private void checkForAppUpdates() {
        try {
            if (appUpdateManager != null) {
                Log.d("Splash", "Checking for app updates...");
                
                appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                    Log.d("Splash", "Update availability: " + appUpdateInfo.updateAvailability());
                    Log.d("Splash", "Available version code: " + appUpdateInfo.availableVersionCode());
                    
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        // Check if update is mandatory (stale for more than 7 days)
                        Integer stalenessDays = appUpdateInfo.clientVersionStalenessDays();
                        boolean isMandatory = stalenessDays != null && stalenessDays >= 7;
                        
                        if (isMandatory) {
                            Log.d("Splash", "Mandatory update available - starting immediate update");
                            startImmediateUpdate(appUpdateInfo);
                        } else {
                            Log.d("Splash", "Optional update available - starting flexible update");
                            startFlexibleUpdate(appUpdateInfo);
                        }
                    } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        Log.d("Splash", "Developer triggered update in progress");
                        // Resume the update
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                APP_UPDATE_REQUEST_CODE
                            );
                        } catch (Exception e) {
                            Log.e("Splash", "Error starting developer triggered update", e);
                        }
                    } else {
                        Log.d("Splash", "No updates available");
                    }
                }).addOnFailureListener(exception -> {
                    Log.e("Splash", "Failed to check for updates", exception);
                });
            }
        } catch (Exception e) {
            Log.e("Splash", "Error checking for app updates", e);
        }
    }

    private void startImmediateUpdate(com.google.android.play.core.appupdate.AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                this,
                APP_UPDATE_REQUEST_CODE
            );
        } catch (Exception e) {
            Log.e("Splash", "Error starting immediate update", e);
        }
    }

    private void startFlexibleUpdate(com.google.android.play.core.appupdate.AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                this,
                APP_UPDATE_REQUEST_CODE
            );
        } catch (Exception e) {
            Log.e("Splash", "Error starting flexible update", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityPaused = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Log.d("Splash", "App update completed successfully");
                    Toast.makeText(this, "App updated successfully!", Toast.LENGTH_SHORT).show();
                    break;
                case RESULT_CANCELED:
                    Log.d("Splash", "App update was cancelled");
                    Toast.makeText(this, "Update cancelled", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.d("Splash", "App update failed with result code: " + resultCode);
                    Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;
        
        // Unregister activity from memory leak detection
        MemoryLeakDetector.unregisterActivity(this);
        
        // Cancel any ongoing network requests
        if (currentRequest != null) {
            currentRequest.cancel();
            currentRequest = null;
        }
        
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
        
        // Remove delayed callbacks to prevent memory leaks
        if (handler != null) {
            if (delayedRunnable != null) {
                handler.removeCallbacks(delayedRunnable);
            }
            if (timeoutRunnable != null) {
                handler.removeCallbacks(timeoutRunnable);
            }
        }
        
        // Shutdown executor
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        
        // Clear context reference
        context = null;
        
        // Check for memory leaks before destroying
        MemoryLeakDetector.checkMemoryLeaks(this);
    }

    /**
     * Check if the activity is still valid for UI operations
     */
    private boolean isActivityValid() {
        return !isActivityDestroyed && !isFinishing() && !isActivityPaused && context != null && !isNavigationInProgress;
    }
    
    /**
     * Check if navigation is already in progress to prevent loops
     */
    private boolean canNavigate() {
        return !isNavigationInProgress && !isActivityDestroyed && !isFinishing();
    }

    /**
     * Start main activity with smooth transition
     */
    private void startMainActivity() {
        if (!canNavigate()) return;
        
        isNavigationInProgress = true;
        
        try {
            Log.d("Splash", "startMainActivity() - Navigating to DashBoard");
            ActivityTransitionHelper.startActivityAndFinishSmooth(
                this, 
                new Intent(Splash.this, ParentMainDashboard.class)
            );
        } catch (Exception e) {
            Log.e("Splash", "Error starting main activity", e);
            isNavigationInProgress = false;
            finish();
        }
    }

    /**
     * Navigate to role selection screen
     */
    private void navigateToRoleSelection() {
        if (!canNavigate()) return;
        
        isNavigationInProgress = true;
        
        try {
            Log.d("Splash", "Navigating to SelectRole");
            ActivityTransitionHelper.startActivityAndFinishSmooth(
                this, 
                new Intent(Splash.this, SelectRole.class)
            );
        } catch (Exception e) {
            Log.e("Splash", "Error navigating to role selection", e);
            isNavigationInProgress = false;
            finish();
        }
    }

    private void load_child(final String parent_parent_id, final String campus_id) {
        if (!isActivityValid()) return;
        
        Log.d("Splash", "=== LOAD_CHILD DEBUG ===");
        Log.d("Splash", "load_child called with parent_parent_id: " + parent_parent_id);
        Log.d("Splash", "load_child called with campus_id: " + campus_id);
        Log.d("Splash", "Member variable parent_id: " + this.parent_id);
        Log.d("Splash", "Member variable campus_id: " + this.campus_id);
        
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                API.load_profile, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!isActivityValid()) return;
                
                Log.d("Splash", "Child data response received");
                try {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    JSONObject respone = new JSONObject(response);
                    JSONObject status = respone.getJSONObject("status");
                    if (status.getString("code").equals("1000")) {
                        JSONArray students_array = respone.getJSONArray("students");
                        if (students_array.length() > 0) {
                            studentList = Arrays.asList(gson.fromJson(students_array.toString(), SharedStudent[].class));
                            child_id = studentList.get(0).getUniqueId();
                            Paper.book().write("students", studentList);

                            // Save Child Subject in background
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for (SharedStudent student : studentList) {
                                            if (student.getSubjects().size() > 0) {
                                                // Convert List into Array
                                                ArrayList<Subject> list = new ArrayList<>();
                                                list.addAll(student.getSubjects());
                                                Paper.book().write(student.getUniqueId(), list);
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e("Splash", "Error saving child subjects", e);
                                    }
                                }
                            });

                            JSONObject campus = respone.getJSONObject("campus");
                            String campus_name = campus.getString("full_name");
                            Paper.book().write("campus_name", campus_name);
                            
                            if (isActivityValid()) {
                                String User_Type = Paper.book().read(Constants.User_Type);
                                if (User_Type.equals("STUDENT")) {
                                    Log.d("Splash", "Loading child data successful, navigating to StudentMainDashboard");
                                    ActivityTransitionHelper.startActivityAndFinishSmooth(
                                        Splash.this, 
                                        new Intent(Splash.this, StudentMainDashboard.class)
                                    );
                                } else {
                                    Log.d("Splash", "Loading child data successful, navigating to ParentMainDashboard");
                                    ActivityTransitionHelper.startActivityAndFinishSmooth(
                                        Splash.this, 
                                        new Intent(Splash.this, ParentMainDashboard.class)
                                    );
                                }
                            }
                        } else {
                            Log.w("Splash", "No students found, navigating to appropriate dashboard");
                            navigateToAppropriateDashboard();
                        }
                    } else {
                        String message = status.getString("message");
                        Log.w("Splash", "API error: " + message);
                        if (isActivityValid()) {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                        navigateToAppropriateDashboard();
                    }
                } catch (JSONException e1) {
                    Log.e("Splash", "JSON parsing error", e1);
                    navigateToAppropriateDashboard();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isActivityValid()) {
                    Log.w("Splash", "Error loading child data: " + error.getMessage());
                    Paper.book().write("students", new ArrayList<>());
                    Paper.book().write("campus_name", "");
                    navigateToAppropriateDashboard();
                }
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                HashMap<String, String> postParam = new HashMap<String, String>();
                postParam.put("parent_parent_id", parent_parent_id);
                postParam.put("parent_id", campus_id);
                return new JSONObject(postParam).toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header_parameter = new HashMap<String, String>();
                header_parameter.put("Content-Type", "application/json");
                return header_parameter;
            }
        };

        // Set the current request for cancellation
        currentRequest = jsonObjectRequest;
        jsonObjectRequest.setTag(this);

        // Optimized retry policy with increased timeout for better data loading
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000, // 20 seconds timeout - increased for better data loading
                2, // 2 retries for better reliability
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Navigate to appropriate dashboard based on user type
     */
    private void navigateToAppropriateDashboard() {
        if (!isActivityValid()) return;
        
        try {
            String User_Type = Paper.book().read(Constants.User_Type, "");
            Log.d("Splash", "Navigating to appropriate dashboard for user type: " + User_Type);
            
            isNavigationInProgress = true;
            
            if ("Teacher".equals(User_Type)) {
                Log.d("Splash", "Teacher detected in fallback, navigating to StaffMainDashboard");
                ActivityTransitionHelper.startActivityAndFinishSmooth(
                    Splash.this, 
                    new Intent(Splash.this, StaffMainDashboard.class)
                );
            } else if ("STUDENT".equals(User_Type)) {
                Log.d("Splash", "SharedStudent detected in fallback, navigating to StudentMainDashboard");
                ActivityTransitionHelper.startActivityAndFinishSmooth(
                    Splash.this, 
                    new Intent(Splash.this, StudentMainDashboard.class)
                );
            } else {
                Log.d("Splash", "Parent detected in fallback, navigating to ParentMainDashboard");
                ActivityTransitionHelper.startActivityAndFinishSmooth(
                    Splash.this, 
                    new Intent(Splash.this, ParentMainDashboard.class)
                );
            }
        } catch (Exception e) {
            Log.e("Splash", "Error navigating to appropriate dashboard", e);
            isNavigationInProgress = false;
            navigateToRoleSelection();
        }
    }

    private void load_exam_session(final String campus_id) {
        if (!isActivityValid()) return;

        try {
            // Check if this is a staff user
            String userType = Paper.book().read(Constants.User_Type, "");
            
            if ("Teacher".equals(userType)) {
                // For staff users, skip exam session loading and go directly to dashboard
                Log.d("Splash", "Staff user detected, skipping exam session loading");
                loadUserTypeInBackground();
                return;
            }
            
            // For parent/student users, load exam session with timeout
            HashMap<String, String> postParam = new HashMap<String, String>();
            postParam.put("parent_id", campus_id);
            postParam.put("staff_id", Constant.staff_id);
            postParam.put("session_id", Constant.current_session);
            RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

            // Set up timeout for exam session request
            final android.os.Handler timeoutHandler = new android.os.Handler();
            final Runnable timeoutRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isActivityValid()) {
                        Log.w("Splash", "Exam session request timeout, navigating to dashboard");
                        navigateToAppropriateDashboard();
                    }
                }
            };
            timeoutHandler.postDelayed(timeoutRunnable, 3000); // 3 second timeout

            Constant.mApiService.load_exam_session_teacher(body).enqueue(new Callback<SessionModel>() {
                @Override
                public void onResponse(Call<SessionModel> call, retrofit2.Response<SessionModel> reportModel) {
                    timeoutHandler.removeCallbacks(timeoutRunnable); // Cancel timeout
                    if (!isActivityValid()) return;
                    
                    if (reportModel.body() != null) {
                        if (reportModel.body().getStatus().getCode().equals("1000")) {
                            List<ExamSession> examSession = reportModel.body().getExamSession();
                            Paper.book().write(Constants.exam_session, examSession);

                            // Read User_Type in background to avoid StrictMode violation
                            loadUserTypeInBackground();
                        } else {
                            Log.w("Splash", "Exam session API error, navigating to appropriate dashboard");
                            navigateToAppropriateDashboard();
                        }
                    } else {
                        Log.w("Splash", "Null response from exam session API, navigating to appropriate dashboard");
                        navigateToAppropriateDashboard();
                    }
                }

                @Override
                public void onFailure(Call<SessionModel> call, Throwable e) {
                    timeoutHandler.removeCallbacks(timeoutRunnable); // Cancel timeout
                    if (isActivityValid()) {
                        Log.w("Splash", "Failed to load exam session: " + e.getMessage());
                        // Fallback to appropriate dashboard on failure
                        navigateToAppropriateDashboard();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("Splash", "Error in load_exam_session", e);
            navigateToAppropriateDashboard();
        }
    }
    
    /**
     * Clear stored login data to force role selection
     */
    private void clearStoredLoginData() {
        try {
            // Clear all authentication-related data
            Paper.book().delete(Constants.is_login);
            Paper.book().delete(Constants.User_Type);
            Paper.book().delete("parent_id");
            Paper.book().delete("campus_id");
            Paper.book().delete("staff_id");
            Paper.book().delete("student_id");
            Paper.book().delete("students");
            Paper.book().delete("campus_name");
            Paper.book().delete("full_name");
            Paper.book().delete("phone");
            Paper.book().delete("picture");
            Paper.book().delete("student_name");
            Paper.book().delete("student_phone");
            Paper.book().delete("student_picture");
            Log.d("Splash", "Cleared all stored login data");
        } catch (Exception e) {
            Log.e("Splash", "Error clearing stored login data", e);
        }
    }

    /**
     * Check if we should clear login data (for testing or reset purposes)
     */
    private boolean shouldClearLoginData() {
        // Development mode has been removed
        // Login data will persist normally
        
        // You can add other conditions here if needed:
        // - Clear on first install
        // - Clear after app update
        // - Clear when specific version is detected
        
        return false; // Never clear login data automatically - maintain login session until logout
    }
    
    /**
     * Verify login persistence and log status
     */
    private void verifyLoginPersistence() {
        try {
            boolean isLoggedIn = Paper.book().read(Constants.is_login, false);
            String userType = Paper.book().read(Constants.User_Type, "");
            
            Log.d("Splash", "=== LOGIN PERSISTENCE VERIFICATION ===");
            Log.d("Splash", "Login Status: " + (isLoggedIn ? "LOGGED IN" : "NOT LOGGED IN"));
            Log.d("Splash", "User Type: " + userType);
            Log.d("Splash", "Login will persist until explicit logout");
            
            if (isLoggedIn && !userType.isEmpty()) {
                Log.d("Splash", "✅ Login session is persistent - user will stay logged in");
            } else {
                Log.d("Splash", "⚠️ No active login session - user needs to login");
            }
        } catch (Exception e) {
            Log.e("Splash", "Error verifying login persistence", e);
        }
    }

    /**
     * Load login data in background thread to avoid StrictMode violations
     */
    private void loadLoginDataInBackground() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Check if this is a legitimate login flow (coming from LoginScreen)
                    boolean isFromLogin = getIntent().getBooleanExtra("from_login", false);
                    
                    if (isFromLogin) {
                        Log.d("Splash", "=== LOGIN FLOW DETECTED ===");
                        Log.d("Splash", "Coming from successful login, not clearing data");
                    } else {
                        // Normal app launch - keep login session persistent
                        Log.d("Splash", "=== NORMAL APP LAUNCH ===");
                        Log.d("Splash", "Keeping login session persistent - not clearing data");
                    }
                    
                    // Check if we should clear login data
                    if (shouldClearLoginData()) {
                        clearStoredLoginData();
                    }
                    
                    // Verify login persistence first
                    verifyLoginPersistence();
                    
                    // Read login data in background
                    final boolean isLoggedIn = Paper.book().read(Constants.is_login, false);
                    final String userType = Paper.book().read(Constants.User_Type, "");
                    
                    Log.d("Splash", "=== LOGIN DATA DEBUG ===");
                    Log.d("Splash", "Login check - IsLoggedIn: " + isLoggedIn + ", UserType: " + userType);
                    Log.d("Splash", "Paper DB - parent_id: " + Paper.book().read("parent_id", ""));
                    Log.d("Splash", "Paper DB - campus_id: " + Paper.book().read("campus_id", ""));
                    Log.d("Splash", "Paper DB - staff_id: " + Paper.book().read("staff_id", ""));
                    Log.d("Splash", "Paper DB - student_id: " + Paper.book().read("student_id", ""));
                    Log.d("Splash", "Paper DB - full_name: " + Paper.book().read("full_name", ""));
                    Log.d("Splash", "Paper DB - email: " + Paper.book().read("email", ""));
                    Log.d("Splash", "Paper DB - phone: " + Paper.book().read("phone", ""));
                    
                    // Check if students table exists and clear it if corrupted
                    try {
                        Paper.book().read("students");
                        Log.d("Splash", "Paper DB - students: EXISTS");
                    } catch (Exception e) {
                        Log.w("Splash", "Corrupted students table detected, clearing it: " + e.getMessage());
                        Paper.book().delete("students");
                        Log.d("Splash", "Paper DB - students: CLEARED");
                    }
                    
                    // Handle UI updates on main thread
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isActivityDestroyed) return;
                            
                            if (!isLoggedIn || userType.isEmpty()) {
                                // User is not logged in, go to role selection
                                Log.d("Splash", "User not logged in, navigating to SelectRole");
                                navigateToRoleSelection();
                                return;
                            }
                            
                            // User is logged in, load additional data and go to dashboard
                            Log.d("Splash", "User is logged in, loading dashboard data");
                            loadAdditionalDataInBackground(userType);
                        }
                    });
                } catch (Exception e) {
                    Log.e("Splash", "Error loading login data", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isActivityDestroyed) {
                                navigateToRoleSelection();
                            }
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Load additional data (parent_id, campus_id) in background thread
     */
    private void loadAdditionalDataInBackground(final String userType) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Handle UI updates on main thread
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isActivityDestroyed) return;
                            
                            if ("Teacher".equals(userType)) {
                                // For staff users, we don't need parent_id or campus_id for navigation
                                // Just go directly to dashboard
                                Log.d("Splash", "Staff user detected, navigating directly to dashboard");
                                loadUserTypeInBackground();
                            } else {
                                // For parent/student users, read additional data
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            // Read additional data in background
                                            final String parentId = Paper.book().read("parent_id");
                                            final String campusId = Paper.book().read("campus_id");
                                            
                                            Log.d("Splash", "=== PARENT/STUDENT DATA DEBUG ===");
                                            Log.d("Splash", "Paper DB - parent_id: " + parentId);
                                            Log.d("Splash", "Paper DB - campus_id: " + campusId);
                                            Log.d("Splash", "Paper DB - User_Type: " + Paper.book().read(Constants.User_Type, ""));
                                            Log.d("Splash", "Paper DB - is_login: " + Paper.book().read(Constants.is_login, false));
                                            
                                            // Handle UI updates on main thread
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isActivityDestroyed) return;
                                                    
                                                    // Update member variables
                                                    parent_id = parentId;
                                                    campus_id = campusId;
                                                    
                                                    // Start auto migration in background (non-blocking)
                                                    startBackgroundMigration();
                                                    
                                                    // Add a minimal delay to ensure smooth rendering
                                                    delayedRunnable = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (!isActivityDestroyed && !isNavigationInProgress) {
                                                                // For parent/student users, load exam session
                                                                load_exam_session(campus_id);
                                                            }
                                                        }
                                                    };
                                                    handler.postDelayed(delayedRunnable, 50); // Reduced delay for faster loading
                                                }
                                            });
                                        } catch (Exception e) {
                                            Log.e("Splash", "Error loading additional data", e);
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (!isActivityDestroyed) {
                                                        navigateToRoleSelection();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("Splash", "Error in loadAdditionalDataInBackground", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isActivityDestroyed) {
                                navigateToRoleSelection();
                            }
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Load user type in background thread to avoid StrictMode violations
     */
    private void loadUserTypeInBackground() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Read User_Type in background
                    final String userType = Paper.book().read(Constants.User_Type);
                    Log.d("Splash", "User Type: " + userType);
                    
                    // Handle UI updates on main thread
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isActivityDestroyed) return;
                            
                            if ("Teacher".equals(userType)) {
                                Log.d("Splash", "Teacher detected, navigating to StaffMainDashboard");
                                if (isActivityValid()) {
                                    try {
                                        ActivityTransitionHelper.startActivityAndFinishSmooth(
                                            Splash.this, 
                                            new Intent(Splash.this, StaffMainDashboard.class)
                                        );
                                    } catch (Exception e) {
                                        Log.e("Splash", "Error starting StaffMainDashboard: " + e.getMessage(), e);
                                        navigateToRoleSelection();
                                    }
                                }
                            } else if ("PARENT".equals(userType)) {
                                Log.d("Splash", "=== PARENT NAVIGATION DEBUG ===");
                                Log.d("Splash", "Parent detected, loading child data");
                                Log.d("Splash", "Member variable parent_id: " + parent_id);
                                Log.d("Splash", "Member variable campus_id: " + campus_id);
                                load_child(parent_id, campus_id);
                            } else if ("STUDENT".equals(userType)) {
                                Log.d("Splash", "STUDENT detected, navigating to StudentMainDashboard");
                                if (isActivityValid()) {
                                    try {
                                        isNavigationInProgress = true;
                                        ActivityTransitionHelper.startActivityAndFinishSmooth(
                                            Splash.this, 
                                            new Intent(Splash.this, StudentMainDashboard.class)
                                        );
                                    } catch (Exception e) {
                                        Log.e("Splash", "Error starting StudentMainDashboard: " + e.getMessage(), e);
                                        isNavigationInProgress = false;
                                        navigateToRoleSelection();
                                    }
                                }
                            } else {
                                Log.w("Splash", "Unknown user type: " + userType + ", navigating to DashBoard");
                                startMainActivity();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("Splash", "Error loading user type", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isActivityDestroyed) {
                                navigateToRoleSelection();
                            }
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Apply theme based on user type if logged in
     */
    private void applyThemeBasedOnUserType() {
        try {
            String userType = Paper.book().read(Constants.User_Type, "");
            if (!userType.isEmpty()) {
                Log.d("Splash", "Applying theme for user type: " + userType);
                
                if (userType.equals("PARENT")) {
                    // Apply unified parent theme for splash screen
                    ParentThemeHelper.applyParentTheme(this, 80); // 80dp for simple pages
                    ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for splash
                    ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for splash
                    ParentThemeHelper.setFooterVisibility(this, false); // No footer for splash
                    ParentThemeHelper.setHeaderTitle(this, "Loading...");
                } else {
                    // Apply other themes as needed
                    ThemeHelper.applySimpleTheme(this, userType);
                }
            } else {
                // Default theme for splash screen
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(android.graphics.Color.WHITE);
                    getWindow().setNavigationBarColor(android.graphics.Color.WHITE);
                }
            }
        } catch (Exception e) {
            Log.e("Splash", "Error applying theme", e);
            // Fallback to white theme
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(android.graphics.Color.WHITE);
                getWindow().setNavigationBarColor(android.graphics.Color.WHITE);
            }
        }
    }
}
