package com.school.app.academic;

import com.school.app.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "standards", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Standard extends BaseEntity {

    @Column(nullable = false, length = 20, unique = true)
    private String name; // e.g. "10", "12"

    @Column(length = 255)
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
