package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ExamSession implements Serializable {
    
    private static final long serialVersionUID = 1L;


    @SerializedName("unique_id")
    @Expose
    private String uniqueId;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("display_order")
    @Expose
    private Integer displayOrder;

    public ExamSession(String uniqueId, String fullName, Integer displayOrder) {
        this.uniqueId = uniqueId;
        this.fullName = fullName;
        this.displayOrder = displayOrder;
    }


    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;

    }

}
