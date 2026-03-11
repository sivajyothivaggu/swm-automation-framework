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
 * dreams. It validates these counts against expected values and logs results.
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

    /**
     * Entry point. Launches a ChromeDriver, navigates to the dreams diary page,
     * reads the table and computes statistics, then validates them.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        WebDriver driver = null;

        try {
            LOGGER.info("Setting up Chrome WebDriver");
            WebDriverManager.chromedriver().setup();

            driver = new ChromeDriver();
            driver.manage().window().maximize();
            String url = "https://arjitnigam.github.io/myDreams/dreams-diary.html";
            LOGGER.info("Navigating to: " + url);
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Wait for table rows to be visible and obtain them
            List<WebElement> rows;
            try {
                rows = wait.until(
                        ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//tbody/tr"))
                );
            } catch (TimeoutException te) {
                LOGGER.log(Level.SEVERE, "Timed out waiting for table rows to be visible", te);
                return;
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

            LOGGER.info("Processing " + (rows == null ? 0 : rows.size()) + " row(s)");
            for (WebElement row : rows) {
                if (Objects.isNull(row)) {
                    LOGGER.fine("Encountered null row element; skipping");
                    continue;
                }

                List<WebElement> cols = row.findElements(By.tagName("td"));
                if (cols == null || cols.size() != 3) {
                    LOGGER.fine("Skipping invalid row (expected 3 columns): " + cols);
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
                if (dreamFrequency.getOrDefault(recurring, 0) > 1) {
                    recurringCount++;
                }
            }

            int totalDreams = rows == null ? 0 : rows.size();

            // Log stats
            LOGGER.info(() -> "Good Dreams: " + goodCount);
            LOGGER.info(() -> "Bad Dreams: " + badCount);
            LOGGER.info(() -> "Total Dreams: " + totalDreams);
            LOGGER.info(() -> "Recurring Dreams: " + recurringCount);

            // Validate stats against expected values and log PASS/FAIL
            logValidation("Good Dreams count", goodCount, 6);
            logValidation("Bad Dreams count", badCount, 4);
            logValidation("Total Dreams count", totalDreams, 10);
            logValidation("Recurring Dreams count", recurringCount, 2);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during dreams stats validation", e);
        } finally {
            if (!Objects.isNull(driver)) {
                try {
                    driver.quit();
                    LOGGER.info("Driver quit successfully");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error while quitting the driver", e);
                }
            }
        }
    }

    /**
     * Safely retrieves the trimmed text content of the cell at the given index.
     * Returns Optional.empty() if the index is out of range or the text is null/empty.
     *
     * @param cols  list of table cell elements
     * @param index index of the desired cell
     * @return optional trimmed text of the cell
     */
    private static Optional<String> getCellText(List<WebElement> cols, int index) {
        if (Objects.isNull(cols) || index < 0 || index >= cols.size()) {
            return Optional.empty();
        }
        try {
            String raw = cols.get(index).getText();
            if (raw == null) {
                return Optional.empty();
            }
            String trimmed = raw.trim();
            return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Failed to get text from cell index " + index, e);
            return Optional.empty();
        }
    }

    /**
     * Logs a PASS/FAIL validation line for a specific metric.
     *
     * @param metricName name of the metric
     * @param actual     actual value found
     * @param expected   expected value
     */
    private static void logValidation(String metricName, int actual, int expected) {
        if (actual == expected) {
            LOGGER.info(() -> "PASS: " + metricName + " is correct");
        } else {
            LOGGER.warning(() -> "FAIL: Expected " + expected + " " + metricName + ", found " + actual);
        }
    }
}