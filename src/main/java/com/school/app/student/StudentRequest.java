package com.school.app.student;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class StudentRequest {
    @NotBlank
    private String admissionNumber;

    private String registerNumber;

    @Size(max = 20)
    private String emisNumber;

    @NotBlank
    private String name;

    @NotNull
    @Past
    private LocalDate dob;

    @NotNull
    private Gender gender;

    private String aadhaarNumber;

    private String fatherName;
    private String motherName;
    private String guardianName;

    @Pattern(regexp = "^$|^[0-9+\\- ]{7,15}$", message = "Invalid phone number")
    private String parentPhone;

    @Email
    private String parentEmail;

    private String address;

    @NotNull
    private Long standardId;

    @NotNull
    private Long sectionGroupId;

    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }
    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }
    public String getEmisNumber() { return emisNumber; }
    public void setEmisNumber(String emisNumber) { this.emisNumber = emisNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
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
    public Long getStandardId() { return standardId; }
    public void setStandardId(Long standardId) { this.standardId = standardId; }
    public Long getSectionGroupId() { return sectionGroupId; }
    public void setSectionGroupId(Long sectionGroupId) { this.sectionGroupId = sectionGroupId; }
}
