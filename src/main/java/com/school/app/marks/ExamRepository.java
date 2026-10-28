package com.school.app.marks;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByStandardId(Long standardId);
    Optional<Exam> findByStandardIdAndName(Long standardId, String name);
    boolean existsByStandardIdAndName(Long standardId, String name);
}
