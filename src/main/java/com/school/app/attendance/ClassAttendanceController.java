package com.school.app.attendance;

import com.school.app.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/class-attendance")
public class ClassAttendanceController {

    private final ClassAttendanceService service;

    public ClassAttendanceController(ClassAttendanceService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLASS_TEACHER','ADMIN')")
    public ApiResponse<List<ClassAttendanceDto>> submit(@Valid @RequestBody ClassAttendanceSubmitRequest request) {
        return ApiResponse.ok("Attendance submitted", service.submit(request).stream()
                .map(ClassAttendanceDto::fromEntity).toList());
    }

    @GetMapping
    public ApiResponse<List<ClassAttendanceDto>> getForDate(
            @RequestParam Long sectionGroupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(service.getForDate(sectionGroupId, date).stream()
                .map(ClassAttendanceDto::fromEntity).toList());
    }

    @GetMapping("/class/{sectionGroupId}")
    public ApiResponse<List<ClassAttendanceDto>> getClassHistory(
            @PathVariable Long sectionGroupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(service.getClassHistory(sectionGroupId, from, to).stream()
                .map(ClassAttendanceDto::fromEntity).toList());
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<List<ClassAttendanceDto>> getStudentHistory(@PathVariable Long studentId) {
        return ApiResponse.ok(service.getStudentHistory(studentId).stream()
                .map(ClassAttendanceDto::fromEntity).toList());
    }

    @GetMapping("/student/{studentId}/percentage")
    public ApiResponse<AttendancePercentageDto> getPercentage(@PathVariable Long studentId) {
        return ApiResponse.ok(service.getPercentage(studentId));
    }
}
