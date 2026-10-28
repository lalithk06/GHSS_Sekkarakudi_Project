package com.school.app.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Snapshot of what the running application actually sees in its database.
 * Built with plain SQL (not Hibernate) so it reflects the real rows, and
 * shows WHICH database the app is connected to - the first thing to check
 * when "the data is in MySQL but the dropdowns are empty".
 */
public class DataHealthReport {
    private String databaseName;
    private String jdbcUrl;
    /** Human-readable label -> row count (null when the table could not be read). */
    private Map<String, Long> counts = new LinkedHashMap<>();
    private List<String> tables = new ArrayList<>();
    /** Plain-English problems that would leave a dropdown empty, with the fix. Empty = all good. */
    private List<String> problems = new ArrayList<>();

    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }
    public Map<String, Long> getCounts() { return counts; }
    public void setCounts(Map<String, Long> counts) { this.counts = counts; }
    public List<String> getTables() { return tables; }
    public void setTables(List<String> tables) { this.tables = tables; }
    public List<String> getProblems() { return problems; }
    public void setProblems(List<String> problems) { this.problems = problems; }
}
