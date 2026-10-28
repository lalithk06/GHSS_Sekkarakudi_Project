package com.school.app.academic;

import jakarta.validation.constraints.NotNull;

public class AssignmentRequest {
    @NotNull
    private Long teacherId;
    @NotNull
    private Long sectionGroupId;
    private Long subjectId; // required only for subject-teacher assignment

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getSectionGroupId() { return sectionGroupId; }
    public void setSectionGroupId(Long sectionGroupId) { this.sectionGroupId = sectionGroupId; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
}
