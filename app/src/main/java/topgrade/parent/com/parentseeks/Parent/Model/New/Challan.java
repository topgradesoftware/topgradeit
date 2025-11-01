
package topgrade.parent.com.parentseeks.Parent.Model.New;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Challan {

    @SerializedName("unique_id")
    @Expose
    private String uniqueId;
    @SerializedName("is_active")
    @Expose
    private Integer isActive;
    @SerializedName("chalan_id")
    @Expose
    private Integer chalanId;
    @SerializedName("employee_id")
    @Expose
    private String employeeId;
    @SerializedName("student_class_id")
    @Expose
    private String studentClassId;
    @SerializedName("section_id")
    @Expose
    private String sectionId;
    @SerializedName("fee_particular_recurring_id")
    @Expose
    private String feeParticularRecurringId;
    @SerializedName("due_date")
    @Expose
    private String dueDate;
    @SerializedName("fine")
    @Expose
    private Integer fine;
    @SerializedName("amount")
    @Expose
    private Integer amount;
    @SerializedName("discount")
    @Expose
    private Integer discount;
    @SerializedName("previous_balance")
    @Expose
    private Integer previousBalance;
    @SerializedName("section_name")
    @Expose
    private String sectionName;
    @SerializedName("paid_date")
    @Expose
    private String paidDate;
    @SerializedName("validity_date")
    @Expose
    private String validityDate;
    @SerializedName("payable")
    @Expose
    private Integer payable;
    @SerializedName("paid")
    @Expose
    private Integer paid;
    @SerializedName("remaining")
    @Expose
    private Integer remaining;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("locked")
    @Expose
    private Integer locked;
    @SerializedName("fprorder")
    @Expose
    private Integer fprorder;
    @SerializedName("chalanorder")
    @Expose
    private Integer chalanorder;
    @SerializedName("classorder")
    @Expose
    private Integer classorder;
    @SerializedName("timestamporder")
    @Expose
    private Integer timestamporder;
    @SerializedName("per_day_fine")
    @Expose
    private Integer perDayFine;
    @SerializedName("fine_paid")
    @Expose
    private Integer finePaid;
    @SerializedName("student_name")
    @Expose
    private String studentName;
    @SerializedName("class_name")
    @Expose
    private String className;
    @SerializedName("roll_number")
    @Expose
    private Integer rollNumber;
    @SerializedName("picture")
    @Expose
    private String picture;
    @SerializedName("advance")
    @Expose
    private Integer advance;
    @SerializedName("fine_discount")
    @Expose
    private Integer fineDiscount;
    @SerializedName("registration_number")
    @Expose
    private Integer registrationNumber;
    @SerializedName("parent")
    @Expose
    private String parent;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("fee_particular_recurring_name")
    @Expose
    private String feeParticularRecurringName;
    @SerializedName("items")
    @Expose
    private List<Item> items = null;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Integer getChalanId() {
        return chalanId;
    }

    public void setChalanId(Integer chalanId) {
        this.chalanId = chalanId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(String studentClassId) {
        this.studentClassId = studentClassId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getFeeParticularRecurringId() {
        return feeParticularRecurringId;
    }

    public void setFeeParticularRecurringId(String feeParticularRecurringId) {
        this.feeParticularRecurringId = feeParticularRecurringId;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getFine() {
        return fine;
    }

    public void setFine(Integer fine) {
        this.fine = fine;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Integer getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(Integer previousBalance) {
        this.previousBalance = previousBalance;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(String paidDate) {
        this.paidDate = paidDate;
    }

    public String getValidityDate() {
        return validityDate;
    }

    public void setValidityDate(String validityDate) {
        this.validityDate = validityDate;
    }

    public Integer getPayable() {
        return payable;
    }

    public void setPayable(Integer payable) {
        this.payable = payable;
    }

    public Integer getPaid() {
        return paid;
    }

    public void setPaid(Integer paid) {
        this.paid = paid;
    }

    public Integer getRemaining() {
        return remaining;
    }

    public void setRemaining(Integer remaining) {
        this.remaining = remaining;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getLocked() {
        return locked;
    }

    public void setLocked(Integer locked) {
        this.locked = locked;
    }

    public Integer getFprorder() {
        return fprorder;
    }

    public void setFprorder(Integer fprorder) {
        this.fprorder = fprorder;
    }

    public Integer getChalanorder() {
        return chalanorder;
    }

    public void setChalanorder(Integer chalanorder) {
        this.chalanorder = chalanorder;
    }

    public Integer getClassorder() {
        return classorder;
    }

    public void setClassorder(Integer classorder) {
        this.classorder = classorder;
    }

    public Integer getTimestamporder() {
        return timestamporder;
    }

    public void setTimestamporder(Integer timestamporder) {
        this.timestamporder = timestamporder;
    }

    public Integer getPerDayFine() {
        return perDayFine;
    }

    public void setPerDayFine(Integer perDayFine) {
        this.perDayFine = perDayFine;
    }

    public Integer getFinePaid() {
        return finePaid;
    }

    public void setFinePaid(Integer finePaid) {
        this.finePaid = finePaid;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(Integer rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Integer getAdvance() {
        return advance;
    }

    public void setAdvance(Integer advance) {
        this.advance = advance;
    }

    public Integer getFineDiscount() {
        return fineDiscount;
    }

    public void setFineDiscount(Integer fineDiscount) {
        this.fineDiscount = fineDiscount;
    }

    public Integer getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(Integer registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFeeParticularRecurringName() {
        return feeParticularRecurringName;
    }

    public void setFeeParticularRecurringName(String feeParticularRecurringName) {
        this.feeParticularRecurringName = feeParticularRecurringName;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

}
