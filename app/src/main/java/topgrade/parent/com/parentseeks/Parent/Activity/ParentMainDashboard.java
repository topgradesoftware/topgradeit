package topgrade.parent.com.parentseeks.Parent.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Parent.Utils.ConsolidatedANRPreventionHelper;
import topgrade.parent.com.parentseeks.Utils.BaseMainDashboard;
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;
import topgrade.parent.com.parentseeks.Parent.Activity.SelectRole;
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange;
import topgrade.parent.com.parentseeks.Parent.Activity.PersonalDashboard;
import topgrade.parent.com.parentseeks.Parent.Activity.OtherOptionsDashboard;
import topgrade.parent.com.parentseeks.Parent.Activity.AcademicsDashboard;

public class ParentMainDashboard extends BaseMainDashboard {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_parent_main_dashboard;
    }

    @Override
    protected int getPrimaryColor() {
        return R.color.parent_primary;
    }

    @Override
    protected String getUserType() {
        return ThemeHelper.THEME_PARENT;
    }

    @Override
    protected String getLogoutAPI() {
        return "logout_parent";
    }

    @Override
    protected String getUserNameKey() {
        return "full_name";
    }

    @Override
    protected String getUserIDKey() {
        return "parent_id";
    }

    @Override
    protected String getDisplayName() {
        return "Parent Member";
    }

    // REFACTORED: loadDataAsync() and logout() are now inherited from BaseMainDashboard
    // which uses centralized UserDataManager and LogoutManager
    // This eliminates ~100 lines of duplicate code!

    @Override
    protected void setupCardClickListeners() {
        setClick(R.id.card_your_dashboard, PersonalDashboard.class, "Personal Dashboard");
        setClick(R.id.card_academics_dashboard, AcademicsDashboard.class, "Child Academics");
        setClick(R.id.card_other_options, OtherOptionsDashboard.class, "More Options");
    }

    private void setClick(int id, Class<?> targetClass, String title) {
        try {
            View card = findViewById(id);
            if (card != null) {
                card.setOnClickListener(v -> {
                    try {
                        // Track dashboard card click
                        topgrade.parent.com.parentseeks.Parent.Utils.AnalyticsManager.INSTANCE.logDashboardCardClick(
                            title, 
                            getUserType()
                        );
                        
                        Intent intent = new Intent(this, targetClass);
                        intent.putExtra("DASHBOARD_TITLE", title);
                        
                        // Use the same simple approach for all activities, including More Options
                        if (title.equals("More Options")) {
                            Log.d("ParentMainDashboard", "Starting OtherOptionsDashboard with simple approach (like Child Academics)");
                        }
                        
                        // Use simple, reliable activity start for ALL options (including More Options)
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("ParentMainDashboard", "Error starting activity: " + targetClass.getSimpleName(), e);
                        Toast.makeText(this, "Error opening " + title, Toast.LENGTH_SHORT).show();
                        
                        // Track error
                        topgrade.parent.com.parentseeks.Parent.Utils.AnalyticsManager.INSTANCE.logError(
                            "navigation_error",
                            e.getMessage() != null ? e.getMessage() : "Unknown error",
                            "ParentMainDashboard"
                        );
                    }
                });
            } else {
                Log.w("ParentMainDashboard", "Card view not found for ID: " + getResources().getResourceEntryName(id));
            }
        } catch (Exception e) {
            Log.e("ParentMainDashboard", "Error setting up click listener for ID: " + id, e);
        }
    }
}