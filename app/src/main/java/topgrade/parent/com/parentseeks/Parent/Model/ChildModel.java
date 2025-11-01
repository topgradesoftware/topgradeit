
package topgrade.parent.com.parentseeks.Parent.Model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;

public class ChildModel {

    @SerializedName("status")
    @Expose
    private SharedStatus status;
    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("students")
    @Expose
    private List<SharedStudent> students = null;

    /**
     * No args constructor for use in serialization
     */
    public ChildModel() {
    }

    /**
     * @param students
     * @param status
     * @param data
     */
    public ChildModel(SharedStatus status, Data data, List<SharedStudent> students) {
        super();
        this.status = status;
        this.data = data;
        this.students = students;
    }

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

    public List<SharedStudent> getStudents() {
        return students;
    }

    public void setStudents(List<SharedStudent> students) {
        this.students = students;
    }

}
