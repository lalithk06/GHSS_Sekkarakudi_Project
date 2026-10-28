package com.school.app.academic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectTeacherAssignmentRepository extends JpaRepository<SubjectTeacherAssignment, Long> {
    List<SubjectTeacherAssignment> findByTeacherIdAndActiveTrue(Long teacherId);
    List<SubjectTeacherAssignment> findBySectionGroupIdAndActiveTrue(Long sectionGroupId);
    boolean existsByTeacherIdAndSectionGroupIdAndSubjectIdAndActiveTrue(Long teacherId, Long sectionGroupId, Long subjectId);
    List<SubjectTeacherAssignment> findByActiveTrue();
    void deleteByTeacherId(Long teacherId);
}
