package com.school.app.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class ClassAttendanceSubmitRequest {
    @NotNull
    private Long sectionGroupId;
    @NotNull
    private LocalDate date;
    @NotEmpty
    @Valid
    private List<AttendanceEntryRequest> entries;

    public Long getSectionGroupId() { return sectionGroupId; }
    public void setSectionGroupId(Long sectionGroupId) { this.sectionGroupId = sectionGroupId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public List<AttendanceEntryRequest> getEntries() { return entries; }
    public void setEntries(List<AttendanceEntryRequest> entries) { this.entries = entries; }
}
