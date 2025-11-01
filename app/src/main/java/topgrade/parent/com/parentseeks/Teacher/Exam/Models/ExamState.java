package topgrade.parent.com.parentseeks.Teacher.Exam.Models;

import java.util.List;

/**
 * ExamState - Holds the current state of exam submission
 * Encapsulates all the data needed for exam submission process
 */
public class ExamState {
    private String sessionId;
    private String classId;
    private String sectionId;
    private String subjectId;
    private String examId;
    private boolean globalSmsEnabled;
    private List<ExamSubmissionData> examSubmissionData;

    public ExamState() {
        // Initialize with empty state
    }

    // MARK: - Getters and Setters

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public boolean isGlobalSmsEnabled() {
        return globalSmsEnabled;
    }

    public void setGlobalSmsEnabled(boolean globalSmsEnabled) {
        this.globalSmsEnabled = globalSmsEnabled;
    }

    public List<ExamSubmissionData> getExamSubmissionData() {
        return examSubmissionData;
    }

    public void setExamSubmissionData(List<ExamSubmissionData> examSubmissionData) {
        this.examSubmissionData = examSubmissionData;
    }

    // MARK: - Validation Methods

    /**
     * Checks if all the necessary exam fields are populated
     */
    public boolean isComplete() {
        return sessionId != null && !sessionId.isEmpty() &&
               classId != null && !classId.isEmpty() &&
               sectionId != null && !sectionId.isEmpty() &&
               subjectId != null && !subjectId.isEmpty() &&
               examId != null && !examId.isEmpty();
    }

    /**
     * Checks if there are students in the submission data
     */
    public boolean hasStudents() {
        return examSubmissionData != null && !examSubmissionData.isEmpty();
    }

    /**
     * Validates if all students have marks entered
     */
    public boolean allMarksEntered() {
        if (examSubmissionData == null) return false;

        for (ExamSubmissionData data : examSubmissionData) {
            if (data.getObtainedMarks() == null || data.getObtainedMarks().isEmpty() || Integer.parseInt(data.getObtainedMarks()) < 0) {
                return false;
            }
        }
        return true;
    }

    // MARK: - Utility Methods

    /**
     * Resets the state of the exam submission
     */
    public void reset(boolean resetSms) {
        sessionId = null;
        classId = null;
        sectionId = null;
        subjectId = null;
        examId = null;
        if (resetSms) {
            globalSmsEnabled = false;
        }
        examSubmissionData = null;
    }

    @Override
    public String toString() {
        return "ExamState{" +
                "sessionId='" + sessionId + '\'' +
                ", classId='" + classId + '\'' +
                ", sectionId='" + sectionId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", examId='" + examId + '\'' +
                ", globalSmsEnabled=" + globalSmsEnabled +
                ", studentCount=" + (examSubmissionData != null ? examSubmissionData.size() : 0) +
                '}';
    }
}
