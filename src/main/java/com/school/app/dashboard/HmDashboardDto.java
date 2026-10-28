package com.school.app.dashboard;

import java.util.List;
import java.util.Map;

public class HmDashboardDto {
    private long totalStudents;
    private Map<String, Long> studentsPerStandard;
    private Map<String, Long> studentsPerSectionGroup;
    private long totalTeachers;
    private long totalClassTeachers;
    private long totalSubjectTeachers;

    // Today's attendance, pulled live from class_attendance so the HM can see,
    // section by section, whether the class teacher has taken attendance yet
    // today and what percentage was present.
    private long totalActiveSections;
    private long sectionsSubmittedToday;
    private List<SectionAttendanceToday> todaysAttendance;

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
    public Map<String, Long> getStudentsPerStandard() { return studentsPerStandard; }
    public void setStudentsPerStandard(Map<String, Long> studentsPerStandard) { this.studentsPerStandard = studentsPerStandard; }
    public Map<String, Long> getStudentsPerSectionGroup() { return studentsPerSectionGroup; }
    public void setStudentsPerSectionGroup(Map<String, Long> studentsPerSectionGroup) { this.studentsPerSectionGroup = studentsPerSectionGroup; }
    public long getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(long totalTeachers) { this.totalTeachers = totalTeachers; }
    public long getTotalClassTeachers() { return totalClassTeachers; }
    public void setTotalClassTeachers(long totalClassTeachers) { this.totalClassTeachers = totalClassTeachers; }
    public long getTotalSubjectTeachers() { return totalSubjectTeachers; }
    public void setTotalSubjectTeachers(long totalSubjectTeachers) { this.totalSubjectTeachers = totalSubjectTeachers; }
    public long getTotalActiveSections() { return totalActiveSections; }
    public void setTotalActiveSections(long totalActiveSections) { this.totalActiveSections = totalActiveSections; }
    public long getSectionsSubmittedToday() { return sectionsSubmittedToday; }
    public void setSectionsSubmittedToday(long sectionsSubmittedToday) { this.sectionsSubmittedToday = sectionsSubmittedToday; }
    public List<SectionAttendanceToday> getTodaysAttendance() { return todaysAttendance; }
    public void setTodaysAttendance(List<SectionAttendanceToday> todaysAttendance) { this.todaysAttendance = todaysAttendance; }

    public static class SectionAttendanceToday {
        private Long sectionGroupId;
        private String standardName;
        private String sectionGroupName;
        private long studentCount;
        private boolean attendanceTakenToday;
        private long presentCount;
        private long markedCount;
        private double percentageToday;

        public Long getSectionGroupId() { return sectionGroupId; }
        public void setSectionGroupId(Long sectionGroupId) { this.sectionGroupId = sectionGroupId; }
        public String getStandardName() { return standardName; }
        public void setStandardName(String standardName) { this.standardName = standardName; }
        public String getSectionGroupName() { return sectionGroupName; }
        public void setSectionGroupName(String sectionGroupName) { this.sectionGroupName = sectionGroupName; }
        public long getStudentCount() { return studentCount; }
        public void setStudentCount(long studentCount) { this.studentCount = studentCount; }
        public boolean isAttendanceTakenToday() { return attendanceTakenToday; }
        public void setAttendanceTakenToday(boolean attendanceTakenToday) { this.attendanceTakenToday = attendanceTakenToday; }
        public long getPresentCount() { return presentCount; }
        public void setPresentCount(long presentCount) { this.presentCount = presentCount; }
        public long getMarkedCount() { return markedCount; }
        public void setMarkedCount(long markedCount) { this.markedCount = markedCount; }
        public double getPercentageToday() { return percentageToday; }
        public void setPercentageToday(double percentageToday) { this.percentageToday = percentageToday; }
    }
}
