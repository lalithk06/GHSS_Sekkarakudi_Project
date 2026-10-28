package com.school.app.marks;

import com.school.app.academic.SectionGroup;
import com.school.app.academic.Standard;
import com.school.app.exception.DuplicateResourceException;
import com.school.app.exception.InvalidRequestException;
import com.school.app.student.Gender;
import com.school.app.student.Student;
import com.school.app.student.StudentRepository;
import com.school.app.subject.Subject;
import com.school.app.support.TestFixtures;
import com.school.app.teacher.Teacher;
import com.school.app.user.Role;
import com.school.app.user.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MarkServiceTests {

    @Autowired private TestFixtures fixtures;
    @Autowired private MarkService markService;
    @Autowired private ExamService examService;
    @Autowired private StudentRepository studentRepository;

    private Student student;
    private Subject subject;
    private Exam exam;

    @BeforeEach
    void setUp() {
        Standard standard = fixtures.createStandard("10-marks-test");
        SectionGroup sectionGroup = fixtures.createSectionGroup(standard, "A-marks-test");
        subject = fixtures.createSubject(standard, sectionGroup, "Mathematics-marks-test");
        Teacher subjectTeacher = fixtures.createTeacher("st_marks_" + System.nanoTime(), Role.SUBJECT_TEACHER, "Subject Teacher");
        fixtures.assignSubjectTeacher(subjectTeacher, sectionGroup, subject);

        Student s = new Student();
        s.setAdmissionNumber("ADM-MARKS-" + System.nanoTime());
        s.setName("Marks Student");
        s.setDob(LocalDate.of(2009, 1, 1));
        s.setGender(Gender.FEMALE);
        s.setStandard(standard);
        s.setSectionGroup(sectionGroup);
        s.setActive(true);
        student = studentRepository.save(s);

        ExamRequest examRequest = new ExamRequest();
        examRequest.setName("Unit Test 1 - " + System.nanoTime());
        examRequest.setStandardId(standard.getId());
        exam = examService.create(examRequest);

        // Act as the assigned subject teacher for these operations
        UserPrincipal principal = UserPrincipal.fromUser(subjectTeacher.getUser());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsMarksAboveMaximum() {
        MarkRequest request = new MarkRequest();
        request.setStudentId(student.getId());
        request.setSubjectId(subject.getId());
        request.setExamId(exam.getId());
        request.setMarksObtained(120.0);
        request.setMaxMarks(100.0);

        assertThrows(InvalidRequestException.class, () -> markService.enterMark(request));
    }

    @Test
    void preventsDuplicateMarkForSameStudentSubjectExam() {
        MarkRequest request = new MarkRequest();
        request.setStudentId(student.getId());
        request.setSubjectId(subject.getId());
        request.setExamId(exam.getId());
        request.setMarksObtained(85.0);
        request.setMaxMarks(100.0);

        markService.enterMark(request);

        assertThrows(DuplicateResourceException.class, () -> markService.enterMark(request));
    }

    @Test
    void rejectsMarksForSubjectNotOfferedToStudentsGroup() {
        // A subject that exists for the same standard, but was never mapped
        // to this student's section/group (e.g. it belongs to a different
        // 12th group) must be rejected even though the subject ID itself is
        // valid and the exam ID is valid. Acting as ADMIN here (rather than
        // the assigned subject teacher) isolates this rule from the
        // separate "teacher owns this subject/class" ownership check.
        Subject unrelatedSubject = fixtures.createSubject(student.getStandard(), "History-marks-test-unrelated");
        var admin = fixtures.createUser("admin_marks_" + System.nanoTime(), Role.ADMIN);
        UserPrincipal adminPrincipal = UserPrincipal.fromUser(admin);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities()));

        MarkRequest request = new MarkRequest();
        request.setStudentId(student.getId());
        request.setSubjectId(unrelatedSubject.getId());
        request.setExamId(exam.getId());
        request.setMarksObtained(50.0);
        request.setMaxMarks(100.0);

        assertThrows(InvalidRequestException.class, () -> markService.enterMark(request));
    }
}
