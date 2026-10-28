package com.school.app.marks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExamRequest {
    @NotBlank
    private String name;
    @NotNull
    private Long standardId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getStandardId() { return standardId; }
    public void setStandardId(Long standardId) { this.standardId = standardId; }
}
