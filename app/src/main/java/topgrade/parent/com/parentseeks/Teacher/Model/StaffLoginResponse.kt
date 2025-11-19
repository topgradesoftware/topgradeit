package topgrade.parent.com.parentseeks.Teacher.Model

import com.google.gson.annotations.SerializedName
import topgrade.parent.com.parentseeks.Teacher.Model.StaffModel

/**
 * Staff Login Response Model
 * 
 * Response structure for staff/teacher login API
 * {
 *   "status": { "code": "1000", "message": "Success" },
 *   "data": [{ ...StaffModel... }],
 *   "campus": { "unique_id": "...", "full_name": "...", "address": "...", "phone": "..." },
 *   "campus_session": { "unique_id": "..." }
 * }
 */
data class StaffLoginResponse(
    @SerializedName("status")
    val status: StaffLoginStatus,
    @SerializedName("data")
    val data: List<StaffDataItem>,
    @SerializedName("campus")
    val campus: StaffCampus,
    @SerializedName("campus_session")
    val campusSession: StaffCampusSession
)

/**
 * Staff data item - matches all fields from StaffModel
 * The API returns data as array with objects containing all StaffModel fields
 */
data class StaffDataItem(
    @SerializedName("unique_id")
    val uniqueId: String,
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("landline")
    val landline: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("picture")
    val picture: String?,
    @SerializedName("dob")
    val dob: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("designation")
    val designation: String?,
    @SerializedName("parent_name")
    val parentName: String?,
    @SerializedName("created_date")
    val createdDate: String?,
    @SerializedName("security")
    val security: String?,
    @SerializedName("qualification")
    val qualification: String?,
    @SerializedName("subject")
    val subject: String?,
    @SerializedName("cnic")
    val cnic: String?,
    @SerializedName("salary")
    val salary: String?,
    @SerializedName("daily_lectures")
    val dailyLectures: String?,
    @SerializedName("city_id")
    val cityId: String?,
    @SerializedName("state_id")
    val stateId: String?,
    @SerializedName("city_name")
    val cityName: String?,
    @SerializedName("state_name")
    val stateName: String?
) {
    /**
     * Convert to StaffModel for backward compatibility
     */
    fun toStaffModel(): StaffModel {
        val model = StaffModel()
        model.setFull_name(fullName ?: "")
        model.setEmail(email ?: "")
        model.setPhone(phone ?: "")
        model.setLandline(landline ?: "")
        model.setAddress(address ?: "")
        model.setPicture(picture ?: "")
        model.setDob(dob ?: "")
        model.setGender(gender ?: "")
        model.setDesignation(designation ?: "")
        model.setParent_name(parentName ?: "")
        model.setCreated_date(createdDate ?: "")
        model.setSecurity(security ?: "")
        model.setQualification(qualification ?: "")
        model.setSubject(subject ?: "")
        model.setCnic(cnic ?: "")
        model.setSalary(salary ?: "")
        model.setDaily_lectures(dailyLectures ?: "")
        model.setCity_id(cityId ?: "")
        model.setState_id(stateId ?: "")
        model.setCity_name(cityName ?: "")
        model.setState_name(stateName ?: "")
        return model
    }
}

data class StaffLoginStatus(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String
)

data class StaffCampus(
    @SerializedName("unique_id")
    val uniqueId: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("phone")
    val phone: String
)

data class StaffCampusSession(
    @SerializedName("unique_id")
    val uniqueId: String
)

