package com.school.app.marks;

import com.school.app.academic.Standard;
import com.school.app.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "exams", uniqueConstraints = @UniqueConstraint(columnNames = {"standard_id", "name"}))
public class Exam extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name; // e.g. "Unit Test 1", "Quarterly" - free text, not hard-coded

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "standard_id", nullable = false)
    private Standard standard;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Standard getStandard() { return standard; }
    public void setStandard(Standard standard) { this.standard = standard; }
}
