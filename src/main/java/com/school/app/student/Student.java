package com.school.app.student;

import com.school.app.academic.SectionGroup;
import com.school.app.academic.Standard;
import com.school.app.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String admissionNumber;

    @Column(length = 50)
    private String registerNumber;

    @Column(unique = true, length = 20)
    private String emisNumber;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(length = 255)
    private String photoPath;

    @Column(length = 20)
    private String aadhaarNumber;

    @Column(length = 150)
    private String fatherName;

    @Column(length = 150)
    private String motherName;

    @Column(length = 150)
    private String guardianName;

    @Column(length = 20)
    private String parentPhone;

    @Column(length = 150)
    private String parentEmail;

    @Column(length = 500)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "standard_id", nullable = false)
    private Standard standard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_group_id", nullable = false)
    private SectionGroup sectionGroup;

    @Column(nullable = false)
    private boolean active = true;

    // getters/setters
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
    public Standard getStandard() { return standard; }
    public void setStandard(Standard standard) { this.standard = standard; }
    public SectionGroup getSectionGroup() { return sectionGroup; }
    public void setSectionGroup(SectionGroup sectionGroup) { this.sectionGroup = sectionGroup; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
