package com.school.app.subject;

import com.school.app.academic.Standard;
import com.school.app.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "subjects", uniqueConstraints = @UniqueConstraint(columnNames = {"standard_id", "name"}))
public class Subject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "standard_id", nullable = false)
    private Standard standard;

    @Column(nullable = false, length = 100)
    private String name;

    public Standard getStandard() { return standard; }
    public void setStandard(Standard standard) { this.standard = standard; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
