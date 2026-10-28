package com.school.app.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    boolean existsByAdmissionNumber(String admissionNumber);
    boolean existsByEmisNumber(String emisNumber);
    Optional<Student> findByEmisNumber(String emisNumber);
    long countBySectionGroupId(Long sectionGroupId);
    long countByStandardId(Long standardId);
    java.util.List<Student> findBySectionGroupId(Long sectionGroupId);
    java.util.List<Student> findByStandardId(Long standardId);
}
