package topgrade.parent.com.parentseeks.Teacher.Diary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import topgrade.parent.com.parentseeks.R;

public class DiaryMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_diary_menu);
        
        // Configure status bar for navy blue background with white icons
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
        
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
        
        // Load staff credentials from Paper database
        topgrade.parent.com.parentseeks.Teacher.Utils.Constant.loadFromPaper();

        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(v -> finish());
    }
    
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);

            if (rootLayout != null) {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars()
                        );

                        LinearLayout mainContent = findViewById(R.id.layout);
                        if (mainContent != null) {
                            int bottomMargin = Math.max(systemInsets.bottom, 0);
                            android.view.ViewGroup.MarginLayoutParams params =
                                (android.view.ViewGroup.MarginLayoutParams) mainContent.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin + 16;
                                mainContent.setLayoutParams(params);
                            }
                        }

                        view.setPadding(0, 0, 0, 0);
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("DiaryMenu", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e("DiaryMenu", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e("DiaryMenu", "Error setting up window insets: " + e.getMessage(), e);
        }
    }

    public void SendDiary_Class(View view) {
        Intent intent = new Intent(DiaryMenu.this, DiarySubmitClass.class);
        startActivity(intent);
    }

    public void SendDiary_Section(View view) {
        Intent intent = new Intent(DiaryMenu.this, DiarySubmitSection.class);
        startActivity(intent);
    }

    public void SendDiary_Subject(View view) {
        Intent intent = new Intent(DiaryMenu.this, DiarySubmitSubject.class);
        startActivity(intent);
    }
}

