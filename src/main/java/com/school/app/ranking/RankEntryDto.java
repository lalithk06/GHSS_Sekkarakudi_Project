package com.school.app.ranking;

public class RankEntryDto {
    private int rank;
    private Long studentId;
    private String studentName;
    private double totalMarks;
    private double totalMaxMarks;
    private double percentage;

    public RankEntryDto(int rank, Long studentId, String studentName, double totalMarks, double totalMaxMarks) {
        this.rank = rank;
        this.studentId = studentId;
        this.studentName = studentName;
        this.totalMarks = totalMarks;
        this.totalMaxMarks = totalMaxMarks;
        this.percentage = totalMaxMarks == 0 ? 0.0 : Math.round((totalMarks * 10000.0) / totalMaxMarks) / 100.0;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public double getTotalMarks() { return totalMarks; }
    public double getTotalMaxMarks() { return totalMaxMarks; }
    public double getPercentage() { return percentage; }
}
