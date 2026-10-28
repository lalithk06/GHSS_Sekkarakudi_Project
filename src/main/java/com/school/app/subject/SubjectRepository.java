package com.school.app.subject;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByStandardId(Long standardId);
    Optional<Subject> findByStandardIdAndName(Long standardId, String name);
    boolean existsByStandardIdAndName(Long standardId, String name);
}
