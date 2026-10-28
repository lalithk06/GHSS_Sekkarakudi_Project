package com.school.app.common;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Maps a list of entities to DTOs one at a time, skipping (and logging) any
 * single record that fails to convert - typically because it points at a
 * related row that no longer exists (e.g. a teacher whose linked user row,
 * or a section/group whose standard, was deleted/edited directly in the
 * database rather than through the API).
 *
 * Without this, ONE bad legacy row would throw and take down the entire
 * list endpoint with a 500 ("An unexpected error occurred"), hiding every
 * other perfectly good record along with it.
 *
 * Skipping must never be SILENT, though: an unexplained empty dropdown is
 * exactly what this used to cause. Whenever rows are skipped the response now
 * carries an {@code X-Skipped-Records} header (read by api.js, which shows a
 * visible warning) and each skipped record is logged at WARN with its id.
 */
public final class SafeMap {
    public static final String SKIPPED_HEADER = "X-Skipped-Records";

    private static final Logger log = LoggerFactory.getLogger(SafeMap.class);

    private SafeMap() {}

    public static <T, R> List<R> map(List<T> items, Function<T, R> mapper, Function<T, String> describe) {
        List<R> result = new ArrayList<>();
        int skipped = 0;
        for (T item : items) {
            try {
                R mapped = mapper.apply(item);
                if (mapped != null) {
                    result.add(mapped);
                }
            } catch (Exception ex) {
                skipped++;
                log.warn("Skipping {} in list response - likely points at a deleted/broken related record: {}",
                        describe.apply(item), ex.toString());
            }
        }
        if (skipped > 0) {
            flagSkipped(skipped);
        }
        return result;
    }

    private static void flagSkipped(int skipped) {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletAttributes) {
                HttpServletResponse response = servletAttributes.getResponse();
                if (response != null && !response.isCommitted()) {
                    response.setHeader(SKIPPED_HEADER, String.valueOf(skipped));
                }
            }
        } catch (Exception ignored) {
            // best-effort only - never let the warning itself break the response
        }
    }
}
