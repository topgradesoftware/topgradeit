package topgrade.parent.com.parentseeks.Teacher.Model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;public class LegderModel {
    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("invoice")
    @Expose
    private List<Invoice> invoice = null;

    public SharedStatus getStatus() {
        return status;
    }

    public List<Invoice> getInvoice() {
        return invoice;
    }

    public void setInvoice(List<Invoice> invoice) {
        this.invoice = invoice;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }


    public class Invoice {


        @SerializedName("unique_id")
        @Expose
        private String uniqueId;
        @SerializedName("full_name")
        @Expose
        private String fullName;
        @SerializedName("invoice_id")
        @Expose
        private Integer invoiceId;
        @SerializedName("staff_id")
        @Expose
        private String staffId;
        @SerializedName("total")
        @Expose
        private String total;
        @SerializedName("is_type")
        @Expose
        private String isType;
        @SerializedName("created_date")
        @Expose
        private String createdDate;
        @SerializedName("is_active")
        @Expose
        private Integer isActive;
        @SerializedName("timestamp")
        @Expose
        private String timestamp;

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

        public Integer getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(Integer invoiceId) {
            this.invoiceId = invoiceId;
        }

        public String getStaffId() {
            return staffId;
        }

        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getIsType() {
            return isType;
        }

        public void setIsType(String isType) {
            this.isType = isType;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }

        public Integer getIsActive() {
            return isActive;
        }

        public void setIsActive(Integer isActive) {
            this.isActive = isActive;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
