package com.school.app.marks;

public class MarkDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;
    private Long examId;
    private String examName;
    private double marksObtained;
    private double maxMarks;
    private String recordedBy;

    public static MarkDto fromEntity(Mark m) {
        MarkDto dto = new MarkDto();
        dto.setId(m.getId());
        dto.setStudentId(m.getStudent().getId());
        dto.setStudentName(m.getStudent().getName());
        dto.setSubjectId(m.getSubject().getId());
        dto.setSubjectName(m.getSubject().getName());
        dto.setExamId(m.getExam().getId());
        dto.setExamName(m.getExam().getName());
        dto.setMarksObtained(m.getMarksObtained());
        dto.setMaxMarks(m.getMaxMarks());
        dto.setRecordedBy(m.getRecordedBy().getUsername());
        return dto;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(double marksObtained) { this.marksObtained = marksObtained; }
    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
}
