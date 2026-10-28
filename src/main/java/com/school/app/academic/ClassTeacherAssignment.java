package com.school.app.academic;

import com.school.app.common.BaseEntity;
import com.school.app.teacher.Teacher;
import jakarta.persistence.*;

@Entity
@Table(name = "class_teacher_assignments")
// Note: "only one active class teacher per section" is enforced in AssignmentService,
// not via a DB unique constraint - a DB-level constraint on (section_group_id) would
// also block legitimate reassignment history (deactivate old assignment, add a new one).
public class ClassTeacherAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_group_id", nullable = false)
    private SectionGroup sectionGroup;

    @Column(nullable = false)
    private boolean active = true;

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public SectionGroup getSectionGroup() { return sectionGroup; }
    public void setSectionGroup(SectionGroup sectionGroup) { this.sectionGroup = sectionGroup; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
