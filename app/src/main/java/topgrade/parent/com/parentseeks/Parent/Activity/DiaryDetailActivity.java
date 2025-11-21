package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import topgrade.parent.com.parentseeks.Parent.Model.DiaryEntry;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;

public class DiaryDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "DiaryDetailActivity";
    
    private ImageView backIcon;
    private TextView headerTitle;
    private ImageView diaryImage;
    private TextView descriptionText;
    private TextView selectedStudentName;
    private TextView selectedClassName;
    private TextView selectedSubjectName;
    private TextView subjectSeparator;
    
    private DiaryEntry diaryEntry;
    private Context context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Configure status bar for dark brown background with white icons
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        
        setContentView(R.layout.activity_diary_detail);
        
        context = this;
        
        // Setup window insets for footer navigation
        setupWindowInsets();
        
        // Get diary entry from intent
        String diaryEntryJson = getIntent().getStringExtra("DIARY_ENTRY");
        if (diaryEntryJson != null && !diaryEntryJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                diaryEntry = gson.fromJson(diaryEntryJson, DiaryEntry.class);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing diary entry from intent", e);
                Toast.makeText(this, "Error loading diary details", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Log.e(TAG, "Diary entry not provided in intent");
            Toast.makeText(this, "Diary entry not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupClickListeners();
        loadDiaryDetails();
    }
    
    private void initializeViews() {
        backIcon = findViewById(R.id.back_icon);
        headerTitle = findViewById(R.id.header_title);
        diaryImage = findViewById(R.id.diary_image);
        descriptionText = findViewById(R.id.description_text);
        selectedStudentName = findViewById(R.id.selected_student_name);
        selectedClassName = findViewById(R.id.selected_class_name);
        selectedSubjectName = findViewById(R.id.selected_subject_name);
        subjectSeparator = findViewById(R.id.subject_separator);
        
        headerTitle.setText("Diary Detail");
    }
    
    private void setupClickListeners() {
        backIcon.setOnClickListener(v -> finish());
    }
    
    private void loadDiaryDetails() {
        if (diaryEntry == null) {
            Log.e(TAG, "Diary entry is null");
            return;
        }
        
        // Log all fields for debugging
        Log.d(TAG, "=== Diary Entry Debug Info ===");
        Log.d(TAG, "ImageUrl: " + diaryEntry.getImageUrl());
        Log.d(TAG, "Picture: " + diaryEntry.getPicture());
        Log.d(TAG, "Description: " + diaryEntry.getDescription());
        Log.d(TAG, "Body: " + diaryEntry.getBody());
        Log.d(TAG, "ClassName: " + diaryEntry.getClassName());
        Log.d(TAG, "Date: " + diaryEntry.getDate());
        
        // Load description
        String description = diaryEntry.getDescription() != null ? diaryEntry.getDescription() : 
                           (diaryEntry.getBody() != null ? diaryEntry.getBody() : "");
        descriptionText.setText(description.isEmpty() ? "No description available" : description);
        
        // Update selection summary
        updateSelectionSummary();
        
        // Load image - using the same pattern as working code (ChildListAdaptor, ParentChildDetail)
        loadDiaryImage();
    }
    
    /**
     * Update the selection summary display with student name, class, and subject (if available)
     */
    private void updateSelectionSummary() {
        try {
            // Update student name
            if (selectedStudentName != null) {
                String studentName = diaryEntry.getFullName() != null ? diaryEntry.getFullName() : "N/A";
                selectedStudentName.setText(studentName);
            }
            
            // Update class name
            if (selectedClassName != null) {
                String className = diaryEntry.getClassName() != null ? diaryEntry.getClassName() : "N/A";
                selectedClassName.setText(className);
            }
            
            // Update subject name (for subject diary) - show only if available
            if (selectedSubjectName != null && subjectSeparator != null) {
                String subjectName = diaryEntry.getSubjectName();
                if (subjectName != null && !subjectName.isEmpty()) {
                    selectedSubjectName.setText(subjectName);
                    selectedSubjectName.setVisibility(android.view.View.VISIBLE);
                    subjectSeparator.setVisibility(android.view.View.VISIBLE);
                } else {
                    selectedSubjectName.setVisibility(android.view.View.GONE);
                    subjectSeparator.setVisibility(android.view.View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating selection summary", e);
        }
    }
    
    private void loadDiaryImage() {
        try {
            String picturePath = null;
            
            // Try picture field first (as used in LoadChildrenDiary)
            if (diaryEntry.getPicture() != null && !diaryEntry.getPicture().isEmpty()) {
                picturePath = diaryEntry.getPicture();
                Log.d(TAG, "Found picture field: " + picturePath);
            }
            // Try imageUrl as fallback
            else if (diaryEntry.getImageUrl() != null && !diaryEntry.getImageUrl().isEmpty()) {
                picturePath = diaryEntry.getImageUrl();
                Log.d(TAG, "Found imageUrl field: " + picturePath);
            }
            
            if (picturePath != null && !picturePath.isEmpty()) {
                // Make final copies for use in inner class
                final String finalPicturePath = picturePath;
                final String imageUrl;
                
                // If it's already a full URL, use it as is
                if (picturePath.startsWith("http://") || picturePath.startsWith("https://")) {
                    imageUrl = picturePath;
                    Log.d(TAG, "Picture is already a full URL: " + imageUrl);
                } else {
                    // Build URL using the same pattern as working code
                    // Try API.image_base_url first (as used in ChildListAdaptor and ParentChildDetail)
                    imageUrl = API.image_base_url + picturePath;
                    Log.d(TAG, "Built image URL: " + imageUrl);
                    Log.d(TAG, "Using base URL: " + API.image_base_url);
                }
                
                // Make final copy of imageUrl for inner class
                final String finalImageUrl = imageUrl;
                
                // Load image using the same simple pattern as working code
                Log.d(TAG, "Loading diary image from: " + finalImageUrl);
                Glide.with(context)
                        .load(finalImageUrl)
                        .placeholder(R.drawable.topgrade_logo)
                        .error(R.drawable.topgrade_logo)
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                Log.e(TAG, "Glide failed to load image: " + finalImageUrl, e);
                                if (e != null && e.getRootCauses() != null) {
                                    for (Throwable cause : e.getRootCauses()) {
                                        Log.e(TAG, "Root cause: " + cause.getMessage());
                                    }
                                }
                                // Post alternative URL loading to main thread handler
                                // (Can't start new Glide loads from within RequestListener callbacks)
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    tryAlternativeImageUrls(finalPicturePath, finalImageUrl);
                                });
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                Log.d(TAG, "Image loaded successfully: " + finalImageUrl);
                                return false;
                            }
                        })
                        .into(diaryImage);
            } else {
                Log.w(TAG, "No picture/imageUrl found in diary entry");
                Log.d(TAG, "Using default logo");
                diaryImage.setImageResource(R.drawable.topgrade_logo);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading diary image", e);
            diaryImage.setImageResource(R.drawable.topgrade_logo);
        }
    }
    
    private void tryAlternativeImageUrls(String picturePath, String failedUrl) {
        // Try alternative base URLs if the first one failed
        String[] alternativeBaseUrls = {
            API.base_url + "uploads/diary/",
            API.base_url + "uploads/",
            API.parent_image_base_url,
            API.employee_image_base_url
        };
        
        for (String baseUrl : alternativeBaseUrls) {
            String alternativeUrl = baseUrl + picturePath;
            if (!alternativeUrl.equals(failedUrl)) {
                Log.d(TAG, "Trying alternative URL: " + alternativeUrl);
                Glide.with(context)
                        .load(alternativeUrl)
                        .placeholder(R.drawable.topgrade_logo)
                        .error(R.drawable.topgrade_logo)
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                Log.w(TAG, "Alternative URL also failed: " + alternativeUrl);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                Log.d(TAG, "Image loaded successfully from alternative URL: " + alternativeUrl);
                                return false;
                            }
                        })
                        .into(diaryImage);
                break; // Try only the first alternative
            }
        }
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * Uses margin approach - footer pushed above navigation bar,
     * navigation bar's dark_brown color creates transparent/blended appearance
     */
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            
            if (rootLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                        );

                        android.widget.LinearLayout footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            int bottomMargin = Math.max(systemInsets.bottom, 0);
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                            }
                        }

                        view.setPadding(0, 0, 0, 0);
                        return WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
        }
    }
}

