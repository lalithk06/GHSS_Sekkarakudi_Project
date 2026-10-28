package com.school.app.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Explains, in plain English, why a dropdown might be empty even though rows
 * exist in MySQL. Every check is an independent plain-SQL query, and any
 * failure is caught, so this can never break the dashboard or startup.
 */
@Service
public class DataHealthService {

    private static final Logger log = LoggerFactory.getLogger(DataHealthService.class);

    private final JdbcTemplate jdbcTemplate;

    public DataHealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DataHealthReport buildReport() {
        DataHealthReport report = new DataHealthReport();
        readConnectionInfo(report);

        Map<String, Long> counts = report.getCounts();
        Long standards = count("Standards", "SELECT COUNT(*) FROM standards", counts);
        Long sections = count("Sections/Groups", "SELECT COUNT(*) FROM section_groups", counts);
        Long activeSections = count("Active sections/groups",
                "SELECT COUNT(*) FROM section_groups WHERE active = TRUE", counts);
        Long subjects = count("Subjects", "SELECT COUNT(*) FROM subjects", counts);
        count("Subject-to-group mappings (active)",
                "SELECT COUNT(*) FROM subject_section_groups WHERE active = TRUE", counts);
        Long unmapped = count("Subjects not offered in any group",
                "SELECT COUNT(*) FROM subjects s WHERE NOT EXISTS (SELECT 1 FROM subject_section_groups m "
                        + "WHERE m.subject_id = s.id AND m.active = TRUE)", counts);
        Long teachers = count("Teachers", "SELECT COUNT(*) FROM teachers", counts);
        Long activeTeachers = count("Active teachers", "SELECT COUNT(*) FROM teachers WHERE active = TRUE", counts);
        count("Students", "SELECT COUNT(*) FROM students", counts);
        Long exams = count("Exams", "SELECT COUNT(*) FROM exams", counts);
        Long orphanTeachers = count("Teachers with a missing user account",
                "SELECT COUNT(*) FROM teachers t LEFT JOIN users u ON u.id = t.user_id WHERE u.id IS NULL", counts);
        Long badRoles = count("Users with an unrecognised role",
                "SELECT COUNT(*) FROM users WHERE role NOT IN "
                        + "('HEADMASTER','ADMIN','CLASS_TEACHER','SUBJECT_TEACHER')", counts);

        List<String> problems = report.getProblems();
        String where = "database '" + report.getDatabaseName() + "' (" + report.getJdbcUrl() + ")";

        if (isZero(standards)) {
            problems.add("The application found 0 standards in " + where + ". If you can see your data in MySQL "
                    + "Workbench / phpMyAdmin but not here, the app is connected to a DIFFERENT database than the one "
                    + "you are looking at (a blank one is created automatically when the name does not exist). "
                    + "Fix: set DB_URL, DB_USERNAME and DB_PASSWORD to the right database and restart. "
                    + "Tables the app can see in this database: " + report.getTables());
        } else if (isZero(sections)) {
            problems.add("Standards exist but there are no rows in section_groups, so every Section/Group "
                    + "dropdown is empty. Add them under Academic Setup -> Sections/Groups.");
        } else if (isZero(activeSections) && sections != null) {
            problems.add("All " + sections + " section/group rows are marked inactive (active = 0). "
                    + "Set section_groups.active = 1 for the ones that should be used.");
        }

        if (isZero(subjects) && !isZero(standards)) {
            problems.add("There are no subjects, so subject dropdowns are empty. Add them under "
                    + "Academic Setup -> Subjects.");
        } else if (unmapped != null && unmapped > 0) {
            problems.add(unmapped + " subject(s) are not offered in any section/group, so they never appear in "
                    + "subject dropdowns (Assignments, Marks). Fix: Academic Setup -> Subjects -> Edit and tick the "
                    + "section(s)/group(s). (Existing subjects are mapped automatically at startup.)");
        }

        if (isZero(teachers)) {
            problems.add("There are no teachers, so teacher dropdowns are empty. Add them under Teachers.");
        } else if (isZero(activeTeachers) && teachers != null) {
            problems.add("All " + teachers + " teachers are inactive (active = 0), so the teacher dropdowns "
                    + "on the Assignments page show 'No active ... teachers'.");
        }
        if (orphanTeachers != null && orphanTeachers > 0) {
            problems.add(orphanTeachers + " teacher row(s) point at a user account that no longer exists. "
                    + "They are left out of teacher lists. Delete and re-create them under Teachers.");
        }
        if (badRoles != null && badRoles > 0) {
            problems.add(badRoles + " user row(s) have a role that is not HEADMASTER, ADMIN, CLASS_TEACHER or "
                    + "SUBJECT_TEACHER (exact, upper-case), so those teachers cannot be read. Correct users.role.");
        }
        if (isZero(exams) && !isZero(standards)) {
            problems.add("There are no exams, so every Exam dropdown (Marks, Rankings, Section detail) is empty. "
                    + "Add them under Academic Setup -> Exams.");
        }
        return report;
    }

    private static boolean isZero(Long value) {
        return value != null && value == 0L;
    }

    private Long count(String label, String sql, Map<String, Long> counts) {
        Long result = null;
        try {
            result = jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception ex) {
            log.warn("Data check could not run [{}]: {}", sql, ex.toString());
        }
        counts.put(label, result);
        return result;
    }

    private void readConnectionInfo(DataHealthReport report) {
        try {
            String[] info = jdbcTemplate.execute((ConnectionCallback<String[]>) con ->
                    new String[]{con.getCatalog(), con.getMetaData().getURL()});
            if (info != null) {
                report.setDatabaseName(info[0]);
                report.setJdbcUrl(stripQuery(info[1]));
            }
        } catch (Exception ex) {
            log.warn("Data check could not read the connection info: {}", ex.toString());
        }
        try {
            List<String> tables = jdbcTemplate.execute((ConnectionCallback<List<String>>) con -> {
                List<String> names = new ArrayList<>();
                try (ResultSet rs = con.getMetaData().getTables(con.getCatalog(), null, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        names.add(rs.getString("TABLE_NAME"));
                    }
                }
                return names;
            });
            if (tables != null) {
                report.setTables(tables);
            }
        } catch (Exception ex) {
            log.warn("Data check could not list tables: {}", ex.toString());
        }
    }

    /** Drop everything after '?' so a password accidentally placed in the URL is never exposed. */
    private static String stripQuery(String url) {
        if (url == null) {
            return null;
        }
        int q = url.indexOf('?');
        return q >= 0 ? url.substring(0, q) : url;
    }
}
