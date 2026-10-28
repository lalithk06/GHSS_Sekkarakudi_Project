package com.school.app.academic;

public class SubjectTeacherAssignmentDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long sectionGroupId;
    private String standardName;
    private String sectionGroupName;
    private Long subjectId;
    private String subjectName;
    private boolean active;

    public static SubjectTeacherAssignmentDto fromEntity(SubjectTeacherAssignment a) {
        SubjectTeacherAssignmentDto dto = new SubjectTeacherAssignmentDto();
        dto.setId(a.getId());
        dto.setTeacherId(a.getTeacher().getId());
        dto.setTeacherName(a.getTeacher().getFullName());
        dto.setSectionGroupId(a.getSectionGroup().getId());
        dto.setStandardName(a.getSectionGroup().getStandard().getName());
        dto.setSectionGroupName(a.getSectionGroup().getName());
        dto.setSubjectId(a.getSubject().getId());
        dto.setSubjectName(a.getSubject().getName());
        dto.setActive(a.isActive());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public Long getSectionGroupId() { return sectionGroupId; }
    public void setSectionGroupId(Long sectionGroupId) { this.sectionGroupId = sectionGroupId; }
    public String getStandardName() { return standardName; }
    public void setStandardName(String standardName) { this.standardName = standardName; }
    public String getSectionGroupName() { return sectionGroupName; }
    public void setSectionGroupName(String sectionGroupName) { this.sectionGroupName = sectionGroupName; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
