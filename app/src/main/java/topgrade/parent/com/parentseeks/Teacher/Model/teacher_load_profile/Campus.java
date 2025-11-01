package topgrade.parent.com.parentseeks.Teacher.Model.teacher_load_profile;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Campus implements Serializable
{

@SerializedName("unique_id")
@Expose
private String uniqueId;
@SerializedName("full_name")
@Expose
private String fullName;
@SerializedName("phone")
@Expose
private String phone;
@SerializedName("picture")
@Expose
private String picture;
private final static long serialVersionUID = 8137725522477023037L;

public String getUniqueId() {
return uniqueId;
}

public void setUniqueId(String uniqueId) {
this.uniqueId = uniqueId;
}

public String getFullName() {
return fullName;
}

public void setFullName(String fullName) {
this.fullName = fullName;
}

public String getPhone() {
return phone;
}

public void setPhone(String phone) {
this.phone = phone;
}

public String getPicture() {
return picture;
}

public void setPicture(String picture) {
this.picture = picture;
}

}