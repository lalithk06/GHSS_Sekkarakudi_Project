package com.school.app.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubjectRequest {
    @NotNull
    private Long standardId;
    @NotBlank
    private String name;

    public Long getStandardId() { return standardId; }
    public void setStandardId(Long standardId) { this.standardId = standardId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
