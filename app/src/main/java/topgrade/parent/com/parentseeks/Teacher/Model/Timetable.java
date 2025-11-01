package topgrade.parent.com.parentseeks.Teacher.Model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Timetable {

    @SerializedName("parent_id")
    @Expose
    private String parentId;
    @SerializedName("unique_id")
    @Expose
    private String uniqueId;
    @SerializedName("session_id")
    @Expose
    private String sessionId;
    @SerializedName("display_order")
    @Expose
    private Integer displayOrder;
    @SerializedName("student_class_id")
    @Expose
    private String studentClassId;
    @SerializedName("timetable_session_id")
    @Expose
    private String timetableSessionId;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("is_active")
    @Expose
    private Integer isActive;
    @SerializedName("display_name_order")
    @Expose
    private String displayNameOrder;
    @SerializedName("display_name_class")
    @Expose
    private Integer displayNameClass;
    @SerializedName("class_order")
    @Expose
    private Integer classOrder;
    @SerializedName("timetable_order")
    @Expose
    private Integer timetableOrder;
    @SerializedName("class_name")
    @Expose
    private String className;
    @SerializedName("timetable_session_name")
    @Expose
    private String timetableSessionName;
    @SerializedName("shift")
    @Expose
    private String shift;
    @SerializedName("staff")
    @Expose
    private String staff;
    @SerializedName("stid")
    @Expose
    private String stid;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("detail")
    @Expose
    private List<Detail> detail = null;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getStudentClassId() {
        return studentClassId;
    }

    public void setStudentClassId(String studentClassId) {
        this.studentClassId = studentClassId;
    }

    public String getTimetableSessionId() {
        return timetableSessionId;
    }

    public void setTimetableSessionId(String timetableSessionId) {
        this.timetableSessionId = timetableSessionId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public String getDisplayNameOrder() {
        return displayNameOrder;
    }

    public void setDisplayNameOrder(String displayNameOrder) {
        this.displayNameOrder = displayNameOrder;
    }

    public Integer getDisplayNameClass() {
        return displayNameClass;
    }

    public void setDisplayNameClass(Integer displayNameClass) {
        this.displayNameClass = displayNameClass;
    }

    public Integer getClassOrder() {
        return classOrder;
    }

    public void setClassOrder(Integer classOrder) {
        this.classOrder = classOrder;
    }

    public Integer getTimetableOrder() {
        return timetableOrder;
    }

    public void setTimetableOrder(Integer timetableOrder) {
        this.timetableOrder = timetableOrder;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTimetableSessionName() {
        return timetableSessionName;
    }

    public void setTimetableSessionName(String timetableSessionName) {
        this.timetableSessionName = timetableSessionName;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getStaff() {
        return staff;
    }

    public void setStaff(String staff) {
        this.staff = staff;
    }

    public String getStid() {
        return stid;
    }

    public void setStid(String stid) {
        this.stid = stid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Detail> getDetail() {
        return detail;
    }

    public void setDetail(List<Detail> detail) {
        this.detail = detail;
    }



    public class Detail {

        @SerializedName("parent_id")
        @Expose
        private String parentId;
        @SerializedName("unique_id")
        @Expose
        private String uniqueId;
        @SerializedName("session_id")
        @Expose
        private String sessionId;
        @SerializedName("display_order")
        @Expose
        private Integer displayOrder;
        @SerializedName("student_class_id")
        @Expose
        private String studentClassId;
        @SerializedName("timetable_session_id")
        @Expose
        private String timetableSessionId;
        @SerializedName("full_name")
        @Expose
        private String fullName;
        @SerializedName("is_active")
        @Expose
        private Integer isActive;
        @SerializedName("display_name_order")
        @Expose
        private String displayNameOrder;
        @SerializedName("display_name_class")
        @Expose
        private Integer displayNameClass;
        @SerializedName("class_order")
        @Expose
        private Integer classOrder;
        @SerializedName("timetable_order")
        @Expose
        private Integer timetableOrder;
        @SerializedName("class_name")
        @Expose
        private String className;
        @SerializedName("timetable_session_name")
        @Expose
        private String timetableSessionName;
        @SerializedName("shift")
        @Expose
        private String shift;
        @SerializedName("subject")
        @Expose
        private String subject;
        @SerializedName("staff")
        @Expose
        private String staff;
        @SerializedName("stid")
        @Expose
        private String stid;
        @SerializedName("phone")
        @Expose
        private String phone;
        @SerializedName("start_time")
        @Expose
        private String startTime;
        @SerializedName("end_time")
        @Expose
        private String endTime;
        @SerializedName("section")
        @Expose
        private String section;

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public Integer getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }

        public String getStudentClassId() {
            return studentClassId;
        }

        public void setStudentClassId(String studentClassId) {
            this.studentClassId = studentClassId;
        }

        public String getTimetableSessionId() {
            return timetableSessionId;
        }

        public void setTimetableSessionId(String timetableSessionId) {
            this.timetableSessionId = timetableSessionId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public Integer getIsActive() {
            return isActive;
        }

        public void setIsActive(Integer isActive) {
            this.isActive = isActive;
        }

        public String getDisplayNameOrder() {
            return displayNameOrder;
        }

        public void setDisplayNameOrder(String displayNameOrder) {
            this.displayNameOrder = displayNameOrder;
        }

        public Integer getDisplayNameClass() {
            return displayNameClass;
        }

        public void setDisplayNameClass(Integer displayNameClass) {
            this.displayNameClass = displayNameClass;
        }

        public Integer getClassOrder() {
            return classOrder;
        }

        public void setClassOrder(Integer classOrder) {
            this.classOrder = classOrder;
        }

        public Integer getTimetableOrder() {
            return timetableOrder;
        }

        public void setTimetableOrder(Integer timetableOrder) {
            this.timetableOrder = timetableOrder;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getTimetableSessionName() {
            return timetableSessionName;
        }

        public void setTimetableSessionName(String timetableSessionName) {
            this.timetableSessionName = timetableSessionName;
        }

        public String getShift() {
            return shift;
        }

        public void setShift(String shift) {
            this.shift = shift;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getStaff() {
            return staff;
        }

        public void setStaff(String staff) {
            this.staff = staff;
        }

        public String getStid() {
            return stid;
        }

        public void setStid(String stid) {
            this.stid = stid;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

    }

}
