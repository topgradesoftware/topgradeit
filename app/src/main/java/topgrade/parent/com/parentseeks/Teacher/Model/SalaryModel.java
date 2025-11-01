package topgrade.parent.com.parentseeks.Teacher.Model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;public class SalaryModel {
    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("salary")
    @Expose
    private List<Salary> salary = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }


    public List<Salary> getSalary() {
        return salary;
    }

    public void setSalary(List<Salary> salary) {
        this.salary = salary;
    }


    public class Salary {

        @SerializedName("unique_id")
        @Expose
        private String uniqueId;
        @SerializedName("is_active")
        @Expose
        private Integer isActive;
        @SerializedName("salary_month_id")
        @Expose
        private String salaryMonthId;
        @SerializedName("staff_id")
        @Expose
        private String staffId;
        @SerializedName("salary_id")
        @Expose
        private Integer salaryId;
        @SerializedName("start_date")
        @Expose
        private String startDate;
        @SerializedName("amount")
        @Expose
        private Integer amount;
        @SerializedName("incentive")
        @Expose
        private Integer incentive;
        @SerializedName("previous_balance")
        @Expose
        private Integer previousBalance;
        @SerializedName("paid_date")
        @Expose
        private String paidDate;
        @SerializedName("staff_type")
        @Expose
        private Integer staffType;
        @SerializedName("display_order")
        @Expose
        private Integer displayOrder;
        @SerializedName("full_name")
        @Expose
        private String fullName;
        @SerializedName("end_date")
        @Expose
        private String endDate;
        @SerializedName("timestamporder")
        @Expose
        private Integer timestamporder;
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
        @SerializedName("deduction")
        @Expose
        private Integer deduction;
        @SerializedName("locked")
        @Expose
        private Integer locked;
        @SerializedName("advance")
        @Expose
        private Integer advance;
        @SerializedName("lecture_assigned")
        @Expose
        private String lectureAssigned;
        @SerializedName("lecture_attended")
        @Expose
        private String lectureAttended;
        @SerializedName("days")
        @Expose
        private Integer days;
        @SerializedName("per_day_rate")
        @Expose
        private String perDayRate;
        @SerializedName("presents")
        @Expose
        private Integer presents;
        @SerializedName("absents")
        @Expose
        private Integer absents;
        @SerializedName("leaves")
        @Expose
        private Integer leaves;
        @SerializedName("half_leaves")
        @Expose
        private Integer halfLeaves;
        @SerializedName("lates")
        @Expose
        private Integer lates;
        @SerializedName("salary_month_name")
        @Expose
        private String salaryMonthName;
        @SerializedName("items")
        @Expose
        private List<Item> items = null;


        public String getLectureAssigned() {
            return lectureAssigned;
        }

        public void setLectureAssigned(String lectureAssigned) {
            this.lectureAssigned = lectureAssigned;
        }

        public String getLectureAttended() {
            return lectureAttended;
        }

        public void setLectureAttended(String lectureAttended) {
            this.lectureAttended = lectureAttended;
        }

        public Integer getDays() {
            return days;
        }

        public void setDays(Integer days) {
            this.days = days;
        }

        public String getPerDayRate() {
            return perDayRate;
        }

        public void setPerDayRate(String perDayRate) {
            this.perDayRate = perDayRate;
        }

        public Integer getPresents() {
            return presents;
        }

        public void setPresents(Integer presents) {
            this.presents = presents;
        }

        public Integer getAbsents() {
            return absents;
        }

        public void setAbsents(Integer absents) {
            this.absents = absents;
        }

        public Integer getLeaves() {
            return leaves;
        }

        public void setLeaves(Integer leaves) {
            this.leaves = leaves;
        }

        public Integer getHalfLeaves() {
            return halfLeaves;
        }

        public void setHalfLeaves(Integer halfLeaves) {
            this.halfLeaves = halfLeaves;
        }

        public Integer getLates() {
            return lates;
        }

        public void setLates(Integer lates) {
            this.lates = lates;
        }

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

        public String getSalaryMonthId() {
            return salaryMonthId;
        }

        public void setSalaryMonthId(String salaryMonthId) {
            this.salaryMonthId = salaryMonthId;
        }

        public String getStaffId() {
            return staffId;
        }

        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }

        public Integer getSalaryId() {
            return salaryId;
        }

        public void setSalaryId(Integer salaryId) {
            this.salaryId = salaryId;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public Integer getIncentive() {
            return incentive;
        }

        public void setIncentive(Integer incentive) {
            this.incentive = incentive;
        }

        public Integer getPreviousBalance() {
            return previousBalance;
        }

        public void setPreviousBalance(Integer previousBalance) {
            this.previousBalance = previousBalance;
        }

        public String getPaidDate() {
            return paidDate;
        }

        public void setPaidDate(String paidDate) {
            this.paidDate = paidDate;
        }

        public Integer getStaffType() {
            return staffType;
        }

        public void setStaffType(Integer staffType) {
            this.staffType = staffType;
        }

        public Integer getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public Integer getTimestamporder() {
            return timestamporder;
        }

        public void setTimestamporder(Integer timestamporder) {
            this.timestamporder = timestamporder;
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

        public Integer getDeduction() {
            return deduction;
        }

        public void setDeduction(Integer deduction) {
            this.deduction = deduction;
        }

        public Integer getLocked() {
            return locked;
        }

        public void setLocked(Integer locked) {
            this.locked = locked;
        }

        public Integer getAdvance() {
            return advance;
        }

        public void setAdvance(Integer advance) {
            this.advance = advance;
        }

        public String getSalaryMonthName() {
            return salaryMonthName;
        }

        public void setSalaryMonthName(String salaryMonthName) {
            this.salaryMonthName = salaryMonthName;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

    }


    public class Item {

        @SerializedName("salary_head_id")
        @Expose
        private String salaryHeadId;
        @SerializedName("amount")
        @Expose
        private Integer amount;
        @SerializedName("salary_head_name")
        @Expose
        private String salaryHeadName;

        public String getSalaryHeadId() {
            return salaryHeadId;
        }

        public void setSalaryHeadId(String salaryHeadId) {
            this.salaryHeadId = salaryHeadId;
        }

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public String getSalaryHeadName() {
            return salaryHeadName;
        }

        public void setSalaryHeadName(String salaryHeadName) {
            this.salaryHeadName = salaryHeadName;
        }

    }

}