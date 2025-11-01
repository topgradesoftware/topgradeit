
package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

    /**
     * No args constructor for use in serialization
     *
     */
    public Data() {
    }

    /**
     *
     * @param loginId
     * @param phone
     * @param cityId
     * @param password
     * @param uniqueId
     * @param timestamp
     * @param isDelete
     * @param isActive
     * @param picture
     * @param parentId
     * @param landline
     * @param lastLogin
     * @param email
     * @param address
     * @param stateId
     * @param fullName
     */
    public Data(String uniqueId, Integer loginId, String parentId, String fullName, String email, String password, String phone, String landline, String address, String cityId, String stateId, Object picture, Integer isActive, Integer isDelete, Object lastLogin, String timestamp) {
        super();
        this.uniqueId = uniqueId;
        this.loginId = loginId;
        this.parentId = parentId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.landline = landline;
        this.address = address;
        this.cityId = cityId;
        this.stateId = stateId;
        this.picture = picture;
        this.isActive = isActive;
        this.isDelete = isDelete;
        this.lastLogin = lastLogin;
        this.timestamp = timestamp;
    }

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
