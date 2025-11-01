package topgrade.parent.com.parentseeks.Teacher.Model;

public class ExamSubmitStrcu {

    String attendance;
    String note;
    String child_id;
    String sms;
    String marks;
    String delete;


    public ExamSubmitStrcu(String attendance, String note, String child_id, String sms, String marks, String delete) {
        this.attendance = attendance;
        this.note = note;
        this.child_id = child_id;
        this.sms = sms;
        this.marks = marks;
        this.delete = delete;
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

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }
}
