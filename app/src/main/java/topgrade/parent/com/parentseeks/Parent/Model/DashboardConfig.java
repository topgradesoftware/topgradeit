package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DashboardConfig {
    @SerializedName("layout_type")
    private String layoutType; // "grid", "list", "card"
    
    @SerializedName("grid_columns")
    private int gridColumns;
    
    @SerializedName("show_categories")
    private boolean showCategories;
    
    @SerializedName("show_search")
    private boolean showSearch;
    
    @SerializedName("show_favorites")
    private boolean showFavorites;
    
    @SerializedName("enable_animations")
    private boolean enableAnimations;
    
    @SerializedName("refresh_interval")
    private int refreshInterval; // in seconds
    
    @SerializedName("theme")
    private String theme; // "light", "dark", "auto"
    
    @SerializedName("categories")
    private List<DashboardCategory> categories;
    
    @SerializedName("menu_items")
    private List<DashboardMenuItem> menuItems;
    
    @SerializedName("user_preferences")
    private UserPreferences userPreferences;

    public DashboardConfig() {
        // Default configuration
        this.layoutType = "grid";
        this.gridColumns = 3;
        this.showCategories = true;
        this.showSearch = true;
        this.showFavorites = true;
        this.enableAnimations = true;
        this.refreshInterval = 300; // 5 minutes
        this.theme = "light";
    }

    // Getters and Setters
    public String getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(String layoutType) {
        this.layoutType = layoutType;
    }

    public int getGridColumns() {
        return gridColumns;
    }

    public void setGridColumns(int gridColumns) {
        this.gridColumns = gridColumns;
    }

    public boolean isShowCategories() {
        return showCategories;
    }

    public void setShowCategories(boolean showCategories) {
        this.showCategories = showCategories;
    }

    public boolean isShowSearch() {
        return showSearch;
    }

    public void setShowSearch(boolean showSearch) {
        this.showSearch = showSearch;
    }

    public boolean isShowFavorites() {
        return showFavorites;
    }

    public void setShowFavorites(boolean showFavorites) {
        this.showFavorites = showFavorites;
    }

    public boolean isEnableAnimations() {
        return enableAnimations;
    }

    public void setEnableAnimations(boolean enableAnimations) {
        this.enableAnimations = enableAnimations;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public List<DashboardCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<DashboardCategory> categories) {
        this.categories = categories;
    }

    public List<DashboardMenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<DashboardMenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    // Helper methods
    public boolean isGridLayout() {
        return "grid".equals(layoutType);
    }

    public boolean isListLayout() {
        return "list".equals(layoutType);
    }

    public boolean isCardLayout() {
        return "card".equals(layoutType);
    }
} 