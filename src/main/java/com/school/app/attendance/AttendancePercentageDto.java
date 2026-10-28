package com.school.app.attendance;

public class AttendancePercentageDto {
    private long presentCount;
    private long totalCount;
    private double percentage;

    public AttendancePercentageDto(long presentCount, long totalCount) {
        this.presentCount = presentCount;
        this.totalCount = totalCount;
        this.percentage = totalCount == 0 ? 0.0 : Math.round((presentCount * 10000.0) / totalCount) / 100.0;
    }

    public long getPresentCount() { return presentCount; }
    public long getTotalCount() { return totalCount; }
    public double getPercentage() { return percentage; }
}
