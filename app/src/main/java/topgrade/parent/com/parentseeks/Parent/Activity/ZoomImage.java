package topgrade.parent.com.parentseeks.Parent.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.jsibbold.zoomage.ZoomageView;
import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.R;

import java.util.Objects;

public class ZoomImage extends AppCompatActivity {

    private static final String TAG = "ZoomImage";

    private ZoomageView myZoomageView;
    private ProgressBar progess_bar;
    private ImageView back_icon;
    private TextView header_title;
    private ImageView headerWave;
    private LinearLayout footerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Paper DB
        try {
        Paper.init(this);
        } catch (Exception e) {
            // Paper might already be initialized, ignore the exception
            Log.d(TAG, "Paper already initialized or error: " + e.getMessage());
        }
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_zoom_image);
        
        // Cache views to avoid multiple findViewById() calls
        headerWave = findViewById(R.id.header_wave);
        footerContainer = findViewById(R.id.footer_container);

        // Initialize views
        myZoomageView = findViewById(R.id.myZoomageView);
        progess_bar = findViewById(R.id.progess_bar);
        back_icon = findViewById(R.id.back_icon);
        header_title = findViewById(R.id.header_title);

        // Setup back button click listener
        back_icon.setOnClickListener(v -> finish());
        
        // Add long press on back button to open theme test (for debugging)
        back_icon.setOnLongClickListener(v -> {
            ThemeTestActivity.start(this);
            return true;
        });
        
        // Add double tap on header title to run theme verification test
        header_title.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;
            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < 500) { // Double tap within 500ms
                    ThemeVerificationHelper.quickTest(ZoomImage.this);
                }
                lastClickTime = currentTime;
            }
        });
        
        // Add long press on header title to debug user type and force staff theme
        header_title.setOnLongClickListener(v -> {
            Log.d(TAG, "=== DEBUGGING USER TYPE ===");
            String currentUserType = Paper.book().read(Constants.User_Type, "NOT_FOUND");
            Log.d(TAG, "Current User_Type from Paper: '" + currentUserType + "'");
            Log.d(TAG, "Constants.User_Type value: '" + Constants.User_Type + "'");
            
            // Try different possible staff user type values
            String[] possibleStaffTypes = {"STAFF", "Staff", "staff", "TEACHER", "Teacher", "teacher"};
            for (String type : possibleStaffTypes) {
                String storedValue = Paper.book().read(type, "NOT_FOUND");
                Log.d(TAG, "Paper value for '" + type + "': '" + storedValue + "'");
            }
            
            // Force set STAFF theme
            Log.d(TAG, "Force setting Teacher theme for debugging");
            Paper.book().write(Constants.User_Type, "Teacher");
            applyTheme(headerWave, footerContainer);
            Toast.makeText(this, "Forced STAFF theme applied. Check logs for user type debug info.", Toast.LENGTH_LONG).show();
            return true;
        });
        
        // Apply dynamic theme based on user type (this will also setup status bar with theme colors)
        applyTheme(headerWave, footerContainer);
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsetsForStaff();
        
        // Handle image loading with improved intent handling
        handleImageLoading();
    }
    
    /**
     * Setup status bar for staff theme - same as Staff Profile
     * Transparent status bar to allow header wave to cover it
     */
    private void setupStatusBarForStaff() {
        // Set transparent status bar to allow header wave to cover it
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
        
        // For Android M and above, ensure white status bar icons on dark background
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        // Configure status bar and navigation bar icons for Android R and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar (white icons on dark background)
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }

        // Additional fix for older Android versions - Force white icons
        getWindow().getDecorView().setSystemUiVisibility(
            getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );
    }

    /**
     * Setup status bar and navigation bar with proper colors and icon appearance
     */
    private void setupStatusBar(int statusBarColor, int navBarColor) {
        // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
getWindow().setStatusBarColor(statusBarColor);
            getWindow().setNavigationBarColor(navBarColor);
            // minSdk is 26, so M (API 23) check is unnecessary
int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
    }
    
    /**
     * Apply dynamic theme based on user type
     */
    private void applyTheme(ImageView headerWave, LinearLayout footerContainer) {
        try {
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d(TAG, "User type detected: " + userType);
            Log.d(TAG, "User type length: " + (userType != null ? userType.length() : "null"));
            Log.d(TAG, "User type equals STUDENT: " + (userType != null && userType.equals("STUDENT")));
            Log.d(TAG, "User type equals STAFF: " + (userType != null && userType.equals("STAFF")));
            Log.d(TAG, "User type equals TEACHER: " + (userType != null && userType.equals("TEACHER")));
            Log.d(TAG, "User type equals Teacher: " + (userType != null && userType.equals("Teacher")));
            Log.d(TAG, "User type contains STAFF: " + (userType != null && userType.contains("STAFF")));
            Log.d(TAG, "User type contains TEACHER: " + (userType != null && userType.contains("TEACHER")));
            Log.d(TAG, "User type contains Teacher: " + (userType != null && userType.contains("Teacher")));
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal)
                ThemeHelper.applyStudentTheme(this);
                
                // Setup status bar with student theme colors
                setupStatusBar(
                    ContextCompat.getColor(this, R.color.student_primary),
                    ContextCompat.getColor(this, R.color.student_primary)
                );
                
                // Set header wave for student theme
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_teal);
                }
                
                // Set footer background for student theme
                if (footerContainer != null) {
                    footerContainer.setBackgroundResource(R.drawable.footer_background_teal);
                }
                
            } else if (userType != null && (userType.equals("STAFF") || userType.equals("TEACHER") || userType.equals("Teacher") || userType.contains("STAFF") || userType.contains("TEACHER") || userType.contains("Teacher"))) {
                // Apply staff theme (navy blue) - same as Staff Profile
                // Setup status bar with transparent status bar and navy navigation bar
                setupStatusBarForStaff();
                
                // Set header wave for staff theme
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_navy_blue);
                }
                
                // Set footer background for staff theme
                if (footerContainer != null) {
                    footerContainer.setBackgroundResource(R.drawable.footer_background_staff_navy);
                }
                
            } else {
                // Apply unified parent theme for zoom image page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for zoom image
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for zoom image
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Image Viewer");
                
                // Set header wave for parent theme
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_dark_brown);
                }
                
                // Set footer background for parent theme
                if (footerContainer != null) {
                    footerContainer.setBackgroundResource(R.drawable.footer_background_brown);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme", e);
        }
    }
    
    /**
     * Handle image loading with improved intent handling
     */
    private void handleImageLoading() {
        try {
            // Use Objects.requireNonNull for safer intent handling
            if (Objects.requireNonNull(getIntent()).hasExtra("image_url")) {
                loadImage(getIntent().getStringExtra("image_url"), getIntent().getStringExtra("name"));
            } else if (getIntent().hasExtra("link")) {
                loadImage(getIntent().getStringExtra("link"), getIntent().getStringExtra("name"));
            } else {
                Toast.makeText(this, "No image data provided", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Intent is null", e);
            Toast.makeText(this, "No intent data available", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error handling image loading", e);
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * Load image using Glide with proper error handling
     */
    private void loadImage(String link, String name) {
        if (link == null || link.trim().isEmpty()) {
            Toast.makeText(this, "Invalid image link", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        header_title.setText((name != null && !name.trim().isEmpty()) ? name + " Image" : "View Image");
        progess_bar.setVisibility(View.VISIBLE);

        // Get themed error fallback based on user type
        int errorDrawable = getThemedErrorDrawable();

        Glide.with(this)
                .load(link)
                .placeholder(R.drawable.ic_loading_placeholder) // Neutral loading placeholder
                .error(errorDrawable) // Themed error fallback
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache for better performance
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progess_bar.setVisibility(View.GONE);
                        Log.e(TAG, "Glide load failed for link: " + link + ", name: " + name, e); // Enhanced logging with context
                        Toast.makeText(ZoomImage.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progess_bar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(myZoomageView);
    }
    
    /**
     * Get themed error drawable based on user type
     */
    private int getThemedErrorDrawable() {
        try {
            String userType = Paper.book().read(Constants.User_Type, "PARENT");
            
            if (userType != null && userType.equals("STUDENT")) {
                return R.drawable.ic_profile_student; // Teal profile icon
            } else if (userType != null && (userType.equals("STAFF") || userType.equals("TEACHER"))) {
                return R.drawable.ic_profile_staff; // Navy blue profile icon
            } else {
                return R.drawable.ic_profile_parent; // Brown profile icon
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting themed error drawable", e);
            return R.drawable.ic_profile_parent; // Default to parent theme
        }
    }
    
    /**
     * Setup window insets for staff theme - same as Staff Profile
     * Allows header wave to cover status bar
     */
    private void setupWindowInsetsForStaff() {
        View rootLayout = findViewById(android.R.id.content);
        
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    Insets systemInsets = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                    );

                    // Add bottom margin to footer container to push it above navigation bar
                    if (footerContainer != null) {
                        int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                        android.view.ViewGroup.MarginLayoutParams params = 
                            (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                        if (params != null) {
                            params.bottomMargin = bottomMargin + 16; // 16dp original margin + navigation bar height
                            footerContainer.setLayoutParams(params);
                        }
                    }
                    
                    // No padding on root layout to avoid touch interference
                    view.setPadding(0, 0, 0, 0);

                    // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                    return WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        } else {
            Log.w(TAG, "Root layout not found, skipping window insets setup");
        }
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
     */
    private void setupWindowInsets() {
        androidx.constraintlayout.widget.ConstraintLayout rootLayout = findViewById(R.id.root_layout);
        
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    // Apply safe padding to avoid overlapping with system bars
                    view.setPadding(
                        systemInsets.left,
                        systemInsets.top,
                        systemInsets.right,
                        systemInsets.bottom
                    );

                    // Return CONSUMED to prevent child views from getting default padding
                    return WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                    return WindowInsetsCompat.CONSUMED;
                }
            });
        } else {
            Log.e(TAG, "rootLayout is null - cannot setup window insets");
        }
    }
    
    /**
     * Static helper method to safely start ZoomImage activity with required extras
     * This reduces runtime errors and provides a clean API for starting this activity
     */
    public static void start(Context context, String imageUrl, String name) {
        if (context == null) {
            Log.e(TAG, "Context is null - cannot start ZoomImage activity");
            return;
        }
        
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            Log.e(TAG, "Image URL is null or empty - cannot start ZoomImage activity");
            Toast.makeText(context, "Invalid image URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Intent intent = new Intent(context, ZoomImage.class);
            intent.putExtra("image_url", imageUrl);
            if (name != null && !name.trim().isEmpty()) {
                intent.putExtra("name", name);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting ZoomImage activity", e);
            Toast.makeText(context, "Error opening image viewer", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Legacy static helper for backward compatibility with "link" parameter
     */
    public static void startWithLink(Context context, String link, String name) {
        if (context == null) {
            Log.e(TAG, "Context is null - cannot start ZoomImage activity");
            return;
        }
        
        if (link == null || link.trim().isEmpty()) {
            Log.e(TAG, "Link is null or empty - cannot start ZoomImage activity");
            Toast.makeText(context, "Invalid image link", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Intent intent = new Intent(context, ZoomImage.class);
            intent.putExtra("link", link);
            if (name != null && !name.trim().isEmpty()) {
                intent.putExtra("name", name);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting ZoomImage activity with link", e);
            Toast.makeText(context, "Error opening image viewer", Toast.LENGTH_SHORT).show();
        }
    }
}
