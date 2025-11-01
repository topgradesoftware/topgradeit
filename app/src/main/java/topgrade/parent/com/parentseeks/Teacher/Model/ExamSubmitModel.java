package topgrade.parent.com.parentseeks.Teacher.Model;

public class ExamSubmitModel {

    private String sms;
    private String rollnumber;
    private String name;
    private String obtainmarks;
    private String totalMarks;
    private String status;
    private String note;

    public ExamSubmitModel(String sms, String rollnumber, String name,
                           String obtainmarks, String totalMarks, String status, String note) {
        this.sms = sms;
        this.rollnumber = rollnumber;
        this.name = name;
        this.obtainmarks = obtainmarks;
        this.totalMarks = totalMarks;
        this.status = status;
        this.note = note;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getRollnumber() {
        return rollnumber;
    }

    public void setRollnumber(String rollnumber) {
        this.rollnumber = rollnumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObtainmarks() {
        return obtainmarks;
    }

    public void setObtainmarks(String obtainmarks) {
        this.obtainmarks = obtainmarks;
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
