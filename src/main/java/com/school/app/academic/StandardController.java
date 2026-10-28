package com.school.app.academic;

import com.school.app.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/standards")
public class StandardController {

    private final AcademicStructureService service;

    public StandardController(AcademicStructureService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<StandardDto>> getAll() {
        return ApiResponse.ok(service.getAllStandards().stream().map(StandardDto::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<StandardDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(StandardDto.fromEntity(service.getStandardById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StandardDto> create(@Valid @RequestBody StandardRequest request) {
        return ApiResponse.ok(StandardDto.fromEntity(service.createStandard(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StandardDto> update(@PathVariable Long id, @Valid @RequestBody StandardRequest request) {
        return ApiResponse.ok(StandardDto.fromEntity(service.updateStandard(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteStandard(id);
        return ApiResponse.ok("Standard deleted", null);
    }
}
