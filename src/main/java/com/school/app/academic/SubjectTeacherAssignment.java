package com.school.app.academic;

import com.school.app.common.BaseEntity;
import com.school.app.subject.Subject;
import com.school.app.teacher.Teacher;
import jakarta.persistence.*;

@Entity
@Table(name = "subject_teacher_assignments")
// Note: duplicate-active-assignment prevention is enforced in AssignmentService,
// not via a DB unique constraint, so a deactivated-then-reassigned combination
// (teacher, section, subject) doesn't get permanently blocked at the DB level.
public class SubjectTeacherAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_group_id", nullable = false)
    private SectionGroup sectionGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private boolean active = true;

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public SectionGroup getSectionGroup() { return sectionGroup; }
    public void setSectionGroup(SectionGroup sectionGroup) { this.sectionGroup = sectionGroup; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
