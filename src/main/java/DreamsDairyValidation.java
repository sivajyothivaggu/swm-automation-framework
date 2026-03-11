package Wingify_test;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
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
        WebDriverManager.chromedriver().setup();

        try (ManagedWebDriver managed = new ManagedWebDriver(new ChromeDriver())) {
            WebDriver driver = managed.get();
            if (Objects.isNull(driver)) {
                LOGGER.severe("WebDriver instance is null after initialization. Aborting.");
                return;
            }

            try {
                driver.manage().window().maximize();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to maximize browser window", e);
            }

            Optional<List<WebElement>> optionalRows = fetchRows(driver);
            if (optionalRows.isEmpty()) {
                LOGGER.severe("No rows found in the dreams diary table. Aborting validation.");
                return;
            }

            List<WebElement> rowsList = optionalRows.get();

            // Validate number of rows
            if (rowsList.size() == 10) {
                LOGGER.info("Exactly 10 dream entries present");
            } else {
                LOGGER.warning("Expected 10 dream entries but found: " + rowsList.size());
            }

            boolean typeCheck = true;
            boolean columnCheck = true;

            // Validate each row
            for (WebElement rowElement : rowsList) {
                try {
                    List<WebElement> columnsList = rowElement.findElements(By.tagName("td"));

                    if (columnsList.size() == 3) {
                        String dreamName = safeGetText(columnsList.get(0));
                        String daysAgo = safeGetText(columnsList.get(1));
                        String dreamType = safeGetText(columnsList.get(2));

                        if (dreamName.isEmpty() || daysAgo.isEmpty() || dreamType.isEmpty()) {
                            columnCheck = false;
                            LOGGER.fine(String.format(
                                    "Missing data in row - Dream Name: '%s', Days Ago: '%s', Dream Type: '%s'",
                                    dreamName, daysAgo, dreamType));
                        }

                        if (!(dreamType.equalsIgnoreCase("Good") || dreamType.equalsIgnoreCase("Bad"))) {
                            typeCheck = false;
                            LOGGER.fine("Invalid dream type encountered: " + dreamType);
                        }
                    } else {
                        columnCheck = false;
                        LOGGER.fine("Row does not have 3 columns. Actual columns: " + columnsList.size());
                    }
                } catch (Exception e) {
                    // Continue validating remaining rows but log the issue.
                    columnCheck = false;
                    typeCheck = false;
                    LOGGER.log(Level.WARNING, "Exception while validating a row. Continuing with next row.", e);
                }
            }

            if (columnCheck) {
                LOGGER.info("All rows have Dream Name, Days Ago, and Dream Type filled");
            } else {
                LOGGER.warning("Some rows have missing data in columns");
            }

            if (typeCheck) {
                LOGGER.info("The dream types are only \"Good\" or \"Bad\".");
            } else {
                LOGGER.warning("Some dream types are invalid");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred while validating dreams diary", e);
        } finally {
            LOGGER.info("DreamsDairyValidation finished");
        }
    }

    /**
     * Fetches visible rows from the dreams diary table.
     *
     * @param driver active WebDriver
     * @return Optional containing the list of rows if found, otherwise Optional.empty()
     */
    private static Optional<List<WebElement>> fetchRows(WebDriver driver) {
        if (Objects.isNull(driver)) {
            LOGGER.severe("fetchRows was called with null WebDriver");
            return Optional.empty();
        }

        try {
            driver.get(TARGET_URL);
            WebDriverWait wait = new WebDriverWait(driver, WAIT_TIMEOUT);
            List<WebElement> rows = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//table/tbody/tr")));
            return Optional.ofNullable(rows);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch rows from the table", e);
            return Optional.empty();
        }
    }

    /**
     * Safely obtains trimmed text from a WebElement.
     *
     * @param element source WebElement
     * @return trimmed text or empty string if element or text is null or an error occurs
     */
    private static String safeGetText(WebElement element) {
        if (Objects.isNull(element)) {
            return "";
        }
        try {
            String text = element.getText();
            return text == null ? "" : text.trim();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Failed to get text from WebElement", e);
            return "";
        }
    }

    /**
     * ManagedWebDriver is a small helper that wraps a WebDriver and ensures it is
     * properly quit when closed. It implements AutoCloseable to allow use in
     * try-with-resources blocks.
     */
    private static class ManagedWebDriver implements AutoCloseable {

        private final WebDriver driver;

        /**
         * Creates a managed wrapper around the provided WebDriver.
         *
         * @param driver the WebDriver to manage
         */
        ManagedWebDriver(WebDriver driver) {
            this.driver = driver;
        }

        /**
         * Returns the underlying WebDriver instance.
         *
         * @return the managed WebDriver
         */
        WebDriver get() {
            return driver;
        }

        /**
         * Quits the underlying WebDriver. Any exceptions during quit are logged but
         * suppressed to avoid masking original exceptions.
         */
        @Override
        public void close() {
            if (Objects.isNull(driver)) {
                LOGGER.fine("No WebDriver to close");
                return;
            }
            try {
                driver.quit();
                LOGGER.fine("WebDriver quit successfully");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception while quitting WebDriver", e);
            }
        }
    }
}