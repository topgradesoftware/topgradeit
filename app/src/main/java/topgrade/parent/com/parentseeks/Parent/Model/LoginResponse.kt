package topgrade.parent.com.parentseeks.Parent.Model

import com.google.gson.annotations.SerializedName
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent

data class LoginResponse(
    @SerializedName("status")
    val status: LoginStatus,
    @SerializedName("data")
    val data: LoginData,
    @SerializedName("students")
    val students: List<SharedStudent>?,
    @SerializedName("campus_session")
    val campusSession: CampusSession?
)

data class LoginStatus(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String
)

data class LoginData(
    @SerializedName("unique_id")
    val uniqueId: String,
    @SerializedName("parent_id")
    val parentId: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("landline")
    val landline: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("picture")
    val picture: String
)

data class CampusSession(
    @SerializedName("unique_id")
    val uniqueId: String
) 