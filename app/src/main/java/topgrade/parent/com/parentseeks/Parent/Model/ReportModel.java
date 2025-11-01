
package topgrade.parent.com.parentseeks.Parent.Model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReportModel {

    @SerializedName("status")
    @Expose
    private SharedStatus status;


    @SerializedName("cp")
    @Expose
    private String cp;

    @SerializedName("data")
    @Expose
    private Data data;

    @SerializedName("exam")
    @Expose
    private List<Exam> exam = null;
    @SerializedName("month")
    @Expose
    private List<Month> month = null;
    @SerializedName("exam_session")
    @Expose
    private List<ExamSession> examSession = null;

    public SharedStatus getStatus() {
        return status;
    }

    public void setStatus(SharedStatus status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public List<Exam> getExam() {
        return exam;
    }

    public void setExam(List<Exam> exam) {
        this.exam = exam;
    }

    public List<Month> getMonth() {
        return month;
    }

    public void setMonth(List<Month> month) {
        this.month = month;
    }

    public List<ExamSession> getExamSession() {
        return examSession;
    }

    public void setExamSession(List<ExamSession> examSession) {
        this.examSession = examSession;
    }


    public class SharedStatus {

        @SerializedName("code")
        @Expose
        private String code;
        @SerializedName("message")
        @Expose
        private String message;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }


    public class Month {

        @SerializedName("cp")
        @Expose
        private Integer cp;
        @SerializedName("obtained_marks")
        @Expose
        private Integer obtainedMarks;
        @SerializedName("total_marks")
        @Expose
        private Integer totalMarks;
        @SerializedName("percentage")
        @Expose
        private String percentage;
        @SerializedName("attendance")
        @Expose
        private String attendance;
        @SerializedName("month_name")
        @Expose
        private String monthName;

        public Integer getCp() {
            return cp;
        }

        public void setCp(Integer cp) {
            this.cp = cp;
        }

        public Integer getObtainedMarks() {
            return obtainedMarks;
        }

        public void setObtainedMarks(Integer obtainedMarks) {
            this.obtainedMarks = obtainedMarks;
        }

        public Integer getTotalMarks() {
            return totalMarks;
        }

        public void setTotalMarks(Integer totalMarks) {
            this.totalMarks = totalMarks;
        }

        public String getPercentage() {
            return percentage;
        }

        public void setPercentage(String percentage) {
            this.percentage = percentage;
        }

        public String getAttendance() {
            return attendance;
        }

        public void setAttendance(String attendance) {
            this.attendance = attendance;
        }

        public String getMonthName() {
            return monthName;
        }

        public void setMonthName(String monthName) {
            this.monthName = monthName;
        }

    }


    public class Exam {

        @SerializedName("subject_id")
        @Expose
        private String subjectId;
        @SerializedName("subject_name")
        @Expose
        private String subjectName;
        @SerializedName("obtained_marks")
        @Expose
        private String obtainedMarks;
        @SerializedName("total_marks")
        @Expose
        private String totalMarks;
        @SerializedName("percentage")
        @Expose
        private String percentage;
        @SerializedName("cp")
        @Expose
        private Integer cp;
        @SerializedName("detail")
        @Expose
        private List<Detail> detail = null;

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

        public String getPercentage() {
            return percentage;
        }

        public void setPercentage(String percentage) {
            this.percentage = percentage;
        }

        public Integer getCp() {
            return cp;
        }

        public void setCp(Integer cp) {
            this.cp = cp;
        }

        public List<Detail> getDetail() {
            return detail;
        }

        public void setDetail(List<Detail> detail) {
            this.detail = detail;
        }

    }

    public class Detail {

        @SerializedName("exam_name")
        @Expose
        private String examName;
        @SerializedName("obtained_marks")
        @Expose
        private Integer obtainedMarks;
        @SerializedName("total_marks")
        @Expose
        private Integer totalMarks;

        public String getExamName() {
            return examName;
        }

        public void setExamName(String examName) {
            this.examName = examName;
        }

        public Integer getObtainedMarks() {
            return obtainedMarks;
        }

        public void setObtainedMarks(Integer obtainedMarks) {
            this.obtainedMarks = obtainedMarks;
        }

        public Integer getTotalMarks() {
            return totalMarks;
        }

        public void setTotalMarks(Integer totalMarks) {
            this.totalMarks = totalMarks;
        }

    }

    public class Data {

        @SerializedName("unique_id")
        @Expose
        private String uniqueId;
        @SerializedName("login_id")
        @Expose
        private Integer loginId;
        @SerializedName("parent_id")
        @Expose
        private String parentId;
        @SerializedName("full_name")
        @Expose
        private String fullName;
        @SerializedName("email")
        @Expose
        private String email;
        @SerializedName("password")
        @Expose
        private String password;
        @SerializedName("phone")
        @Expose
        private String phone;
        @SerializedName("landline")
        @Expose
        private String landline;
        @SerializedName("address")
        @Expose
        private String address;
        @SerializedName("city_id")
        @Expose
        private String cityId;
        @SerializedName("state_id")
        @Expose
        private String stateId;
        @SerializedName("picture")
        @Expose
        private Object picture;
        @SerializedName("is_active")
        @Expose
        private Integer isActive;
        @SerializedName("is_delete")
        @Expose
        private Integer isDelete;
        @SerializedName("last_login")
        @Expose
        private Object lastLogin;
        @SerializedName("timestamp")
        @Expose
        private String timestamp;

        public String getUniqueId() {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        public Integer getLoginId() {
            return loginId;
        }

        public void setLoginId(Integer loginId) {
            this.loginId = loginId;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getLandline() {
            return landline;
        }

        public void setLandline(String landline) {
            this.landline = landline;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCityId() {
            return cityId;
        }

        public void setCityId(String cityId) {
            this.cityId = cityId;
        }

        public String getStateId() {
            return stateId;
        }

        public void setStateId(String stateId) {
            this.stateId = stateId;
        }

        public Object getPicture() {
            return picture;
        }

        public void setPicture(Object picture) {
            this.picture = picture;
        }

        public Integer getIsActive() {
            return isActive;
        }

        public void setIsActive(Integer isActive) {
            this.isActive = isActive;
        }

        public Integer getIsDelete() {
            return isDelete;
        }

        public void setIsDelete(Integer isDelete) {
            this.isDelete = isDelete;
        }

        public Object getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(Object lastLogin) {
            this.lastLogin = lastLogin;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }
}
