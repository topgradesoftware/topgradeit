package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;public class TimetableSessionModel {

    @SerializedName("status")
    @Expose
    private SharedStatus status;

    @SerializedName("timetable_session")
    @Expose
    private List<TimetableSession> timetable_session = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<TimetableSession> getTimetableSession() {
        return timetable_session;
    }

    public void setTimetableSession(List<TimetableSession> timetableSession) {
        this.timetable_session = timetableSession;
    }
}
