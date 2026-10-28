package com.school.app.academic;

import com.school.app.common.ApiResponse;
import com.school.app.common.SafeMap;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/class-teacher-assignments")
@PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
public class ClassTeacherAssignmentController {

    private final AssignmentService assignmentService;

    public ClassTeacherAssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ApiResponse<List<ClassTeacherAssignmentDto>> getAll() {
        return ApiResponse.ok(SafeMap.map(assignmentService.getAllClassTeacherAssignments(), ClassTeacherAssignmentDto::fromEntity, a -> "class-teacher assignment #" + a.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClassTeacherAssignmentDto> create(@Valid @RequestBody AssignmentRequest request) {
        return ApiResponse.ok(ClassTeacherAssignmentDto.fromEntity(assignmentService.assignClassTeacher(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        assignmentService.removeClassTeacherAssignment(id);
        return ApiResponse.ok("Assignment removed", null);
    }
}
