package com.school.app.academic;

import com.school.app.common.ApiResponse;
import com.school.app.common.SafeMap;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class SectionGroupController {

    private final AcademicStructureService service;

    public SectionGroupController(AcademicStructureService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SectionGroupDto>> getAll(@RequestParam(required = false) Long standardId) {
        List<SectionGroup> groups = standardId != null
                ? service.getSectionGroupsByStandard(standardId)
                : service.getAllSectionGroups();
        return ApiResponse.ok(SafeMap.map(groups, SectionGroupDto::fromEntity, sg -> "section/group #" + sg.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SectionGroupDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(SectionGroupDto.fromEntity(service.getSectionGroupById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SectionGroupDto> create(@Valid @RequestBody SectionGroupRequest request) {
        return ApiResponse.ok(SectionGroupDto.fromEntity(service.createSectionGroup(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SectionGroupDto> update(@PathVariable Long id, @Valid @RequestBody SectionGroupRequest request) {
        return ApiResponse.ok(SectionGroupDto.fromEntity(service.updateSectionGroup(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteSectionGroup(id);
        return ApiResponse.ok("Section/Group deleted", null);
    }
}
