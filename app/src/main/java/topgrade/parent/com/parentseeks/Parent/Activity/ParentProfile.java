package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.EnhancedImageLoader;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.R;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ParentProfile extends AppCompatActivity {
    TextView phone, email, landline, address, Name;
    CircleImageView image;
    Context context;

    ProgressBar progress_bar;
    RelativeLayout image_action;

    private void applyTheme() {
        try {
            // Check user type and apply appropriate theme
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("ParentProfile", "User Type: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
int tealColor = ContextCompat.getColor(this, R.color.student_primary);
                    getWindow().setStatusBarColor(tealColor);
                    getWindow().setNavigationBarColor(tealColor);
                
                // Force dark navigation bar icons (prevent light appearance)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // 0 = do NOT use light icons
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }
                
                // Change wave background to teal for student theme
                ImageView headerWave = findViewById(R.id.header_wave);
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_teal);
                }
                
                // Change edit button color to student theme (teal)
                MaterialButton editButton = findViewById(R.id.edit_profile);
                if (editButton != null) {
                    editButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.student_primary));
                }
                
                // Change footer background to student theme (teal)
                LinearLayout footerContainer = findViewById(R.id.footer_container);
                if (footerContainer != null) {
                    // Find the MaterialCardView inside footer and update its background
                    com.google.android.material.card.MaterialCardView footerCard = 
                        (com.google.android.material.card.MaterialCardView) footerContainer.getChildAt(0);
                    if (footerCard != null) {
                        footerCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.student_primary));
                    }
                }
                
                Log.d("ParentProfile", "Student theme applied successfully");
            } else {
                // For parent theme, don't use ParentThemeHelper as it overwrites navigation bar color
                // System bars are already configured in onCreate() to match complaint menu
                // Just update header title if needed
                TextView headerTitle = findViewById(R.id.header_title);
                if (headerTitle != null) {
                    headerTitle.setText("Parent Profile");
                }
                
                Log.d("ParentProfile", "Parent theme - system bars already configured in onCreate()");
            }
        } catch (Exception e) {
            Log.e("ParentProfile", "Error applying theme", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_parent_profile);
        
        // Configure status bar for dark brown background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            // Set navigation bar to dark brown to match parent theme
            getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.dark_brown));
            
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
                    0, // No light icons for status bar and navigation bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }

        // Additional fix for older Android versions - Force white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Ensure status bar and navigation bar icons are light (white) on dark background
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            // For Android O and above, also ensure navigation bar icons are white
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();
        
        // Initialize Paper database
        Paper.init(this);
        
        // Load constants from Paper (if needed)
        // Note: Not using ParentThemeHelper for parent theme to avoid overwriting navigation bar color
        // System bars are already configured above, matching complaint menu approach
        
        // Apply theme based on user type (only for student theme)
        applyTheme();

        // Initialize views with null safety
            try {
                image_action = findViewById(R.id.image_action);
            } catch (ClassCastException e) {
                Log.e("ParentProfile", "Error casting image_action to RelativeLayout", e);
                // Try to find the view as a generic View first
                View imageActionView = findViewById(R.id.image_action);
                if (imageActionView instanceof RelativeLayout) {
                    image_action = (RelativeLayout) imageActionView;
                } else {
                    Log.w("ParentProfile", "image_action is not a RelativeLayout, skipping initialization");
                    image_action = null;
                }
            }
            Name = findViewById(R.id.Name);
            image = findViewById(R.id.image);
            landline = findViewById(R.id.landline);
            email = findViewById(R.id.email);
            phone = findViewById(R.id.phone);
            address = findViewById(R.id.address);
            progress_bar = findViewById(R.id.progress_bar);
            
        // Debug: Check what's actually in Paper DB
        Log.d("ParentProfile", "=== PAPER DB DEBUG CHECK ===");
        Log.d("ParentProfile", "is_login: " + Paper.book().read("is_login"));
        Log.d("ParentProfile", "User_Type: " + Paper.book().read("User_Type"));
        Log.d("ParentProfile", "parent_id: " + Paper.book().read("parent_id"));
        Log.d("ParentProfile", "full_name: " + Paper.book().read("full_name"));
        Log.d("ParentProfile", "email: " + Paper.book().read("email"));
        Log.d("ParentProfile", "phone: " + Paper.book().read("phone"));
        Log.d("ParentProfile", "landline: " + Paper.book().read("landline"));
        Log.d("ParentProfile", "address: " + Paper.book().read("address"));
        Log.d("ParentProfile", "picture: " + Paper.book().read("picture"));
        Log.d("ParentProfile", "=== END PAPER DB DEBUG CHECK ===");
        
        // Load data immediately after initialization (same as Edit_ProfileParent)
        Log.d("ParentProfile", "=== LOADING DATA IMMEDIATELY ===");
        loadDataDirectly();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ParentProfile", "=== PARENT PROFILE onResume() CALLED ===");
        // Reload data to ensure it's up to date after returning from edit form
        loadDataDirectly();
    }
    
    /**
     * Load data directly from Paper DB (exactly like Edit_ProfileParent)
     */
    private void loadDataDirectly() {
        Log.d("ParentProfile", "=== LOADING DATA DIRECTLY (like Edit_ProfileParent) ===");
        
        // Load data using same keys for both user types (only theme differs)
        String userType = Paper.book().read(Constants.User_Type, "");
        Log.d("ParentProfile", "Current user type: " + userType);
        
        // Use same data keys for both student and parent users when viewing parent profile
        final String full_name_ = Paper.book().read("full_name");
        String email_ = Paper.book().read("email");
        String phone_ = Paper.book().read("phone");
        String landline_ = Paper.book().read("landline");
        String address_ = Paper.book().read("address");
        final String picture_ = Paper.book().read("picture");
        
        Log.d("ParentProfile", "=== DIRECT DATA DEBUG ===");
        Log.d("ParentProfile", "full_name: '" + full_name_ + "'");
        Log.d("ParentProfile", "email: '" + email_ + "'");
        Log.d("ParentProfile", "phone: '" + phone_ + "'");
        Log.d("ParentProfile", "landline: '" + landline_ + "'");
        Log.d("ParentProfile", "address: '" + address_ + "'");
        Log.d("ParentProfile", "picture: '" + picture_ + "'");
        Log.d("ParentProfile", "=== END DIRECT DATA DEBUG ===");
        
        // Set text data (exactly like Edit_ProfileParent)
        if (Name != null) {
            Name.setText(full_name_);
            Log.d("ParentProfile", "Set name: '" + full_name_ + "'");
        }
        if (email != null) {
            email.setText(email_);
            Log.d("ParentProfile", "Set email: '" + email_ + "'");
        }
        if (phone != null) {
            phone.setText(phone_);
            Log.d("ParentProfile", "Set phone: '" + phone_ + "'");
        }
        if (landline != null) {
            landline.setText(landline_);
            Log.d("ParentProfile", "Set landline: '" + landline_ + "'");
        }
        if (address != null) {
            address.setText(address_);
            Log.d("ParentProfile", "Set address: '" + address_ + "'");
        }
        
        // Load profile image (exactly like Edit_ProfileParent)
        if (picture_ != null && !picture_.isEmpty()) {
            String imageUrl = API.parent_image_base_url + picture_;
            Log.d("ParentProfile", "Loading image from: " + imageUrl);
            
            EnhancedImageLoader imageLoader = new EnhancedImageLoader(this);
            imageLoader.loadImage(image, imageUrl, new EnhancedImageLoader.ImageLoadCallback() {
                @Override
                public void onSuccess(Drawable drawable) {
                    Log.d("ParentProfile", "Profile image loaded successfully");
                }
                
                @Override
                public void onError(Exception e) {
                    Log.w("ParentProfile", "Failed to load profile image, using fallback", e);
                }
            });
        } else {
            Log.d("ParentProfile", "No picture found, using default image");
            if (image != null) {
                image.setImageResource(R.drawable.man_brown);
            }
        }
        
        // Setup custom header with ic_arrow_back and title
        ImageView backIcon = findViewById(R.id.back_icon);
        TextView headerTitle = findViewById(R.id.header_title);
        
        if (backIcon != null) {
            backIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        
        if (headerTitle != null) {
            String title = (full_name_ != null && !full_name_.isEmpty()) ? 
                full_name_ + " Profile" : "Parent Profile";
            headerTitle.setText(title);
        }
        
        // Setup image click listener
        if (image_action != null) {
            image_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (picture_ != null && !picture_.isEmpty()) {
                        Intent intent = new Intent(ParentProfile.this, ZoomImage.class);
                        intent.putExtra("image_url", API.parent_image_base_url + picture_);
                        intent.putExtra("name", full_name_ != null ? full_name_ : "Parent");
                        startActivity(intent);
                        Log.d("ParentProfile", "Opening zoomed image for: " + full_name_);
                    } else {
                        Toast.makeText(ParentProfile.this, "No profile picture available", Toast.LENGTH_SHORT).show();
                        Log.w("ParentProfile", "Attempted to view image but picture is null or empty");
                    }
                }
            });
        }
        
        Log.d("ParentProfile", "=== FINISHED LOADING DATA DIRECTLY ===");
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * Uses margin approach like complaint page - footer pushed above navigation bar,
     * navigation bar's dark_brown color creates transparent/blended appearance
     */
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            
            if (rootLayout != null) {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars()
                        );

                        android.view.View footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                            }
                        }
                        
                        view.setPadding(0, 0, 0, 0);
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("ParentProfile", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e("ParentProfile", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e("ParentProfile", "Error setting up window insets: " + e.getMessage(), e);
        }
    }

    public void editParentProfile(View view) {
        try {
            startActivity(new Intent(this, Edit_ProfileParent.class));
        } catch (Exception e) {
            Log.e("ParentProfile", "Error opening edit profile: " + e.getMessage(), e);
            Toast.makeText(this, "Unable to open edit profile", Toast.LENGTH_SHORT).show();
        }
    }
}