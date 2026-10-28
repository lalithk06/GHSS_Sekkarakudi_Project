package com.school.app.dashboard;

import com.school.app.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DataHealthService dataHealthService;

    public DashboardController(DashboardService dashboardService, DataHealthService dataHealthService) {
        this.dashboardService = dashboardService;
        this.dataHealthService = dataHealthService;
    }

    /**
     * What the app actually sees in its database, and why a dropdown might be
     * empty. Read-only; shown as the "Data check" card on the Admin dashboard.
     */
    @GetMapping("/data-health")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ApiResponse<DataHealthReport> dataHealth() {
        return ApiResponse.ok(dataHealthService.buildReport());
    }

    @GetMapping("/hm")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ApiResponse<HmDashboardDto> hmDashboard() {
        return ApiResponse.ok(dashboardService.getHmDashboard());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminDashboardDto> adminDashboard() {
        return ApiResponse.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/class-teacher")
    @PreAuthorize("hasRole('CLASS_TEACHER')")
    public ApiResponse<ClassTeacherDashboardDto> classTeacherDashboard() {
        return ApiResponse.ok(dashboardService.getClassTeacherDashboard());
    }

    @GetMapping("/subject-teacher")
    @PreAuthorize("hasRole('SUBJECT_TEACHER')")
    public ApiResponse<SubjectTeacherDashboardDto> subjectTeacherDashboard() {
        return ApiResponse.ok(dashboardService.getSubjectTeacherDashboard());
    }
}
