package com.school.app.marks;

import com.school.app.academic.AssignmentService;
import com.school.app.exception.DuplicateResourceException;
import com.school.app.exception.ForbiddenAccessException;
import com.school.app.exception.InvalidRequestException;
import com.school.app.exception.ResourceNotFoundException;
import com.school.app.security.CurrentUser;
import com.school.app.student.Student;
import com.school.app.student.StudentService;
import com.school.app.subject.Subject;
import com.school.app.subject.SubjectService;
import com.school.app.teacher.Teacher;
import com.school.app.teacher.TeacherContextService;
import com.school.app.user.Role;
import com.school.app.user.User;
import com.school.app.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarkService {

    private final MarkRepository markRepository;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final ExamService examService;
    private final AssignmentService assignmentService;
    private final TeacherContextService teacherContextService;
    private final UserRepository userRepository;

    public MarkService(MarkRepository markRepository, StudentService studentService, SubjectService subjectService,
                        ExamService examService, AssignmentService assignmentService,
                        TeacherContextService teacherContextService, UserRepository userRepository) {
        this.markRepository = markRepository;
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.examService = examService;
        this.assignmentService = assignmentService;
        this.teacherContextService = teacherContextService;
        this.userRepository = userRepository;
    }

    private boolean hasRole(Role role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

    private void assertOwnsSubjectClass(Long sectionGroupId, Long subjectId) {
        if (hasRole(Role.SUBJECT_TEACHER)) {
            Teacher teacher = teacherContextService.currentTeacher();
            if (!assignmentService.isSubjectTeacherOf(teacher.getId(), sectionGroupId, subjectId)) {
                throw new ForbiddenAccessException("You are not assigned to this subject/class combination");
            }
        }
    }

    private void validateRange(double obtained, double max) {
        if (obtained < 0) throw new InvalidRequestException("marksObtained cannot be negative");
        if (obtained > max) throw new InvalidRequestException("marksObtained cannot exceed maxMarks");
    }

    @Transactional
    public Mark enterMark(MarkRequest request) {
        Student student = studentService.getById(request.getStudentId());
        Subject subject = subjectService.getById(request.getSubjectId());
        Exam exam = examService.getById(request.getExamId());

        assertOwnsSubjectClass(student.getSectionGroup().getId(), subject.getId());
        // Independent of who is submitting (subject teacher or admin): the
        // subject must actually be offered to this student's section/group
        // (e.g. a Bio-Maths student can never receive Computer marks).
        subjectService.assertSubjectValidForSectionGroup(subject.getId(), student.getSectionGroup().getId());
        validateRange(request.getMarksObtained(), request.getMaxMarks());

        if (markRepository.existsByStudentIdAndSubjectIdAndExamId(student.getId(), subject.getId(), exam.getId())) {
            throw new DuplicateResourceException("Marks already recorded for this student/subject/exam");
        }

        User recorder = userRepository.findById(CurrentUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Mark mark = new Mark();
        mark.setStudent(student);
        mark.setSubject(subject);
        mark.setExam(exam);
        mark.setMarksObtained(request.getMarksObtained());
        mark.setMaxMarks(request.getMaxMarks());
        mark.setRecordedBy(recorder);
        return markRepository.save(mark);
    }

    @Transactional
    public Mark updateMark(Long id, MarkRequest request) {
        Mark mark = markRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mark not found: " + id));
        assertOwnsSubjectClass(mark.getStudent().getSectionGroup().getId(), mark.getSubject().getId());
        validateRange(request.getMarksObtained(), request.getMaxMarks());
        mark.setMarksObtained(request.getMarksObtained());
        mark.setMaxMarks(request.getMaxMarks());
        return markRepository.save(mark);
    }

    public List<Mark> getForStudent(Long studentId) {
        studentService.getById(studentId);
        return markRepository.findByStudentId(studentId);
    }

    public List<Mark> getForClassExam(Long sectionGroupId, Long examId) {
        return markRepository.findByExamIdAndSectionGroupId(examId, sectionGroupId);
    }

    public List<Mark> getForSubjectExam(Long subjectId, Long examId) {
        return markRepository.findBySubjectIdAndExamId(subjectId, examId);
    }

    public List<Mark> getExamResults(Long examId) {
        return markRepository.findByExamId(examId);
    }
}
