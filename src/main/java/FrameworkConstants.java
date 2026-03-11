package com.swm.core.constants;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FrameworkConstants is a utility holder for application-wide constants.
 *
 * <p>
 * This class centralizes timing and path-related constants used across the framework.
 * It performs safe initialization of file system related constants, falling back to
 * the current working directory if the system property "user.dir" is unavailable.
 * </p>
 *
 * <p>
 * Naming and compatibility:
 * - Primary constant names follow Java conventions (UPPER_CASE).
 * - Deprecated snake_case aliases are retained for backward compatibility.
 * </p>
 *
 * The class is final and has a private constructor to prevent instantiation.
 */
public final class FrameworkConstants {

    private static final Logger LOGGER = Logger.getLogger(FrameworkConstants.class.getName());

    // Timing constants (unchanged behavior)
    /**
     * Explicit wait time in seconds.
     */
    public static final int EXPLICIT_WAIT = 20;

    /**
     * Implicit wait time in seconds.
     */
    public static final int IMPLICIT_WAIT = 10;

    /**
     * Page load timeout in seconds.
     */
    public static final int PAGE_LOAD_TIMEOUT = 30;

    // Internally resolved user directory (safe initialization with fallback)
    private static final String USER_DIR;
    static {
        String dir = null;
        try {
            dir = System.getProperty("user.dir");
            if (Objects.isNull(dir) || dir.trim().isEmpty()) {
                dir = Paths.get("").toAbsolutePath().toString();
                LOGGER.warning("System property 'user.dir' was null or empty; falling back to current "
                        + "working directory: " + dir);
            }
        } catch (SecurityException se) {
            dir = Paths.get("").toAbsolutePath().toString();
            LOGGER.log(Level.WARNING, "Security manager prevented access to system property "
                    + "'user.dir'. Falling back to cwd: " + dir, se);
        } catch (Exception e) {
            dir = Paths.get("").toAbsolutePath().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error while resolving user directory. Falling "
                    + "back to cwd: " + dir, e);
        }
        USER_DIR = dir;
    }

    // File separator for the current platform
    private static final String SEP = File.separator;

    // Primary path constants (UPPER_CASE) - preserve original values and behavior.
    /**
     * Path to the reports directory. Always ends with the platform file separator.
     */
    public static final String REPORTS_PATH = Paths.get(USER_DIR, "reports").toString() + SEP;

    /**
     * Path to the logs directory. Always ends with the platform file separator.
     */
    public static final String LOGS_PATH = Paths.get(USER_DIR, "logs").toString() + SEP;

    /**
     * Path to the screenshots directory (inside reports). Always ends with the platform file separator.
     */
    public static final String SCREENSHOTS_PATH = Paths.get(USER_DIR, "reports", "screenshots").toString() + SEP;

    /**
     * Path to the test data directory. Always ends with the platform file separator.
     */
    public static final String TESTDATA_PATH = Paths.get(USER_DIR, "src", "test", "resources", "testdata")
            .toString() + SEP;

    // Backwards-compatible (deprecated) snake_case aliases for existing public constants.
    // Keep these to preserve existing behavior while encouraging migration to UPPER_CASE names.

    /**
     * @deprecated Use {@link #REPORTS_PATH} or {@link #getReportsPath()} instead.
     */
    @Deprecated
    public static final String reports_path = REPORTS_PATH;

    /**
     * @deprecated Use {@link #LOGS_PATH} or {@link #getLogsPath()} instead.
     */
    @Deprecated
    public static final String logs_path = LOGS_PATH;

    /**
     * @deprecated Use {@link #SCREENSHOTS_PATH} or {@link #getScreenshotsPath()} instead.
     */
    @Deprecated
    public static final String screenshots_path = SCREENSHOTS_PATH;

    /**
     * @deprecated Use {@link #TESTDATA_PATH} or {@link #getTestdataPath()} instead.
     */
    @Deprecated
    public static final String testdata_path = TESTDATA_PATH;

    // Private constructor to prevent instantiation
    private FrameworkConstants() {
        throw new AssertionError("FrameworkConstants is a utility class and must not be instantiated");
    }

    /**
     * Returns an Optional wrapping the reports path.
     *
     * @return Optional containing reports path string, empty only if unexpectedly null.
     */
    public static Optional<String> getReportsPath() {
        return Optional.ofNullable(REPORTS_PATH);
    }

    /**
     * Returns an Optional wrapping the logs path.
     *
     * @return Optional containing logs path string, empty only if unexpectedly null.
     */
    public static Optional<String> getLogsPath() {
        return Optional.ofNullable(LOGS_PATH);
    }

    /**
     * Returns an Optional wrapping the screenshots path.
     *
     * @return Optional containing screenshots path string, empty only if unexpectedly null.
     */
    public static Optional<String> getScreenshotsPath() {
        return Optional.ofNullable(SCREENSHOTS_PATH);
    }

    /**
     * Returns an Optional wrapping the test data path.
     *
     * @return Optional containing test data path string, empty only if unexpectedly null.
     */
    public static Optional<String> getTestdataPath() {
        return Optional.ofNullable(TESTDATA_PATH);
    }

    /**
     * Returns the user directory that was resolved during class initialization.
     *
     * @return Optional containing the resolved user directory, empty only if unexpectedly null.
     */
    public static Optional<String> getUserDir() {
        return Optional.ofNullable(USER_DIR);
    }

    /**
     * Returns the platform-specific file separator.
     *
     * @return the file separator string (never null).
     */
    public static String getSeparator() {
        return SEP;
    }
}