package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;

public class TimetableModel {
    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("timetable")
    @Expose
    List<Timetable> timetable = null;


    @SerializedName("timetable_sms")
    @Expose
    String timetable_sms;


    public String getTimetable_sms() {
        return timetable_sms;
    }

    public void setTimetable_sms(String timetable_sms) {
        this.timetable_sms = timetable_sms;
    }

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<Timetable> getTimetable() {
        return timetable;
    }

    public void setTimetable(List<Timetable> timetable) {
        this.timetable = timetable;
    }
}
