package topgrade.parent.com.parentseeks.Teacher.Exam.Components;

import android.util.Log;

import java.util.List;

import topgrade.parent.com.parentseeks.Teacher.Exam.Models.ExamState;
import topgrade.parent.com.parentseeks.Teacher.Exam.Models.ExamSubmissionData;

/**
 * ExamValidationManager - Handles all validation logic for exam submission
 * Responsibilities:
 * - Validating exam state
 * - Validating student data
 * - Validating marks
 * - Providing validation error messages
 */
public class ExamValidationManager {
    
    private static final String TAG = "ExamValidationManager";

    // MARK: - State Validation

    /**
     * Validates if the exam state is complete for submission
     */
    public boolean validateSubmission(ExamState state) {
        if (state == null) {
            Log.w(TAG, "Exam state is null");
            return false;
        }

        // Check if all required fields are filled
        if (!state.isComplete()) {
            Log.w(TAG, "Exam state is incomplete: " + state.toString());
            return false;
        }

        // Check if students are loaded
        if (!state.hasStudents()) {
            Log.w(TAG, "No students loaded for exam");
            return false;
        }

        return true;
    }

    /**
     * Validates if all students have marks entered
     */
    public boolean validateAllMarksEntered(List<ExamSubmissionData> students) {
        if (students == null || students.isEmpty()) {
            Log.w(TAG, "No students to validate");
            return false;
        }

        for (ExamSubmissionData student : students) {
            if (!student.hasMarks()) {
                Log.w(TAG, "Student " + student.getStudentName() + " has no marks entered");
                return false;
            }
        }

        return true;
    }

    // MARK: - Individual Student Validation

    /**
     * Validates marks for a single student
     */
    public boolean validateStudentMarks(ExamSubmissionData student) {
        if (student == null) {
            Log.w(TAG, "Student data is null");
            return false;
        }

        // Check if marks are entered
        if (!student.hasMarks()) {
            Log.w(TAG, "Student " + student.getStudentName() + " has no marks");
            return false;
        }

        // Check if marks are valid
        if (!student.isValidMarks()) {
            Log.w(TAG, "Student " + student.getStudentName() + " has invalid marks: " + 
                      student.getObtainedMarks() + "/" + student.getTotalMarks());
            return false;
        }

        return true;
    }

    /**
     * Validates student information
     */
    public boolean validateStudentInfo(ExamSubmissionData student) {
        if (student == null) {
            Log.w(TAG, "Student data is null");
            return false;
        }

        // Check required fields
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            Log.w(TAG, "Student ID is missing");
            return false;
        }

        if (student.getStudentName() == null || student.getStudentName().isEmpty()) {
            Log.w(TAG, "Student name is missing");
            return false;
        }

        if (student.getTotalMarks() == null || student.getTotalMarks().isEmpty()) {
            Log.w(TAG, "Total marks is missing");
            return false;
        }

        return true;
    }

    // MARK: - Marks Validation

    /**
     * Validates if marks are within valid range
     */
    public boolean validateMarksRange(String obtainedMarks, String totalMarks) {
        try {
            int obtained = Integer.parseInt(obtainedMarks);
            int total = Integer.parseInt(totalMarks);
            
            if (obtained < 0) {
                Log.w(TAG, "Obtained marks cannot be negative: " + obtained);
                return false;
            }
            
            if (obtained > total) {
                Log.w(TAG, "Obtained marks cannot exceed total marks: " + obtained + " > " + total);
                return false;
            }
            
            return true;
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid marks format: " + obtainedMarks + "/" + totalMarks);
            return false;
        }
    }

    /**
     * Validates if marks are numeric
     */
    public boolean validateMarksFormat(String marks) {
        if (marks == null || marks.isEmpty()) {
            return false;
        }
        
        try {
            Integer.parseInt(marks);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // MARK: - Session Validation

    /**
     * Validates exam session
     */
    public boolean validateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            Log.w(TAG, "Session ID is missing");
            return false;
        }
        return true;
    }

    /**
     * Validates class selection
     */
    public boolean validateClass(String classId) {
        if (classId == null || classId.isEmpty()) {
            Log.w(TAG, "Class ID is missing");
            return false;
        }
        return true;
    }

    /**
     * Validates section selection
     */
    public boolean validateSection(String sectionId) {
        if (sectionId == null || sectionId.isEmpty()) {
            Log.w(TAG, "Section ID is missing");
            return false;
        }
        return true;
    }

    /**
     * Validates subject selection
     */
    public boolean validateSubject(String subjectId) {
        if (subjectId == null || subjectId.isEmpty()) {
            Log.w(TAG, "Subject ID is missing");
            return false;
        }
        return true;
    }

    /**
     * Validates exam selection
     */
    public boolean validateExam(String examId) {
        if (examId == null || examId.isEmpty()) {
            Log.w(TAG, "Exam ID is missing");
            return false;
        }
        return true;
    }

    // MARK: - Error Messages

    /**
     * Gets validation error message for exam state
     */
    public String getStateValidationError(ExamState state) {
        if (state == null) {
            return "Exam state is not initialized";
        }

        if (!state.isComplete()) {
            StringBuilder error = new StringBuilder("Please complete the following:");
            
            if (state.getSessionId() == null || state.getSessionId().isEmpty()) {
                error.append("\n- Select a session");
            }
            if (state.getClassId() == null || state.getClassId().isEmpty()) {
                error.append("\n- Select a class");
            }
            if (state.getSectionId() == null || state.getSectionId().isEmpty()) {
                error.append("\n- Select a section");
            }
            if (state.getSubjectId() == null || state.getSubjectId().isEmpty()) {
                error.append("\n- Select a subject");
            }
            if (state.getExamId() == null || state.getExamId().isEmpty()) {
                error.append("\n- Select an exam");
            }
            
            return error.toString();
        }

        if (!state.hasStudents()) {
            return "No students found for the selected criteria";
        }

        return null;
    }

    /**
     * Gets validation error message for student marks
     */
    public String getMarksValidationError(ExamSubmissionData student) {
        if (student == null) {
            return "Student data is missing";
        }

        if (!student.hasMarks()) {
            return "Please enter marks for " + student.getStudentName();
        }

        if (!student.isValidMarks()) {
            return "Invalid marks for " + student.getStudentName() + 
                   " (must be between 0 and " + student.getTotalMarks() + ")";
        }

        return null;
    }

    /**
     * Gets validation error message for all students
     */
    public String getAllMarksValidationError(List<ExamSubmissionData> students) {
        if (students == null || students.isEmpty()) {
            return "No students to validate";
        }

        StringBuilder error = new StringBuilder("Please enter marks for the following students:");
        boolean hasErrors = false;

        for (ExamSubmissionData student : students) {
            if (!student.hasMarks()) {
                error.append("\n- ").append(student.getStudentName());
                hasErrors = true;
            } else if (!student.isValidMarks()) {
                error.append("\n- ").append(student.getStudentName())
                     .append(" (invalid marks: ").append(student.getObtainedMarks())
                     .append("/").append(student.getTotalMarks()).append(")");
                hasErrors = true;
            }
        }

        return hasErrors ? error.toString() : null;
    }

    // MARK: - Utility Methods

    /**
     * Logs validation errors for debugging
     */
    public void logValidationErrors(ExamState state) {
        if (state == null) {
            Log.e(TAG, "Exam state is null");
            return;
        }

        if (!state.isComplete()) {
            Log.e(TAG, "Exam state incomplete: " + state.toString());
        }

        if (state.getExamSubmissionData() != null) {
            for (ExamSubmissionData student : state.getExamSubmissionData()) {
                if (!validateStudentMarks(student)) {
                    Log.e(TAG, "Student validation failed: " + student.getStudentName());
                }
            }
        }
    }
}
