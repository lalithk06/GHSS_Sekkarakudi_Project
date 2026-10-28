package com.school.app.subject;

import com.school.app.academic.SectionGroup;
import com.school.app.common.BaseEntity;
import jakarta.persistence.*;

/**
 * Associates a {@link Subject} with a specific {@link SectionGroup} (e.g. a
 * 10th section A/B/C/D, or a 12th group like Bio-Maths/History).
 *
 * This exists because a Subject is scoped to a Standard only (Subject can be
 * shared by name across groups, e.g. "Tamil" or "Mathematics"), but which
 * subjects are actually offered differs per group within 12th standard
 * (Bio-Maths vs History, etc). This join table is the single source of
 * truth for "is subject X valid for section/group Y" - every marks,
 * attendance and assignment check goes through it instead of re-deriving
 * the rule in multiple places.
 */
@Entity
@Table(name = "subject_section_groups",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "section_group_id"}))
public class SubjectSectionGroup extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_group_id", nullable = false)
    private SectionGroup sectionGroup;

    @Column(nullable = false)
    private boolean active = true;

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public SectionGroup getSectionGroup() { return sectionGroup; }
    public void setSectionGroup(SectionGroup sectionGroup) { this.sectionGroup = sectionGroup; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
