package com.school.app.marks;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class MarkRequest {
    @NotNull
    private Long studentId;
    @NotNull
    private Long subjectId;
    @NotNull
    private Long examId;
    @NotNull
    @DecimalMin(value = "0.0", message = "marksObtained must be >= 0")
    private Double marksObtained;
    @NotNull
    @DecimalMin(value = "0.01", message = "maxMarks must be > 0")
    private Double maxMarks;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }
    public Double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Double maxMarks) { this.maxMarks = maxMarks; }
}
