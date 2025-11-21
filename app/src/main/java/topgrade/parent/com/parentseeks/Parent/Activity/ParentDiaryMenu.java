package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import topgrade.parent.com.parentseeks.R;

public class ParentDiaryMenu extends AppCompatActivity {

    private static final String TAG = "ParentDiaryMenu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_parent_diary_menu);
        
        // Configure status bar for parent theme with white icons
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.parent_primary));
        
        // Ensure white status bar icons on dark background
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        
        // Configure status bar and navigation bar icons for Android R and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0,
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | 
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        // Setup window insets
        setupWindowInsets();

        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(v -> finish());
    }
    
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);

            if (rootLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                        );

                        // Add bottom margin to footer container to push it above navigation bar
                        LinearLayout footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            // Set bottom margin to navigation bar height to ensure footer is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
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
            } else {
                Log.e(TAG, "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
        }
    }

    public void ViewDiary_Class(View view) {
        Intent intent = new Intent(ParentDiaryMenu.this, ParentViewClassDiary.class);
        startActivity(intent);
    }

    public void ViewDiary_Subject(View view) {
        Intent intent = new Intent(ParentDiaryMenu.this, ParentViewSubjectDiary.class);
        startActivity(intent);
    }
}
