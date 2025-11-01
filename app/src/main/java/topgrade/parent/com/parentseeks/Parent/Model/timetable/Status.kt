package topgrade.parent.com.parentseeks.Parent.Model.timetable


import com.google.gson.annotations.SerializedName

data class Status(
    @SerializedName("code")
    var code: String?,
    @SerializedName("message")
    var message: String?
)