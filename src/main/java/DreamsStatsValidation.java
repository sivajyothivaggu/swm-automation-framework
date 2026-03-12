package Wingify_test;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DreamsStatsValidation
 *
 * <p>
 * This class opens the dreams diary page, reads the table rows and computes
 * statistics: number of good dreams, bad dreams, total dreams and recurring
 * dreams. It validates these counts against optional expected values and logs results.
 * </p>
 *
 * <p>Behavior is preserved from the original implementation but improves:</p>
 * <ul>
 *   <li>Logging instead of printing to stdout</li>
 *   <li>Robust null checks and optional handling</li>
 *   <li>Comprehensive error handling and resource cleanup</li>
 *   <li>JavaDoc and clearer code structure</li>
 * </ul>
 */
public class DreamsStatsValidation {

    private static final Logger LOGGER = Logger.getLogger(DreamsStatsValidation.class.getName());

    // Optional expected values: set these to validate against computed statistics.
    // Leave Optional.empty() to skip validation for a given metric.
    private static final Optional<Integer> EXPECTED_GOOD_COUNT = Optional.empty();
    private static final Optional<Integer> EXPECTED_BAD_COUNT = Optional.empty();
    private static final Optional<Integer> EXPECTED_TOTAL_COUNT = Optional.empty();
    private static final Optional<Integer> EXPECTED_RECURRING_COUNT = Optional.empty();

    // Default URL of the dreams diary application under test.
    private static final String DREAMS_URL = "http://qa.wingify.com/dreams";

    // CSS locators (can be adjusted to match the actual page under test)
    private static final String TABLE_SELECTOR = "table#dreamsTable";
    private static final String TABLE_ROWS_SELECTOR = TABLE_SELECTOR + " tbody tr";

    /**
     * Main entry point to run the validation.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        LOGGER.info("Starting DreamsStatsValidation");

        // Setup the driver binary; safe to call even if already configured.
        WebDriverManager.chromedriver().setup();

        // Use try-with-resources to ensure driver is closed.
        // ChromeDriver implements AutoCloseable in recent selenium versions.
        try (ChromeDriver driver = new ChromeDriver()) {
            runValidation(driver, DREAMS_URL);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal error while running validation", e);
        }

        LOGGER.info("DreamsStatsValidation finished");
    }

    /**
     * Runs the full validation flow: opens the URL, reads and computes statistics and validates
     * against expected values if present.
     *
     * @param driver the WebDriver instance to use
     * @param url    the URL of the dreams diary
     */
    public static void runValidation(WebDriver driver, String url) {
        Objects.requireNonNull(driver, "WebDriver must not be null");
        if (Objects.isNull(url) || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL must not be null or empty");
        }

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            driver.get(url);
            LOGGER.info(() -> "Navigated to " + url);

            Optional<List<WebElement>> maybeRows = findTableRows(driver, Duration.ofSeconds(10));
            if (maybeRows.isEmpty()) {
                LOGGER.severe("Could not find dreams table or table has no rows. Aborting validation.");
                return;
            }

            List<WebElement> rows = maybeRows.get();
            Stats stats = computeStats(rows);

            LOGGER.info(() -> "Computed stats: " + stats.toString());

            validateIfExpected("Good", EXPECTED_GOOD_COUNT, stats.getGoodCount());
            validateIfExpected("Bad", EXPECTED_BAD_COUNT, stats.getBadCount());
            validateIfExpected("Total", EXPECTED_TOTAL_COUNT, stats.getTotalCount());
            validateIfExpected("Recurring", EXPECTED_RECURRING_COUNT, stats.getRecurringCount());

        } catch (TimeoutException te) {
            LOGGER.log(Level.SEVERE, "Timeout while waiting for page elements", te);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during validation", e);
        } finally {
            try {
                // Attempt to quit in case try-with-resources did not (defensive).
                if (driver != null) {
                    driver.quit();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error while quitting driver", e);
            }
        }
    }

    /**
     * Finds the table rows containing dreams entries.
     *
     * @param driver  the WebDriver instance
     * @param timeout the maximum wait time
     * @return Optional of list of rows - empty if not found or no rows exist
     */
    private static Optional<List<WebElement>> findTableRows(WebDriver driver, Duration timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(TABLE_SELECTOR)));

            List<WebElement> rows = driver.findElements(By.cssSelector(TABLE_ROWS_SELECTOR));
            if (rows == null || rows.isEmpty()) {
                LOGGER.warning("Table located but contains no rows");
                return Optional.empty();
            }
            return Optional.of(rows);
        } catch (TimeoutException te) {
            LOGGER.log(Level.WARNING, "Timed out waiting for dreams table to appear", te);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while locating table rows", e);
            return Optional.empty();
        }
    }

    /**
     * Parses rows and computes statistics.
     * The implementation is resilient to small DOM differences: it searches row text
     * for keywords "good", "bad" and an indicator for recurring.
     *
     * @param rows list of WebElement rows
     * @return a Stats object with computed values
     */
    private static Stats computeStats(List<WebElement> rows) {
        int good = 0;
        int bad = 0;
        int recurring = 0;
        int total = 0;

        for (WebElement row : rows) {
            try {
                if (Objects.isNull(row)) {
                    LOGGER.finer("Encountered null row element; skipping");
                    continue;
                }

                String rowText = safeGetText(row).orElse("");

                if (rowText.isBlank()) {
                    LOGGER.finer("Empty row text; skipping");
                    continue;
                }

                total++;

                String lower = rowText.toLowerCase();
                if (lower.contains("good")) {
                    good++;
                } else if (lower.contains("bad")) {
                    bad++;
                }

                // Determine recurring: presence of the word "recurring" or an element with class 'recurring'
                if (lower.contains("recurring")) {
                    recurring++;
                } else {
                    // try to detect a child element denoting recurring (defensive)
                    try {
                        List<WebElement> recurringMarkers = row.findElements(By.cssSelector(".recurring"));
                        if (!recurringMarkers.isEmpty()) {
                            recurring++;
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.FINE, "Unable to check for recurring marker on row", e);
                    }
                }
            } catch (Exception e) {
                // Continue processing other rows even if one row fails parsing
                LOGGER.log(Level.WARNING, "Failed to parse a row; skipping", e);
            }
        }

        return new Stats(good, bad, total, recurring);
    }

    /**
     * Safely gets the text from a WebElement and returns it as Optional.
     *
     * @param element the element to get text from
     * @return Optional containing text if present, otherwise Optional.empty()
     */
    private static Optional<String> safeGetText(WebElement element) {
        if (Objects.isNull(element)) {
            return Optional.empty();
        }
        try {
            String txt = element.getText();
            return Optional.ofNullable(txt);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error retrieving text from element", e);
            return Optional.empty();
        }
    }

    /**
     * Validates a computed metric against an expected Optional value if present.
     *
     * @param name       the metric name for logging
     * @param expected   optional expected value
     * @param actual     the computed actual value
     */
    private static void validateIfExpected(String name, Optional<Integer> expected, int actual) {
        Objects.requireNonNull(name, "Metric name must not be null");
        if (expected == null) {
            // Defensive: expected should never be null, but handle gracefully
            LOGGER.warning(() -> "Expected value Optional for " + name + " is null; skipping validation");
            return;
        }

        if (expected.isPresent()) {
            int exp = expected.get();
            if (exp != actual) {
                LOGGER.severe(() -> String.format("%s count mismatch: expected=%d, actual=%d", name, exp, actual));
            } else {
                LOGGER.info(() -> String.format("%s count matches expected value: %d", name, actual));
            }
        } else {
            LOGGER.info(() -> String.format("%s count (no expected value provided): %d", name, actual));
        }
    }

    /**
     * Simple container class for statistics.
     */
    private static final class Stats {
        private final int goodCount;
        private final int badCount;
        private final int totalCount;
        private final int recurringCount;

        Stats(int goodCount, int badCount, int totalCount, int recurringCount) {
            this.goodCount = goodCount;
            this.badCount = badCount;
            this.totalCount = totalCount;
            this.recurringCount = recurringCount;
        }

        int getGoodCount() {
            return goodCount;
        }

        int getBadCount() {
            return badCount;
        }

        int getTotalCount() {
            return totalCount;
        }

        int getRecurringCount() {
            return recurringCount;
        }

        @Override
        public String toString() {
            return "Stats{" +
                    "good=" + goodCount +
                    ", bad=" + badCount +
                    ", total=" + totalCount +
                    ", recurring=" + recurringCount +
                    '}';
        }
    }
}