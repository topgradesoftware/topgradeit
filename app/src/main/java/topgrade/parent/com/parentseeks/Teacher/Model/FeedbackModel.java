package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;public class FeedbackModel {
    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("feedback")
    @Expose
    private List<Feedback> feedback = null;





    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<Feedback> getFeedback() {
        return feedback;
    }

    public void setFeedback(List<Feedback> feedback) {
        this.feedback = feedback;
    }


}
