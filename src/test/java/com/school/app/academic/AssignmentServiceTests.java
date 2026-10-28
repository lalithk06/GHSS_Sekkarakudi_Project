package com.school.app.academic;

import com.school.app.exception.InvalidRequestException;
import com.school.app.subject.Subject;
import com.school.app.support.TestFixtures;
import com.school.app.teacher.Teacher;
import com.school.app.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class AssignmentServiceTests {

    @Autowired private TestFixtures fixtures;
    @Autowired private AssignmentService assignmentService;

    private Standard twelfth;
    private SectionGroup bioMaths;
    private SectionGroup history;
    private Subject biology;
    private Subject computerNotOffered;
    private Subject physicsNotOfferedToHistory;
    private Subject historySubject;
    private Teacher subjectTeacher;

    @BeforeEach
    void setUp() {
        long tag = System.nanoTime();
        twelfth = fixtures.createStandard("12-assignment-test-" + tag);
        bioMaths = fixtures.createSectionGroup(twelfth, "Bio-Maths");
        history = fixtures.createSectionGroup(twelfth, "History");

        biology = fixtures.createSubject(twelfth, bioMaths, "Biology");
        // Created for the standard but deliberately never mapped to Bio-Maths.
        computerNotOffered = fixtures.createSubject(twelfth, "Computer");
        physicsNotOfferedToHistory = fixtures.createSubject(twelfth, "Physics");
        historySubject = fixtures.createSubject(twelfth, history, "History");

        subjectTeacher = fixtures.createTeacher("assign_st_" + tag, Role.SUBJECT_TEACHER, "Assignment Test Teacher");
    }

    @Test
    void allowsAssigningTeacherToASubjectValidForTheGroup() {
        AssignmentRequest request = new AssignmentRequest();
        request.setTeacherId(subjectTeacher.getId());
        request.setSectionGroupId(bioMaths.getId());
        request.setSubjectId(biology.getId());

        assertDoesNotThrow(() -> assignmentService.assignSubjectTeacher(request));
    }

    @Test
    void rejectsAssigningTeacherToASubjectNotOfferedByTheGroup() {
        // Teacher A -> 12th -> Bio-Maths -> Computer must be rejected
        AssignmentRequest request = new AssignmentRequest();
        request.setTeacherId(subjectTeacher.getId());
        request.setSectionGroupId(bioMaths.getId());
        request.setSubjectId(computerNotOffered.getId());

        assertThrows(InvalidRequestException.class, () -> assignmentService.assignSubjectTeacher(request));
    }

    @Test
    void rejectsAssigningHistoryTeacherToPhysics() {
        // Teacher B -> 12th -> History -> Physics must be rejected
        AssignmentRequest request = new AssignmentRequest();
        request.setTeacherId(subjectTeacher.getId());
        request.setSectionGroupId(history.getId());
        request.setSubjectId(physicsNotOfferedToHistory.getId());

        assertThrows(InvalidRequestException.class, () -> assignmentService.assignSubjectTeacher(request));
    }

    @Test
    void allowsAssigningHistoryTeacherToHistorySubject() {
        AssignmentRequest request = new AssignmentRequest();
        request.setTeacherId(subjectTeacher.getId());
        request.setSectionGroupId(history.getId());
        request.setSubjectId(historySubject.getId());

        assertDoesNotThrow(() -> assignmentService.assignSubjectTeacher(request));
    }
}
