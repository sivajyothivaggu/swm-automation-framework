package Wingify_test;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DreamsStatsValidation
 *
 * <p>
 * This class opens a dreams diary page, reads table rows and computes statistics:
 * number of good dreams, bad dreams, total dreams and recurring dreams. It validates
 * these counts against optional expected values and logs results.
 * </p>
 *
 * <p>
 * Improvements over a simple script:
 * - Robust null checks using Objects.isNull / Objects.nonNull
 * - Optional for nullable returns
 * - Comprehensive error handling and logging
 * - Clear method separation and documentation
 * </p>
 */
public class DreamsStatsValidation {

    private static final Logger LOGGER = Logger.getLogger(DreamsStatsValidation.class.getName());

    // Optional expected values: set these to validate against computed statistics.
    // Leave Optional.empty() to skip validation for a given metric.
    private static final Optional<Integer> EXPECTED_GOOD_COUNT = Optional.empty();
    private static final Optional<Integer> EXPECTED_BAD_COUNT = Optional.empty();
    private static final Optional<Integer> EXPECTED_TOTAL_COUNT = Optional.empty();
    private static final Optional<Integer> EXPECTED_RECURRING_COUNT = Optional.empty();

    // Default URL for the dreams diary page. Override by passing a URL as first program argument.
    private static final String DEFAULT_PAGE_URL = "https://qainterview.pythonanywhere.com/";

    // CSS selector for table rows. Adjust if the target page structure differs.
    private static final String ROW_SELECTOR = "table#dreams tbody tr";

    // Wait timeouts
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration ELEMENT_WAIT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Simple immutable container for computed statistics.
     */
    public static final class Stats {
        private final int good;
        private final int bad;
        private final int total;
        private final int recurring;

        public Stats(int good, int bad, int total, int recurring) {
            this.good = good;
            this.bad = bad;
            this.total = total;
            this.recurring = recurring;
        }

        public int getGood() {
            return good;
        }

        public int getBad() {
            return bad;
        }

        public int getTotal() {
            return total;
        }

        public int getRecurring() {
            return recurring;
        }

        @Override
        public String toString() {
            return "Stats{good=" + good + ", bad=" + bad + ", total=" + total + ", recurring=" + recurring + "}";
        }
    }

    /**
     * Main entry point. Launches a ChromeDriver, navigates to the dreams diary page,
     * reads the table and computes statistics. Validates computed metrics against
     * optionally provided expected values.
     *
     * @param args if provided, args[0] is used as the page URL instead of the default
     */
    public static void main(String[] args) {
        String pageUrl = (args != null && args.length > 0 && Objects.nonNull(args[0]) && !args[0].isBlank())
                ? args[0]
                : DEFAULT_PAGE_URL;

        WebDriverManager.chromedriver().setup();
        ChromeDriver driver = null;
        try {
            driver = new ChromeDriver();
            driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);

            LOGGER.info(() -> "Navigating to page: " + pageUrl);
            driver.get(pageUrl);

            Optional<Stats> maybeStats = fetchAndComputeStats(driver, ROW_SELECTOR);
            if (maybeStats.isPresent()) {
                Stats stats = maybeStats.get();
                LOGGER.info(() -> "Computed statistics: " + stats.toString());
                validateAndLog("Good dreams", EXPECTED_GOOD_COUNT, stats.getGood());
                validateAndLog("Bad dreams", EXPECTED_BAD_COUNT, stats.getBad());
                validateAndLog("Total dreams", EXPECTED_TOTAL_COUNT, stats.getTotal());
                validateAndLog("Recurring dreams", EXPECTED_RECURRING_COUNT, stats.getRecurring());
            } else {
                LOGGER.warning("No statistics were computed. The table may be missing or no rows were found.");
            }
        } catch (TimeoutException te) {
            LOGGER.log(Level.SEVERE, "Page load timed out: " + te.getMessage(), te);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during execution: " + e.getMessage(), e);
        } finally {
            if (Objects.nonNull(driver)) {
                try {
                    LOGGER.info("Closing WebDriver.");
                    driver.quit();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error while quitting WebDriver: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Waits for the table rows to be present, then computes statistics based on the
     * content of each row. The method is tolerant to minor DOM variations:
     * it inspects row text and increments counters when it finds known keywords.
     *
     * @param driver      active WebDriver instance
     * @param rowSelector CSS selector to locate table rows
     * @return Optional containing Stats when rows were found and processed; Optional.empty() otherwise
     */
    private static Optional<Stats> fetchAndComputeStats(WebDriver driver, String rowSelector) {
        if (Objects.isNull(driver)) {
            LOGGER.severe("WebDriver instance is null. Aborting statistics computation.");
            return Optional.empty();
        }

        List<WebElement> rows;
        try {
            WebDriverWait wait = new WebDriverWait(driver, ELEMENT_WAIT_TIMEOUT);
            rows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(rowSelector)));
        } catch (TimeoutException te) {
            LOGGER.log(Level.WARNING, "Timed out waiting for table rows using selector: " + rowSelector, te);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected error while locating table rows: " + e.getMessage(), e);
            return Optional.empty();
        }

        if (rows == null || rows.isEmpty()) {
            LOGGER.info("No table rows found with selector: " + rowSelector);
            return Optional.empty();
        }

        int good = 0;
        int bad = 0;
        int recurring = 0;
        int total = 0;

        for (WebElement row : rows) {
            if (Objects.isNull(row)) {
                // Shouldn't happen because we filtered presenceOfAllElements, but be defensive.
                continue;
            }
            String text;
            try {
                text = Optional.ofNullable(row.getText()).orElse("").toLowerCase();
            } catch (NoSuchElementException nse) {
                LOGGER.log(Level.FINE, "Skipping a row due to missing element: " + nse.getMessage(), nse);
                continue;
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Error reading row text, skipping row: " + e.getMessage(), e);
                continue;
            }

            // If the row is empty (e.g., whitespace), skip counting as a dream.
            if (text.isBlank()) {
                continue;
            }

            total++;

            // Determine good/bad by textual heuristics.
            if (text.contains("good") || text.contains("positive") || text.contains("pleasant")) {
                good++;
            } else if (text.contains("bad") || text.contains("negative") || text.contains("nightmare")) {
                bad++;
            } else {
                // If no explicit good/bad keywords, we leave counts as-is.
                // Additional heuristics can be added here if needed.
            }

            // Recurring detection
            if (text.contains("recurring") || text.contains("recurs") || text.contains("again")) {
                recurring++;
            }
        }

        return Optional.of(new Stats(good, bad, total, recurring));
    }

    /**
     * Validates an actual metric against an optional expected value. Logs the result.
     *
     * @param metricName name of the metric for logging
     * @param expected   optional expected value; if empty, validation is skipped
     * @param actual     actual computed value
     */
    private static void validateAndLog(String metricName, Optional<Integer> expected, int actual) {
        if (Objects.isNull(expected)) {
            // This would be an unusual case since expected is Optional; log and return.
            LOGGER.warning(() -> "Expected value container is null for metric: " + metricName + ". Skipping validation.");
            return;
        }

        if (expected.isPresent()) {
            int expectedValue = expected.get();
            if (expectedValue == actual) {
                LOGGER.info(() -> String.format("%s validation passed. Expected=%d, Actual=%d", metricName, expectedValue, actual));
            } else {
                LOGGER.warning(() -> String.format("%s validation FAILED. Expected=%d, Actual=%d", metricName, expectedValue, actual));
            }
        } else {
            LOGGER.info(() -> String.format("%s validation skipped (no expected value provided). Actual=%d", metricName, actual));
        }
    }
}