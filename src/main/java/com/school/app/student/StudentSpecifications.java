package com.school.app.student;

import org.springframework.data.jpa.domain.Specification;

public final class StudentSpecifications {

    private StudentSpecifications() {}

    public static Specification<Student> nameContains(String name) {
        return (root, query, cb) -> name == null ? null :
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Student> registerNumberEquals(String regNo) {
        return (root, query, cb) -> regNo == null ? null : cb.equal(root.get("registerNumber"), regNo);
    }

    public static Specification<Student> admissionNumberEquals(String admNo) {
        return (root, query, cb) -> admNo == null ? null : cb.equal(root.get("admissionNumber"), admNo);
    }

    public static Specification<Student> emisNumberEquals(String emis) {
        return (root, query, cb) -> emis == null ? null : cb.equal(root.get("emisNumber"), emis);
    }

    public static Specification<Student> standardIdEquals(Long standardId) {
        return (root, query, cb) -> standardId == null ? null : cb.equal(root.get("standard").get("id"), standardId);
    }

    public static Specification<Student> sectionGroupIdEquals(Long sectionGroupId) {
        return (root, query, cb) -> sectionGroupId == null ? null : cb.equal(root.get("sectionGroup").get("id"), sectionGroupId);
    }

    public static Specification<Student> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }
}
