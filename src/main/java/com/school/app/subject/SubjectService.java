package com.school.app.subject;

import com.school.app.academic.AcademicStructureService;
import com.school.app.academic.SectionGroup;
import com.school.app.academic.Standard;
import com.school.app.exception.DuplicateResourceException;
import com.school.app.exception.InvalidRequestException;
import com.school.app.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectSectionGroupRepository subjectSectionGroupRepository;
    private final AcademicStructureService academicStructureService;

    public SubjectService(SubjectRepository subjectRepository,
                           SubjectSectionGroupRepository subjectSectionGroupRepository,
                           AcademicStructureService academicStructureService) {
        this.subjectRepository = subjectRepository;
        this.subjectSectionGroupRepository = subjectSectionGroupRepository;
        this.academicStructureService = academicStructureService;
    }

    @Transactional
    public Subject create(SubjectRequest request) {
        Standard standard = academicStructureService.getStandardById(request.getStandardId());
        if (subjectRepository.existsByStandardIdAndName(standard.getId(), request.getName())) {
            throw new DuplicateResourceException("Subject already exists for this standard: " + request.getName());
        }
        Subject subject = new Subject();
        subject.setStandard(standard);
        subject.setName(request.getName());
        return subjectRepository.save(subject);
    }

    public List<Subject> getAll() { return subjectRepository.findAll(); }

    public List<Subject> getByStandard(Long standardId) { return subjectRepository.findByStandardId(standardId); }

    /**
     * Subjects actually valid for a specific section/group (e.g. only Tamil,
     * English, Mathematics, Physics, Chemistry, Biology for a 12th Bio-Maths
     * group) - this is what the frontend/admin should use instead of the
     * whole standard's subject list whenever a section/group is known.
     */
    public List<Subject> getBySectionGroup(Long sectionGroupId) {
        return subjectSectionGroupRepository.findBySectionGroupIdAndActiveTrue(sectionGroupId).stream()
                .map(SubjectSectionGroup::getSubject)
                .toList();
    }

    /**
     * The active section/group mappings for a subject - used by the Admin
     * "Subjects" screen so the admin can see (and edit) which group(s) a
     * subject like "Commerce" or "Economics" is actually offered to, instead
     * of that only being visible indirectly through subject_section_groups.
     */
    public List<SectionGroup> getGroupsForSubject(Long subjectId) {
        return subjectSectionGroupRepository.findBySubjectIdAndActiveTrue(subjectId).stream()
                .map(SubjectSectionGroup::getSectionGroup)
                .toList();
    }

    public Subject getById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + id));
    }

    @Transactional
    public Subject update(Long id, SubjectRequest request) {
        Subject subject = getById(id);
        Standard standard = academicStructureService.getStandardById(request.getStandardId());
        subject.setStandard(standard);
        subject.setName(request.getName());
        return subjectRepository.save(subject);
    }

    @Transactional
    public void delete(Long id) {
        subjectRepository.delete(getById(id));
    }

    // ---- Subject <-> Section/Group mapping ----

    public boolean isValidForSectionGroup(Long subjectId, Long sectionGroupId) {
        return subjectSectionGroupRepository.existsBySubjectIdAndSectionGroupIdAndActiveTrue(subjectId, sectionGroupId);
    }

    /**
     * Single reusable enforcement point: throws if the given subject is not
     * (actively) offered for the given section/group. Every service that
     * touches marks, attendance or subject-teacher assignments calls this
     * instead of re-implementing the rule.
     */
    public void assertSubjectValidForSectionGroup(Long subjectId, Long sectionGroupId) {
        if (!isValidForSectionGroup(subjectId, sectionGroupId)) {
            Subject subject = getById(subjectId);
            throw new InvalidRequestException(
                    "Subject '" + subject.getName() + "' is not offered for this section/group");
        }
    }

    /**
     * Idempotent - creates the mapping if it doesn't exist yet, or
     * re-activates it if it was previously deactivated. Also verifies the
     * subject and section/group belong to the same standard, since a
     * subject can only ever be offered to groups within its own standard.
     */
    @Transactional
    public SubjectSectionGroup mapSubjectToSectionGroup(Long subjectId, Long sectionGroupId) {
        Subject subject = getById(subjectId);
        SectionGroup sectionGroup = academicStructureService.getSectionGroupById(sectionGroupId);
        if (!subject.getStandard().getId().equals(sectionGroup.getStandard().getId())) {
            throw new InvalidRequestException("Subject and section/group must belong to the same standard");
        }
        SubjectSectionGroup mapping = subjectSectionGroupRepository
                .findBySubjectIdAndSectionGroupId(subjectId, sectionGroupId)
                .orElseGet(() -> {
                    SubjectSectionGroup m = new SubjectSectionGroup();
                    m.setSubject(subject);
                    m.setSectionGroup(sectionGroup);
                    return m;
                });
        mapping.setActive(true);
        return subjectSectionGroupRepository.save(mapping);
    }

    @Transactional
    public void unmapSubjectFromSectionGroup(Long subjectId, Long sectionGroupId) {
        subjectSectionGroupRepository.findBySubjectIdAndSectionGroupId(subjectId, sectionGroupId)
                .ifPresent(mapping -> {
                    mapping.setActive(false);
                    subjectSectionGroupRepository.save(mapping);
                });
    }
}
