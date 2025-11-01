package topgrade.parent.com.parentseeks.Teacher.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StaffModel {

    @SerializedName("full_name")
    @Expose
    private String full_name;
    
    @SerializedName("email")
    @Expose
    private String email;
    
    @SerializedName("phone")
    @Expose
    private String phone;
    
    @SerializedName("landline")
    @Expose
    private String landline;
    
    @SerializedName("address")
    @Expose
    private String address;
    
    @SerializedName("picture")
    @Expose
    private String picture;
    
    @SerializedName("dob")
    @Expose
    private String dob;
    
    @SerializedName("gender")
    @Expose
    private String gender;
    
    @SerializedName("designation")
    @Expose
    private String designation;
    
    @SerializedName("parent_name")
    @Expose
    private String parent_name;
    
    @SerializedName("created_date")
    @Expose
    private String created_date;
    
    @SerializedName("security")
    @Expose
    private String security;
    
    @SerializedName("qualification")
    @Expose
    private String qualification;
    
    @SerializedName("subject")
    @Expose
    private String subject;

    @SerializedName("cnic")
    @Expose
    private String cnic;

    @SerializedName("salary")
    @Expose
    private String salary;
    
    @SerializedName("daily_lectures")
    @Expose
    private String daily_lectures;
    
    @SerializedName("city_id")
    @Expose
    private String city_id;
    
    @SerializedName("state_id")
    @Expose
    private String state_id;
    
    @SerializedName("city_name")
    @Expose
    private String city_name;
    
    @SerializedName("state_name")
    @Expose
    private String state_name;


    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getState_name() {
        return state_name;
    }

    public void setState_name(String state_name) {
        this.state_name = state_name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setSubject(String subject) {this.subject = subject;}

    public void setCnic(String cnic) {this.cnic = cnic;}

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getCity_id() {
        return city_id;
    }

    public void setCity_id(String city_id) {
        this.city_id = city_id;
    }

    public String getState_id() {
        return state_id;
    }

    public void setState_id(String state_id) {
        this.state_id = state_id;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getParent_name() {
        return parent_name;
    }

    public void setParent_name(String parent_name) {
        this.parent_name = parent_name;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getSubject() {
        return subject;
    }

    public String getCnic() {return cnic; }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getDaily_lectures() {
        return daily_lectures;
    }

    public void setDaily_lectures(String daily_lectures) {
        this.daily_lectures = daily_lectures;
    }
    
    @Override
    public String toString() {
        return "StaffModel{" +
                "full_name='" + full_name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", landline='" + landline + '\'' +
                ", address='" + address + '\'' +
                ", picture='" + picture + '\'' +
                ", dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", designation='" + designation + '\'' +
                ", parent_name='" + parent_name + '\'' +
                ", created_date='" + created_date + '\'' +
                ", security='" + security + '\'' +
                ", qualification='" + qualification + '\'' +
                ", subject='" + subject + '\'' +
                ", cnic='" + cnic + '\'' +
                ", salary='" + salary + '\'' +
                ", daily_lectures='" + daily_lectures + '\'' +
                ", city_id='" + city_id + '\'' +
                ", state_id='" + state_id + '\'' +
                ", city_name='" + city_name + '\'' +
                ", state_name='" + state_name + '\'' +
                '}';
    }
}
