package com.school.app.attendance;

import java.time.LocalDate;

public class ClassAttendanceDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long sectionGroupId;
    private LocalDate date;
    private String status;
    private String recordedBy;

    public static ClassAttendanceDto fromEntity(ClassAttendance a) {
        ClassAttendanceDto dto = new ClassAttendanceDto();
        dto.setId(a.getId());
        dto.setStudentId(a.getStudent().getId());
        dto.setStudentName(a.getStudent().getName());
        dto.setSectionGroupId(a.getSectionGroup().getId());
        dto.setDate(a.getAttendanceDate());
        dto.setStatus(a.getStatus().name());
        dto.setRecordedBy(a.getRecordedBy().getUsername());
        return dto;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getSectionGroupId() { return sectionGroupId; }
    public void setSectionGroupId(Long sectionGroupId) { this.sectionGroupId = sectionGroupId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
}
