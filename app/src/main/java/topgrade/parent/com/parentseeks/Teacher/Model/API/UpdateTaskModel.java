package topgrade.parent.com.parentseeks.Teacher.Model.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class UpdateTaskModel {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }
}

