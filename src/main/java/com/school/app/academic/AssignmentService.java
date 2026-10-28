package com.school.app.academic;

import com.school.app.exception.DuplicateResourceException;
import com.school.app.exception.InvalidRequestException;
import com.school.app.exception.ResourceNotFoundException;
import com.school.app.subject.Subject;
import com.school.app.subject.SubjectRepository;
import com.school.app.subject.SubjectService;
import com.school.app.teacher.Teacher;
import com.school.app.teacher.TeacherRepository;
import com.school.app.user.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignmentService {

    private final ClassTeacherAssignmentRepository classRepo;
    private final SubjectTeacherAssignmentRepository subjectRepo;
    private final TeacherRepository teacherRepository;
    private final SectionGroupRepository sectionGroupRepository;
    private final SubjectRepository subjectRepository;
    private final SubjectService subjectService;

    public AssignmentService(ClassTeacherAssignmentRepository classRepo,
                              SubjectTeacherAssignmentRepository subjectRepo,
                              TeacherRepository teacherRepository,
                              SectionGroupRepository sectionGroupRepository,
                              SubjectRepository subjectRepository,
                              SubjectService subjectService) {
        this.classRepo = classRepo;
        this.subjectRepo = subjectRepo;
        this.teacherRepository = teacherRepository;
        this.sectionGroupRepository = sectionGroupRepository;
        this.subjectRepository = subjectRepository;
        this.subjectService = subjectService;
    }

    @Transactional
    public ClassTeacherAssignment assignClassTeacher(AssignmentRequest request) {
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + request.getTeacherId()));
        Role currentRole = teacher.getUser().getRole();
        if (currentRole != Role.CLASS_TEACHER && currentRole != Role.SUBJECT_TEACHER) {
            throw new InvalidRequestException("Teacher must have CLASS_TEACHER or SUBJECT_TEACHER role");
        }
        // A subject teacher who is made a class teacher is upgraded to the Class Teacher
        // login type, which can do everything a subject teacher can (subjects + marks) too.
        if (currentRole == Role.SUBJECT_TEACHER) {
            teacher.getUser().setRole(Role.CLASS_TEACHER);
        }
        SectionGroup sectionGroup = sectionGroupRepository.findById(request.getSectionGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Section/Group not found: " + request.getSectionGroupId()));

        if (classRepo.findBySectionGroupIdAndActiveTrue(sectionGroup.getId()).isPresent()) {
            throw new DuplicateResourceException("This class/group already has an active class teacher");
        }

        ClassTeacherAssignment assignment = new ClassTeacherAssignment();
        assignment.setTeacher(teacher);
        assignment.setSectionGroup(sectionGroup);
        assignment.setActive(true);
        return classRepo.save(assignment);
    }

    @Transactional
    public void removeClassTeacherAssignment(Long id) {
        ClassTeacherAssignment assignment = classRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + id));
        assignment.setActive(false);
        classRepo.save(assignment);
    }

    public List<ClassTeacherAssignment> getAllClassTeacherAssignments() {
        return classRepo.findByActiveTrue();
    }

    @Transactional
    public SubjectTeacherAssignment assignSubjectTeacher(AssignmentRequest request) {
        if (request.getSubjectId() == null) {
            throw new InvalidRequestException("subjectId is required for subject-teacher assignment");
        }
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + request.getTeacherId()));
        Role teacherRole = teacher.getUser().getRole();
        if (teacherRole != Role.SUBJECT_TEACHER && teacherRole != Role.CLASS_TEACHER) {
            throw new InvalidRequestException("Teacher must have SUBJECT_TEACHER or CLASS_TEACHER role");
        }
        SectionGroup sectionGroup = sectionGroupRepository.findById(request.getSectionGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Section/Group not found: " + request.getSectionGroupId()));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + request.getSubjectId()));

        // A subject teacher can only be assigned to a subject that is actually
        // offered for this specific section/group (e.g. a Bio-Maths group
        // cannot get a Computer teacher). Never trust sectionGroupId/subjectId
        // combinations from the request without checking this server-side.
        subjectService.assertSubjectValidForSectionGroup(subject.getId(), sectionGroup.getId());

        if (subjectRepo.existsByTeacherIdAndSectionGroupIdAndSubjectIdAndActiveTrue(teacher.getId(), sectionGroup.getId(), subject.getId())) {
            throw new DuplicateResourceException("This assignment already exists");
        }

        SubjectTeacherAssignment assignment = new SubjectTeacherAssignment();
        assignment.setTeacher(teacher);
        assignment.setSectionGroup(sectionGroup);
        assignment.setSubject(subject);
        assignment.setActive(true);
        return subjectRepo.save(assignment);
    }

    @Transactional
    public void removeSubjectTeacherAssignment(Long id) {
        SubjectTeacherAssignment assignment = subjectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + id));
        assignment.setActive(false);
        subjectRepo.save(assignment);
    }

    public List<SubjectTeacherAssignment> getAllSubjectTeacherAssignments() {
        return subjectRepo.findByActiveTrue();
    }

    // ---- Authorization helpers (used by other modules) ----

    public boolean isClassTeacherOfSection(Long teacherId, Long sectionGroupId) {
        return classRepo.existsByTeacherIdAndSectionGroupIdAndActiveTrue(teacherId, sectionGroupId);
    }

    public List<ClassTeacherAssignment> getActiveAssignmentsForTeacher(Long teacherId) {
        return classRepo.findByTeacherIdAndActiveTrue(teacherId);
    }

    public boolean isSubjectTeacherOf(Long teacherId, Long sectionGroupId, Long subjectId) {
        return subjectRepo.existsByTeacherIdAndSectionGroupIdAndSubjectIdAndActiveTrue(teacherId, sectionGroupId, subjectId);
    }

    public List<SubjectTeacherAssignment> getActiveSubjectAssignmentsForTeacher(Long teacherId) {
        return subjectRepo.findByTeacherIdAndActiveTrue(teacherId);
    }
}
