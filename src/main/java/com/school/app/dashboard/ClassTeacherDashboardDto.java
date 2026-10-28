package com.school.app.dashboard;

public class ClassTeacherDashboardDto {
    private Long sectionGroupId;
    private String standardName;
    private String sectionGroupName;
    private long studentCount;
    private boolean attendanceTakenToday;
    private double classAttendancePercentageToday;

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
    public double getClassAttendancePercentageToday() { return classAttendancePercentageToday; }
    public void setClassAttendancePercentageToday(double classAttendancePercentageToday) { this.classAttendancePercentageToday = classAttendancePercentageToday; }
}
