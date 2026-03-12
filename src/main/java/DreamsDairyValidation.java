package Wingify_test;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * DreamsDairyValidation validates the contents of the dreams diary table on the
 * provided webpage. It checks:
 * - There are exactly 10 rows in the table.
 * - Each row contains three non-empty columns: Dream Name, Days Ago, Dream Type.
 * - Dream Type is either "Good" or "Bad".
 *
 * This class uses a managed WebDriver to ensure proper shutdown and provides
 * comprehensive logging and error handling for production readiness.
 */
public class DreamsDairyValidation {

    private static final Logger LOGGER = Logger.getLogger(DreamsDairyValidation.class.getName());
    private static final String TARGET_URL = "https://arjitnigam.github.io/myDreams/dreams-diary.html";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final int EXPECTED_ROW_COUNT = 10;

    /**
     * Entry point for the validation.
     *
     * @param args CLI args (not used)
     */
    public static void main(String[] args) {
        LOGGER.info("Starting DreamsDairyValidation");
        WebDriverManager.chromedriver().setup();

        try (ManagedWebDriver managed = new ManagedWebDriver(new ChromeDriver())) {
            Optional<WebDriver> optionalDriver = managed.get();
            if (optionalDriver.isEmpty()) {
                LOGGER.severe("WebDriver instance is not present after initialization. Aborting.");
                return;
            }

            WebDriver driver = optionalDriver.get();

            try {
                // maximize may throw on headless or remote setups; handle gracefully
                try {
                    driver.manage().window().maximize();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to maximize browser window. Continuing without maximize.", e);
                }

                boolean valid = validateDreamsTable(driver);
                if (valid) {
                    LOGGER.info("Dreams diary validation succeeded.");
                } else {
                    LOGGER.warning("Dreams diary validation failed. See logs for details.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error during validation", e);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize ManagedWebDriver", e);
        } finally {
            LOGGER.info("DreamsDairyValidation finished.");
        }
    }

    /**
     * Validates the dreams diary table on the target URL.
     *
     * @param driver the WebDriver instance to use
     * @return true if validation passes; false otherwise
     */
    private static boolean validateDreamsTable(WebDriver driver) {
        Objects.requireNonNull(driver, "WebDriver must not be null");

        try {
            driver.get(TARGET_URL);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load target URL: " + TARGET_URL, e);
            return false;
        }

        WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        try {
            // Wait until at least one table row is present in any table on the page
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table tbody tr")));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Timeout waiting for table rows to be present on the page", e);
            return false;
        }

        List<WebElement> rows;
        try {
            rows = driver.findElements(By.cssSelector("table tbody tr"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving table rows", e);
            return false;
        }

        if (Objects.isNull(rows)) {
            LOGGER.severe("Retrieved rows list is null");
            return false;
        }

        if (rows.size() != EXPECTED_ROW_COUNT) {
            LOGGER.log(Level.SEVERE, "Unexpected number of rows. Expected {0} but found {1}",
                    new Object[] { EXPECTED_ROW_COUNT, rows.size() });
            return false;
        }

        // Validate each row
        int rowIndex = 0;
        for (WebElement row : rows) {
            rowIndex++;
            try {
                if (Objects.isNull(row)) {
                    LOGGER.log(Level.SEVERE, "Row {0} is null", rowIndex);
                    return false;
                }

                List<WebElement> columns = row.findElements(By.tagName("td"));
                if (Objects.isNull(columns) || columns.size() < 3) {
                    LOGGER.log(Level.SEVERE, "Row {0} does not contain at least 3 columns. Columns found: {1}",
                            new Object[] { rowIndex, (columns == null ? "null" : columns.size()) });
                    return false;
                }

                String dreamName = safeGetText(columns.get(0));
                String daysAgo = safeGetText(columns.get(1));
                String dreamType = safeGetText(columns.get(2));

                if (dreamName.isBlank()) {
                    LOGGER.log(Level.SEVERE, "Row {0} has empty Dream Name", rowIndex);
                    return false;
                }
                if (daysAgo.isBlank()) {
                    LOGGER.log(Level.SEVERE, "Row {0} has empty Days Ago", rowIndex);
                    return false;
                }
                if (dreamType.isBlank()) {
                    LOGGER.log(Level.SEVERE, "Row {0} has empty Dream Type", rowIndex);
                    return false;
                }

                String normalizedType = dreamType.trim();
                if (!normalizedType.equalsIgnoreCase("Good") && !normalizedType.equalsIgnoreCase("Bad")) {
                    LOGGER.log(Level.SEVERE, "Row {0} has invalid Dream Type: {1}. Expected 'Good' or 'Bad'.",
                            new Object[] { rowIndex, dreamType });
                    return false;
                }

                LOGGER.log(Level.FINE, "Row {0} validated: DreamName=''{1}'', DaysAgo=''{2}'', DreamType=''{3}''",
                        new Object[] { rowIndex, dreamName, daysAgo, dreamType });

            } catch (StaleElementReferenceException sere) {
                LOGGER.log(Level.WARNING, "Stale element encountered while processing row " + rowIndex + ". Retrying once.", sere);
                // Try a single retry for transient stale element
                try {
                    List<WebElement> refreshedRows = driver.findElements(By.cssSelector("table tbody tr"));
                    if (rowIndex - 1 < refreshedRows.size()) {
                        WebElement refreshedRow = refreshedRows.get(rowIndex - 1);
                        List<WebElement> columns = refreshedRow.findElements(By.tagName("td"));
                        String dreamName = safeGetText(columns.get(0));
                        String daysAgo = safeGetText(columns.get(1));
                        String dreamType = safeGetText(columns.get(2));

                        if (dreamName.isBlank() || daysAgo.isBlank() || dreamType.isBlank()) {
                            LOGGER.log(Level.SEVERE, "Row {0} failed validation on retry due to empty columns", rowIndex);
                            return false;
                        }
                        String normalizedType = dreamType.trim();
                        if (!normalizedType.equalsIgnoreCase("Good") && !normalizedType.equalsIgnoreCase("Bad")) {
                            LOGGER.log(Level.SEVERE, "Row {0} has invalid Dream Type on retry: {1}", new Object[] { rowIndex, dreamType });
                            return false;
                        }
                    } else {
                        LOGGER.log(Level.SEVERE, "After retry, row {0} no longer exists.", rowIndex);
                        return false;
                    }
                } catch (Exception retryEx) {
                    LOGGER.log(Level.SEVERE, "Retry after StaleElementReferenceException failed for row " + rowIndex, retryEx);
                    return false;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error validating row " + rowIndex, e);
                return false;
            }
        }

        // All rows validated
        return true;
    }

    /**
     * Safely extracts trimmed text from a WebElement. Returns empty string if the
     * element or its text is null.
     *
     * @param element the WebElement to extract text from
     * @return trimmed text or empty string
     */
    private static String safeGetText(WebElement element) {
        if (Objects.isNull(element)) {
            return "";
        }
        try {
            String text = element.getText();
            return text == null ? "" : text.trim();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to retrieve text from element", e);
            return "";
        }
    }

    /**
     * ManagedWebDriver wraps a WebDriver to provide automatic cleanup and provide
     * Optional-based access to the underlying driver.
     */
    private static class ManagedWebDriver implements AutoCloseable {

        private final WebDriver driver;
        private volatile boolean closed = false;

        /**
         * Constructs a ManagedWebDriver.
         *
         * @param driver the WebDriver to manage
         */
        ManagedWebDriver(WebDriver driver) {
            this.driver = driver;
        }

        /**
         * Returns an Optional containing the managed WebDriver if available.
         *
         * @return Optional of WebDriver
         */
        Optional<WebDriver> get() {
            if (closed || Objects.isNull(driver)) {
                return Optional.empty();
            }
            return Optional.of(driver);
        }

        /**
         * Closes the managed WebDriver, attempting to quit the browser and logging any
         * issues encountered.
         */
        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (Objects.isNull(driver)) {
                LOGGER.warning("No WebDriver to close.");
                return;
            }
            try {
                driver.quit();
                LOGGER.fine("WebDriver quit successfully.");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception while quitting WebDriver", e);
                try {
                    // Try to close as a fallback
                    driver.close();
                    LOGGER.fine("WebDriver closed successfully as fallback.");
                } catch (Exception closeEx) {
                    LOGGER.log(Level.SEVERE, "Failed to close WebDriver as fallback", closeEx);
                }
            }
        }
    }
}