package topgrade.parent.com.parentseeks.Shared.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Shared Status model for use across Parent and Teacher modules
 * Consolidates duplicate Status classes to prevent conflicts
 */
public class SharedStatus {

    @SerializedName("status")
    @Expose
    private String status;
    
    @SerializedName("message")
    @Expose
    private String message;
    
    @SerializedName("code")
    @Expose
    private String code;
    
    @SerializedName("data")
    @Expose
    private Object data;

    public SharedStatus() {
    }

    public SharedStatus(String status, String message, String code, Object data) {
        this.status = status;
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SharedStatus{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", data=" + data +
                '}';
    }
}
