package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CategoryModel {
    
    @SerializedName("category_id")
    @Expose
    private String categoryId;
    
    @SerializedName("full_name")
    @Expose
    private String fullName;
    
    public String getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

