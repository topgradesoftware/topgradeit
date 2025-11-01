package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class ComplaintModel {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("data")
    @Expose
    private List<Complaint> data = null;

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

        @SerializedName("staff_id")
        @Expose
        private String staffId;

        @SerializedName("staff_name")
        @Expose
        private String staffName;

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

        public String getStaffId() {
            return staffId;
        }

        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }

        public String getStaffName() {
            return staffName;
        }

        public void setStaffName(String staffName) {
            this.staffName = staffName;
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
}
