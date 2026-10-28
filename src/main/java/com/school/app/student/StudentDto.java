package com.school.app.student;

import java.time.LocalDate;

public class StudentDto {
    private Long id;
    private String admissionNumber;
    private String registerNumber;
    private String emisNumber;
    private String name;
    private LocalDate dob;
    private String gender;
    private String photoPath;
    private String aadhaarNumber;
    private String fatherName;
    private String motherName;
    private String guardianName;
    private String parentPhone;
    private String parentEmail;
    private String address;
    private Long standardId;
    private String standardName;
    private Long sectionGroupId;
    private String sectionGroupName;
    private boolean active;

    public static StudentDto fromEntity(Student s) {
        StudentDto dto = new StudentDto();
        dto.setId(s.getId());
        dto.setAdmissionNumber(s.getAdmissionNumber());
        dto.setRegisterNumber(s.getRegisterNumber());
        dto.setEmisNumber(s.getEmisNumber());
        dto.setName(s.getName());
        dto.setDob(s.getDob());
        dto.setGender(s.getGender() == null ? null : s.getGender().name());
        dto.setPhotoPath(s.getPhotoPath());
        dto.setAadhaarNumber(s.getAadhaarNumber());
        dto.setFatherName(s.getFatherName());
        dto.setMotherName(s.getMotherName());
        dto.setGuardianName(s.getGuardianName());
        dto.setParentPhone(s.getParentPhone());
        dto.setParentEmail(s.getParentEmail());
        dto.setAddress(s.getAddress());
        dto.setStandardId(s.getStandard().getId());
        dto.setStandardName(s.getStandard().getName());
        dto.setSectionGroupId(s.getSectionGroup().getId());
        dto.setSectionGroupName(s.getSectionGroup().getName());
        dto.setActive(s.isActive());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
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
    public String getStandardName() { return standardName; }
    public void setStandardName(String standardName) { this.standardName = standardName; }
    public Long getSectionGroupId() { return sectionGroupId; }
    public void setSectionGroupId(Long sectionGroupId) { this.sectionGroupId = sectionGroupId; }
    public String getSectionGroupName() { return sectionGroupName; }
    public void setSectionGroupName(String sectionGroupName) { this.sectionGroupName = sectionGroupName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
