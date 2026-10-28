package com.school.app.academic;

public class SectionGroupDto {
    private Long id;
    private Long standardId;
    private String standardName;
    private String name;
    private boolean active;

    public static SectionGroupDto fromEntity(SectionGroup sg) {
        SectionGroupDto dto = new SectionGroupDto();
        dto.setId(sg.getId());
        dto.setStandardId(sg.getStandard().getId());
        dto.setStandardName(sg.getStandard().getName());
        dto.setName(sg.getName());
        dto.setActive(sg.isActive());
        return dto;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStandardId() { return standardId; }
    public void setStandardId(Long standardId) { this.standardId = standardId; }
    public String getStandardName() { return standardName; }
    public void setStandardName(String standardName) { this.standardName = standardName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
