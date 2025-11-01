package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("attendence")
    @Expose
    private String attendence;
    @SerializedName("obtained_marks")
    @Expose
    private String obtained_marks;
    @SerializedName("note")
    @Expose
    private String note;

    public String getAttendance() {
        return attendence;
    }

    public void setAttendance(String attendence) {
        this.attendence = attendence;
    }

    public String getObtained_marks() {
        return obtained_marks;
    }

    public void setObtained_marks(String obtained_marks) {
        this.obtained_marks = obtained_marks;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }



}
