package com.school.app.subject;

import com.school.app.academic.SectionGroup;
import com.school.app.academic.Standard;
import com.school.app.exception.InvalidRequestException;
import com.school.app.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the core business rule: a 12th-standard subject only belongs to
 * the specific group(s) it was mapped to, never to every group of the
 * standard. Uses an isolated standard/group/subject set per test run
 * (rather than the app's seeded "12" standard) so this suite is independent
 * of DataInitializer and can't collide with other tests in the same
 * Spring context.
 */
@SpringBootTest
@ActiveProfiles("test")
class SubjectGroupValidationTests {

    @Autowired private TestFixtures fixtures;
    @Autowired private SubjectService subjectService;

    private Standard twelfth;
    private SectionGroup bioMaths;
    private SectionGroup mathsComputer;
    private SectionGroup bioComputer;
    private SectionGroup history;

    private Subject mathematics;
    private Subject biology;
    private Subject computer;
    private Subject physics;
    private Subject chemistry;
    private Subject commerce;
    private Subject accountancy;
    private Subject historySubject;
    private Subject economics;

    @BeforeEach
    void setUp() {
        long tag = System.nanoTime();
        twelfth = fixtures.createStandard("12-group-test-" + tag);
        bioMaths = fixtures.createSectionGroup(twelfth, "Bio-Maths");
        mathsComputer = fixtures.createSectionGroup(twelfth, "Maths-Computer");
        bioComputer = fixtures.createSectionGroup(twelfth, "Bio-Computer");
        history = fixtures.createSectionGroup(twelfth, "History");

        // Shared subjects, created once each, then mapped to whichever
        // groups actually offer them - mirroring DataInitializer's approach.
        Subject tamil = fixtures.createSubject(twelfth, "Tamil");
        Subject english = fixtures.createSubject(twelfth, "English");
        mathematics = fixtures.createSubject(twelfth, "Mathematics");
        physics = fixtures.createSubject(twelfth, "Physics");
        chemistry = fixtures.createSubject(twelfth, "Chemistry");
        biology = fixtures.createSubject(twelfth, "Biology");
        computer = fixtures.createSubject(twelfth, "Computer");
        commerce = fixtures.createSubject(twelfth, "Commerce");
        accountancy = fixtures.createSubject(twelfth, "Accountancy");
        historySubject = fixtures.createSubject(twelfth, "History");
        economics = fixtures.createSubject(twelfth, "Economics");

        for (SectionGroup group : new SectionGroup[]{bioMaths, mathsComputer, bioComputer, history}) {
            fixtures.mapSubjectToGroup(tamil, group);
            fixtures.mapSubjectToGroup(english, group);
        }
        fixtures.mapSubjectToGroup(mathematics, bioMaths);
        fixtures.mapSubjectToGroup(physics, bioMaths);
        fixtures.mapSubjectToGroup(chemistry, bioMaths);
        fixtures.mapSubjectToGroup(biology, bioMaths);

        fixtures.mapSubjectToGroup(mathematics, mathsComputer);
        fixtures.mapSubjectToGroup(physics, mathsComputer);
        fixtures.mapSubjectToGroup(chemistry, mathsComputer);
        fixtures.mapSubjectToGroup(computer, mathsComputer);

        fixtures.mapSubjectToGroup(physics, bioComputer);
        fixtures.mapSubjectToGroup(chemistry, bioComputer);
        fixtures.mapSubjectToGroup(biology, bioComputer);
        fixtures.mapSubjectToGroup(computer, bioComputer);

        fixtures.mapSubjectToGroup(commerce, history);
        fixtures.mapSubjectToGroup(accountancy, history);
        fixtures.mapSubjectToGroup(historySubject, history);
        fixtures.mapSubjectToGroup(economics, history);
    }

    @Test
    void bioMathsAcceptsBiology() {
        assertTrue(subjectService.isValidForSectionGroup(biology.getId(), bioMaths.getId()));
    }

    @Test
    void bioMathsAcceptsMathematics() {
        assertTrue(subjectService.isValidForSectionGroup(mathematics.getId(), bioMaths.getId()));
    }

    @Test
    void bioMathsRejectsComputer() {
        assertFalse(subjectService.isValidForSectionGroup(computer.getId(), bioMaths.getId()));
        assertThrows(InvalidRequestException.class,
                () -> subjectService.assertSubjectValidForSectionGroup(computer.getId(), bioMaths.getId()));
    }

    @Test
    void mathsComputerAcceptsComputer() {
        assertTrue(subjectService.isValidForSectionGroup(computer.getId(), mathsComputer.getId()));
    }

    @Test
    void mathsComputerRejectsBiology() {
        assertFalse(subjectService.isValidForSectionGroup(biology.getId(), mathsComputer.getId()));
    }

    @Test
    void bioComputerAcceptsBiology() {
        assertTrue(subjectService.isValidForSectionGroup(biology.getId(), bioComputer.getId()));
    }

    @Test
    void bioComputerAcceptsComputer() {
        assertTrue(subjectService.isValidForSectionGroup(computer.getId(), bioComputer.getId()));
    }

    @Test
    void bioComputerRejectsMathematics() {
        assertFalse(subjectService.isValidForSectionGroup(mathematics.getId(), bioComputer.getId()));
    }

    @Test
    void historyAcceptsCommerce() {
        assertTrue(subjectService.isValidForSectionGroup(commerce.getId(), history.getId()));
    }

    @Test
    void historyAcceptsAccountancy() {
        assertTrue(subjectService.isValidForSectionGroup(accountancy.getId(), history.getId()));
    }

    @Test
    void historyAcceptsHistorySubject() {
        assertTrue(subjectService.isValidForSectionGroup(historySubject.getId(), history.getId()));
    }

    @Test
    void historyAcceptsEconomics() {
        assertTrue(subjectService.isValidForSectionGroup(economics.getId(), history.getId()));
    }

    @Test
    void historyRejectsPhysics() {
        assertFalse(subjectService.isValidForSectionGroup(physics.getId(), history.getId()));
        assertThrows(InvalidRequestException.class,
                () -> subjectService.assertSubjectValidForSectionGroup(physics.getId(), history.getId()));
    }

    @Test
    void bioMathsGroupListingReturnsExactlySixSubjects() {
        var subjects = subjectService.getBySectionGroup(bioMaths.getId());
        assertEquals(6, subjects.size());
        assertTrue(subjects.stream().anyMatch(s -> s.getName().equals("Biology")));
        assertTrue(subjects.stream().noneMatch(s -> s.getName().equals("Computer")));
    }

    @Test
    void historyGroupListingReturnsExactlySixSubjects() {
        var subjects = subjectService.getBySectionGroup(history.getId());
        assertEquals(6, subjects.size());
        assertTrue(subjects.stream().anyMatch(s -> s.getName().equals("Economics")));
        assertTrue(subjects.stream().noneMatch(s -> s.getName().equals("Physics")));
    }
}
