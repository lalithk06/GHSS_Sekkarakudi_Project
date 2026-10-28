package com.school.app.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plain unit tests (no Spring context) for which subject is offered in which group by default. */
class LegacyDataRepairRulesTests {

    @Test
    void tenthStandardOffersEverySubjectInEverySection() {
        assertTrue(LegacyDataRepair.isOfferedIn("10", "A", "Tamil"));
        assertTrue(LegacyDataRepair.isOfferedIn("10", "D", "Social Science"));
    }

    @Test
    void twelfthStandardFollowsTheGroupCurriculum() {
        assertTrue(LegacyDataRepair.isOfferedIn("12", "History", "Commerce"));
        assertFalse(LegacyDataRepair.isOfferedIn("12", "Bio-Maths", "Commerce"));
        assertTrue(LegacyDataRepair.isOfferedIn("12", "Bio-Maths", "Biology"));
        assertFalse(LegacyDataRepair.isOfferedIn("12", "Maths-Computer", "Biology"));
        assertTrue(LegacyDataRepair.isOfferedIn("12", "Bio-Computer", "Computer"));
        assertFalse(LegacyDataRepair.isOfferedIn("12", "History", "Computer"));
        assertTrue(LegacyDataRepair.isOfferedIn("12", "History", "Tamil"));
    }

    @Test
    void unrecognisedSubjectsAndGroupsAreOfferedEverywhereSoNothingStaysHidden() {
        assertTrue(LegacyDataRepair.isOfferedIn("12", "Bio-Maths", "Physical Education"));
        assertTrue(LegacyDataRepair.isOfferedIn("12", "Science Stream", "Physics"));
    }

    @Test
    void handEnteredNamesStillMatch() {
        assertTrue(LegacyDataRepair.isOfferedIn("12th", " bio-maths ", "BIOLOGY"));
        assertFalse(LegacyDataRepair.isOfferedIn("Class 12", "History", "mathematics"));
    }
}
