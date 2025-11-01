package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.SerializedName;

public class DashboardMenuItem {
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("icon")
    private String icon;
    
    @SerializedName("icon_resource")
    private int iconResource;
    
    @SerializedName("activity_class")
    private String activityClass;
    
    @SerializedName("permissions")
    private String[] permissions;
    
    @SerializedName("roles")
    private String[] roles;
    
    @SerializedName("is_enabled")
    private boolean isEnabled;
    
    @SerializedName("is_visible")
    private boolean isVisible;
    
    @SerializedName("order")
    private int order;
    
    @SerializedName("badge_count")
    private int badgeCount;
    
    @SerializedName("badge_text")
    private String badgeText;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("requires_internet")
    private boolean requiresInternet;
    
    @SerializedName("is_featured")
    private boolean isFeatured;

    // Constructor for backward compatibility
    public DashboardMenuItem(int iconResource, String title) {
        this.iconResource = iconResource;
        this.title = title;
        this.isEnabled = true;
        this.isVisible = true;
        this.order = 0;
        this.badgeCount = 0;
    }

    // Full constructor
    public DashboardMenuItem(String id, String title, int iconResource, String activityClass, 
                           String[] permissions, String[] roles, boolean isEnabled, 
                           boolean isVisible, int order, String category) {
        this.id = id;
        this.title = title;
        this.iconResource = iconResource;
        this.activityClass = activityClass;
        this.permissions = permissions;
        this.roles = roles;
        this.isEnabled = isEnabled;
        this.isVisible = isVisible;
        this.order = order;
        this.category = category;
        this.badgeCount = 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }

    public String getActivityClass() {
        return activityClass;
    }

    public void setActivityClass(String activityClass) {
        this.activityClass = activityClass;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isRequiresInternet() {
        return requiresInternet;
    }

    public void setRequiresInternet(boolean requiresInternet) {
        this.requiresInternet = requiresInternet;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    // Helper methods
    public boolean hasPermission(String permission) {
        if (permissions == null) return true;
        for (String perm : permissions) {
            if (perm.equals(permission)) return true;
        }
        return false;
    }

    public boolean hasRole(String role) {
        if (roles == null) return true;
        for (String r : roles) {
            if (r.equals(role)) return true;
        }
        return false;
    }

    public boolean hasBadge() {
        return badgeCount > 0 || (badgeText != null && !badgeText.isEmpty());
    }
} 