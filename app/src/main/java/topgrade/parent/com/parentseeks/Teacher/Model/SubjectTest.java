package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SubjectTest implements Serializable {
    
    private static final long serialVersionUID = 1L;


    @SerializedName("subject_id")
    @Expose
    private String subjectId;
    @SerializedName("subject")
    @Expose
    private String subject;

    public SubjectTest(String subjectId, String subject) {
        this.subjectId = subjectId;
        this.subject = subject;
    }


    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }



    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return subject;
    }
}
