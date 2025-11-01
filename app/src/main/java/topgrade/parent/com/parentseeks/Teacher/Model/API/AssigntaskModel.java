package topgrade.parent.com.parentseeks.Teacher.Model.API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;
import topgrade.parent.com.parentseeks.Teacher.Model.Task;

public class AssigntaskModel {


    @SerializedName("status")
    @Expose
    private SharedStatus status;


    @SerializedName("task")
    @Expose
    private List<Task> task = null;


    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<Task> getTask() {
        return task;
    }

    public void setTask(List<Task> task) {
        this.task = task;
    }
}
