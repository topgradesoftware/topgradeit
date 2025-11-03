package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class LeaveApplicationResponse {
    
    @SerializedName("status")
    @Expose
    private SharedStatus status;
    
    @SerializedName("data")
    @Expose
    private List<LeaveApplicationData> data;
    
    @SerializedName("categories")
    @Expose
    private List<CategoryModel> categories;
    
    public SharedStatus getStatus() {
        return status;
    }
    
    public void setStatus(SharedStatus status) {
        this.status = status;
    }
    
    public List<LeaveApplicationData> getData() {
        return data;
    }
    
    public void setData(List<LeaveApplicationData> data) {
        this.data = data;
    }
    
    public List<CategoryModel> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategoryModel> categories) {
        this.categories = categories;
    }
    
    public static class LeaveApplicationData {
        @SerializedName("campus_id")
        @Expose
        private String campusId;
        
        @SerializedName("staff_id")
        @Expose
        private String staffId;
        
        @SerializedName("title")
        @Expose
        private String title;
        
        @SerializedName("body")
        @Expose
        private String body;
        
        @SerializedName("start_date")
        @Expose
        private String startDate;
        
        @SerializedName("end_date")
        @Expose
        private String endDate;
        
        @SerializedName("is_active")
        @Expose
        private String isActive;
        
        @SerializedName("timestamp")
        @Expose
        private String timestamp;
        
        @SerializedName("title_id")
        @Expose
        private String titleId;
        
        public String getCampusId() {
            return campusId;
        }
        
        public void setCampusId(String campusId) {
            this.campusId = campusId;
        }
        
        public String getStaffId() {
            return staffId;
        }
        
        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getBody() {
            return body;
        }
        
        public void setBody(String body) {
            this.body = body;
        }
        
        public String getStartDate() {
            return startDate;
        }
        
        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }
        
        public String getEndDate() {
            return endDate;
        }
        
        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
        
        public String getIsActive() {
            return isActive;
        }
        
        public void setIsActive(String isActive) {
            this.isActive = isActive;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getTitleId() {
            return titleId;
        }
        
        public void setTitleId(String titleId) {
            this.titleId = titleId;
        }
    }
}
