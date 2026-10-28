package com.school.app.teacher;

import com.school.app.common.ApiResponse;
import com.school.app.common.SafeMap;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HEADMASTER','ADMIN')")
    public ApiResponse<List<TeacherDto>> getAll() {
        return ApiResponse.ok(SafeMap.map(teacherService.getAll(), TeacherDto::fromEntity, t -> "teacher #" + t.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HEADMASTER','ADMIN')")
    public ApiResponse<TeacherDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(TeacherDto.fromEntity(teacherService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TeacherDto> create(@Valid @RequestBody CreateTeacherRequest request) {
        return ApiResponse.ok("Teacher created", TeacherDto.fromEntity(teacherService.createTeacher(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TeacherDto> update(@PathVariable Long id, @Valid @RequestBody CreateTeacherRequest request) {
        return ApiResponse.ok(TeacherDto.fromEntity(teacherService.update(id, request)));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        teacherService.deactivate(id);
        return ApiResponse.ok("Teacher deactivated", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ApiResponse.ok("Teacher removed", null);
    }
}
