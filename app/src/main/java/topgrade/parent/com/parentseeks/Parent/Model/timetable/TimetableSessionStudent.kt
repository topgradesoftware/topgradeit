package topgrade.parent.com.parentseeks.Parent.Model.timetable


import com.google.gson.annotations.SerializedName

data class TimetableSessionStudent(
    @SerializedName("full_name")
    var fullName: String?,
    @SerializedName("shift")
    var shift: String?,
    @SerializedName("unique_id")
    var uniqueId: String?
)