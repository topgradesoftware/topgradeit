package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.AnnouncementsAdapter;
import topgrade.parent.com.parentseeks.Teacher.Model.Announcement_Model;
import topgrade.parent.com.parentseeks.Teacher.Model.Event_Model;
import topgrade.parent.com.parentseeks.Teacher.Model.News_Model;

public class StaffAnnouncements extends AppCompatActivity {
    private static final String TAG = "StaffAnnouncements";
    
    BaseApiService mApiService = API.getAPIService();
    RecyclerView announcementsRcv;
    ProgressBar progressBar;
    Context context;
    List<Announcement_Model.Announcement> allAnnouncements = new ArrayList<>();
    TextView totalRecords;
    
    // Filter buttons
    MaterialButton btnAll, btnEvents, btnText;
    
    // Adapter
    AnnouncementsAdapter adapter;
    
    // Empty state view
    TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_announcements);
        
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

        // Initialize views
        initializeViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Load data
        loadAnnouncements();
    }
    
    private void initializeViews() {
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        announcementsRcv = findViewById(R.id.announcements_rcv);
        progressBar = findViewById(R.id.progress_bar);
        totalRecords = findViewById(R.id.total_records);
        emptyStateText = findViewById(R.id.empty_state_text);
        
        // Filter buttons
        btnAll = findViewById(R.id.btn_all_news);
        btnEvents = findViewById(R.id.btn_events);
        btnText = findViewById(R.id.btn_text);
        
        // Ensure button text is set explicitly
        btnAll.setText("All");
        btnEvents.setText("Events");
        btnText.setText("News");
        
        // Hide News tab since no News API exists
        btnText.setVisibility(View.GONE);
        
        // Setup filter button listeners
        setupFilterButtons();
        
        // Set initial filter selection to "All"
        updateFilterSelection("all");

        context = StaffAnnouncements.this;
        Paper.init(context);
    }
    
    private void setupFilterButtons() {
        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFilterSelection("all");
                if (adapter != null) {
                adapter.filterByType("all");
                updateTotalRecords();
                }
            }
        });
        
        btnEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFilterSelection("events");
                if (adapter != null) {
                adapter.filterByType("events");
                updateTotalRecords();
                }
            }
        });
        
        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFilterSelection("text");
                if (adapter != null) {
                adapter.filterByType("text");
                updateTotalRecords();
                }
            }
        });
    }
    
    private void updateFilterSelection(String selectedFilter) {
        // Reset all buttons
        btnAll.setSelected(false);
        btnEvents.setSelected(false);
        btnText.setSelected(false);
        
        // Set the selected button
        if (selectedFilter.equals("all")) {
            btnAll.setSelected(true);
        } else if (selectedFilter.equals("events")) {
            btnEvents.setSelected(true);
        } else if (selectedFilter.equals("text")) {
            btnText.setSelected(true);
        }
        
        // Force refresh of button states
        btnAll.invalidate();
        btnEvents.invalidate();
        btnText.invalidate();
    }
    
    private void setupRecyclerView() {
        announcementsRcv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnnouncementsAdapter(allAnnouncements);
        announcementsRcv.setAdapter(adapter);
    }
    
    private void updateTotalRecords() {
        if (adapter != null) {
        int count = adapter.getFilteredCount();
            totalRecords.setText(getString(R.string.total_records_format, count));
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
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                            }
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                        }
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "rootLayout is null - cannot setup window insets");
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
            }
        }
    }

    private void loadAnnouncements() {
        runOnUiThread(() -> {
        progressBar.setVisibility(View.VISIBLE);
        allAnnouncements.clear(); // Clear existing data
            hideEmptyState(); // Hide any existing empty state
        });
        
        // Reset API response counter and status
        apiResponses = 0;
        newsLoaded = true; // Set to true since no news API exists
        eventsLoaded = false;
        
        // Load only events (no separate news API exists)
        // loadNews(); // Commented out - no news API available
        loadEvents();
    }
    
    /**
     * Create request body for API calls
     */
    private RequestBody createRequestBody(String campusId) {
        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("parent_id", campusId);
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());
    }
    
    /**
     * Check if campus ID is valid and show appropriate error if not
     */
    private boolean isCampusIdValid(String campusId) {
        if (campusId.isEmpty()) {
            showCampusIdError();
            checkAndHideProgress(); // Still count this as a response
            return false;
        }
        return true;
    }
    
    /**
     * Show persistent error message for missing campus ID
     */
    private void showCampusIdError() {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "Campus ID not found - configuration issue");
        }
        // Show persistent error message instead of just Toast
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(getString(R.string.campus_id_error));
        } else {
            Toast.makeText(context, getString(R.string.campus_id_not_found), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Check and hide progress bar when the API has responded
     */
    private void checkAndHideProgress() {
        apiResponses++;
        if (apiResponses == 1) { // Only Events API responds (no News API exists)
            // Update adapter with available data on UI thread, then hide progress bar
            runOnUiThread(() -> {
                updateAdapter();
                progressBar.setVisibility(View.GONE);
                
                // No partial data warning since we only load events
                // (newsLoaded is always true since no news API exists)
            });
        }
    }
    
    // Add API response counter for proper progress bar handling
    private int apiResponses = 0;
    private boolean newsLoaded = false;
    private boolean eventsLoaded = false;
    
    private void loadNews() {
        String campusId = Paper.book().read("campus_id", "");
        if (!isCampusIdValid(campusId)) {
            return;
        }
        
        RequestBody body = createRequestBody(campusId);
                
        mApiService.load_news(body).enqueue(new Callback<News_Model>() {
            @Override
            public void onResponse(Call<News_Model> call, Response<News_Model> response) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "News API Response Code: " + response.code());
                }
                
                if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                    List<News_Model.News> newsList = response.body().getNews();
                    if (BuildConfig.DEBUG) {
                    Log.d(TAG, "News count: " + (newsList != null ? newsList.size() : 0));
                    }
                    
                    if (newsList != null) {
                        // Convert news to announcements with duplicate checking
                        for (News_Model.News news : newsList) {
                            Announcement_Model.Announcement announcement = new Announcement_Model.Announcement(
                                news.getUniqueId(),
                                news.getTitle(),
                                news.getDescription(),
                                news.getPublishDate(),
                                news.getAuthor(),
                                news.getCategory()
                            );
                            allAnnouncements.add(announcement);
                        }
                    }
                    newsLoaded = true;
                } else {
                    if (BuildConfig.DEBUG) {
                    Log.e(TAG, "News API failed: " + (response.body() != null ? response.body().getStatus().getMessage() : "null response"));
                }
                }
                
                // Check and hide progress bar
                checkAndHideProgress();
            }

            @Override
            public void onFailure(Call<News_Model> call, Throwable e) {
                if (BuildConfig.DEBUG) {
                Log.e(TAG, "Failed to load news: " + e.getMessage());
                }
                Toast.makeText(context, getString(R.string.failed_to_load_news), Toast.LENGTH_SHORT).show();
                
                // Check and hide progress bar
                checkAndHideProgress();
            }
        });
    }
    
    private void loadEvents() {
        String campusId = Paper.book().read("campus_id", "");
        if (!isCampusIdValid(campusId)) {
            return;
        }
        
        RequestBody body = createRequestBody(campusId);
                
        mApiService.load_events(body).enqueue(new Callback<Event_Model>() {
            @Override
            public void onResponse(Call<Event_Model> call, Response<Event_Model> response) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Events API Response Code: " + response.code());
                }
                
                if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                    List<Event_Model.Event> eventList = response.body().getEvent();
                    if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Events count: " + (eventList != null ? eventList.size() : 0));
                    }
                    
                    if (eventList != null) {
                        // Convert events to announcements with duplicate checking
                        for (Event_Model.Event event : eventList) {
                            Announcement_Model.Announcement announcement = new Announcement_Model.Announcement(
                                event.getUniqueId(),
                                event.getFullName(),
                                event.getStartDate(),
                                event.getEnddate()
                            );
                            allAnnouncements.add(announcement);
                        }
                    }
                    eventsLoaded = true;
                } else {
                    if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Events API failed: " + (response.body() != null ? response.body().getStatus().getMessage() : "null response"));
                    }
                    Toast.makeText(context, getString(R.string.failed_to_load_announcements), Toast.LENGTH_SHORT).show();
                }
                
                // Check and hide progress bar
                checkAndHideProgress();
            }

            @Override
            public void onFailure(Call<Event_Model> call, Throwable e) {
                if (BuildConfig.DEBUG) {
                Log.e(TAG, "Failed to load events: " + e.getMessage());
                }
                Toast.makeText(context, getString(R.string.failed_to_load_events), Toast.LENGTH_SHORT).show();
                
                // Check and hide progress bar
                checkAndHideProgress();
            }
        });
    }
    
    private void updateAdapter() {
        if (BuildConfig.DEBUG) {
        Log.d(TAG, "Updating adapter with " + allAnnouncements.size() + " announcements");
        }
        
        // Remove duplicates before updating adapter using efficient method
        removeDuplicatesEfficient();
        
        adapter.updateData(allAnnouncements);
        
        // Apply initial filter to show all items
        adapter.filterByType("all");
        updateTotalRecords();
        
        if (allAnnouncements.isEmpty()) {
            // Show empty state instead of just Toast
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }
    
        /**
     * Remove duplicate announcements based on unique ID using efficient LinkedHashMap
     * This preserves order and provides O(n) performance
     */
    private void removeDuplicatesEfficient() {
        LinkedHashMap<String, Announcement_Model.Announcement> uniqueMap = new LinkedHashMap<>();
        
        for (Announcement_Model.Announcement announcement : allAnnouncements) {
            uniqueMap.put(announcement.getUniqueId(), announcement);
        }
        
        allAnnouncements.clear();
        allAnnouncements.addAll(uniqueMap.values());
        
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "After removing duplicates: " + allAnnouncements.size() + " announcements");
        }
    }
    
    /**
     * Remove duplicate announcements based on unique ID (legacy method)
     */
    private void removeDuplicates() {
        Set<String> uniqueIds = new HashSet<>();
        List<Announcement_Model.Announcement> uniqueAnnouncements = new ArrayList<>();
        
        for (Announcement_Model.Announcement announcement : allAnnouncements) {
            if (uniqueIds.add(announcement.getUniqueId())) {
                // First occurrence of this ID, keep it
                uniqueAnnouncements.add(announcement);
            } else {
                // Duplicate found, skip it
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Removing duplicate announcement with ID: " + announcement.getUniqueId());
                }
            }
        }
        
        allAnnouncements.clear();
        allAnnouncements.addAll(uniqueAnnouncements);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "After removing duplicates: " + allAnnouncements.size() + " announcements");
        }
    }
    
    /**
     * Show empty state when no announcements are available
     */
    private void showEmptyState() {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(getString(R.string.no_announcements_found));
            
            // Make empty state clickable for retry functionality
            emptyStateText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadAnnouncements(); // Retry loading data
                }
            });
        } else {
            // Fallback to Toast if empty state view is not available
            Toast.makeText(context, getString(R.string.no_announcements_found), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Hide empty state when announcements are available
     */
    private void hideEmptyState() {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.GONE);
            // Remove click listener when hiding
            emptyStateText.setOnClickListener(null);
        }
    }
}
