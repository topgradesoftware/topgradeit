
package topgrade.parent.com.parentseeks.Parent.Model.New;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {

    @SerializedName("fee_particular_id")
    @Expose
    private String feeParticularId;
    @SerializedName("amount")
    @Expose
    private Integer amount;
    @SerializedName("discount")
    @Expose
    private Integer discount;
    @SerializedName("payable")
    @Expose
    private Integer payable;
    @SerializedName("paid")
    @Expose
    private Integer paid;
    @SerializedName("remaining")
    @Expose
    private Integer remaining;
    @SerializedName("previous_balance")
    @Expose
    private Integer previousBalance;
    @SerializedName("fee_particular_name")
    @Expose
    private String feeParticularName;

    public String getFeeParticularId() {
        return feeParticularId;
    }

    public void setFeeParticularId(String feeParticularId) {
        this.feeParticularId = feeParticularId;
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

    public Integer getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(Integer previousBalance) {
        this.previousBalance = previousBalance;
    }

    public String getFeeParticularName() {
        return feeParticularName;
    }

    public void setFeeParticularName(String feeParticularName) {
        this.feeParticularName = feeParticularName;
    }

}
