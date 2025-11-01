package topgrade.parent.com.parentseeks.Student.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class StudentComplaintModel {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("data")
    @Expose
    private List<Complaint> data = null;

    @SerializedName("titles")
    @Expose
    private List<ComplaintTitle> titles = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<Complaint> getData() {
        return data;
    }

    public void setData(List<Complaint> data) {
        this.data = data;
    }

    public List<ComplaintTitle> getTitles() {
        return titles;
    }

    public void setTitles(List<ComplaintTitle> titles) {
        this.titles = titles;
    }

    public static class Complaint {

        @SerializedName("complaint_id")
        @Expose
        private String complaintId;

        @SerializedName("complaint_title")
        @Expose
        private String complaintTitle;

        @SerializedName("complaint_description")
        @Expose
        private String complaintDescription;

        @SerializedName("complaint_status")
        @Expose
        private String complaintStatus;

        @SerializedName("complaint_date")
        @Expose
        private String complaintDate;

        @SerializedName("student_id")
        @Expose
        private String studentId;

        @SerializedName("student_name")
        @Expose
        private String studentName;

        @SerializedName("response")
        @Expose
        private String response;

        @SerializedName("response_date")
        @Expose
        private String responseDate;

        public String getComplaintId() {
            return complaintId;
        }

        public void setComplaintId(String complaintId) {
            this.complaintId = complaintId;
        }

        public String getComplaintTitle() {
            return complaintTitle;
        }

        public void setComplaintTitle(String complaintTitle) {
            this.complaintTitle = complaintTitle;
        }

        public String getComplaintDescription() {
            return complaintDescription;
        }

        public void setComplaintDescription(String complaintDescription) {
            this.complaintDescription = complaintDescription;
        }

        public String getComplaintStatus() {
            return complaintStatus;
        }

        public void setComplaintStatus(String complaintStatus) {
            this.complaintStatus = complaintStatus;
        }

        public String getComplaintDate() {
            return complaintDate;
        }

        public void setComplaintDate(String complaintDate) {
            this.complaintDate = complaintDate;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public String getResponseDate() {
            return responseDate;
        }

        public void setResponseDate(String responseDate) {
            this.responseDate = responseDate;
        }
    }

    public static class ComplaintTitle {

        @SerializedName("title_id")
        @Expose
        private String titleId;

        @SerializedName("title")
        @Expose
        private String title;

        @SerializedName("is_active")
        @Expose
        private String isActive;

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

        public String getIsActive() {
            return isActive;
        }

        public void setIsActive(String isActive) {
            this.isActive = isActive;
        }
    }
}

