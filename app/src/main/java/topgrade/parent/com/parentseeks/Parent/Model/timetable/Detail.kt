package topgrade.parent.com.parentseeks.Parent.Model.timetable


import com.google.gson.annotations.SerializedName

data class Detail(
    @SerializedName("class_name")
    var className: String?,
    @SerializedName("class_order")
    var classOrder: Int?,
    @SerializedName("display_name_class")
    var displayNameClass: Int?,
    @SerializedName("display_name_order")
    var displayNameOrder: String?,
    @SerializedName("display_order")
    var displayOrder: Int?,
    @SerializedName("end_time")
    var endTime: String?,
    @SerializedName("full_name")
    var fullName: String?,
    @SerializedName("is_active")
    var isActive: Int?,
    @SerializedName("parent_id")
    var parentId: String?,
    @SerializedName("phone")
    var phone: String?,
    @SerializedName("section")
    var section: String?,
    @SerializedName("session_id")
    var sessionId: String?,
    @SerializedName("shift")
    var shift: String?,
    @SerializedName("staff")
    var staff: String?,
    @SerializedName("start_time")
    var startTime: String?,
    @SerializedName("stid")
    var stid: String?,
    @SerializedName("student_class_id")
    var studentClassId: String?,
    @SerializedName("subject")
    var subject: String?,
    @SerializedName("timetable_order")
    var timetableOrder: Int?,
    @SerializedName("timetable_session_id")
    var timetableSessionId: String?,
    @SerializedName("timetable_session_name")
    var timetableSessionName: String?,
    @SerializedName("unique_id")
    var uniqueId: String?
)