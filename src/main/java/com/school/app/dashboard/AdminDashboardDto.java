package com.school.app.dashboard;

public class AdminDashboardDto {
    private long totalTeachers;
    private long classTeachers;
    private long subjectTeachers;
    private long classTeacherAssignments;
    private long subjectTeacherAssignments;
    private long totalStandards;
    private long totalSectionGroups;
    private long totalSubjects;
    private long totalActiveSections;
    private long sectionsSubmittedToday;
    private java.util.List<HmDashboardDto.SectionAttendanceToday> todaysAttendance;

    public long getTotalActiveSections() { return totalActiveSections; }
    public void setTotalActiveSections(long v) { this.totalActiveSections = v; }
    public long getSectionsSubmittedToday() { return sectionsSubmittedToday; }
    public void setSectionsSubmittedToday(long v) { this.sectionsSubmittedToday = v; }
    public java.util.List<HmDashboardDto.SectionAttendanceToday> getTodaysAttendance() { return todaysAttendance; }
    public void setTodaysAttendance(java.util.List<HmDashboardDto.SectionAttendanceToday> v) { this.todaysAttendance = v; }

    public long getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(long totalTeachers) { this.totalTeachers = totalTeachers; }
    public long getClassTeachers() { return classTeachers; }
    public void setClassTeachers(long classTeachers) { this.classTeachers = classTeachers; }
    public long getSubjectTeachers() { return subjectTeachers; }
    public void setSubjectTeachers(long subjectTeachers) { this.subjectTeachers = subjectTeachers; }
    public long getClassTeacherAssignments() { return classTeacherAssignments; }
    public void setClassTeacherAssignments(long classTeacherAssignments) { this.classTeacherAssignments = classTeacherAssignments; }
    public long getSubjectTeacherAssignments() { return subjectTeacherAssignments; }
    public void setSubjectTeacherAssignments(long subjectTeacherAssignments) { this.subjectTeacherAssignments = subjectTeacherAssignments; }
    public long getTotalStandards() { return totalStandards; }
    public void setTotalStandards(long totalStandards) { this.totalStandards = totalStandards; }
    public long getTotalSectionGroups() { return totalSectionGroups; }
    public void setTotalSectionGroups(long totalSectionGroups) { this.totalSectionGroups = totalSectionGroups; }
    public long getTotalSubjects() { return totalSubjects; }
    public void setTotalSubjects(long totalSubjects) { this.totalSubjects = totalSubjects; }
}
