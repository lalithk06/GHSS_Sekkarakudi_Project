package com.school.app.subject;

import com.school.app.common.ApiResponse;
import com.school.app.common.SafeMap;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    /**
     * When sectionGroupId is supplied, only subjects actually offered for
     * that specific section/group are returned (e.g. a 12th Bio-Maths
     * group only gets Tamil/English/Mathematics/Physics/Chemistry/Biology,
     * never Computer/Commerce/etc). standardId alone still returns every
     * subject defined for that standard (used by admin screens that manage
     * the subject catalogue itself, before it's mapped to any group).
     */
    @GetMapping
    public ApiResponse<List<SubjectDto>> getAll(@RequestParam(required = false) Long standardId,
                                                 @RequestParam(required = false) Long sectionGroupId) {
        List<Subject> subjects;
        if (sectionGroupId != null) {
            subjects = subjectService.getBySectionGroup(sectionGroupId);
        } else if (standardId != null) {
            subjects = subjectService.getByStandard(standardId);
        } else {
            subjects = subjectService.getAll();
        }
        return ApiResponse.ok(SafeMap.map(subjects, this::toDtoWithGroups, s -> "subject #" + s.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SubjectDto> getById(@PathVariable Long id) {
        return ApiResponse.ok(toDtoWithGroups(subjectService.getById(id)));
    }

    // Attaches the subject's currently-active section/group mappings so the
    // Admin "Subjects" screen can show (and edit) exactly which group(s) a
    // subject like "Commerce" or "Economics" is actually offered to.
    private SubjectDto toDtoWithGroups(Subject subject) {
        SubjectDto dto = SubjectDto.fromEntity(subject);
        List<com.school.app.academic.SectionGroup> groups = subjectService.getGroupsForSubject(subject.getId());
        dto.setSectionGroupIds(groups.stream().map(com.school.app.academic.SectionGroup::getId).toList());
        dto.setSectionGroupNames(groups.stream().map(com.school.app.academic.SectionGroup::getName).toList());
        return dto;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubjectDto> create(@Valid @RequestBody SubjectRequest request) {
        return ApiResponse.ok(SubjectDto.fromEntity(subjectService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SubjectDto> update(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return ApiResponse.ok(SubjectDto.fromEntity(subjectService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ApiResponse.ok("Subject deleted", null);
    }

    // ---- Subject <-> Section/Group mapping (admin only) ----

    @PostMapping("/{subjectId}/groups/{sectionGroupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> mapToSectionGroup(@PathVariable Long subjectId, @PathVariable Long sectionGroupId) {
        subjectService.mapSubjectToSectionGroup(subjectId, sectionGroupId);
        return ApiResponse.ok("Subject mapped to section/group", null);
    }

    @DeleteMapping("/{subjectId}/groups/{sectionGroupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unmapFromSectionGroup(@PathVariable Long subjectId, @PathVariable Long sectionGroupId) {
        subjectService.unmapSubjectFromSectionGroup(subjectId, sectionGroupId);
        return ApiResponse.ok("Subject unmapped from section/group", null);
    }
}
