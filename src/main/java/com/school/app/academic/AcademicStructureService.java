package com.school.app.academic;

import com.school.app.exception.DuplicateResourceException;
import com.school.app.exception.InvalidRequestException;
import com.school.app.exception.ResourceNotFoundException;
import com.school.app.student.StudentRepository;
import com.school.app.subject.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AcademicStructureService {

    private final StandardRepository standardRepository;
    private final SectionGroupRepository sectionGroupRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final ClassTeacherAssignmentRepository classTeacherAssignmentRepository;
    private final SubjectTeacherAssignmentRepository subjectTeacherAssignmentRepository;

    public AcademicStructureService(StandardRepository standardRepository, SectionGroupRepository sectionGroupRepository,
                                     SubjectRepository subjectRepository, StudentRepository studentRepository,
                                     ClassTeacherAssignmentRepository classTeacherAssignmentRepository,
                                     SubjectTeacherAssignmentRepository subjectTeacherAssignmentRepository) {
        this.standardRepository = standardRepository;
        this.sectionGroupRepository = sectionGroupRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.classTeacherAssignmentRepository = classTeacherAssignmentRepository;
        this.subjectTeacherAssignmentRepository = subjectTeacherAssignmentRepository;
    }

    // ---- Standards ----

    @Transactional
    public Standard createStandard(StandardRequest request) {
        if (standardRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Standard already exists: " + request.getName());
        }
        Standard s = new Standard();
        s.setName(request.getName());
        s.setDescription(request.getDescription());
        return standardRepository.save(s);
    }

    public List<Standard> getAllStandards() {
        return standardRepository.findAll();
    }

    public Standard getStandardById(Long id) {
        return standardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Standard not found: " + id));
    }

    @Transactional
    public Standard updateStandard(Long id, StandardRequest request) {
        Standard s = getStandardById(id);
        s.setName(request.getName());
        s.setDescription(request.getDescription());
        return standardRepository.save(s);
    }

    @Transactional
    public void deleteStandard(Long id) {
        Standard standard = getStandardById(id);
        long sectionCount = sectionGroupRepository.findByStandardId(id).size();
        long subjectCount = subjectRepository.findByStandardId(id).size();
        if (sectionCount > 0 || subjectCount > 0) {
            throw new InvalidRequestException("Cannot delete '" + standard.getDescription() + "' - it still has "
                    + sectionCount + " section(s)/group(s) and " + subjectCount
                    + " subject(s) under it. Remove those first.");
        }
        standardRepository.delete(standard);
    }

    // ---- Section/Group ----

    @Transactional
    public SectionGroup createSectionGroup(SectionGroupRequest request) {
        Standard standard = getStandardById(request.getStandardId());
        if (sectionGroupRepository.existsByStandardIdAndName(standard.getId(), request.getName())) {
            throw new DuplicateResourceException("Section/Group already exists for this standard: " + request.getName());
        }
        SectionGroup sg = new SectionGroup();
        sg.setStandard(standard);
        sg.setName(request.getName());
        sg.setActive(true);
        return sectionGroupRepository.save(sg);
    }

    public List<SectionGroup> getAllSectionGroups() {
        return sectionGroupRepository.findAll();
    }

    public List<SectionGroup> getSectionGroupsByStandard(Long standardId) {
        return sectionGroupRepository.findByStandardId(standardId);
    }

    public SectionGroup getSectionGroupById(Long id) {
        return sectionGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section/Group not found: " + id));
    }

    @Transactional
    public SectionGroup updateSectionGroup(Long id, SectionGroupRequest request) {
        SectionGroup sg = getSectionGroupById(id);
        Standard standard = getStandardById(request.getStandardId());
        sg.setStandard(standard);
        sg.setName(request.getName());
        return sectionGroupRepository.save(sg);
    }

    @Transactional
    public void deleteSectionGroup(Long id) {
        SectionGroup sectionGroup = getSectionGroupById(id);
        long studentCount = studentRepository.countBySectionGroupId(id);
        boolean hasClassTeacher = classTeacherAssignmentRepository.findBySectionGroupIdAndActiveTrue(id).isPresent();
        boolean hasSubjectTeachers = !subjectTeacherAssignmentRepository.findBySectionGroupIdAndActiveTrue(id).isEmpty();
        if (studentCount > 0 || hasClassTeacher || hasSubjectTeachers) {
            throw new InvalidRequestException("Cannot delete '" + sectionGroup.getName() + "' - it still has "
                    + studentCount + " student(s)"
                    + (hasClassTeacher ? ", an active class teacher" : "")
                    + (hasSubjectTeachers ? ", active subject teacher assignment(s)" : "")
                    + ". Remove those first.");
        }
        sectionGroupRepository.delete(sectionGroup);
    }
}
