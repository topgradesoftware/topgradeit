package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class Diary {

    @SerializedName("status")
    @Expose
    private StatusModel status;

    @SerializedName("title")
    @Expose
    private Map<String, DiaryEntry> title;

    public StatusModel getStatus() {
        return status;
    }

    public void setStatus(StatusModel status) {
        this.status = status;
    }

    public Map<String, DiaryEntry> getTitle() {
        return title;
    }

    public void setTitle(Map<String, DiaryEntry> title) {
        this.title = title;
    }
} 