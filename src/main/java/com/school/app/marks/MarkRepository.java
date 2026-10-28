package com.school.app.marks;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarkRepository extends JpaRepository<Mark, Long> {
    boolean existsByRecordedById(Long userId);
    boolean existsByStudentIdAndSubjectIdAndExamId(Long studentId, Long subjectId, Long examId);
    Optional<Mark> findByStudentIdAndSubjectIdAndExamId(Long studentId, Long subjectId, Long examId);
    List<Mark> findByStudentId(Long studentId);
    List<Mark> findByStudentIdAndExamId(Long studentId, Long examId);
    List<Mark> findBySubjectIdAndExamId(Long subjectId, Long examId);
    List<Mark> findByExamId(Long examId);

    @org.springframework.data.jpa.repository.Query(
        "select m from Mark m where m.exam.id = :examId and m.student.sectionGroup.id = :sectionGroupId")
    List<Mark> findByExamIdAndSectionGroupId(Long examId, Long sectionGroupId);
}
