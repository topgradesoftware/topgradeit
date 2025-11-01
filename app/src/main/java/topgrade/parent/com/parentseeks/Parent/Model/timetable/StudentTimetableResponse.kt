package topgrade.parent.com.parentseeks.Parent.Model.timetable


import com.google.gson.annotations.SerializedName

data class StudentTimetableResponse(
    @SerializedName("status")
    var status: Status?,
    @SerializedName("timetable")
    var timetable: List<Timetable>?
)