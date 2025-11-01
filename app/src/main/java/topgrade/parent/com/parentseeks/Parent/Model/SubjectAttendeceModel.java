package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;public class SubjectAttendeceModel {


    @SerializedName("status")
    @Expose
    private SharedStatus status;
    @SerializedName("attendance_list")
    @Expose
    private List<AttendanceList> attendanceList = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public List<AttendanceList> getAttendanceList() {
        return attendanceList;
    }

    public void setAttendanceList(List<AttendanceList> attendanceList) {
        this.attendanceList = attendanceList;
    }


    public class Attendance {

        @SerializedName("created_date")
        @Expose
        private String createdDate;
        @SerializedName("note")
        @Expose
        private Object note;
        @SerializedName("attendance")
        @Expose
        private String attendance;

        public String getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }

        public Object getNote() {
            return note;
        }

        public void setNote(Object note) {
            this.note = note;
        }

        public String getAttendance() {
            return attendance;
        }

        public void setAttendance(String attendance) {
            this.attendance = attendance;
        }

    }


    public class AttendanceList {

        @SerializedName("subject")
        @Expose
        private String subject;
        @SerializedName("attendence")
        @Expose
        private List<Attendance> attendence = null;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public List<Attendance> getAttendance() {
            return attendence;
        }

        public void setAttendance(List<Attendance> attendence) {
            this.attendence = attendence;
        }

    }

}
