package com.school.app.student;

import com.school.app.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ApiResponse<List<StudentDto>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String registerNumber,
            @RequestParam(required = false) String admissionNumber,
            @RequestParam(required = false) String emisNumber,
            @RequestParam(required = false) Long standardId,
            @RequestParam(required = false) Long sectionGroupId) {
        List<Student> results = studentService.search(search, registerNumber, admissionNumber, emisNumber, standardId, sectionGroupId);
        return ApiResponse.ok(results.stream().map(StudentDto::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(StudentDto.fromEntity(studentService.getById(id)));
    }

    // The Admin adds students (basic details), edits any field and deletes students.
    // The class teacher completes/updates the detailed fields via PUT /{id}/details below.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudentDto> create(@Valid @RequestBody StudentRequest request) {
        return ApiResponse.ok("Student created", StudentDto.fromEntity(studentService.createStudent(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StudentDto> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ApiResponse.ok(StudentDto.fromEntity(studentService.updateStudent(id, request)));
    }

    // Class teacher (of that section) or Admin fills in / updates the detailed fields.
    @PutMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('ADMIN','CLASS_TEACHER')")
    public ApiResponse<StudentDto> updateDetails(@PathVariable Long id, @Valid @RequestBody StudentDetailsRequest request) {
        return ApiResponse.ok("Student details updated", StudentDto.fromEntity(studentService.updateDetails(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ApiResponse.ok("Student deleted", null);
    }
}
