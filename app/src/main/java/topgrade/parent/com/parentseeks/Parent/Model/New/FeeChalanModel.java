
package topgrade.parent.com.parentseeks.Parent.Model.New;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;
import topgrade.parent.com.parentseeks.Parent.Model.Data;

public class FeeChalanModel {

    @SerializedName("status")
    @Expose
    private SharedStatus status;
    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("challan")
    @Expose
    private List<Challan> challan = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public List<Challan> getChallan() {
        return challan;
    }

    public void setChallan(List<Challan> challan) {
        this.challan = challan;
    }


}
