package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DashboardCategory {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("icon")
    private String icon;
    
    @SerializedName("icon_resource")
    private int iconResource;
    
    @SerializedName("color")
    private String color;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("order")
    private int order;
    
    @SerializedName("is_visible")
    private boolean isVisible;
    
    @SerializedName("menu_items")
    private List<DashboardMenuItem> menuItems;

    public DashboardCategory(String id, String name, int iconResource, String color, int order) {
        this.id = id;
        this.name = name;
        this.iconResource = iconResource;
        this.color = color;
        this.order = order;
        this.isVisible = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public List<DashboardMenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<DashboardMenuItem> menuItems) {
        this.menuItems = menuItems;
    }
} 