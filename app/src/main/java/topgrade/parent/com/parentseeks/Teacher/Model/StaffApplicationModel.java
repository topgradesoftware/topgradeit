package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class StaffApplicationModel {
    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("data")
    @Expose
    private List<Application> applications = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public static class Application {
        @SerializedName("title_id")
        @Expose
        private String titleId;

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

        @SerializedName("created_date")
        @Expose
        private String createdDate;

        public String getTitleId() {
            return titleId;
        }

        public void setTitleId(String titleId) {
            this.titleId = titleId;
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

        public String getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }
    }
}
