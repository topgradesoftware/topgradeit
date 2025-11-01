package topgrade.parent.com.parentseeks.Parent.Model.timetable


import com.google.gson.annotations.SerializedName

data class StudentTimetableSessionResponse(
    @SerializedName("status")
    var status: Status?,
    @SerializedName("timetable_session")
    var timetableSessionStudent: List<TimetableSessionStudent>?
)