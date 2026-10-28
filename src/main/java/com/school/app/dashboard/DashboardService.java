package com.school.app.dashboard;

import com.school.app.academic.*;
import com.school.app.attendance.AttendanceStatus;
import com.school.app.attendance.ClassAttendanceRepository;
import com.school.app.student.Student;
import com.school.app.student.StudentRepository;
import com.school.app.subject.SubjectRepository;
import com.school.app.teacher.Teacher;
import com.school.app.teacher.TeacherContextService;
import com.school.app.teacher.TeacherRepository;
import com.school.app.user.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StandardRepository standardRepository;
    private final SectionGroupRepository sectionGroupRepository;
    private final SubjectRepository subjectRepository;
    private final ClassTeacherAssignmentRepository classTeacherAssignmentRepository;
    private final SubjectTeacherAssignmentRepository subjectTeacherAssignmentRepository;
    private final ClassAttendanceRepository classAttendanceRepository;
    private final TeacherContextService teacherContextService;

    public DashboardService(StudentRepository studentRepository, TeacherRepository teacherRepository,
                             StandardRepository standardRepository, SectionGroupRepository sectionGroupRepository,
                             SubjectRepository subjectRepository,
                             ClassTeacherAssignmentRepository classTeacherAssignmentRepository,
                             SubjectTeacherAssignmentRepository subjectTeacherAssignmentRepository,
                             ClassAttendanceRepository classAttendanceRepository,
                             TeacherContextService teacherContextService) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.standardRepository = standardRepository;
        this.sectionGroupRepository = sectionGroupRepository;
        this.subjectRepository = subjectRepository;
        this.classTeacherAssignmentRepository = classTeacherAssignmentRepository;
        this.subjectTeacherAssignmentRepository = subjectTeacherAssignmentRepository;
        this.classAttendanceRepository = classAttendanceRepository;
        this.teacherContextService = teacherContextService;
    }

    public HmDashboardDto getHmDashboard() {
        HmDashboardDto dto = new HmDashboardDto();
        List<Student> allStudents = studentRepository.findAll();
        dto.setTotalStudents(allStudents.size());

        Map<String, Long> perStandard = allStudents.stream()
                .filter(this::hasIntactAcademicPlacement)
                .collect(Collectors.groupingBy(s -> s.getStandard().getName(), Collectors.counting()));
        dto.setStudentsPerStandard(perStandard);

        Map<String, Long> perSection = allStudents.stream()
                .filter(this::hasIntactAcademicPlacement)
                .collect(Collectors.groupingBy(
                        s -> s.getStandard().getName() + " - " + s.getSectionGroup().getName(),
                        Collectors.counting()));
        dto.setStudentsPerSectionGroup(perSection);

        List<Teacher> allTeachers = teacherRepository.findAll();
        long classTeachers = allTeachers.stream().filter(t -> hasRole(t, Role.CLASS_TEACHER)).count();
        long subjectTeachers = allTeachers.stream().filter(t -> hasRole(t, Role.SUBJECT_TEACHER)).count();
        dto.setTotalClassTeachers(classTeachers);
        dto.setTotalSubjectTeachers(subjectTeachers);
        dto.setTotalTeachers(classTeachers + subjectTeachers);

        return dto;
    }

    /** True unless the student's standard/section-group reference is broken (e.g. deleted directly in the DB). */
    private boolean hasIntactAcademicPlacement(Student student) {
        try {
            return student.getStandard() != null && student.getStandard().getName() != null
                    && student.getSectionGroup() != null && student.getSectionGroup().getName() != null;
        } catch (Exception ex) {
            log.warn("Student #{} has a broken standard/section-group reference, excluding from dashboard counts: {}",
                    student.getId(), ex.toString());
            return false;
        }
    }

    private boolean hasIntactStandard(SectionGroup sectionGroup) {
        try {
            return sectionGroup.getStandard() != null && sectionGroup.getStandard().getName() != null;
        } catch (Exception ex) {
            log.warn("Section/group #{} has a broken standard reference, excluding from dashboard: {}",
                    sectionGroup.getId(), ex.toString());
            return false;
        }
    }

    /** True if the teacher's linked user account is intact and has the given role; false (never throws) otherwise. */
    private boolean hasRole(Teacher teacher, Role role) {
        try {
            return teacher.getUser() != null && teacher.getUser().getRole() == role;
        } catch (Exception ex) {
            log.warn("Teacher #{} has a broken linked user account, excluding from dashboard counts: {}",
                    teacher.getId(), ex.toString());
            return false;
        }
    }


    /** Today's attendance per active section/group (shown on the Admin dashboard). */
    private void fillTodaysAttendance(AdminDashboardDto dto) {
        List<SectionGroup> activeSections = sectionGroupRepository.findAll().stream()
                .filter(SectionGroup::isActive)
                .filter(this::hasIntactStandard)
                .toList();
        LocalDate today = LocalDate.now();
        List<HmDashboardDto.SectionAttendanceToday> todaysAttendance = new ArrayList<>();
        long sectionsSubmittedToday = 0;
        for (SectionGroup sectionGroup : activeSections) {
            var records = classAttendanceRepository.findBySectionGroupIdAndAttendanceDate(sectionGroup.getId(), today);
            boolean submitted = !records.isEmpty();
            if (submitted) sectionsSubmittedToday++;
            long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();

            HmDashboardDto.SectionAttendanceToday summary = new HmDashboardDto.SectionAttendanceToday();
            summary.setSectionGroupId(sectionGroup.getId());
            summary.setStandardName(sectionGroup.getStandard().getName());
            summary.setSectionGroupName(sectionGroup.getName());
            summary.setStudentCount(studentRepository.countBySectionGroupId(sectionGroup.getId()));
            summary.setAttendanceTakenToday(submitted);
            summary.setPresentCount(present);
            summary.setMarkedCount(records.size());
            summary.setPercentageToday(records.isEmpty() ? 0.0 : Math.round((present * 10000.0) / records.size()) / 100.0);
            todaysAttendance.add(summary);
        }
        dto.setTotalActiveSections(activeSections.size());
        dto.setSectionsSubmittedToday(sectionsSubmittedToday);
        dto.setTodaysAttendance(todaysAttendance);
    }

    public AdminDashboardDto getAdminDashboard() {
        AdminDashboardDto dto = new AdminDashboardDto();
        List<Teacher> allTeachers = teacherRepository.findAll();
        long classTeachers = allTeachers.stream().filter(t -> hasRole(t, Role.CLASS_TEACHER)).count();
        long subjectTeachers = allTeachers.stream().filter(t -> hasRole(t, Role.SUBJECT_TEACHER)).count();
        dto.setClassTeachers(classTeachers);
        dto.setSubjectTeachers(subjectTeachers);
        dto.setTotalTeachers(classTeachers + subjectTeachers);
        dto.setClassTeacherAssignments(classTeacherAssignmentRepository.findByActiveTrue().size());
        dto.setSubjectTeacherAssignments(subjectTeacherAssignmentRepository.findByActiveTrue().size());
        dto.setTotalStandards(standardRepository.count());
        dto.setTotalSectionGroups(sectionGroupRepository.count());
        dto.setTotalSubjects(subjectRepository.count());
        fillTodaysAttendance(dto);
        return dto;
    }

    public ClassTeacherDashboardDto getClassTeacherDashboard() {
        Teacher teacher = teacherContextService.currentTeacher();
        ClassTeacherDashboardDto dto = new ClassTeacherDashboardDto();
        List<ClassTeacherAssignment> assignments = classTeacherAssignmentRepository.findByTeacherIdAndActiveTrue(teacher.getId());
        if (assignments.isEmpty()) {
            return dto;
        }
        SectionGroup sectionGroup = assignments.get(0).getSectionGroup();
        dto.setSectionGroupId(sectionGroup.getId());
        dto.setStandardName(sectionGroup.getStandard().getName());
        dto.setSectionGroupName(sectionGroup.getName());
        long studentCount = studentRepository.countBySectionGroupId(sectionGroup.getId());
        dto.setStudentCount(studentCount);

        var todayRecords = classAttendanceRepository.findBySectionGroupIdAndAttendanceDate(sectionGroup.getId(), LocalDate.now());
        dto.setAttendanceTakenToday(!todayRecords.isEmpty());
        long present = todayRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        dto.setClassAttendancePercentageToday(todayRecords.isEmpty() ? 0.0
                : Math.round((present * 10000.0) / todayRecords.size()) / 100.0);
        return dto;
    }

    public SubjectTeacherDashboardDto getSubjectTeacherDashboard() {
        Teacher teacher = teacherContextService.currentTeacher();
        SubjectTeacherDashboardDto dto = new SubjectTeacherDashboardDto();
        List<SubjectTeacherAssignment> assignments = subjectTeacherAssignmentRepository.findByTeacherIdAndActiveTrue(teacher.getId());

        dto.setAssignedSubjects(assignments.stream().map(a -> a.getSubject().getName()).distinct().toList());
        dto.setAssignedClasses(assignments.stream()
                .map(a -> a.getSectionGroup().getStandard().getName() + " - " + a.getSectionGroup().getName())
                .distinct().toList());
        dto.setAssignments(assignments.stream()
                .map(a -> new SubjectTeacherDashboardDto.AssignmentSummary(
                        a.getSectionGroup().getId(),
                        a.getSectionGroup().getStandard().getName(),
                        a.getSectionGroup().getName(),
                        a.getSubject().getId(),
                        a.getSubject().getName()))
                .toList());

        long totalStudents = assignments.stream()
                .map(a -> a.getSectionGroup().getId())
                .distinct()
                .mapToLong(studentRepository::countBySectionGroupId)
                .sum();
        dto.setTotalStudentsTaught(totalStudents);
        return dto;
    }
}
