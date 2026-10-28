package com.school.app.marks;

import com.school.app.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ApiResponse<List<ExamDto>> getAll(@RequestParam(required = false) Long standardId) {
        List<Exam> exams = standardId != null ? examService.getByStandard(standardId) : examService.getAll();
        return ApiResponse.ok(exams.stream().map(ExamDto::fromEntity).toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ExamDto> create(@Valid @RequestBody ExamRequest request) {
        return ApiResponse.ok(ExamDto.fromEntity(examService.create(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return ApiResponse.ok("Exam deleted", null);
    }
}
