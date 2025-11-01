package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Adaptor.ChildListAdaptor;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.R;
import android.util.Log;

public class ChildList extends AppCompatActivity {

    private RecyclerView myrecycleview;
    List<SharedStudent> students;
    Context context;
    TextView no_list;
    // TextView toolbar_title; // Removed - using new header structure
    SwipeRefreshLayout swipe_refresh;

    private void applyTheme() {
        try {
            // Check user type and apply appropriate theme
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("ChildList", "User Type: " + userType);
            
            // For ChildList, we want to apply parent theme by default since this is a parent feature
            // Only apply student theme if explicitly accessed by a student user
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
                
                // Toolbar removed - using new header structure
                
                // Change footer background to teal for student theme
                View footerContainer = findViewById(R.id.footer_container);
                if (footerContainer != null) {
                    footerContainer.setBackgroundResource(R.drawable.footer_background_teal);
                }
                
                // Change text color to teal for student theme
                TextView noListText = findViewById(R.id.no_list);
                if (noListText != null) {
                    noListText.setTextColor(ContextCompat.getColor(this, R.color.student_primary));
                }
                
                Log.d("ChildList", "Student theme applied successfully");
            } else {
                // Apply unified parent theme for child list page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for child list
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for child list
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Child List");
                
                Log.d("ChildList", "Parent theme applied successfully - UserType: " + userType);
            }
            
            // Apply footer theming based on user type
            ThemeHelper.applyFooterTheme(this, userType);
            
        } catch (Exception e) {
            Log.e("ChildList", "Error applying theme", e);
            // Fallback: apply parent theme if there's an error
            try {
                ThemeHelper.applyParentTheme(this);
                Log.d("ChildList", "Fallback parent theme applied due to error");
            } catch (Exception fallbackError) {
                Log.e("ChildList", "Error applying fallback theme", fallbackError);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_child_list);

        // Configure status bar for dark brown background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.dark_brown));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar and navigation bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        setupWindowInsets();

        // Apply student theme using ThemeHelper
        applyTheme();

        // Set up back button with new header structure
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
            headerTitle.setText(getString(R.string.child_list));
        }

        swipe_refresh = findViewById(R.id.swipe_refresh);
        myrecycleview = findViewById(R.id.wanted_search);
        no_list = findViewById(R.id.no_list);
        // toolbar_title removed - using new header structure

        context = ChildList.this;
        
        // Title is now set in the header_title TextView in the new header structure
        Paper.init(context);
        students = new ArrayList<>();
        
        // Setup swipe-to-refresh functionality
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume(); // reload data
                swipe_refresh.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if user is a student
        String userType = Paper.book().read(Constants.User_Type, "");
        Log.d("ChildList", "User Type: " + userType);
        
        // Fix: Use default empty list to prevent null pointer exception
        students = Paper.book().read("students", new ArrayList<>());
        
        Log.d("ChildList", "Loaded students count: " + students.size());
        Log.d("ChildList", "User Type: " + userType);

        if (students.size() > 0) {
            no_list.setVisibility(View.GONE);

            ChildListAdaptor childListAdaptor = new ChildListAdaptor(context, students,
                    new OnClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            startActivity(new Intent(context, ParentChildDetail.class));
                            Paper.book().write("item_position", position);
                            Paper.book().write("current_child_model", students.get(position));
                        }
                    });

            myrecycleview.setLayoutManager(new GridLayoutManager(context, 2));
            myrecycleview.setAdapter(childListAdaptor);
            Log.d("ChildList", "Child list adapter set successfully");
        } else {
            no_list.setVisibility(View.VISIBLE);
            if (userType != null && userType.equals("STUDENT")) {
                no_list.setText(getString(R.string.no_siblings_found));
            } else {
                no_list.setText(getString(R.string.no_children_found));
            }
            Log.d("ChildList", "No students found, showing empty state");
        }
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
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

                        // Add bottom margin to footer container to push it above navigation bar
                        android.widget.LinearLayout footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            // Set bottom margin to navigation bar height to ensure footer is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            Log.d("ChildList", "Setting footer bottom margin: " + bottomMargin + "dp");
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                                Log.d("ChildList", "Footer margin applied successfully");
                            }
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("ChildList", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("ChildList", "Error setting up window insets: " + e.getMessage());
        }
    }
}