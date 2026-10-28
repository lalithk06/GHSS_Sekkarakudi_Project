package com.school.app.marks;

import com.school.app.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marks")
public class MarkController {

    private final MarkService markService;

    public MarkController(MarkService markService) {
        this.markService = markService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUBJECT_TEACHER','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MarkDto> enter(@Valid @RequestBody MarkRequest request) {
        return ApiResponse.ok("Marks recorded", MarkDto.fromEntity(markService.enterMark(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUBJECT_TEACHER','ADMIN')")
    public ApiResponse<MarkDto> update(@PathVariable Long id, @Valid @RequestBody MarkRequest request) {
        return ApiResponse.ok(MarkDto.fromEntity(markService.updateMark(id, request)));
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<List<MarkDto>> getForStudent(@PathVariable Long studentId) {
        return ApiResponse.ok(markService.getForStudent(studentId).stream().map(MarkDto::fromEntity).toList());
    }

    @GetMapping("/class/{sectionGroupId}")
    public ApiResponse<List<MarkDto>> getForClass(@PathVariable Long sectionGroupId, @RequestParam Long examId) {
        return ApiResponse.ok(markService.getForClassExam(sectionGroupId, examId).stream().map(MarkDto::fromEntity).toList());
    }

    @GetMapping("/subject/{subjectId}")
    public ApiResponse<List<MarkDto>> getForSubject(@PathVariable Long subjectId, @RequestParam Long examId) {
        return ApiResponse.ok(markService.getForSubjectExam(subjectId, examId).stream().map(MarkDto::fromEntity).toList());
    }

    @GetMapping("/exam/{examId}/results")
    public ApiResponse<List<MarkDto>> getExamResults(@PathVariable Long examId) {
        return ApiResponse.ok(markService.getExamResults(examId).stream().map(MarkDto::fromEntity).toList());
    }
}
