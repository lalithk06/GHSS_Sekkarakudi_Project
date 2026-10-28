package com.school.app.attendance;

import com.school.app.academic.SectionGroup;
import com.school.app.academic.Standard;
import com.school.app.exception.DuplicateResourceException;
import com.school.app.student.Gender;
import com.school.app.student.Student;
import com.school.app.student.StudentRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ClassAttendanceServiceTests {

    @Autowired private TestFixtures fixtures;
    @Autowired private ClassAttendanceService classAttendanceService;
    @Autowired private StudentRepository studentRepository;

    private Student student;
    private SectionGroup sectionGroup;

    @BeforeEach
    void setUp() {
        Standard standard = fixtures.createStandard("10-att-test");
        sectionGroup = fixtures.createSectionGroup(standard, "A-att-test");
        Teacher classTeacher = fixtures.createTeacher("ct_att_" + System.nanoTime(), Role.CLASS_TEACHER, "Class Teacher");
        fixtures.assignClassTeacher(classTeacher, sectionGroup);

        Student s = new Student();
        s.setAdmissionNumber("ADM-ATT-" + System.nanoTime());
        s.setName("Attendance Student");
        s.setDob(LocalDate.of(2009, 1, 1));
        s.setGender(Gender.MALE);
        s.setStandard(standard);
        s.setSectionGroup(sectionGroup);
        s.setActive(true);
        student = studentRepository.save(s);

        UserPrincipal principal = UserPrincipal.fromUser(classTeacher.getUser());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void preventsDuplicateAttendanceForSameStudentClassDate() {
        ClassAttendanceSubmitRequest request = new ClassAttendanceSubmitRequest();
        request.setSectionGroupId(sectionGroup.getId());
        request.setDate(LocalDate.now());

        AttendanceEntryRequest entry = new AttendanceEntryRequest();
        entry.setStudentId(student.getId());
        entry.setStatus(AttendanceStatus.PRESENT);
        request.setEntries(List.of(entry));

        classAttendanceService.submit(request);

        assertThrows(DuplicateResourceException.class, () -> classAttendanceService.submit(request));
    }
}
