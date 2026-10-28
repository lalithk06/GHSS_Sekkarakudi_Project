package com.school.app.attendance;

import com.school.app.academic.SectionGroup;
import com.school.app.common.BaseEntity;
import com.school.app.student.Student;
import com.school.app.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "class_attendance", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "section_group_id", "attendance_date"}))
public class ClassAttendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_group_id", nullable = false)
    private SectionGroup sectionGroup;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttendanceStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public SectionGroup getSectionGroup() { return sectionGroup; }
    public void setSectionGroup(SectionGroup sectionGroup) { this.sectionGroup = sectionGroup; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public User getRecordedBy() { return recordedBy; }
    public void setRecordedBy(User recordedBy) { this.recordedBy = recordedBy; }
}
