package com.school.app.student;

import com.school.app.academic.AcademicStructureService;
import com.school.app.academic.AssignmentService;
import com.school.app.academic.SectionGroup;
import com.school.app.academic.Standard;
import com.school.app.exception.DuplicateResourceException;
import com.school.app.exception.ForbiddenAccessException;
import com.school.app.exception.ResourceNotFoundException;
import com.school.app.teacher.Teacher;
import com.school.app.teacher.TeacherContextService;
import com.school.app.user.Role;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AcademicStructureService academicStructureService;
    private final AssignmentService assignmentService;
    private final TeacherContextService teacherContextService;

    public StudentService(StudentRepository studentRepository,
                           AcademicStructureService academicStructureService,
                           AssignmentService assignmentService,
                           TeacherContextService teacherContextService) {
        this.studentRepository = studentRepository;
        this.academicStructureService = academicStructureService;
        this.assignmentService = assignmentService;
        this.teacherContextService = teacherContextService;
    }

    private boolean currentUserHasRole(Role role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

    /** Enforces that a CLASS_TEACHER may only touch students in their assigned section/group. */
    private void assertClassTeacherOwnsSection(Long sectionGroupId) {
        if (currentUserHasRole(Role.CLASS_TEACHER)) {
            Teacher teacher = teacherContextService.currentTeacher();
            if (!assignmentService.isClassTeacherOfSection(teacher.getId(), sectionGroupId)) {
                throw new ForbiddenAccessException("You are not the class teacher for this section/group");
            }
        }
    }

    @Transactional
    public Student createStudent(StudentRequest request) {
        if (studentRepository.existsByAdmissionNumber(request.getAdmissionNumber())) {
            throw new DuplicateResourceException("Admission number already exists: " + request.getAdmissionNumber());
        }
        if (request.getEmisNumber() != null && !request.getEmisNumber().isBlank()
                && studentRepository.existsByEmisNumber(request.getEmisNumber())) {
            throw new DuplicateResourceException("EMIS number already exists: " + request.getEmisNumber());
        }

        assertClassTeacherOwnsSection(request.getSectionGroupId());

        Standard standard = academicStructureService.getStandardById(request.getStandardId());
        SectionGroup sectionGroup = academicStructureService.getSectionGroupById(request.getSectionGroupId());
        if (!sectionGroup.getStandard().getId().equals(standard.getId())) {
            throw new com.school.app.exception.InvalidRequestException("Section/Group does not belong to the given standard");
        }

        Student student = new Student();
        applyRequest(student, request, standard, sectionGroup);
        student.setActive(true);
        return studentRepository.save(student);
    }

    private void applyRequest(Student student, StudentRequest request, Standard standard, SectionGroup sectionGroup) {
        student.setAdmissionNumber(request.getAdmissionNumber());
        student.setRegisterNumber(request.getRegisterNumber());
        student.setEmisNumber(request.getEmisNumber());
        student.setName(request.getName());
        student.setDob(request.getDob());
        student.setGender(request.getGender());
        student.setAadhaarNumber(request.getAadhaarNumber());
        student.setFatherName(request.getFatherName());
        student.setMotherName(request.getMotherName());
        student.setGuardianName(request.getGuardianName());
        student.setParentPhone(request.getParentPhone());
        student.setParentEmail(request.getParentEmail());
        student.setAddress(request.getAddress());
        student.setStandard(standard);
        student.setSectionGroup(sectionGroup);
    }

    public Student getById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        assertReadAccess(student);
        return student;
    }

    private void assertReadAccess(Student student) {
        if (currentUserHasRole(Role.CLASS_TEACHER)) {
            // A class teacher can read students of their own class, and also of any
            // section where they have been assigned as a subject teacher (read-only).
            Teacher ct = teacherContextService.currentTeacher();
            Long sgId = student.getSectionGroup().getId();
            boolean subjectHere = assignmentService.getActiveSubjectAssignmentsForTeacher(ct.getId()).stream()
                    .anyMatch(a -> a.getSectionGroup().getId().equals(sgId));
            if (!subjectHere) {
                assertClassTeacherOwnsSection(sgId);
            }
        } else if (currentUserHasRole(Role.SUBJECT_TEACHER)) {
            Teacher teacher = teacherContextService.currentTeacher();
            boolean hasAny = assignmentService.getActiveSubjectAssignmentsForTeacher(teacher.getId()).stream()
                    .anyMatch(a -> a.getSectionGroup().getId().equals(student.getSectionGroup().getId()));
            if (!hasAny) {
                throw new ForbiddenAccessException("You are not assigned to this student's class/group");
            }
        }
        // HEADMASTER and ADMIN can read all students
    }

    @Transactional
    public Student updateStudent(Long id, StudentRequest request) {
        Student student = getById(id); // enforces read access first
        assertClassTeacherOwnsSection(student.getSectionGroup().getId());

        if (!student.getAdmissionNumber().equals(request.getAdmissionNumber())
                && studentRepository.existsByAdmissionNumber(request.getAdmissionNumber())) {
            throw new DuplicateResourceException("Admission number already exists: " + request.getAdmissionNumber());
        }

        Standard standard = academicStructureService.getStandardById(request.getStandardId());
        SectionGroup sectionGroup = academicStructureService.getSectionGroupById(request.getSectionGroupId());
        // If moving the student to a new section, ensure teacher is authorized for target too
        assertClassTeacherOwnsSection(sectionGroup.getId());

        applyRequest(student, request, standard, sectionGroup);
        return studentRepository.save(student);
    }

    /**
     * Updates only the detailed fields (EMIS, Aadhaar, parents, contact, address).
     * Used by the class teacher of the student's section; the Admin may use it too.
     */
    @Transactional
    public Student updateDetails(Long id, StudentDetailsRequest request) {
        Student student = getById(id); // enforces read access first
        assertClassTeacherOwnsSection(student.getSectionGroup().getId());

        String emis = request.getEmisNumber() == null || request.getEmisNumber().isBlank() ? null : request.getEmisNumber().trim();
        if (emis != null && !emis.equals(student.getEmisNumber()) && studentRepository.existsByEmisNumber(emis)) {
            throw new DuplicateResourceException("EMIS number already exists: " + emis);
        }
        student.setEmisNumber(emis);
        student.setAadhaarNumber(request.getAadhaarNumber());
        student.setFatherName(request.getFatherName());
        student.setMotherName(request.getMotherName());
        student.setGuardianName(request.getGuardianName());
        student.setParentPhone(request.getParentPhone());
        student.setParentEmail(request.getParentEmail());
        student.setAddress(request.getAddress());
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = getById(id);
        assertClassTeacherOwnsSection(student.getSectionGroup().getId());
        studentRepository.delete(student);
    }

    public List<Student> search(String name, String registerNumber, String admissionNumber, String emisNumber,
                                 Long standardId, Long sectionGroupId) {
        Specification<Student> spec = Specification.where(StudentSpecifications.nameContains(name))
                .and(StudentSpecifications.registerNumberEquals(registerNumber))
                .and(StudentSpecifications.admissionNumberEquals(admissionNumber))
                .and(StudentSpecifications.emisNumberEquals(emisNumber))
                .and(StudentSpecifications.standardIdEquals(standardId))
                .and(StudentSpecifications.sectionGroupIdEquals(sectionGroupId));

        // Scope results for class/subject teachers to their own assignments
        if (currentUserHasRole(Role.CLASS_TEACHER)) {
            Teacher teacher = teacherContextService.currentTeacher();
            List<Long> sectionIds = new java.util.ArrayList<>(assignmentService.getActiveAssignmentsForTeacher(teacher.getId()).stream()
                    .map(a -> a.getSectionGroup().getId()).toList());
            // When one specific section is requested (e.g. Marks Entry), also allow
            // sections where this class teacher is a subject teacher.
            if (sectionGroupId != null && !sectionIds.contains(sectionGroupId)
                    && assignmentService.getActiveSubjectAssignmentsForTeacher(teacher.getId()).stream()
                        .anyMatch(a -> a.getSectionGroup().getId().equals(sectionGroupId))) {
                sectionIds.add(sectionGroupId);
            }
            if (sectionIds.isEmpty()) return List.of();
            spec = spec.and((root, query, cb) -> root.get("sectionGroup").get("id").in(sectionIds));
        } else if (currentUserHasRole(Role.SUBJECT_TEACHER)) {
            Teacher teacher = teacherContextService.currentTeacher();
            List<Long> sectionIds = assignmentService.getActiveSubjectAssignmentsForTeacher(teacher.getId()).stream()
                    .map(a -> a.getSectionGroup().getId()).distinct().toList();
            if (sectionIds.isEmpty()) return List.of();
            spec = spec.and((root, query, cb) -> root.get("sectionGroup").get("id").in(sectionIds));
        }

        return studentRepository.findAll(spec);
    }
}
