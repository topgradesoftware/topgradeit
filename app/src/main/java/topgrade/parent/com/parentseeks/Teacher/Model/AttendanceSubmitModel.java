package topgrade.parent.com.parentseeks.Teacher.Model;

public class AttendanceSubmitModel {

    String attendance;
    String note;
    String child_id;
    String section_id;
    String parent_id;
    String sms;


    public AttendanceSubmitModel(String attendance, String note, String child_id, String section_id, String parent_id, String sms) {
        this.attendance = attendance;
        this.note = note;
        this.child_id = child_id;
        this.section_id = section_id;
        this.parent_id = parent_id;
        this.sms = sms;
    }

    public String getAttendance() {
        return attendance;
    }

    public void setAttendance(String attendance) {
        this.attendance = attendance;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getChild_id() {
        return child_id;
    }

    public void setChild_id(String child_id) {
        this.child_id = child_id;
    }

    public String getSection_id() {
        return section_id;
    }

    public void setSection_id(String section_id) {
        this.section_id = section_id;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }
}
