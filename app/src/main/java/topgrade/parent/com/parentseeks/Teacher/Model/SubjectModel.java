package topgrade.parent.com.parentseeks.Teacher.Model;

public class SubjectModel {
    private String subject_id;
    private String subject_name;
    private String class_id;
    private String section_id;

    public SubjectModel(String subject_id, String subject_name, String class_id, String section_id) {
        this.subject_id = subject_id;
        this.subject_name = subject_name;
        this.class_id = class_id;
        this.section_id = section_id;
    }

    public String getSubject_id() {
        return subject_id;
    }

    public void setSubject_id(String subject_id) {
        this.subject_id = subject_id;
    }

    public String getSubject_name() {
        return subject_name;
    }

    public void setSubject_name(String subject_name) {
        this.subject_name = subject_name;
    }

    public String getClass_id() {
        return class_id;
    }

    public void setClass_id(String class_id) {
        this.class_id = class_id;
    }

    public String getSection_id() {
        return section_id;
    }

    public void setSection_id(String section_id) {
        this.section_id = section_id;
    }
}
