package com.school.app.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The "deep" student details that the class teacher fills in and keeps up to date
 * (the Admin only enters the basic details when adding a student).
 */
public class StudentDetailsRequest {
    @Size(max = 20)
    private String emisNumber;
    private String aadhaarNumber;
    private String fatherName;
    private String motherName;
    private String guardianName;

    @Pattern(regexp = "^$|^[0-9+\\- ]{7,15}$", message = "Invalid phone number")
    private String parentPhone;

    @Email
    private String parentEmail;

    private String address;

    public String getEmisNumber() { return emisNumber; }
    public void setEmisNumber(String emisNumber) { this.emisNumber = emisNumber; }
    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    public String getParentEmail() { return parentEmail; }
    public void setParentEmail(String parentEmail) { this.parentEmail = parentEmail; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
