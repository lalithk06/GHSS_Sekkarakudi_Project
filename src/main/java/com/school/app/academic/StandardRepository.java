package com.school.app.academic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StandardRepository extends JpaRepository<Standard, Long> {
    Optional<Standard> findByName(String name);
    boolean existsByName(String name);
}
