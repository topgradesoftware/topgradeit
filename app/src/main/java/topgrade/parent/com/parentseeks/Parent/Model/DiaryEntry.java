package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DiaryEntry {

    @SerializedName("subject_id")
    @Expose
    private String subjectId;

    @SerializedName("subject_name")
    @Expose
    private String subjectName;

    @SerializedName("class_name")
    @Expose
    private String className;

    @SerializedName("section_name")
    @Expose
    private String sectionName;

    @SerializedName("student_name")
    @Expose
    private String studentName;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("date")
    @Expose
    private String date;

    @SerializedName("teacher_name")
    @Expose
    private String teacherName;

    @SerializedName("image_url")
    @Expose
    private String imageUrl;

    @SerializedName("body")
    @Expose
    private String body;

    @SerializedName("full_name")
    @Expose
    private String fullName;

    @SerializedName("picture")
    @Expose
    private String picture;

    @SerializedName("parent_id")
    @Expose
    private String parentId;

    @SerializedName("unique_id")
    @Expose
    private String uniqueId;

    @SerializedName("session_id")
    @Expose
    private String sessionId;

    @SerializedName("student_class_id")
    @Expose
    private String studentClassId;

    @SerializedName("is_active")
    @Expose
    private Integer isActive;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 