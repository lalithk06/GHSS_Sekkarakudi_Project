package com.school.app.dashboard;

import java.util.List;

public class SubjectTeacherDashboardDto {
    private List<String> assignedSubjects;
    private List<String> assignedClasses;
    private long totalStudentsTaught;
    /** Added for frontend integration: subject teachers have no other endpoint
     *  that exposes their own sectionGroupId/subjectId pairs (the assignment
     *  list endpoints are ADMIN/HEADMASTER-only), so the Marks and Subject
     *  Attendance screens need this to know which class+subject to load. */
    private List<AssignmentSummary> assignments;

    public List<String> getAssignedSubjects() { return assignedSubjects; }
    public void setAssignedSubjects(List<String> assignedSubjects) { this.assignedSubjects = assignedSubjects; }
    public List<String> getAssignedClasses() { return assignedClasses; }
    public void setAssignedClasses(List<String> assignedClasses) { this.assignedClasses = assignedClasses; }
    public long getTotalStudentsTaught() { return totalStudentsTaught; }
    public void setTotalStudentsTaught(long totalStudentsTaught) { this.totalStudentsTaught = totalStudentsTaught; }
    public List<AssignmentSummary> getAssignments() { return assignments; }
    public void setAssignments(List<AssignmentSummary> assignments) { this.assignments = assignments; }

    public static class AssignmentSummary {
        private Long sectionGroupId;
        private String standardName;
        private String sectionGroupName;
        private Long subjectId;
        private String subjectName;

        public AssignmentSummary(Long sectionGroupId, String standardName, String sectionGroupName,
                                  Long subjectId, String subjectName) {
            this.sectionGroupId = sectionGroupId;
            this.standardName = standardName;
            this.sectionGroupName = sectionGroupName;
            this.subjectId = subjectId;
            this.subjectName = subjectName;
        }

        public Long getSectionGroupId() { return sectionGroupId; }
        public String getStandardName() { return standardName; }
        public String getSectionGroupName() { return sectionGroupName; }
        public Long getSubjectId() { return subjectId; }
        public String getSubjectName() { return subjectName; }
    }
}
