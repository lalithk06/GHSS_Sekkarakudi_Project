package com.school.app.academic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionGroupRepository extends JpaRepository<SectionGroup, Long> {
    List<SectionGroup> findByStandardId(Long standardId);
    Optional<SectionGroup> findByStandardIdAndName(Long standardId, String name);
    boolean existsByStandardIdAndName(Long standardId, String name);
}
