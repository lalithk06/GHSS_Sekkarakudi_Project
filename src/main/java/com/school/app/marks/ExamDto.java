package com.school.app.marks;

public class ExamDto {
    private Long id;
    private String name;
    private Long standardId;
    private String standardName;

    public static ExamDto fromEntity(Exam e) {
        ExamDto dto = new ExamDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setStandardId(e.getStandard().getId());
        dto.setStandardName(e.getStandard().getName());
        return dto;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getStandardId() { return standardId; }
    public void setStandardId(Long standardId) { this.standardId = standardId; }
    public String getStandardName() { return standardName; }
    public void setStandardName(String standardName) { this.standardName = standardName; }
}
