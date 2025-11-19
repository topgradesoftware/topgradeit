package topgrade.parent.com.parentseeks.Teacher.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Activity.ZoomImage;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffModel;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;


public class StaffProfile extends AppCompatActivity {
    Context context;
    TextView phone, email, landline, address, Name, subject, cnic;
    TextView dob, gender, designation, parent_name, created_date, security;
    TextView qualification, salary, daily_lectures, State, City;
    StaffModel parentModel;

    CircleImageView image;
    ProgressBar progress_bar;
    ImageView back_icon;

    RelativeLayout image_action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Disable StrictMode to prevent ANR issues
        android.os.StrictMode.setThreadPolicy(new android.os.StrictMode.ThreadPolicy.Builder()
            .build());
        
        // Remove problematic anti-flickering flags that cause visibility issues
        // topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.applyAntiFlickeringFlags(this);
        // topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white);
        // topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.optimizeWindow(this);
        
        // Safety check: Ensure this activity is only opened by staff users
        Paper.init(this);
        String userType = Paper.book().read("User_Type", "");
        if (!"Teacher".equals(userType)) {
            Log.e("StaffProfile", "StaffProfile opened by non-staff user: " + userType);
            Toast.makeText(this, "Access denied: Staff profile is only available for staff members", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        setContentView(R.layout.activity_staff_profile);
        
        // Configure status bar for navy blue background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
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

        // Additional fix for older Android versions - Force white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Ensure status bar icons are light (white) on dark background
            getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility() & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();

        context = StaffProfile.this;
        Paper.init(context);

        // Initialize views
        back_icon = findViewById(R.id.back_icon);
        image_action = findViewById(R.id.image_action);
        Name = findViewById(R.id.Name);
        image = findViewById(R.id.image);
        landline = findViewById(R.id.landline);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        dob = findViewById(R.id.dob);
        qualification = findViewById(R.id.qualification);
        subject = findViewById(R.id.subject);
        cnic = findViewById(R.id.cnic);
        salary = findViewById(R.id.salary);
        daily_lectures = findViewById(R.id.daily_lectures);
        State = findViewById(R.id.State);
        City = findViewById(R.id.City);
        gender = findViewById(R.id.gender);
        designation = findViewById(R.id.designation);
        parent_name = findViewById(R.id.parent_name);
        created_date = findViewById(R.id.created_date);
        security = findViewById(R.id.security);
        address = findViewById(R.id.address);
        progress_bar = findViewById(R.id.progress_bar);

        // Setup back button click listener
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        image_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String image_link = Paper.book().read("picture", "");
                String link = API.employee_image_base_url + image_link;
                Intent intent = new Intent(StaffProfile.this, ZoomImage.class);
                intent.putExtra("image_url", link);
                startActivity(intent);
            }
        });

        // Load staff data (like the working code)
        loadStaffData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload staff data when returning from edit activity
        loadStaffData();
    }

    private void loadStaffData() {
        // Load staff data directly from Paper DB (like the working code)
        parentModel = Paper.book().read("Staff_Model", new StaffModel());
        updateUIWithStaffData();
    }

    private void updateUIWithStaffData() {
        // Debug: Log the entire StaffModel object
        Log.d("StaffProfile", "=== STAFF MODEL DEBUG ===");
        Log.d("StaffProfile", "Full StaffModel: " + parentModel.toString());
        Log.d("StaffProfile", "Gender: " + parentModel.getGender());
        Log.d("StaffProfile", "State: " + parentModel.getState_name());
        Log.d("StaffProfile", "City: " + parentModel.getCity_name());
        Log.d("StaffProfile", "Email: " + parentModel.getEmail());
        Log.d("StaffProfile", "Name: " + parentModel.getFull_name());
        Log.d("StaffProfile", "Phone: " + parentModel.getPhone());
        Log.d("StaffProfile", "=== END DEBUG ===");

        // Load image in background with activity lifecycle check
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String image_link = Paper.book().read("picture", "");
            
            // Run UI updates on main thread
            runOnUiThread(() -> {
                // Check if activity is still valid before loading image
                if (!isFinishing() && !isDestroyed() && image != null) {
                    String link = API.employee_image_base_url + image_link;
                    try {
                        Glide.with(StaffProfile.this)
                                .load(link)
                                .placeholder(R.drawable.man)
                                .error(R.drawable.man)
                                .into(image);
                    } catch (Exception e) {
                        Log.e("StaffProfile", "Error loading image with Glide: " + e.getMessage());
                        // Set default image if Glide fails
                        if (image != null) {
                            image.setImageResource(R.drawable.man);
                        }
                    }
                } else {
                    Log.w("StaffProfile", "Activity is finishing/destroyed, skipping image load");
                }
            });
        });
        executor.shutdown();

        // Set staff information with null safety
        email.setText(getSafeStringValue(parentModel.getEmail()));
        phone.setText(getSafeStringValue(parentModel.getPhone()));
        landline.setText(getSafeStringValue(parentModel.getLandline()));
        address.setText(getSafeStringValue(parentModel.getAddress()));
        Name.setText(getSafeStringValue(parentModel.getFull_name()));
        subject.setText(getSafeStringValue(parentModel.getSubject()));
        cnic.setText(getSafeStringValue(parentModel.getCnic()));

        // Format date of birth - using 'yy' format as per project convention
        String dobValue = parentModel.getDob();
        if (dobValue != null && !dobValue.isEmpty()) {
            String inputPattern = "yyyy-MM-dd";
            String outputPattern = "dd/MM/yy";
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
            try {
                Date date = inputFormat.parse(dobValue);
                String strDate = outputFormat.format(date);
                dob.setText(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
                // Set default value if parsing fails
                dob.setText("Not specified");
            }
        } else {
            // Set default value if dob is null or empty
            dob.setText("Not specified");
        }

        // Set Gender, State, and City (like the working code)
        gender.setText(getSafeStringValue(parentModel.getGender()));
        State.setText(getSafeStringValue(parentModel.getState_name()));
        City.setText(getSafeStringValue(parentModel.getCity_name()));

        qualification.setText(getSafeStringValue(parentModel.getQualification()));
        designation.setText(getSafeStringValue(parentModel.getDesignation()));
        parent_name.setText(getSafeStringValue(parentModel.getParent_name()));
        created_date.setText(getSafeStringValue(parentModel.getCreated_date()));
        security.setText(getSafeStringValue(parentModel.getSecurity()));
        salary.setText(getSafeStringValue(parentModel.getSalary()));
        daily_lectures.setText(getSafeStringValue(parentModel.getDaily_lectures()));
    }

    public void editStaffProfile(View view) {
        startActivity(new Intent(StaffProfile.this, Edit_Profile.class));
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the footer is visible above the navigation bar, not hidden behind it
     */
    private void setupWindowInsets() {
        android.view.View rootLayout = findViewById(android.R.id.content);
        
        if (rootLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars()
                    );

                    // Add bottom margin to edit profile button to push it above navigation bar
                    com.google.android.material.button.MaterialButton editButton = findViewById(R.id.btn_update_staff_profile);
                    if (editButton != null) {
                        // Set bottom margin to navigation bar height to ensure button is visible
                        int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                        android.view.ViewGroup.MarginLayoutParams params = 
                            (android.view.ViewGroup.MarginLayoutParams) editButton.getLayoutParams();
                        if (params != null) {
                            params.bottomMargin = bottomMargin + 16; // 16dp original margin + navigation bar height
                            editButton.setLayoutParams(params);
                        }
                    }
                    
                    // No padding on root layout to avoid touch interference
                    view.setPadding(0, 0, 0, 0);

                    // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e("StaffProfile", "Error in window insets listener: " + e.getMessage());
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                }
            });
        } else {
            Log.e("StaffProfile", "rootLayout is null - cannot setup window insets");
        }
    }

    
    /**
     * Helper method to safely handle null, empty, or "null" string values
     * @param value The string value to check
     * @return The original value if valid, or "Not specified" if null/empty/invalid
     */
    private String getSafeStringValue(String value) {
        if (value == null || value.trim().isEmpty() || value.equals("null") || value.equals("")) {
            return "Not specified";
        }
        return value.trim();
    }
}
