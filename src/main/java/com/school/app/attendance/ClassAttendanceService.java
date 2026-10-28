package com.school.app.attendance;

import com.school.app.academic.AssignmentService;
import com.school.app.academic.SectionGroup;
import com.school.app.exception.DuplicateResourceException;
import com.school.app.exception.ForbiddenAccessException;
import com.school.app.exception.ResourceNotFoundException;
import com.school.app.security.CurrentUser;
import com.school.app.student.Student;
import com.school.app.student.StudentService;
import com.school.app.teacher.Teacher;
import com.school.app.teacher.TeacherContextService;
import com.school.app.user.Role;
import com.school.app.user.User;
import com.school.app.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClassAttendanceService {

    private final ClassAttendanceRepository repository;
    private final StudentService studentService;
    private final AssignmentService assignmentService;
    private final TeacherContextService teacherContextService;
    private final UserRepository userRepository;

    public ClassAttendanceService(ClassAttendanceRepository repository, StudentService studentService,
                                   AssignmentService assignmentService, TeacherContextService teacherContextService,
                                   UserRepository userRepository) {
        this.repository = repository;
        this.studentService = studentService;
        this.assignmentService = assignmentService;
        this.teacherContextService = teacherContextService;
        this.userRepository = userRepository;
    }

    private boolean hasRole(Role role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

    private void assertOwnsSection(Long sectionGroupId) {
        if (hasRole(Role.CLASS_TEACHER)) {
            Teacher teacher = teacherContextService.currentTeacher();
            if (!assignmentService.isClassTeacherOfSection(teacher.getId(), sectionGroupId)) {
                throw new ForbiddenAccessException("You are not the class teacher for this section/group");
            }
        }
    }

    @Transactional
    public List<ClassAttendance> submit(ClassAttendanceSubmitRequest request) {
        assertOwnsSection(request.getSectionGroupId());
        User recorder = userRepository.findById(CurrentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        return request.getEntries().stream().map(entry -> {
            if (repository.existsByStudentIdAndSectionGroupIdAndAttendanceDate(
                    entry.getStudentId(), request.getSectionGroupId(), request.getDate())) {
                throw new DuplicateResourceException(
                        "Attendance already recorded for student " + entry.getStudentId() + " on " + request.getDate());
            }
            Student student = studentService.getById(entry.getStudentId());
            SectionGroup sectionGroup = student.getSectionGroup();

            ClassAttendance attendance = new ClassAttendance();
            attendance.setStudent(student);
            attendance.setSectionGroup(sectionGroup);
            attendance.setAttendanceDate(request.getDate());
            attendance.setStatus(entry.getStatus());
            attendance.setRecordedBy(recorder);
            return repository.save(attendance);
        }).toList();
    }

    public List<ClassAttendance> getForDate(Long sectionGroupId, LocalDate date) {
        assertOwnsSection(sectionGroupId);
        return repository.findBySectionGroupIdAndAttendanceDate(sectionGroupId, date);
    }

    public List<ClassAttendance> getStudentHistory(Long studentId) {
        studentService.getById(studentId); // enforces access
        return repository.findByStudentIdOrderByAttendanceDateDesc(studentId);
    }

    public List<ClassAttendance> getClassHistory(Long sectionGroupId, LocalDate from, LocalDate to) {
        assertOwnsSection(sectionGroupId);
        return repository.findBySectionGroupIdAndAttendanceDateBetween(sectionGroupId, from, to);
    }

    public AttendancePercentageDto getPercentage(Long studentId) {
        studentService.getById(studentId);
        long present = repository.countByStudentIdAndStatus(studentId, AttendanceStatus.PRESENT);
        long total = repository.countByStudentId(studentId);
        return new AttendancePercentageDto(present, total);
    }
}
