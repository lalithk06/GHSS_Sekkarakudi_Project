package com.school.app.config;

import com.school.app.academic.SectionGroup;
import com.school.app.academic.SectionGroupRepository;
import com.school.app.academic.Standard;
import com.school.app.academic.StandardRepository;
import com.school.app.subject.Subject;
import com.school.app.subject.SubjectRepository;
import com.school.app.subject.SubjectSectionGroup;
import com.school.app.subject.SubjectSectionGroupRepository;
import com.school.app.subject.SubjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Subjects that exist in the database but have no subject_section_groups row
 * (inserted by SQL, or created before that table existed) never showed up in
 * any subject dropdown. These tests prove the repair makes them visible.
 * Everything runs in a transaction that is rolled back, so the shared test
 * database is left untouched.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LegacyDataRepairTests {

    @Autowired private LegacyDataRepair repair;
    @Autowired private StandardRepository standardRepository;
    @Autowired private SectionGroupRepository sectionGroupRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private SubjectSectionGroupRepository subjectSectionGroupRepository;
    @Autowired private SubjectService subjectService;

    private Standard newStandard(String name) {
        Standard standard = new Standard();
        standard.setName(name);
        return standardRepository.save(standard);
    }

    private SectionGroup newSection(Standard standard, String name) {
        SectionGroup section = new SectionGroup();
        section.setStandard(standard);
        section.setName(name);
        section.setActive(true);
        return sectionGroupRepository.save(section);
    }

    private Subject newSubject(Standard standard, String name) {
        Subject subject = new Subject();
        subject.setStandard(standard);
        subject.setName(name);
        return subjectRepository.save(subject);
    }

    private List<Long> subjectIdsFor(SectionGroup section) {
        return subjectService.getBySectionGroup(section.getId()).stream().map(Subject::getId).toList();
    }

    @Test
    void legacySubjectsBecomeVisibleInEverySectionOfTheirStandard() {
        Standard standard = newStandard("REPAIR-10");
        SectionGroup a = newSection(standard, "A");
        SectionGroup b = newSection(standard, "B");
        Subject tamil = newSubject(standard, "Tamil");

        assertTrue(subjectIdsFor(a).isEmpty(), "precondition: an unmapped subject is invisible in the dropdown");

        repair.repairSubjectMappings();

        assertEquals(List.of(tamil.getId()), subjectIdsFor(a));
        assertEquals(List.of(tamil.getId()), subjectIdsFor(b));
    }

    @Test
    void repairIsIdempotent() {
        Standard standard = newStandard("REPAIR-IDEM");
        newSection(standard, "A");
        newSubject(standard, "English");

        repair.repairSubjectMappings();
        int secondRun = repair.repairSubjectMappings();

        assertEquals(0, secondRun);
    }

    @Test
    void aSubjectTheAdminDeliberatelyUntickedIsNotRemapped() {
        Standard standard = newStandard("REPAIR-OFF");
        SectionGroup a = newSection(standard, "A");
        Subject science = newSubject(standard, "Science");

        SubjectSectionGroup switchedOff = new SubjectSectionGroup();
        switchedOff.setSubject(science);
        switchedOff.setSectionGroup(a);
        switchedOff.setActive(false);
        subjectSectionGroupRepository.save(switchedOff);

        repair.repairSubjectMappings();

        assertTrue(subjectIdsFor(a).isEmpty());
    }
}
