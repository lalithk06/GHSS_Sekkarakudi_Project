package com.school.app.config;

import com.school.app.academic.SectionGroup;
import com.school.app.academic.SectionGroupRepository;
import com.school.app.subject.Subject;
import com.school.app.subject.SubjectRepository;
import com.school.app.subject.SubjectSectionGroup;
import com.school.app.subject.SubjectSectionGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Repairs data that already exists in the database but is invisible to the
 * dropdowns because it was created before (or outside of) the
 * subject &lt;-&gt; section/group mapping table.
 *
 * WHY THIS EXISTS: every subject dropdown (Assignments, Marks, ...) is driven
 * by the {@code subject_section_groups} table, not by the subjects table. A
 * subject row that was inserted directly in MySQL, or that pre-dates that
 * table, has no mapping row - so the API correctly returns an empty list even
 * though the subject is "in the database". Previously nothing ever created
 * those mapping rows for existing data.
 *
 * WHAT IT DOES: for every subject that has NO mapping row at all (neither
 * active nor inactive - a subject the admin deliberately un-ticked is left
 * alone), it creates the mappings:
 *  - 10th (and any standard other than 12th): the subject is offered in every
 *    section of its standard.
 *  - 12th: the well-known curriculum is applied (e.g. Commerce only for the
 *    History group, Biology only for Bio-Maths / Bio-Computer). Subjects or
 *    groups it does not recognise are offered everywhere so nothing stays
 *    hidden; the admin can trim them under Academic Setup -> Subjects.
 *
 * It is idempotent (a second run creates nothing) and never fails startup.
 */
@Component
@Order(10)
public class LegacyDataRepair implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyDataRepair.class);

    // Lower-cased names. Same curriculum the original seed data used.
    private static final Map<String, Set<String>> TWELFTH_GROUP_SUBJECTS = Map.of(
            "bio-maths", Set.of("tamil", "english", "mathematics", "physics", "chemistry", "biology"),
            "maths-computer", Set.of("tamil", "english", "mathematics", "physics", "chemistry", "computer"),
            "bio-computer", Set.of("tamil", "english", "physics", "chemistry", "biology", "computer"),
            "history", Set.of("tamil", "english", "commerce", "accountancy", "history", "economics"));

    private final SubjectRepository subjectRepository;
    private final SectionGroupRepository sectionGroupRepository;
    private final SubjectSectionGroupRepository subjectSectionGroupRepository;
    private final TransactionTemplate transactionTemplate;

    public LegacyDataRepair(SubjectRepository subjectRepository,
                            SectionGroupRepository sectionGroupRepository,
                            SubjectSectionGroupRepository subjectSectionGroupRepository,
                            PlatformTransactionManager transactionManager) {
        this.subjectRepository = subjectRepository;
        this.sectionGroupRepository = sectionGroupRepository;
        this.subjectSectionGroupRepository = subjectSectionGroupRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(String... args) {
        try {
            int created = repairSubjectMappings();
            if (created > 0) {
                log.info("Legacy data repair: created {} missing subject <-> section/group mapping(s) so existing "
                        + "subjects now appear in the dropdowns. Review them under Academic Setup -> Subjects.", created);
            }
        } catch (Exception ex) {
            log.error("Legacy data repair failed (the application will still start): {}", ex.toString(), ex);
        }
    }

    /** Creates the missing mappings. Returns how many mapping rows were created. */
    public int repairSubjectMappings() {
        Integer created = transactionTemplate.execute(status -> doRepair());
        return created == null ? 0 : created;
    }

    private int doRepair() {
        Set<Long> subjectsWithAnyMapping = new HashSet<>();
        for (SubjectSectionGroup mapping : subjectSectionGroupRepository.findAll()) {
            try {
                subjectsWithAnyMapping.add(mapping.getSubject().getId());
            } catch (RuntimeException ex) {
                log.warn("Ignoring unreadable subject mapping #{}: {}", mapping.getId(), ex.toString());
            }
        }

        Map<Long, List<SectionGroup>> groupsByStandard = new HashMap<>();
        int created = 0;
        for (Subject subject : subjectRepository.findAll()) {
            if (subjectsWithAnyMapping.contains(subject.getId())) {
                continue;
            }
            try {
                Long standardId = subject.getStandard().getId();
                String standardName = subject.getStandard().getName();
                List<SectionGroup> groups = groupsByStandard.computeIfAbsent(
                        standardId, sectionGroupRepository::findByStandardId);
                for (SectionGroup group : groups) {
                    if (!isOfferedIn(standardName, group.getName(), subject.getName())) {
                        continue;
                    }
                    SubjectSectionGroup mapping = new SubjectSectionGroup();
                    mapping.setSubject(subject);
                    mapping.setSectionGroup(group);
                    mapping.setActive(true);
                    subjectSectionGroupRepository.save(mapping);
                    created++;
                }
            } catch (RuntimeException ex) {
                log.warn("Could not map subject #{} (broken standard/section reference?): {}",
                        subject.getId(), ex.toString());
            }
        }
        return created;
    }

    /**
     * Whether a subject should be offered in a section/group by default.
     * Only the 12th standard has per-group rules; everything else (and
     * anything unrecognised) is offered everywhere.
     */
    public static boolean isOfferedIn(String standardName, String groupName, String subjectName) {
        if (standardName == null || groupName == null || subjectName == null) {
            return true;
        }
        if (!"12".equals(standardName.replaceAll("\\D", ""))) {
            return true;
        }
        Set<String> groupSubjects = TWELFTH_GROUP_SUBJECTS.get(normalise(groupName));
        if (groupSubjects == null) {
            return true; // custom group name: offer everything, admin can trim
        }
        String subject = normalise(subjectName);
        boolean knownSubject = TWELFTH_GROUP_SUBJECTS.values().stream().anyMatch(s -> s.contains(subject));
        return !knownSubject || groupSubjects.contains(subject);
    }

    private static String normalise(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
