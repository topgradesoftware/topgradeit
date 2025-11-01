package topgrade.parent.com.parentseeks.Parent.Model;

/**
 * Parent-specific attendance model for simple attendance tracking
 * Renamed from ParentAttendanceModel to avoid conflicts with Teacher module
 */
public class ParentAttendanceModel {

    private String created_date;
    private String note;
    private String attendance;

    public ParentAttendanceModel(String created_date, String note, String attendance) {
        this.created_date = created_date;
        this.note = note;
        this.attendance = attendance;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getAttendance() {
        return attendance;
    }

    public void setAttendance(String attendance) {
        this.attendance = attendance;
    }
}
