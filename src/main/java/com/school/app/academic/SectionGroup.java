package com.school.app.academic;

import com.school.app.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "section_groups", uniqueConstraints = @UniqueConstraint(columnNames = {"standard_id", "name"}))
public class SectionGroup extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "standard_id", nullable = false)
    private Standard standard;

    @Column(nullable = false, length = 50)
    private String name; // e.g. "A", "Bio-Maths"

    @Column(nullable = false)
    private boolean active = true;

    public Standard getStandard() { return standard; }
    public void setStandard(Standard standard) { this.standard = standard; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
