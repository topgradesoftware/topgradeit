package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange;
import topgrade.parent.com.parentseeks.Parent.Activity.SelectRole;
import topgrade.parent.com.parentseeks.Parent.Adaptor.HomeAdaptorStaff;
import topgrade.parent.com.parentseeks.Parent.Adaptor.NavDrawerAdapter;
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickDrawerItem;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Interface.OnCloseNavigationDrawer;
import topgrade.parent.com.parentseeks.Parent.Model.HomeModel;
import topgrade.parent.com.parentseeks.Parent.Model.NavDrawerItem;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.NetworkErrorHandler;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.DashboardConstants;
import topgrade.parent.com.parentseeks.Teacher.Utils.DrawerManager;
import topgrade.parent.com.parentseeks.Teacher.Utils.NetworkManager;
import topgrade.parent.com.parentseeks.Teacher.Utils.PerformanceOptimizer;
import topgrade.parent.com.parentseeks.Teacher.Utils.RecyclerViewManager;
import topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper;
import topgrade.parent.com.parentseeks.Teacher.Activity.Application.StaffAddApplication;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffTaskMenu;

import topgrade.parent.com.parentseeks.Teacher.Activity.FeedbackList;
import topgrade.parent.com.parentseeks.Teacher.Interface.OnMenuCLick;
import topgrade.parent.com.parentseeks.Parent.Utils.UserType;

public class StaffOthersDashboard extends AppCompatActivity implements
        OnClickListener, OnClickDrawerItem, OnCloseNavigationDrawer, OnMenuCLick {


    ProgressBar progress_bar;
    
    // UI Components
    DrawerLayout drawer;
    NavigationView navView;
    CircleImageView pic;
    TextView name;
    TextView location;
    RecyclerView home_rcv;
    TextView version;
    ImageView more_option;
    
    // Managers
    private DrawerManager drawerManager;
    private RecyclerViewManager recyclerViewManager;
    private NetworkManager networkManager;
    private PerformanceOptimizer performanceOptimizer;
    
    // Context
    Context context;
    
    // Popup menu
    private CustomPopupMenu customPopupMenu;


    @Override
    protected void onResume() {
        super.onResume();
        
        // Ensure activity stays visible
        ensureActivityVisibility();
        
        // Only update UI if activity is still valid
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        String name_str = Paper.book().read("full_name", "");
        String location_str = Paper.book().read("phone", "");
        final String pic_str = Paper.book().read("picture", "");
        
        // Update the header elements directly since the layout doesn't use proper NavigationView header
        if (name != null && !name.getText().equals(name_str)) {
            name.setText(name_str);
        }
        if (location != null && !location.getText().equals(location_str)) {
            location.setText(location_str);
        }
        if (pic != null && pic_str != null && !pic_str.isEmpty()) {
            // Only load image if it's different from current
            String currentPicTag = (String) pic.getTag();
            if (!pic_str.equals(currentPicTag)) {
                Glide.with(this)
                        .load(API.employee_image_base_url + pic_str)
                        .placeholder(R.drawable.man)
                        .error(R.drawable.man)
                        .into(pic);
                pic.setTag(pic_str);
            }
        }
    }
    
    private void ensureActivityVisibility() {
        try {
            // Force the activity to be visible
            getWindow().getDecorView().setVisibility(View.VISIBLE);
            
            // Ensure the window is not stopped by checking its state
            if (getWindow().getDecorView().getVisibility() != View.VISIBLE) {
                Log.w("StaffOthersDashboard", "Window was not visible, forcing visibility");
                getWindow().getDecorView().setVisibility(View.VISIBLE);
            }
            
            // Force a layout update
            getWindow().getDecorView().requestLayout();
            
            // Ensure the window is not stopped
            if (getWindow().getAttributes().flags != 0) {
                Log.d("StaffOthersDashboard", "Window flags: " + getWindow().getAttributes().flags);
            }
            
            Log.d("StaffOthersDashboard", "Activity visibility ensured");
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error ensuring activity visibility", e);
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d("StaffOthersDashboard", "onWindowFocusChanged: " + hasFocus);
        
        if (hasFocus) {
            // Window gained focus, ensure visibility
            ensureActivityVisibility();
        } else {
            // Window lost focus, log it but don't panic
            Log.w("StaffOthersDashboard", "Window lost focus");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Remove problematic anti-flickering flags that cause visibility issues
        // topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.applyAntiFlickeringFlags(this);
        // topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white);
        // topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.optimizeWindow(this);
        
        setContentView(R.layout.activity_staff_more_options);
        
        // Configure status bar for navy blue background with white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));

            // For Android M and above, ensure white status bar icons on dark background
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }

        // Configure status bar and navigation bar icons for Android R and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();

        // Initialize context and PaperDB
        context = this;
        Paper.init(context);

        // Initialize UI components
        initUI();
        
        // Setup drawer and navigation
        setupDrawer();
        
        // Setup RecyclerView and adapter
        setupRecyclerView();
        
        // Setup more options popup
        setupMoreOptions();
        
        // Initialize performance optimizations
        initializePerformanceOptimizations();
        
        // Additional CPU optimization
        optimizeCPUUsage();
        
        // Initialize network error handling
        initializeNetworkErrorHandling();
    }
    
    private void initUI() {
        try {
            // Toolbar setup
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }
            
            // Drawer + NavigationView setup
            drawer = findViewById(R.id.drawer_layout);
            navView = findViewById(R.id.nav_view);
            
            // Drawer header info - handle null case
            if (navView != null) {
                // Since the layout doesn't use proper header mechanism, find views directly
                pic = findViewById(R.id.pic);
                name = findViewById(R.id.name);
                location = findViewById(R.id.location);
            }
            
            // Version text
            version = findViewById(R.id.version);
            if (version != null) {
                version.setText("V " + BuildConfig.VERSION_NAME);
            }
            
            // RecyclerView (home menu cards)
            home_rcv = findViewById(R.id.home_rcv);
            
            // More options button
            more_option = findViewById(R.id.more_option);
            
            // Progress bar
            progress_bar = findViewById(R.id.progress_bar);
            
            // Set dynamic title from intent
            TextView dashboardTitle = findViewById(R.id.header_title);
            if (dashboardTitle != null) {
                String dashboardTitleText = getIntent().getStringExtra("DASHBOARD_TITLE");
                if (dashboardTitleText != null && !dashboardTitleText.isEmpty() && !dashboardTitleText.equalsIgnoreCase("DEMO")) {
                    dashboardTitle.setText(dashboardTitleText);
                    Log.d("StaffOthersDashboard", "Header title set from intent: " + dashboardTitleText);
                } else {
                    dashboardTitle.setText("More Options");
                    Log.d("StaffOthersDashboard", "Header title set to default: More Options");
                }
            }
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error in initUI: " + e.getMessage());
        }
    }
    
    private void setupDrawer() {
        // Setup drawer toggle
        if (drawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this,
                    drawer,
                    (Toolbar) findViewById(R.id.toolbar),
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
            );
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }
        
        // Setup NavigationView
        if (navView != null) {
            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                
                if (id == R.id.nav_home) {
                    // Handle home navigation
                    Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, StaffProfile.class));
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_salary) {
                    startActivity(new Intent(this, StaffSalary.class));
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_advanced_salary) {
                    startActivity(new Intent(this, AdvancedSalary.class));
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_ledger) {
                    startActivity(new Intent(this, StaffLedger.class));
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_invoice) {
                    startActivity(new Intent(this, StaffInvoice.class));
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_feedback) {
                    startActivity(new Intent(this, FeedbackMenu.class));
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_timetable) {
                    startActivity(new Intent(this, StaffTimeTable.class));
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_assign_task) {
                    startActivity(new Intent(this, StaffTaskMenu.class));
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_complain) {
                    startActivity(new Intent(this, topgrade.parent.com.parentseeks.Teacher.Activity.Complaint.StaffSubmitComplaint.class));
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_leave_application) {
                    startActivity(new Intent(this, topgrade.parent.com.parentseeks.Teacher.Activity.Application.StaffApplicationMenu.class));
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_logout) {
                    // Handle logout
                    Paper.book().destroy(); // Clear session
                    startActivity(new Intent(this, SelectRole.class));
                    finish();
                    return true;
                }
                
                return false;
            });
        }
    }
    
    private void setupRecyclerView() {
        if (home_rcv == null) {
            Log.e("StaffOthersDashboard", "RecyclerView is null, cannot setup");
            return;
        }
        
        // Use RecyclerViewManager for better organization and performance
        recyclerViewManager = new RecyclerViewManager(this, home_rcv, this, this);
        recyclerViewManager.initialize();
        
        // Apply performance optimizations
        if (performanceOptimizer != null) {
            performanceOptimizer.optimizeRecyclerView(home_rcv);
        }
        
        // Load staff utilities menu items
        loadStaffUtilitiesMenu();
        
        // Ensure RecyclerView is visible
        home_rcv.setVisibility(View.VISIBLE);
    }
    
    private void loadStaffUtilitiesMenu() {
        try {
            List<HomeModel> utilitiesList = new ArrayList<>();
            
            // Staff More Options - Standardized with 8 cards for consistency
                        utilitiesList.add(new HomeModel(
                1, 
                R.drawable.ic_settings,
                "Settings", 
                "App configuration"
            ));
            
            utilitiesList.add(new HomeModel(
                2, 
                R.drawable.ic_help,
                "Help", 
                "User support"
            ));
            
            utilitiesList.add(new HomeModel(
                3, 
                R.drawable.ic_info,
                "About", 
                "App information"
            ));
            
            utilitiesList.add(new HomeModel(
                4, 
                R.drawable.share,
                "Share App",
                "Share with friends"
            ));
            
            utilitiesList.add(new HomeModel(
                5, 
                R.drawable.rate,
                "Rate",
                "Rate our app"
            ));
            
            utilitiesList.add(new HomeModel(
                6, 
                R.drawable.key,
                "Change Password",
                "Update password"
            ));
            
            utilitiesList.add(new HomeModel(
                7, 
                R.drawable.ic_home,
                "Back to Home",
                "Return to"
            ));
            
            utilitiesList.add(new HomeModel(
                8, 
                R.drawable.logout,
                "Logout",
                "Sign Out"
            ));
            
            // Update adapter with new list
            if (recyclerViewManager != null) {
                recyclerViewManager.updateData(utilitiesList);
            }
            
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error loading staff utilities menu: " + e.getMessage());
        }
    }
    
    private void setupMoreOptions() {
        try {
            // Get user data from PaperDB
            String parentId = Paper.book().read("parent_id");
            String campusId = Paper.book().read("campus_id");
            
            // More options (popup menu) - using the same pattern as parent dashboard
            if (more_option != null) {
                more_option.setOnClickListener(v -> {
                    if (isActivityValid()) {
                        showPopupMenu(v);
                    }
                });
            } else {
                Log.e("StaffOthersDashboard", "more_option is null - cannot setup click listener");
            }
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error in setupMoreOptions: " + e.getMessage());
        }
    }
    
    private boolean isActivityValid() {
        return !isFinishing() && !isDestroyed();
    }

    private void showPopupMenu(View view) {
        try {
            if (customPopupMenu == null) {
                customPopupMenu = new CustomPopupMenu(this, view, UserType.TEACHER.getValue());
                customPopupMenu.setOnMenuItemClickListener(title -> {
                    switch (title) {
                        case "Share Application":
                            share();
                            break;
                        case "Rate":
                            rate_us();
                            break;
                        case "Change Login Password":
                            changePasswordDialog();
                            break;
                        case "Logout":
                            logout();
                            break;
                    }
                    return true;
                });
            }
            
            if (customPopupMenu.isShowing()) {
                customPopupMenu.dismiss();
            } else {
                customPopupMenu.show();
            }
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error showing popup menu: " + e.getMessage());
            // Fallback to simple toast if popup fails
            Toast.makeText(this, "More options: Share, Rate, Change Password, Logout", Toast.LENGTH_SHORT).show();
        }
    }
    


    private void logout() {
        try {
            showLoading(true);
            
            // Load staff and campus IDs from Paper
            String staff_id = Paper.book().read("staff_id", "");
            String campus_id = Paper.book().read("campus_id", "");
            
            // Create request body
            HashMap<String, String> postParam = new HashMap<>();
            postParam.put("staff_id", staff_id);
            postParam.put("campus_id", campus_id);
            
            String jsonString = new JSONObject(postParam).toString();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
            
            // Make API call
            API.getAPIService().logout_teacher(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    showLoading(false);
                    if (response.body() != null) {
                        // Clear all authentication data
                        Paper.book().write(Constants.is_login, false);
                        Paper.book().delete(Constants.User_Type);
                        Paper.book().delete("staff_id");
                        Paper.book().delete("campus_id");
                        Paper.book().delete("full_name");
                        Paper.book().delete("phone");
                        Paper.book().delete("picture");
                        Paper.book().delete("campus_name");
                        
                        // Navigate to role selection with proper flags
                        Intent intent = new Intent(StaffOthersDashboard.this, SelectRole.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        showError(response.raw().message() != null ? response.raw().message() : "Logout failed");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("StaffOthersDashboard", "Logout failed", t);
                    showLoading(false);
                    showError("Logout failed: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error during logout", e);
            showError("Error during logout");
            showLoading(false);
        }
    }
    
    private void showLoading(boolean show) {
        if (progress_bar != null) {
            progress_bar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void changePasswordDialog() {
        startActivity(new Intent(StaffOthersDashboard.this, PasswordsChange.class)
                .putExtra("User_TYpe", UserType.TEACHER.getValue())
        );
    }


    private void share() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage = "\nLet me recommend you this application to view student atttendence,fee Chalan and Reports.\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rate_us() {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
            startActivity(rateIntent);
        } catch (android.content.ActivityNotFoundException e) {
            // Fallback to web browser if Play Store is not available
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
            startActivity(rateIntent);
        }
    }
    
    private void backToHome() {
        try {
            // Navigate back to the main staff dashboard with simple, reliable transition
            Intent intent = new Intent(StaffOthersDashboard.this, StaffMainDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error going back to home", e);
            Toast.makeText(this, "Error going back to home", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        // Not used with modern NavigationView approach
    }

    @Override
    public void closeNavigationDrawer() {
        // Not used with modern NavigationView approach
    }

    @Override
    public void OnMenuCLick(View view, String title) {
        // Handle menu clicks from RecyclerView items
        Log.d("StaffOthersDashboard", "OnMenuCLick called with title: " + title);
        
        try {
            switch (title) {
                case "Settings":
                    showSettings();
                    break;
                    
                case "Help":
                    showHelp();
                    break;
                    
                case "About":
                    showAbout();
                    break;
                    
                case "Share App":
                    share();
                    break;
                    
                case "Rate":
                    rate_us();
                    break;
                    
                case "Change Password":
                    changePasswordDialog();
                    break;
                    
                case "Back to Home":
                    backToHome();
                    break;
                    
                case "Logout":
                    logout();
                    break;
                    
                default:
                    Log.w("StaffOthersDashboard", "Unknown menu item clicked: " + title);
                    Toast.makeText(this, "Function not implemented yet: " + title, Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error handling menu click for: " + title, e);
            Toast.makeText(this, "Error opening " + title, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the footer won't be hidden behind the navigation bar
     */
    private void setupWindowInsets() {
        android.view.View rootLayout = findViewById(android.R.id.content);
        
        if (rootLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());

                    // Only add bottom padding for navigation bar, no other padding to avoid touch interference
                    int bottomPadding = systemInsets.bottom > 20 ? systemInsets.bottom : 0;
                    
                    // Set padding only on the root layout, not affecting child touch events
                    view.setPadding(
                        0, // left - no padding to avoid touch interference
                        0, // top - no padding to avoid touch interference  
                        0, // right - no padding to avoid touch interference
                        bottomPadding // only bottom padding for navigation bar
                    );

                    // Return CONSUMED to prevent child views from getting default padding
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e("StaffOthersDashboard", "Error in window insets listener: " + e.getMessage());
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                }
            });
        } else {
            Log.e("StaffOthersDashboard", "rootLayout is null - cannot setup window insets");
        }
    }

    @Override
    public void clickDrawerItem(String title) {
        // Handle drawer item clicks
        switch (title) {
            case "Home":
                // Navigate to home
                break;
            case "Profile":
                // Navigate to profile
                break;
            case "Settings":
                // Navigate to settings
                break;
            case "Logout":
                logout();
                break;
            default:
                // Handle other drawer items
                break;
        }
    }
    
    /**
     * Initialize performance optimizations for this activity
     */
    private void initializePerformanceOptimizations() {
        try {
            // Use PerformanceOptimizer for proper optimizations
            if (performanceOptimizer != null) {
                performanceOptimizer.optimizeActivity(this);
            }
            
            Log.d("PerformanceOptimizer", "Performance optimizations applied to StaffOthersDashboard");
        } catch (Exception e) {
            Log.e("PerformanceOptimizer", "Error applying performance optimizations", e);
        }
    }

/**
 * Additional performance optimizations for this activity
 */
private void optimizeCPUUsage() {
    try {
        // Use PerformanceOptimizer for proper optimizations
        if (performanceOptimizer != null) {
            performanceOptimizer.optimizeRecyclerView(home_rcv);
        }
        
        Log.d("PerformanceOptimizer", "Additional optimizations applied to StaffOthersDashboard");
    } catch (Exception e) {
        Log.e("PerformanceOptimizer", "Error applying additional optimizations", e);
    }
}
    
    /**
     * Initialize network error handling for this activity
     */
    private void initializeNetworkErrorHandling() {
        try {
            NetworkErrorHandler networkErrorHandler = NetworkErrorHandler.getInstance(this);
            
            // Add network callback for this activity
            networkErrorHandler.addNetworkCallback(new NetworkErrorHandler.NetworkCallback() {
                @Override
                public void onNetworkAvailable(android.net.Network network) {
                    Log.d("StaffOthersDashboard", "Network available");
                    // Optionally refresh data when network becomes available
                }
                
                @Override
                public void onNetworkLost(android.net.Network network) {
                    Log.w("StaffOthersDashboard", "Network lost");
                    // Show network status to user
                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(StaffOthersDashboard.this, 
                                "Network connection lost", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                
                @Override
                public void onNetworkUnavailable() {
                    Log.e("StaffOthersDashboard", "Network unavailable");
                    // Handle network unavailability
                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(StaffOthersDashboard.this, 
                                "No network connection available", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            
            // Add error handler for system network errors
            networkErrorHandler.addErrorHandler(new NetworkErrorHandler.NetworkErrorCallback() {
                @Override
                public void onSystemNetworkError(Exception exception) {
                    Log.w("StaffOthersDashboard", "System network error: " + exception.getMessage());
                    // Log the error but don't crash the app
                    // These are system-level errors that we can't control
                }
            });
            
            Log.d("StaffOthersDashboard", "Network error handling initialized successfully");
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error initializing network error handling", e);
        }
    }
    
    @Override
    protected void onDestroy() {
        try {
            // Cleanup managers
            if (networkManager != null) {
                networkManager.cleanup();
                networkManager = null;
            }
            
            if (performanceOptimizer != null) {
                performanceOptimizer.cleanup();
                performanceOptimizer = null;
            }
            
            if (recyclerViewManager != null) {
                recyclerViewManager.clearData();
                recyclerViewManager = null;
            }
            
            if (drawerManager != null) {
                drawerManager = null;
            }
            
            // Clear references
            context = null;
            customPopupMenu = null;
            
            Log.d("StaffOthersDashboard", "Cleanup completed");
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error during cleanup", e);
        } finally {
            super.onDestroy();
        }
    }
    
    /**
     * Open App Settings - copied from main login screen (SelectRole)
     */
    private void showSettings() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error opening app settings", e);
            Toast.makeText(this, "Error opening settings", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Open Help Documentation - copied from main login screen (SelectRole)
     */
    private void showHelp() {
        try {
            // Directly open the TopGrade Software help and support page
            openUrl("https://topgradesoftware.com/help_support_form.php");
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error opening help documentation", e);
            Toast.makeText(this, "Error opening help documentation", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show App Info - copied from main login screen (SelectRole)
     */
    private void showAbout() {
        try {
            android.content.pm.PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            
            String message = "Version: " + versionName + " (" + versionCode + ")\n\n" +
                           "Developed by Top Grade Software\n\n" +
                           "Contact Information:\n" +
                           "📱 WhatsApp: +923006616622\n" +
                           "📧 Email: support@topgradesoftware.com\n" +
                           "🌐 Website: https://topgradesoftware.com\n\n" +
                           "For support and inquiries, please contact us through any of the above channels.";
            
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("About " + getString(R.string.app_name))
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error showing app info", e);
            Toast.makeText(this, "Error showing app info", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to open URLs - copied from main login screen (SelectRole)
     */
    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            intent.setPackage("com.android.chrome");
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
            } catch (Exception e2) {
                Log.e("StaffOthersDashboard", "Error opening URL: " + url, e2);
                Toast.makeText(this, "Error opening link", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("StaffOthersDashboard", "Error opening URL: " + url, e);
            Toast.makeText(this, "Error opening link", Toast.LENGTH_SHORT).show();
        }
    }
}
