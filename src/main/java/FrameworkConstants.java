package com.swm.core.constants;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FrameworkConstants is a utility holder for application-wide constants.
 * <p>
 * This class centralizes timing and path-related constants used across the framework.
 * It performs safe initialization of file system related constants, falling back to
 * the current working directory if the system property "user.dir" is unavailable.
 * </p>
 *
 * Usage:
 * - Use the provided public constants (snake_case) or the getter methods which return Optionals.
 * - Deprecated uppercase aliases are provided for backward compatibility but should be avoided
 *   in new code.
 *
 * The class is final and has a private constructor to prevent instantiation.
 */
public final class FrameworkConstants {

    private static final Logger LOGGER = Logger.getLogger(FrameworkConstants.class.getName());

    // Timing constants (unchanged behavior)
    public static final int EXPLICIT_WAIT = 20;
    public static final int IMPLICIT_WAIT = 10;
    public static final int PAGE_LOAD_TIMEOUT = 30;

    // Internally resolved user directory (safe initialization with fallback)
    private static final String USER_DIR;
    static {
        String dir;
        try {
            dir = System.getProperty("user.dir");
            if (Objects.isNull(dir) || dir.trim().isEmpty()) {
                dir = Paths.get("").toAbsolutePath().toString();
                LOGGER.warning("System property 'user.dir' was null or empty; falling back to current working directory: " + dir);
            }
        } catch (SecurityException se) {
            dir = Paths.get("").toAbsolutePath().toString();
            LOGGER.log(Level.WARNING, "Security manager prevented access to system property 'user.dir'. Falling back to cwd: " + dir, se);
        } catch (Exception e) {
            dir = Paths.get("").toAbsolutePath().toString();
            LOGGER.log(Level.SEVERE, "Unexpected error while resolving user directory. Falling back to cwd: " + dir, e);
        }
        USER_DIR = dir;
    }

    // File separator for the current platform
    private static final String SEP = File.separator;

    /**
     * Path to the reports directory.
     */
    public static final String reports_path = Paths.get(USER_DIR, "reports").toString() + SEP;

    /**
     * Path to the logs directory.
     */
    public static final String logs_path = Paths.get(USER_DIR, "logs").toString() + SEP;

    /**
     * Path to the screenshots directory (inside reports).
     */
    public static final String screenshots_path = Paths.get(USER_DIR, "reports", "screenshots").toString() + SEP;

    /**
     * Path to the test data directory.
     */
    public static final String testdata_path = Paths.get(USER_DIR, "src", "test", "resources", "testdata").toString() + SEP;

    // Backwards-compatible (deprecated) aliases for existing public constants.
    // Keep these to preserve existing behavior while encouraging migration to snake_case names.
    /**
     * @deprecated Use {@link #reports_path} or {@link #getReportsPath()} instead.
     */
    @Deprecated
    public static final String REPORTS_PATH = reports_path;

    /**
     * @deprecated Use {@link #logs_path} or {@link #getLogsPath()} instead.
     */
    @Deprecated
    public static final String LOGS_PATH = logs_path;

    /**
     * @deprecated Use {@link #screenshots_path} or {@link #getScreenshotsPath()} instead.
     */
    @Deprecated
    public static final String SCREENSHOTS_PATH = screenshots_path;

    /**
     * @deprecated Use {@link #testdata_path} or {@link #getTestdataPath()} instead.
     */
    @Deprecated
    public static final String TESTDATA_PATH = testdata_path;

    // Private constructor to prevent instantiation
    private FrameworkConstants() {
        throw new AssertionError("FrameworkConstants is a utility class and must not be instantiated");
    }

    /**
     * Returns an Optional wrapping the reports path.
     *
     * @return Optional containing reports path string, empty only if initialization failed unexpectedly.
     */
    public static Optional<String> getReportsPath() {
        return Optional.ofNullable(reports_path);
    }

    /**
     * Returns an Optional wrapping the logs path.
     *
     * @return Optional containing logs path string, empty only if initialization failed unexpectedly.
     */
    public static Optional<String> getLogsPath() {
        return Optional.ofNullable(logs_path);
    }

    /**
     * Returns an Optional wrapping the screenshots path.
     *
     * @return Optional containing screenshots path string, empty only if initialization failed unexpectedly.
     */
    public static Optional<String> getScreenshotsPath() {
        return Optional.ofNullable(screenshots_path);
    }

    /**
     * Returns an Optional wrapping the test data path.
     *
     * @return Optional containing test data path string, empty only if initialization failed unexpectedly.
     */
    public static Optional<String> getTestdataPath() {
        return Optional.ofNullable(testdata_path);
    }
}