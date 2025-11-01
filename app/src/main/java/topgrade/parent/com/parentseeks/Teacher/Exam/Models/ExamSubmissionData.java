package topgrade.parent.com.parentseeks.Teacher.Exam.Models;

/**
 * ExamSubmissionData - Represents individual student exam submission data
 * Contains all the information needed for a single student's exam submission
 */
public class ExamSubmissionData {
    private String studentId;
    private String studentName;
    private String studentRollNumber;
    private String studentPicture;
    private String obtainedMarks;
    private String totalMarks;
    private String note;
    private boolean smsEnabled;
    private String parentId;
    private String sectionId;

    public ExamSubmissionData() {
        // Default constructor
    }

    public ExamSubmissionData(String studentId, String studentName, String studentRollNumber, 
                             String totalMarks, String parentId, String sectionId) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentRollNumber = studentRollNumber;
        this.totalMarks = totalMarks;
        this.parentId = parentId;
        this.sectionId = sectionId;
        this.smsEnabled = false; // Default to false
        this.obtainedMarks = ""; // Empty by default
        this.note = ""; // Empty by default
    }

    // MARK: - Getters and Setters

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getStudentPicture() {
        return studentPicture;
    }

    public void setStudentPicture(String studentPicture) {
        this.studentPicture = studentPicture;
    }

    public String getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(String obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    // MARK: - Validation Methods

    public boolean hasMarks() {
        return obtainedMarks != null && !obtainedMarks.isEmpty();
    }

    public boolean isValidMarks() {
        if (!hasMarks()) return false;
        
        try {
            int obtained = Integer.parseInt(obtainedMarks);
            int total = Integer.parseInt(totalMarks);
            return obtained >= 0 && obtained <= total;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public double getPercentage() {
        if (!hasMarks() || totalMarks == null || totalMarks.isEmpty()) {
            return 0.0;
        }
        
        try {
            double obtained = Double.parseDouble(obtainedMarks);
            double total = Double.parseDouble(totalMarks);
            return (obtained / total) * 100.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public String getGrade() {
        double percentage = getPercentage();
        
        if (percentage >= 90) return "A+";
        else if (percentage >= 80) return "A";
        else if (percentage >= 70) return "B+";
        else if (percentage >= 60) return "B";
        else if (percentage >= 50) return "C+";
        else if (percentage >= 40) return "C";
        else return "F";
    }

    // MARK: - Utility Methods

    public void reset() {
        obtainedMarks = "";
        note = "";
        smsEnabled = false;
    }

    @Override
    public String toString() {
        return "ExamSubmissionData{" +
                "studentId='" + studentId + '\'' +
                ", studentName='" + studentName + '\'' +
                ", obtainedMarks='" + obtainedMarks + '\'' +
                ", totalMarks='" + totalMarks + '\'' +
                ", smsEnabled=" + smsEnabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ExamSubmissionData that = (ExamSubmissionData) o;
        return studentId != null ? studentId.equals(that.studentId) : that.studentId == null;
    }

    @Override
    public int hashCode() {
        return studentId != null ? studentId.hashCode() : 0;
    }
}
