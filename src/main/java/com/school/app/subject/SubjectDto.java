package com.school.app.subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectDto {
    private Long id;
    private Long standardId;
    private String standardName;
    private String name;
    // Which section(s)/group(s) this subject is actually offered to, e.g. for
    // 10th this is every section (A/B/C/D); for 12th it's whichever specific
    // groups the admin has mapped it to (e.g. "Commerce" -> only "History").
    private List<Long> sectionGroupIds = new ArrayList<>();
    private List<String> sectionGroupNames = new ArrayList<>();

    public static SubjectDto fromEntity(Subject s) {
        SubjectDto dto = new SubjectDto();
        dto.setId(s.getId());
        dto.setStandardId(s.getStandard().getId());
        dto.setStandardName(s.getStandard().getName());
        dto.setName(s.getName());
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
    public List<Long> getSectionGroupIds() { return sectionGroupIds; }
    public void setSectionGroupIds(List<Long> sectionGroupIds) { this.sectionGroupIds = sectionGroupIds; }
    public List<String> getSectionGroupNames() { return sectionGroupNames; }
    public void setSectionGroupNames(List<String> sectionGroupNames) { this.sectionGroupNames = sectionGroupNames; }
}
