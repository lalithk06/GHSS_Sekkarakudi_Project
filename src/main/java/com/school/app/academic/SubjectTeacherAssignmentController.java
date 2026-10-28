package com.school.app.academic;

import com.school.app.common.ApiResponse;
import com.school.app.common.SafeMap;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subject-teacher-assignments")
@PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
public class SubjectTeacherAssignmentController {

    private final AssignmentService assignmentService;

    public SubjectTeacherAssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ApiResponse<List<SubjectTeacherAssignmentDto>> getAll() {
        return ApiResponse.ok(SafeMap.map(assignmentService.getAllSubjectTeacherAssignments(), SubjectTeacherAssignmentDto::fromEntity, a -> "subject-teacher assignment #" + a.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubjectTeacherAssignmentDto> create(@Valid @RequestBody AssignmentRequest request) {
        return ApiResponse.ok(SubjectTeacherAssignmentDto.fromEntity(assignmentService.assignSubjectTeacher(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        assignmentService.removeSubjectTeacherAssignment(id);
        return ApiResponse.ok("Assignment removed", null);
    }
}
