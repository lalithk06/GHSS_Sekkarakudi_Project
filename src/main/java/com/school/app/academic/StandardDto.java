package com.school.app.academic;

public class StandardDto {
    private Long id;
    private String name;
    private String description;

    public static StandardDto fromEntity(Standard s) {
        StandardDto dto = new StandardDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setDescription(s.getDescription());
        return dto;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
