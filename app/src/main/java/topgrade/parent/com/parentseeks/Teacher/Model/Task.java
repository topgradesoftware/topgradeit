package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Task {

    @SerializedName("task_title")
    @Expose
    private String taskTitle;

    @SerializedName("response")
    @Expose
    private String response;

    @SerializedName("task_body")
    @Expose
    private String taskBody;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("task_id")
    @Expose
    private String taskId;
    @SerializedName("is_completed")
    @Expose
    private String isCompleted;


    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskBody() {
        return taskBody;
    }

    public String getTaskResponse() {
        return response;
    }


    public void setTaskBody(String taskBody) {
        this.taskBody = taskBody;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(String isCompleted) {
        this.isCompleted = isCompleted;
    }
}
