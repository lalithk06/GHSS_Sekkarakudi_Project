package com.school.app.config;

import com.school.app.user.Role;
import com.school.app.user.User;
import com.school.app.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds only the two built-in logins (Headmaster and Admin) on first startup.
 * Every other login (class teachers, subject teachers) and the entire
 * academic structure (standards, sections/groups, subjects, subject-group
 * mappings) is created by the Admin through the Admin Dashboard - nothing
 * else is pre-built, by design. Safe to run repeatedly: each step checks
 * for an existing user first.
 * Demo passwords are provided only for local development; change them
 * immediately in any shared/production environment.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDemoUsers();
    }

    private void seedDemoUsers() {
        if (!userRepository.existsByUsername("headmaster")) {
            User hm = new User();
            hm.setUsername("headmaster");
            hm.setPasswordHash(passwordEncoder.encode("Headmaster@123"));
            hm.setRole(Role.HEADMASTER);
            hm.setActive(true);
            userRepository.save(hm);
        }
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
        }
    }
}
