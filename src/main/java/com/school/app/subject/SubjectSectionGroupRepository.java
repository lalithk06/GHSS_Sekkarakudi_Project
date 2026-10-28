package com.school.app.subject;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectSectionGroupRepository extends JpaRepository<SubjectSectionGroup, Long> {
    List<SubjectSectionGroup> findBySectionGroupIdAndActiveTrue(Long sectionGroupId);
    List<SubjectSectionGroup> findBySubjectIdAndActiveTrue(Long subjectId);
    Optional<SubjectSectionGroup> findBySubjectIdAndSectionGroupId(Long subjectId, Long sectionGroupId);
    boolean existsBySubjectIdAndSectionGroupIdAndActiveTrue(Long subjectId, Long sectionGroupId);
}
