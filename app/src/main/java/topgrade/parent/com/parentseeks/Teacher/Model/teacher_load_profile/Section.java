package topgrade.parent.com.parentseeks.Teacher.Model.teacher_load_profile;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Section implements Serializable
{

@SerializedName("section_id")
@Expose
private String sectionId;
@SerializedName("display_order_class")
@Expose
private Integer displayOrderClass;
@SerializedName("display_order_section")
@Expose
private Integer displayOrderSection;
@SerializedName("section_name")
@Expose
private String sectionName;
@SerializedName("class_name")
@Expose
private String className;
@SerializedName("student_class_id")
@Expose
private String studentClassId;
private final static long serialVersionUID = -1315385334642043096L;

public String getSectionId() {
return sectionId;
}

public void setSectionId(String sectionId) {
this.sectionId = sectionId;
}

public Integer getDisplayOrderClass() {
return displayOrderClass;
}

public void setDisplayOrderClass(Integer displayOrderClass) {
this.displayOrderClass = displayOrderClass;
}

public Integer getDisplayOrderSection() {
return displayOrderSection;
}

public void setDisplayOrderSection(Integer displayOrderSection) {
this.displayOrderSection = displayOrderSection;
}

public String getSectionName() {
return sectionName;
}

public void setSectionName(String sectionName) {
this.sectionName = sectionName;
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

}