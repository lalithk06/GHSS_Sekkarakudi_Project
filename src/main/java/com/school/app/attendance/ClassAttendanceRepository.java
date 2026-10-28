package com.school.app.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClassAttendanceRepository extends JpaRepository<ClassAttendance, Long> {
    boolean existsByRecordedById(Long userId);
    boolean existsByStudentIdAndSectionGroupIdAndAttendanceDate(Long studentId, Long sectionGroupId, LocalDate date);
    Optional<ClassAttendance> findByStudentIdAndSectionGroupIdAndAttendanceDate(Long studentId, Long sectionGroupId, LocalDate date);
    List<ClassAttendance> findBySectionGroupIdAndAttendanceDate(Long sectionGroupId, LocalDate date);
    List<ClassAttendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);
    List<ClassAttendance> findBySectionGroupIdAndAttendanceDateBetween(Long sectionGroupId, LocalDate from, LocalDate to);
    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);
    long countByStudentId(Long studentId);
}
