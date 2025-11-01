package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Teach implements Serializable {

        @SerializedName("subject_id")
        @Expose
        private String subjectId;
        @SerializedName("display_order_class")
        @Expose
        private Integer displayOrderClass;
        @SerializedName("display_order_subject")
        @Expose
        private Integer displayOrderSubject;
        @SerializedName("subject_name")
        @Expose
        private String subjectName;
        @SerializedName("class_name")
        @Expose
        private String className;
        @SerializedName("student_class_id")
        @Expose
        private String studentClassId;
        @SerializedName("section_id")
        @Expose
        private String sectionId;
        @SerializedName("section_name")
        @Expose
        private String sectionName;
        @SerializedName("is_class_incharge")
        @Expose
        private String isClassIncharge;
        private final static long serialVersionUID = 7813338661942700022L;

        public String getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(String subjectId) {
            this.subjectId = subjectId;
        }

        public Integer getDisplayOrderClass() {
            return displayOrderClass;
        }

        public void setDisplayOrderClass(Integer displayOrderClass) {
            this.displayOrderClass = displayOrderClass;
        }

        public Integer getDisplayOrderSubject() {
            return displayOrderSubject;
        }

        public void setDisplayOrderSubject(Integer displayOrderSubject) {
            this.displayOrderSubject = displayOrderSubject;
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

        public String getStudentClassId() {
            return studentClassId;
        }

        public void setStudentClassId(String studentClassId) {
            this.studentClassId = studentClassId;
        }

        public String getSectionId() {
            return sectionId;
        }

        public void setSectionId(String sectionId) {
            this.sectionId = sectionId;
        }

        public String getSectionName() {
            return sectionName;
        }

        public void setSectionName(String sectionName) {
            this.sectionName = sectionName;
        }

        public String getIsClassIncharge() {
            return isClassIncharge;
        }

        public void setIsClassIncharge(String isClassIncharge) {
            this.isClassIncharge = isClassIncharge;
        }
    }

