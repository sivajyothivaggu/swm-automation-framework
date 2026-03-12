package Wingify_test;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
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

    /**
     * Entry point for the validation.
     *
     * @param args CLI args (not used)
     */
    public static void main(String[] args) {
        LOGGER.info("Starting DreamsDairyValidation");
        try {
            WebDriverManager.chromedriver().setup();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to setup WebDriverManager: " + e.getMessage(), e);
            return;
        }

        try (ManagedWebDriver managed = new ManagedWebDriver(new ChromeDriver())) {
            WebDriver driver = managed.get();
            if (Objects.isNull(driver)) {
                LOGGER.severe("WebDriver instance is null after initialization. Aborting.");
                return;
            }

            try {
                driver.manage().window().maximize();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to maximize browser window: " + e.getMessage(), e);
            }

            try {
                driver.get(TARGET_URL);
                Optional<List<WebElement>> maybeRows = waitForTableRows(driver);
                if (maybeRows.isEmpty()) {
                    LOGGER.severe("Table rows were not found on the page within timeout. Aborting validation.");
                    return;
                }

                List<WebElement> rows = maybeRows.get();
                validateRows(rows);
                LOGGER.info("Validation completed.");
            } catch (TimeoutException te) {
                LOGGER.log(Level.SEVERE, "Timed out waiting for page elements: " + te.getMessage(), te);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Unexpected error during validation: " + ex.getMessage(), ex);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize or close WebDriver: " + e.getMessage(), e);
        }
    }

    /**
     * Waits for table rows to become available and returns them wrapped in an
     * Optional. Uses a conservative selector that looks for table > tbody > tr.
     *
     * @param driver WebDriver instance
     * @return Optional containing list of row elements or empty if not found/timed out
     */
    private static Optional<List<WebElement>> waitForTableRows(WebDriver driver) {
        if (Objects.isNull(driver)) {
            LOGGER.severe("waitForTableRows called with null driver.");
            return Optional.empty();
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table tbody tr")));
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
            if (rows == null || rows.isEmpty()) {
                LOGGER.warning("No table rows found after wait.");
                return Optional.empty();
            }
            LOGGER.info("Found " + rows.size() + " table rows.");
            return Optional.of(rows);
        } catch (TimeoutException te) {
            LOGGER.log(Level.WARNING, "Timeout while waiting for table rows: " + te.getMessage(), te);
            return Optional.empty();
        } catch (NoSuchElementException ne) {
            LOGGER.log(Level.WARNING, "NoSuchElement while locating table rows: " + ne.getMessage(), ne);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected error while waiting for table rows: " + e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Validates the given table rows according to the rules:
     * - Exactly 10 rows in the table
     * - Each row has exactly 3 non-empty columns
     * - Dream Type (third column) must be "Good" or "Bad"
     *
     * @param rows list of table row WebElements
     */
    private static void validateRows(List<WebElement> rows) {
        if (Objects.isNull(rows)) {
            LOGGER.severe("validateRows called with null rows list.");
            return;
        }

        if (rows.size() != 10) {
            LOGGER.severe("Validation failed: Expected exactly 10 rows but found " + rows.size());
        } else {
            LOGGER.info("Row count validation passed (10 rows).");
        }

        int rowIndex = 0;
        for (WebElement row : rows) {
            rowIndex++;
            try {
                List<WebElement> cols = row.findElements(By.tagName("td"));
                if (cols == null) {
                    LOGGER.severe("Row " + rowIndex + " returned null columns list.");
                    continue;
                }

                if (cols.size() != 3) {
                    LOGGER.severe("Row " + rowIndex + " validation failed: Expected 3 columns but found " + cols.size());
                    continue;
                }

                String dreamName = safeGetText(cols.get(0));
                String daysAgo = safeGetText(cols.get(1));
                String dreamType = safeGetText(cols.get(2));

                if (dreamName.isEmpty()) {
                    LOGGER.severe("Row " + rowIndex + " validation failed: Dream Name is empty.");
                }
                if (daysAgo.isEmpty()) {
                    LOGGER.severe("Row " + rowIndex + " validation failed: Days Ago is empty.");
                }
                if (dreamType.isEmpty()) {
                    LOGGER.severe("Row " + rowIndex + " validation failed: Dream Type is empty.");
                } else {
                    String normalized = dreamType.trim();
                    if (!"Good".equalsIgnoreCase(normalized) && !"Bad".equalsIgnoreCase(normalized)) {
                        LOGGER.severe("Row " + rowIndex + " validation failed: Dream Type must be 'Good' or 'Bad' but was '" + dreamType + "'.");
                    } else {
                        LOGGER.fine("Row " + rowIndex + " Dream Type is valid: " + dreamType);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception while validating row " + rowIndex + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * Safely returns the trimmed text of a WebElement or an empty string if the
     * element is null or text retrieval fails.
     *
     * @param element WebElement to extract text from
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
            LOGGER.log(Level.WARNING, "Failed to get text from element: " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * ManagedWebDriver is a simple AutoCloseable wrapper around WebDriver to
     * ensure proper shutdown (quit) when used in try-with-resources blocks.
     */
    private static class ManagedWebDriver implements AutoCloseable {

        private final WebDriver driver;

        /**
         * Constructs a ManagedWebDriver wrapper around the provided driver.
         *
         * @param driver WebDriver instance to manage; must not be null
         */
        ManagedWebDriver(WebDriver driver) {
            this.driver = driver;
        }

        /**
         * Returns the underlying WebDriver instance.
         *
         * @return WebDriver or null if underlying driver is null
         */
        WebDriver get() {
            return this.driver;
        }

        /**
         * Ensures the WebDriver is cleanly quit. Any exceptions during quit are
         * logged but not rethrown to avoid masking primary exceptions.
         */
        @Override
        public void close() {
            if (Objects.isNull(driver)) {
                LOGGER.warning("ManagedWebDriver.close() called but driver is already null.");
                return;
            }
            try {
                driver.quit();
                LOGGER.info("WebDriver quit successfully.");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception while quitting WebDriver: " + e.getMessage(), e);
            }
        }
    }
}