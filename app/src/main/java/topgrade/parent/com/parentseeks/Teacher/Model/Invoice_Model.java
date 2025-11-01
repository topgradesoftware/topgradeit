package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class Invoice_Model {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("invoice_items")
    @Expose
    private List<InvoiceItem> invoiceItems = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<InvoiceItem> getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(List<InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    public static class InvoiceItem {

        @SerializedName("serial")
        @Expose
        private String serial;

        @SerializedName("description")
        @Expose
        private String description;

        @SerializedName("debit")
        @Expose
        private String debit;

        @SerializedName("credit")
        @Expose
        private String credit;

        @SerializedName("date")
        @Expose
        private String date;

        // Constructor
        public InvoiceItem(String serial, String description, String debit, String credit, String date) {
            this.serial = serial;
            this.description = description;
            this.debit = debit;
            this.credit = credit;
            this.date = date;
        }

        // Default constructor
        public InvoiceItem() {}

        // Getters and Setters
        public String getSerial() {
            return serial;
        }

        public void setSerial(String serial) {
            this.serial = serial;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDebit() {
            return debit;
        }

        public void setDebit(String debit) {
            this.debit = debit;
        }

        public String getCredit() {
            return credit;
        }

        public void setCredit(String credit) {
            this.credit = credit;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}
