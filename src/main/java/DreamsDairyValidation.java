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
 * This class uses a managed WebDriver to ensure proper shutdown and extensive
 * logging and error handling to be production-ready.
 */
public class DreamsDairyValidation {

    private static final Logger LOGGER = Logger.getLogger(DreamsDairyValidation.class.getName());
    private static final String TARGET_URL = "https://arjitnigam.github.io/myDreams/dreams-diary.html";

    /**
     * Entry point for the validation.
     *
     * @param args CLI args (not used)
     */
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        try (ManagedWebDriver managed = new ManagedWebDriver(new ChromeDriver())) {
            WebDriver driver = managed.get();
            driver.manage().window().maximize();

            Optional<List<WebElement>> optional_rows = fetchRows(driver);
            if (optional_rows.isEmpty()) {
                LOGGER.severe("No rows found in the dreams diary table. Aborting validation.");
                return;
            }

            List<WebElement> rows_list = optional_rows.get();

            // Validate number of rows
            if (rows_list.size() == 10) {
                LOGGER.info("Exactly 10 dream entries present");
            } else {
                LOGGER.warning("Expected 10 dream entries but found: " + rows_list.size());
            }

            boolean type_check = true;
            boolean column_check = true;

            // Validate each row
            for (WebElement row_elem : rows_list) {
                List<WebElement> columns_list = row_elem.findElements(By.tagName("td"));

                if (columns_list.size() == 3) {
                    String dream_name = safeGetText(columns_list.get(0));
                    String days_ago = safeGetText(columns_list.get(1));
                    String dream_type = safeGetText(columns_list.get(2));

                    if (dream_name.isEmpty() || days_ago.isEmpty() || dream_type.isEmpty()) {
                        column_check = false;
                        LOGGER.fine(String.format("Missing data in row - Dream Name: '%s', Days Ago: '%s', Dream Type: '%s'",
                                dream_name, days_ago, dream_type));
                    }

                    if (!(dream_type.equalsIgnoreCase("Good") || dream_type.equalsIgnoreCase("Bad"))) {
                        type_check = false;
                        LOGGER.fine("Invalid dream type encountered: " + dream_type);
                    }
                } else {
                    column_check = false;
                    LOGGER.fine("Row does not have 3 columns. Actual columns: " + columns_list.size());
                }
            }

            if (column_check) {
                LOGGER.info("All rows have Dream Name, Days Ago, and Dream Type filled");
            } else {
                LOGGER.warning("Some rows have missing data in columns");
            }

            if (type_check) {
                LOGGER.info("The dream types are only \"Good\" or \"Bad\".");
            } else {
                LOGGER.warning("Some dream types are invalid");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred while validating dreams diary", e);
        }
    }

    /**
     * Fetches visible rows from the dreams diary table.
     *
     * @param driver active WebDriver
     * @return Optional containing the list of rows if found, otherwise Optional.empty()
     */
    private static Optional<List<WebElement>> fetchRows(WebDriver driver) {
        Objects.requireNonNull(driver, "WebDriver must not be null");
        try {
            driver.get(TARGET_URL);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            List<WebElement> rows = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//table/tbody/tr")));
            return Optional.ofNullable(rows);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch rows from the table", e);
            return Optional.empty();
        }
    }

    /**
     * Safely obtains trimmed text from a WebElement. Returns empty string for nulls.
     *
     * @param element WebElement to extract text from
     * @return trimmed text or empty string
     */
    private static String safeGetText(WebElement element) {
        if (Objects.isNull(element)) {
            return "";
        }
        return Optional.ofNullable(element.getText()).map(String::trim).orElse("");
    }

    /**
     * A small wrapper to allow try-with-resources behaviour for WebDriver and to ensure
     * graceful shutdown with logging on errors.
     */
    private static class ManagedWebDriver implements AutoCloseable {
        private final WebDriver driver;

        ManagedWebDriver(WebDriver driver) {
            this.driver = driver;
        }

        public WebDriver get() {
            return driver;
        }

        @Override
        public void close() {
            if (Objects.nonNull(driver)) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error while quitting WebDriver", e);
                }
            }
        }
    }
}