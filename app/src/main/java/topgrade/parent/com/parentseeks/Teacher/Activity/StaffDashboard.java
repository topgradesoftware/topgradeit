package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickDrawerItem;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Interface.OnCloseNavigationDrawer;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Shared.Utils.LoadingStateManager;
import topgrade.parent.com.parentseeks.Teacher.Activity.Application.StaffAddApplication;
import topgrade.parent.com.parentseeks.Teacher.Activity.Complaint.StaffSubmitComplaint;
// Activity imports - all used in createDashboardCards() and clickDrawerItem()
import topgrade.parent.com.parentseeks.Teacher.Activity.FeedbackList;
import topgrade.parent.com.parentseeks.Teacher.Activity.FeedbackMenu;
import topgrade.parent.com.parentseeks.Teacher.Activity.PaymentHistory;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffAnnouncements;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffTaskMenu;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffMainDashboard;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffProfile;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffSalary;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffTimeTable;
import topgrade.parent.com.parentseeks.Teacher.Adapter.StaffDashboardGridAdapter;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffDashboardCard;
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;
import topgrade.parent.com.parentseeks.databinding.ActivityStaffPersonalDashboardBinding;

public class StaffDashboard extends AppCompatActivity implements
        OnClickListener, OnClickDrawerItem, OnCloseNavigationDrawer {

    private static final String TAG = "StaffDashboard";

    // Keys centralization
    private static final String KEY_DEMO = "DEMO";

    // ViewBinding
    private ActivityStaffPersonalDashboardBinding binding;

    // Single shared executor for background tasks
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Thread-safety
    private final AtomicBoolean isDataLoading = new AtomicBoolean(false);

    // Cached values
    private String campus_id = "";
    private String staffNameCached = "";
    private String pictureCached = "";

    // UI / Helpers
    private CustomPopupMenu customPopupMenu;
    private LoadingStateManager loadingStateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // ViewBinding
        binding = ActivityStaffPersonalDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Init PaperDB
        Paper.init(this);

        // Back press behavior
        setupBackPress();

        // Initialize UI and listeners
        initUi();

        // Setup RecyclerView grid
        setupGridLayout();

        // Setup loading manager
        setupLoadingStateManager();

        // Schedule light background reads
        mainHandler.postDelayed(() -> {
            loadCampusNameInBackground();
            loadIdsInBackground();
        }, 100);
    }

    private void initUi() {
        // Header title from Paper (cached)
        String fullName = safePaperRead("full_name");
        if (!fullName.isEmpty() && !fullName.equalsIgnoreCase(KEY_DEMO)) {
            binding.headerTitle.setText(fullName);
            staffNameCached = fullName;
        } else {
            binding.headerTitle.setText(getString(R.string.staff_member));
        }

        // Back button
        binding.backButton.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        });

        // More option (popup)
        binding.moreOption.setOnClickListener(v -> {
            if (!isFinishing() && !isDestroyed()) showPopupMenu(v);
        });

        // Version text (if present in layout) - Optional binding
        // Note: versionText may not exist in your layout
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupGridLayout() {
        try {
            binding.homeRcv.setLayoutManager(new GridLayoutManager(this, 3));
            binding.homeRcv.setHasFixedSize(true);
            binding.homeRcv.setItemViewCacheSize(20);
            binding.homeRcv.setNestedScrollingEnabled(false);

            List<StaffDashboardCard> cards = createDashboardCards();
            StaffDashboardGridAdapter gridAdapter = new StaffDashboardGridAdapter(this, cards, this::onCardClick);
            binding.homeRcv.setAdapter(gridAdapter);
        } catch (Exception e) {
            Log.e(TAG, "setupGridLayout: ", e);
        }
    }

    private List<StaffDashboardCard> createDashboardCards() {
        List<StaffDashboardCard> cards = new ArrayList<>();
        cards.add(new StaffDashboardCard(1, "Staff Profile", "View Profile Details", R.drawable.man,
                StaffProfile.class, null, "staff_profile"));
        cards.add(new StaffDashboardCard(2, "Salary", "View Salary Details", R.drawable.salary,
                StaffSalary.class, null, "salary"));
        cards.add(new StaffDashboardCard(3, "View TimeTable", "Check Schedule", R.drawable.timetablee,
                StaffTimeTable.class, null, "timetable"));
        cards.add(new StaffDashboardCard(4, "Assign Task", "Check Assigned Tasks", R.drawable.assign_task,
                StaffTaskMenu.class, null, "assign_task"));
        cards.add(new StaffDashboardCard(5, "Complaints", "Submit & View Complaints", R.drawable.ic_complaints,
                topgrade.parent.com.parentseeks.Teacher.Activity.Complaint.StaffComplaintMenu.class, null, "complaint"));
        cards.add(new StaffDashboardCard(6, "Leave Application", "Apply for Leave", R.drawable.leave_application,
                topgrade.parent.com.parentseeks.Teacher.Activity.Application.StaffApplicationMenu.class, null, "leave_application"));
        cards.add(new StaffDashboardCard(7, "Announcements", "View News & Events", R.drawable.news,
                StaffAnnouncements.class, null, "announcements"));
        cards.add(new StaffDashboardCard(8, "Back to Home", "Return to Main Menu", R.drawable.ic_home,
                null, this::backToHome, "back_to_home"));
        cards.add(new StaffDashboardCard(9, "Logout", "Sign Out", R.drawable.logout,
                null, this::logout, "logout"));
        return cards;
    }

    private void onCardClick(StaffDashboardCard card) {
        if (card == null) return;
        if (card.getTargetActivity() != null) {
            startActivity(new Intent(this, card.getTargetActivity()));
        } else if (card.getAction() != null) {
            try {
                card.getAction().run();
            } catch (Exception e) {
                Log.e(TAG, "onCardClick action error", e);
            }
        }
    }

    private void setupLoadingStateManager() {
        try {
            // Handle different binding types for error view
            View errorView = null;
            try {
                // Try to get errorView as a View (if it exists as a simple View)
                errorView = findViewById(R.id.error_view);
            } catch (Exception ignored) {
                // errorView might not exist or be a different type
            }
            
            loadingStateManager = new LoadingStateManager(binding.progressBar, binding.contentContainer, errorView);
                if (errorView != null) {
                View retryBtn = errorView.findViewById(R.id.btn_retry);
                if (retryBtn != null) retryBtn.setOnClickListener(v -> retryLoadingData());
            }
            loadingStateManager.showLoading();
            simulateDataLoading();
        } catch (Exception e) {
            Log.e(TAG, "setupLoadingStateManager: ", e);
        }
    }
    
    private void simulateDataLoading() {
        mainHandler.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            // Always show content after loading - no more error simulation
            loadingStateManager.showContent();
        }, 3000); // Increased delay for better loading experience
    }

    private void retryLoadingData() {
        if (loadingStateManager != null) {
            loadingStateManager.showLoading();
            simulateDataLoading();
        }
    }

    // ------------------- popup / share / rate / password -------------------

    private void showPopupMenu(View anchor) {
        try {
            if (customPopupMenu == null) {
                customPopupMenu = new CustomPopupMenu(this, anchor, "staff");
                customPopupMenu.setOnMenuItemClickListener(title -> {
                    switch (title) {
                        case "Share Application":
                            shareApp();
                            break;
                        case "Rate":
                            rateUs();
                            break;
                        case "Change Login Password":
                            changePassword();
                            break;
                        case "Logout":
                            logout();
                            break;
                    }
                    return true;
                });
            }
            if (customPopupMenu.isShowing()) customPopupMenu.dismiss();
            else customPopupMenu.show();
        } catch (Exception e) {
            Log.e(TAG, "showPopupMenu: ", e);
        }
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));

            String shareMessage = "\nLet me recommend you this application to view student attendance, fee challan and reports.\n\n" +
                    "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            Log.e(TAG, "shareApp: ", e);
            showSnackbar("Unable to share app");
        }
    }

    private void rateUs() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
        } catch (Exception e) {
            Log.e(TAG, "rateUs: ", e);
            showSnackbar("Play Store not available");
        }
    }

    private void changePassword() {
        startActivity(new Intent(this, PasswordsChange.class).putExtra("User_TYpe", "Staff"));
    }

    // ------------------- logout flow (no AsyncTask) -------------------

    private void logout() {
        executorService.execute(() -> {
            String staffId = safePaperRead("staff_id");
            // Use cached campus_id instead of reading from PaperDB again
            mainHandler.post(() -> performLogout(staffId, campus_id));
        });
    }

    private void performLogout(@NonNull String staff_id, @NonNull String campus_id) {
        try {
            showLoading(true);
            BaseApiService mApiService = API.getAPIService();
            HashMap<String, String> postParam = new HashMap<>();
            postParam.put("staff_id", staff_id);
            postParam.put("campus_id", campus_id);
            RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(),
                    MediaType.parse("application/json; charset=utf-8"));

            mApiService.logout_teacher(body).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        clearLoginAndGoToRole();
                    } else {
                        String msg = "Logout failed";
                        try {
                            msg = response.raw().message();
                        } catch (Exception ignored) {}
                        showSnackbar(msg);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    showLoading(false);
                    Log.e(TAG, "Logout failed", t);
                    showSnackbar("Logout failed: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "performLogout: ", e);
            showLoading(false);
            showSnackbar("Error during logout");
        }
    }

    private void clearLoginAndGoToRole() {
        try {
                        Paper.book().write(Constants.is_login, false);
                        Paper.book().delete(Constants.User_Type);
                        Paper.book().delete("staff_id");
                        Paper.book().delete("campus_id");
                        Paper.book().delete("full_name");
                        Paper.book().delete("phone");
                        Paper.book().delete("picture");
                        Paper.book().delete("campus_name");
                        
                        Intent intent = new Intent(StaffDashboard.this, SelectRole.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
        } catch (Exception e) {
            Log.e(TAG, "clearLoginAndGoToRole: ", e);
        }
    }
    
    private void showLoading(boolean show) {
        try {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        } catch (Exception ignored) {}
    }

    private void showSnackbar(String message) {
        try {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        } catch (Exception ignored) {}
    }

    // ------------------- user data background loader -------------------

    private void loadUserDataInBackground() {
        if (!isDataLoading.compareAndSet(false, true)) {
            Log.d(TAG, "User data load already in progress.");
            return;
        }
        
        executorService.execute(() -> {
            try {
                String name = safePaperRead("full_name");
                String pic = safePaperRead("picture");

                staffNameCached = name;
                pictureCached = pic;

                mainHandler.post(() -> {
                    try {
                        if (staffNameCached != null && !staffNameCached.isEmpty() && !staffNameCached.equalsIgnoreCase(KEY_DEMO)) {
                            binding.headerTitle.setText(staffNameCached);
                        } else {
                            binding.headerTitle.setText(getString(R.string.staff_member));
                        }

                        if (pictureCached != null && !pictureCached.isEmpty()) {
                            // Try to find profile picture view (may not exist in your layout)
                            try {
                                CircleImageView profile = findViewById(R.id.pic); // Try common ID names
                                if (profile != null) {
                                Glide.with(StaffDashboard.this)
                                            .load(API.employee_image_base_url + pictureCached)
                                        .placeholder(R.drawable.man)
                                        .error(R.drawable.man)
                                            .into(profile);
                                }
                            } catch (Exception ignored) {
                                // Profile picture view doesn't exist in this layout
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "UI update after user data load", e);
                        }
                    });
                } catch (Exception e) {
                Log.e(TAG, "loadUserDataInBackground: ", e);
                } finally {
                isDataLoading.set(false);
            }
        });
    }

    private void loadCampusNameInBackground() {
        executorService.execute(() -> {
            try {
                final String campus_name = safePaperRead("campus_name");
                mainHandler.post(() -> {
                    try {
                        String title = (!campus_name.equalsIgnoreCase(KEY_DEMO)) ? campus_name : "My Dashboard";
                        binding.headerTitle.setText(title);
                    } catch (Exception e) {
                        Log.e(TAG, "UI update in loadCampusNameInBackground", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "loadCampusNameInBackground: ", e);
            }
        });
    }

    private void loadIdsInBackground() {
        executorService.execute(() -> {
            try {
                campus_id = safePaperRead("campus_id");
            } catch (Exception e) {
                Log.e(TAG, "loadIdsInBackground: ", e);
            }
        });
    }

    // ------------------- helpers -------------------

    private String safePaperRead(String key) {
        try {
            Object v = Paper.book().read(key);
            return v != null ? v.toString() : "";
        } catch (Exception e) {
            Log.e(TAG, "safePaperRead error for key: " + key, e);
            return "";
        }
    }
    
    private void backToHome() {
        try {
            Intent intent = new Intent(StaffDashboard.this, StaffMainDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "backToHome: ", e);
            showSnackbar("Error going back to home");
        }
    }

    // ------------------- drawer & navigation -------------------


    @Override
    public void clickDrawerItem(String title) {
        switch (title) {
            case "Staff Profile":
                startActivity(new Intent(this, StaffProfile.class));
                break;
            case "Salary":
                startActivity(new Intent(this, StaffSalary.class));
                break;
            case "Payment History":
                startActivity(new Intent(this, PaymentHistory.class));
                break;
            case "Feedback Students":
                startActivity(new Intent(this, FeedbackMenu.class));
                break;
            case "View TimeTable":
                startActivity(new Intent(this, StaffTimeTable.class));
                break;
            case "View Assign Task":
                startActivity(new Intent(this, StaffTaskMenu.class));
                break;
            case "Complain Box":
                startActivity(new Intent(this, StaffSubmitComplaint.class));
                break;
            case "Leave Application":
                startActivity(new Intent(this, topgrade.parent.com.parentseeks.Teacher.Activity.Application.StaffApplicationMenu.class));
                break;
            case "Events":
            case "News":
            case "Announcements":
                startActivity(new Intent(this, StaffAnnouncements.class));
                break;
            case "Logout":
                logout();
                break;
            case "Back":
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                break;
        }
    }

    @Override
    public void closeNavigationDrawer() {
        // no-op
    }

    @Override
    public void onItemClick(View view, int position) {
        // implement if needed
    }


                @Override
    protected void onResume() {
        super.onResume();
        mainHandler.postDelayed(this::loadUserDataInBackground, 200);
    }

                @Override
    protected void onStop() {
        super.onStop();
        try {
            if (customPopupMenu != null && customPopupMenu.isShowing()) customPopupMenu.dismiss();
        } catch (Exception ignored) {}
    }

            @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mainHandler.removeCallbacksAndMessages(null);
            if (executorService != null && !executorService.isShutdown()) executorService.shutdownNow();
                } catch (Exception e) {
            Log.e(TAG, "onDestroy executor shutdown", e);
                }
        binding = null;
    }
}