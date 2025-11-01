package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserPreferences {
    @SerializedName("user_id")
    private String userId;
    
    @SerializedName("favorite_items")
    private List<String> favoriteItems; // List of menu item IDs
    
    @SerializedName("hidden_items")
    private List<String> hiddenItems; // List of menu item IDs
    
    @SerializedName("custom_order")
    private List<String> customOrder; // Custom order of menu items
    
    @SerializedName("layout_preference")
    private String layoutPreference; // "grid", "list", "card"
    
    @SerializedName("grid_columns")
    private int gridColumns;
    
    @SerializedName("theme_preference")
    private String themePreference; // "light", "dark", "auto"
    
    @SerializedName("show_notifications")
    private boolean showNotifications;
    
    @SerializedName("auto_refresh")
    private boolean autoRefresh;
    
    @SerializedName("refresh_interval")
    private int refreshInterval;
    
    @SerializedName("show_badges")
    private boolean showBadges;
    
    @SerializedName("compact_mode")
    private boolean compactMode;
    
    @SerializedName("last_updated")
    private long lastUpdated;

    public UserPreferences() {
        this.layoutPreference = "grid";
        this.gridColumns = 3;
        this.themePreference = "light";
        this.showNotifications = true;
        this.autoRefresh = true;
        this.refreshInterval = 300; // 5 minutes
        this.showBadges = true;
        this.compactMode = false;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getFavoriteItems() {
        return favoriteItems;
    }

    public void setFavoriteItems(List<String> favoriteItems) {
        this.favoriteItems = favoriteItems;
    }

    public List<String> getHiddenItems() {
        return hiddenItems;
    }

    public void setHiddenItems(List<String> hiddenItems) {
        this.hiddenItems = hiddenItems;
    }

    public List<String> getCustomOrder() {
        return customOrder;
    }

    public void setCustomOrder(List<String> customOrder) {
        this.customOrder = customOrder;
    }

    public String getLayoutPreference() {
        return layoutPreference;
    }

    public void setLayoutPreference(String layoutPreference) {
        this.layoutPreference = layoutPreference;
    }

    public int getGridColumns() {
        return gridColumns;
    }

    public void setGridColumns(int gridColumns) {
        this.gridColumns = gridColumns;
    }

    public String getThemePreference() {
        return themePreference;
    }

    public void setThemePreference(String themePreference) {
        this.themePreference = themePreference;
    }

    public boolean isShowNotifications() {
        return showNotifications;
    }

    public void setShowNotifications(boolean showNotifications) {
        this.showNotifications = showNotifications;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public boolean isShowBadges() {
        return showBadges;
    }

    public void setShowBadges(boolean showBadges) {
        this.showBadges = showBadges;
    }

    public boolean isCompactMode() {
        return compactMode;
    }

    public void setCompactMode(boolean compactMode) {
        this.compactMode = compactMode;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Helper methods
    public boolean isFavorite(String itemId) {
        return favoriteItems != null && favoriteItems.contains(itemId);
    }

    public boolean isHidden(String itemId) {
        return hiddenItems != null && hiddenItems.contains(itemId);
    }

    public void addFavorite(String itemId) {
        if (favoriteItems != null && !favoriteItems.contains(itemId)) {
            favoriteItems.add(itemId);
        }
    }

    public void removeFavorite(String itemId) {
        if (favoriteItems != null) {
            favoriteItems.remove(itemId);
        }
    }

    public void hideItem(String itemId) {
        if (hiddenItems != null && !hiddenItems.contains(itemId)) {
            hiddenItems.add(itemId);
        }
    }

    public void showItem(String itemId) {
        if (hiddenItems != null) {
            hiddenItems.remove(itemId);
        }
    }
} 