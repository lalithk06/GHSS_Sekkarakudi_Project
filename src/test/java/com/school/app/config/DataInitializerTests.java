package com.school.app.config;

import com.school.app.user.Role;
import com.school.app.user.User;
import com.school.app.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DataInitializer runs automatically on application/context startup (it's a
 * CommandLineRunner). It seeds ONLY the two built-in logins - the whole
 * academic structure (standards, sections/groups, subjects, mappings) is
 * created by the Admin through the Admin Dashboard, or repaired from existing
 * rows by {@link LegacyDataRepair}. These tests verify the seed is exactly
 * right and that running it again (a second app startup against the same
 * database) creates no duplicates.
 */
@SpringBootTest
@ActiveProfiles("test")
class DataInitializerTests {

    @Autowired private DataInitializer dataInitializer;
    @Autowired private UserRepository userRepository;

    @Test
    void seedsTheHeadmasterAndAdminLogins() {
        User headmaster = userRepository.findByUsername("headmaster").orElseThrow();
        User admin = userRepository.findByUsername("admin").orElseThrow();
        assertEquals(Role.HEADMASTER, headmaster.getRole());
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(headmaster.isActive());
        assertTrue(admin.isActive());
    }

    @Test
    void runningInitializerAgainCreatesNoDuplicates() {
        long usersBefore = userRepository.count();

        dataInitializer.run();
        dataInitializer.run();

        assertEquals(usersBefore, userRepository.count());
    }
}
