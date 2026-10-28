package com.school.app.academic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassTeacherAssignmentRepository extends JpaRepository<ClassTeacherAssignment, Long> {
    List<ClassTeacherAssignment> findByTeacherIdAndActiveTrue(Long teacherId);
    Optional<ClassTeacherAssignment> findBySectionGroupIdAndActiveTrue(Long sectionGroupId);
    boolean existsByTeacherIdAndSectionGroupIdAndActiveTrue(Long teacherId, Long sectionGroupId);
    List<ClassTeacherAssignment> findByActiveTrue();
    void deleteByTeacherId(Long teacherId);
}
