package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class StatusModel {

    @SerializedName("status")
    @Expose
    public SharedStatus status;
}
