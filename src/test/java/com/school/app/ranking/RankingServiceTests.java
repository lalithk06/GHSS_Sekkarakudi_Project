package com.school.app.ranking;

import com.school.app.academic.SectionGroup;
import com.school.app.academic.Standard;
import com.school.app.marks.Exam;
import com.school.app.marks.ExamRequest;
import com.school.app.marks.ExamService;
import com.school.app.marks.Mark;
import com.school.app.marks.MarkRepository;
import com.school.app.student.Gender;
import com.school.app.student.Student;
import com.school.app.student.StudentRepository;
import com.school.app.subject.Subject;
import com.school.app.support.TestFixtures;
import com.school.app.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class RankingServiceTests {

    @Autowired private TestFixtures fixtures;
    @Autowired private RankingService rankingService;
    @Autowired private StudentRepository studentRepository;
    @Autowired private MarkRepository markRepository;
    @Autowired private ExamService examService;

    private SectionGroup sectionGroup;
    private Standard standard;
    private Exam exam;
    private User recorder;
    private Student aliceStudent;

    @BeforeEach
    void setUp() {
        standard = fixtures.createStandard("10-rank-test-" + System.nanoTime());
        sectionGroup = fixtures.createSectionGroup(standard, "A-rank-test");
        Subject subject = fixtures.createSubject(standard, "Science-rank-test");
        recorder = fixtures.createUser("recorder_" + System.nanoTime(), com.school.app.user.Role.ADMIN);

        ExamRequest examRequest = new ExamRequest();
        examRequest.setName("Model Exam - " + System.nanoTime());
        examRequest.setStandardId(standard.getId());
        exam = examService.create(examRequest);

        // Two students tie for first place, one comes third
        Student first = newStudent(standard, "Alice");
        Student second = newStudent(standard, "Bob");
        Student third = newStudent(standard, "Cara");
        aliceStudent = first;

        saveMark(first, subject, 90);
        saveMark(second, subject, 90);
        saveMark(third, subject, 70);
    }

    private Student newStudent(Standard standard, String name) {
        Student s = new Student();
        s.setAdmissionNumber("ADM-RANK-" + name + "-" + System.nanoTime());
        s.setName(name);
        s.setDob(LocalDate.of(2009, 1, 1));
        s.setGender(Gender.FEMALE);
        s.setStandard(standard);
        s.setSectionGroup(sectionGroup);
        s.setActive(true);
        return studentRepository.save(s);
    }

    private void saveMark(Student student, Subject subject, double marks) {
        Mark mark = new Mark();
        mark.setStudent(student);
        mark.setSubject(subject);
        mark.setExam(exam);
        mark.setMarksObtained(marks);
        mark.setMaxMarks(100);
        mark.setRecordedBy(recorder);
        markRepository.save(mark);
    }

    @Test
    void tiedStudentsShareTheSameRank() {
        List<RankEntryDto> ranking = rankingService.rankBySectionGroupAndExam(sectionGroup.getId(), exam.getId());

        assertEquals(3, ranking.size());
        assertEquals(1, ranking.get(0).getRank());
        assertEquals(1, ranking.get(1).getRank()); // tied with rank 1
        assertEquals(3, ranking.get(2).getRank()); // next rank skips to 3
    }

    @Test
    void excludesMarksForASubjectNotOfferedToTheStudentsGroup() {
        // Simulate a stray Mark row for a subject that was never mapped to
        // this section/group (e.g. left over from before a student
        // transferred groups, or written directly to the DB). Even though
        // the row exists, ranking totals must not count it.
        Subject unrelatedSubject = fixtures.createSubject(standard, "Unrelated-rank-subject-" + System.nanoTime());
        saveMark(aliceStudent, unrelatedSubject, 999);

        List<RankEntryDto> ranking = rankingService.rankBySectionGroupAndExam(sectionGroup.getId(), exam.getId());

        RankEntryDto alice = ranking.stream().filter(r -> r.getStudentName().equals("Alice")).findFirst().orElseThrow();
        // Still 90 (the valid Science-rank-test mark), not 90 + 999 -
        // the unrelated, unmapped subject's marks are excluded.
        assertEquals(90.0, alice.getTotalMarks(), 0.01);
    }
}
