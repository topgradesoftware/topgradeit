package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Diary {

    @SerializedName("status")
    @Expose
    private StatusModel status;

    @SerializedName("title")
    @Expose
    private Map<String, DiaryEntry> title;
    
    @SerializedName("data")
    @Expose
    private List<Object> data; // API sometimes returns "data" instead of "title"

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
    
    public List<Object> getData() {
        return data;
    }
    
    public void setData(List<Object> data) {
        this.data = data;
    }
} 