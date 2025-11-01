package topgrade.parent.com.parentseeks.Teacher.Model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;public class AdvancedSalaryModel {
    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("advance")
    @Expose
    private List<Advanced> advance = null;

    public SharedStatus getStatus() {
        return status;
    }

    public List<Advanced> getAdvance() {
        return advance;
    }

    public void setAdvance(List<Advanced> advance) {
        this.advance = advance;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }


    public class Advanced {


        @SerializedName("unique_id")
        @Expose
        private String uniqueId;
        @SerializedName("full_name")
        @Expose
        private String fullName;
        @SerializedName("session_id")
        @Expose
        private String sessionId;
        @SerializedName("staff_id")
        @Expose
        private String staffId;
        @SerializedName("salary_month_id")
        @Expose
        private String salaryMonthId;
        @SerializedName("invoice_id")
        @Expose
        private Integer invoiceId;
        @SerializedName("amount")
        @Expose
        private Integer amount;
        @SerializedName("is_active")
        @Expose
        private Integer isActive;
        @SerializedName("deduction")
        @Expose
        private Integer deduction;
        @SerializedName("created_date")
        @Expose
        private String createdDate;
        @SerializedName("timestamp")
        @Expose
        private String timestamp;
        @SerializedName("salary_month_name")
        @Expose
        private String salaryMonthName;

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

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getStaffId() {
            return staffId;
        }

        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }

        public String getSalaryMonthId() {
            return salaryMonthId;
        }

        public void setSalaryMonthId(String salaryMonthId) {
            this.salaryMonthId = salaryMonthId;
        }

        public Integer getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(Integer invoiceId) {
            this.invoiceId = invoiceId;
        }

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public Integer getIsActive() {
            return isActive;
        }

        public void setIsActive(Integer isActive) {
            this.isActive = isActive;
        }

        public Integer getDeduction() {
            return deduction;
        }

        public void setDeduction(Integer deduction) {
            this.deduction = deduction;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getSalaryMonthName() {
            return salaryMonthName;
        }

        public void setSalaryMonthName(String salaryMonthName) {
            this.salaryMonthName = salaryMonthName;
        }
    }


}