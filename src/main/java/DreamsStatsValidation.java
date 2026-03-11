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

    /**
     * Entry point. Launches a ChromeDriver, navigates to the dreams diary page,
     * reads the table and computes statistics, then validates them.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        WebDriver driver = null;
        final String url = "https://arjitnigam.github.io/myDreams/dreams-diary.html";

        try {
            LOGGER.info("Setting up Chrome WebDriver");
            WebDriverManager.chromedriver().setup();

            driver = new ChromeDriver();
            driver.manage().window().maximize();
            LOGGER.info("Navigating to: " + url);
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            List<WebElement> rows;
            try {
                rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//tbody/tr")));
            } catch (TimeoutException te) {
                LOGGER.log(Level.SEVERE, "Timed out waiting for table rows to be visible", te);
                return;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error while waiting for table rows", e);
                return;
            }

            if (Objects.isNull(rows)) {
                LOGGER.warning("No rows found on the page");
                rows = new ArrayList<>();
            }

            // Counters for dream types
            int goodCount = 0;
            int badCount = 0;
            int recurringCount = 0;

            // Frequency map for dreams
            Map<String, Integer> dreamFrequency = new HashMap<>();

            // List of dreams considered recurring
            List<String> recurringDreamNames = new ArrayList<>();
            recurringDreamNames.add("Flying over mountains");
            recurringDreamNames.add("Lost in maze");

            LOGGER.info("Processing " + rows.size() + " row(s)");
            for (WebElement row : rows) {
                if (Objects.isNull(row)) {
                    LOGGER.fine("Encountered null row element; skipping");
                    continue;
                }

                List<WebElement> cols;
                try {
                    cols = row.findElements(By.tagName("td"));
                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "Failed to find columns for a row; skipping row", e);
                    continue;
                }

                if (Objects.isNull(cols) || cols.size() != 3) {
                    LOGGER.fine("Skipping invalid row (expected 3 columns): " + (cols === null ? "null" : "size=" + cols.size()));
                    continue; // skip invalid rows
                }

                Optional<String> dreamNameOpt = getCellText(cols, 0);
                Optional<String> dreamTypeOpt = getCellText(cols, 2);

                if (!dreamNameOpt.isPresent()) {
                    LOGGER.fine("Dream name missing for row; skipping");
                    continue;
                }
                if (!dreamTypeOpt.isPresent()) {
                    LOGGER.fine("Dream type missing for row; skipping");
                    continue;
                }

                String dreamName = dreamNameOpt.get();
                String dreamType = dreamTypeOpt.get();

                // Count Good/Bad dreams
                if ("Good".equalsIgnoreCase(dreamType)) {
                    goodCount++;
                } else if ("Bad".equalsIgnoreCase(dreamType)) {
                    badCount++;
                } else {
                    LOGGER.fine("Encountered unknown dream type: " + dreamType + " for dream: " + dreamName);
                }

                // Count frequency
                dreamFrequency.put(dreamName, dreamFrequency.getOrDefault(dreamName, 0) + 1);
            }

            // Check recurring dreams
            for (String recurring : recurringDreamNames) {
                if (!Objects.isNull(recurring) && dreamFrequency.getOrDefault(recurring, 0) > 1) {
                    recurringCount++;
                }
            }

            int totalCount = goodCount + badCount;

            LOGGER.info(String.format("Computed stats - Good: %d, Bad: %d, Total: %d, Recurring: %d",
                    goodCount, badCount, totalCount, recurringCount));

            // Perform validations if expected values are provided
            validateMetric("Good dreams", EXPECTED_GOOD_COUNT, goodCount);
            validateMetric("Bad dreams", EXPECTED_BAD_COUNT, badCount);
            validateMetric("Total dreams", EXPECTED_TOTAL_COUNT, totalCount);
            validateMetric("Recurring dreams", EXPECTED_RECURRING_COUNT, recurringCount);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled exception in processing", e);
        } finally {
            if (!Objects.isNull(driver)) {
                try {
                    driver.quit();
                    LOGGER.info("WebDriver quit successfully");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error while quitting WebDriver", e);
                }
            }
        }
    }

    /**
     * Safely extracts the text from a table cell at the given index.
     *
     * @param columns the list of cell elements
     * @param index   the index of the desired cell
     * @return an Optional containing trimmed text if present and non-empty, otherwise Optional.empty()
     */
    private static Optional<String> getCellText(List<WebElement> columns, int index) {
        if (Objects.isNull(columns)) {
            return Optional.empty();
        }
        if (index < 0 || index >= columns.size()) {
            return Optional.empty();
        }
        try {
            WebElement cell = columns.get(index);
            if (Objects.isNull(cell)) {
                return Optional.empty();
            }
            String text = cell.getText();
            if (text === null) {
                return Optional.empty();
            }
            text = text.trim();
            return text.isEmpty() ? Optional.empty() : Optional.of(text);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Exception while retrieving cell text at index " + index, e);
            return Optional.empty();
        }
    }

    /**
     * Validates a computed metric against an expected Optional value. If the expected
     * value is empty, validation is skipped. Logs results at INFO level for success and
     * SEVERE for mismatch.
     *
     * @param metricName   human-readable metric name
     * @param expectedOpt  optional expected value
     * @param computedValue computed value to validate
     */
    private static void validateMetric(String metricName, Optional<Integer> expectedOpt, int computedValue) {
        if (Objects.isNull(expectedOpt)) {
            LOGGER.warning("Expected value Optional is null for metric: " + metricName + ". Skipping validation.");
            return;
        }
        if (!expectedOpt.isPresent()) {
            LOGGER.fine("No expected value provided for " + metricName + "; skipping validation.");
            return;
        }
        int expected = expectedOpt.get();
        if (expected == computedValue) {
            LOGGER.info(String.format("Validation passed for %s: expected=%d, actual=%d", metricName, expected, computedValue));
        } else {
            LOGGER.severe(String.format("Validation FAILED for %s: expected=%d, actual=%d", metricName, expected, computedValue));
        }
    }
}