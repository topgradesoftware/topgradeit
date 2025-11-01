package topgrade.parent.com.parentseeks.Teacher.Model.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Teacher.Model.Complain;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class Complain_title_List {


    @SerializedName("status")
    @Expose
    private SharedStatus status;


    @SerializedName("complain_title")
    @Expose
    private List<Complain> complain_title = null;


    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<Complain> getComplain_title() {
        return complain_title;
    }

    public void setComplain_title(List<Complain> complain_title) {
        this.complain_title = complain_title;
    }


}
