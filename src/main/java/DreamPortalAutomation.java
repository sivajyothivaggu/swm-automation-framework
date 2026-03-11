package wingify_test;

import java.time.Duration;
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
 * Automation utility to exercise the "My Dreams" portal page.
 *
 * <p>
 * This class launches a Chrome browser, navigates to the demo page, waits for
 * the loading animation to disappear, verifies the visibility of the "My
 * Dreams" button and clicks it. All interactions are logged and errors are
 * handled gracefully.
 * </p>
 *
 * Important implementation notes:
 * - Uses try-with-resources to ensure the browser is closed reliably.
 * - Uses Optional to represent potentially absent elements.
 * - Uses java.util.logging for logging.
 */
public final class DreamPortalAutomation {

    private static final Logger logger = Logger.getLogger(
            DreamPortalAutomation.class.getName());

    private DreamPortalAutomation() {
        // Utility class - prevent instantiation.
    }

    /**
     * Entry point.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        // Use a wrapper that implements AutoCloseable so we can use try-with-resources.
        try (AutoCloseableWebDriver auto_closeable_driver = new AutoCloseableWebDriver(
                new ChromeDriver())) {

            WebDriver web_driver = auto_closeable_driver.getDriver();
            web_driver.manage().window().maximize();

            WebDriverWait web_driver_wait = new WebDriverWait(web_driver,
                    Duration.ofSeconds(20));

            try {
                web_driver.get("https://arjitnigam.github.io/myDreams/");

                By loader_by = By.xpath("//*[@id='loadingAnimation']");

                Optional<WebElement> loader_opt = findElementOptional(web_driver,
                        loader_by, 5);

                if (loader_opt.isPresent() && loader_opt.get().isDisplayed()) {
                    logger.info("Loader is visible on page load");
                } else {
                    logger.info("Loader not visible on initial inspection");
                }

                // Wait for the loader to disappear before proceeding.
                web_driver_wait.until(ExpectedConditions
                        .invisibilityOfElementLocated(loader_by));
                logger.info("Loader disappeared after wait");

                By my_dreams_by = By.xpath("//button[contains(.,'My Dreams')]");

                WebElement my_dreams_btn = web_driver_wait.until(
                        ExpectedConditions.visibilityOfElementLocated(my_dreams_by));

                if (Objects.nonNull(my_dreams_btn) && my_dreams_btn.isDisplayed()) {
                    logger.info("My Dreams button is visible");
                }

                String main_window = web_driver.getWindowHandle();
                logger.finer("Current window handle captured: " + main_window);

                my_dreams_btn.click();
                logger.info(
                        "The page navigates and opens dreams-diary.html and dreams-total.html in new tabs/windows.");

            } catch (Exception innerEx) {
                logger.log(Level.SEVERE, "An error occurred during the page automation",
                        innerEx);
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to initialize or close the WebDriver", ex);
        }
    }

    /**
     * Attempts to locate an element using the provided driver and locator.
     *
     * @param driver         the WebDriver instance
     * @param by             the locator to use
     * @param timeoutSeconds how many seconds to wait for presence
     * @return Optional containing the found WebElement or empty if not found
     */
    private static Optional<WebElement> findElementOptional(WebDriver driver,
            By by, long timeoutSeconds) {

        if (Objects.isNull(driver) || Objects.isNull(by)) {
            logger.fine("Driver or locator is null in findElementOptional");
            return Optional.empty();
        }

        try {
            WebDriverWait short_wait = new WebDriverWait(driver,
                    Duration.ofSeconds(timeoutSeconds));
            WebElement element = short_wait.until(
                    ExpectedConditions.presenceOfElementLocated(by));
            return Optional.ofNullable(element);
        } catch (Exception e) {
            logger.fine("Element not found for locator " + by + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Simple AutoCloseable wrapper around WebDriver to allow try-with-resources
     * management. Ensures quit() is called during close.
     */
    private static final class AutoCloseableWebDriver implements AutoCloseable {

        private final WebDriver driver;

        AutoCloseableWebDriver(WebDriver driver) {
            this.driver = driver;
        }

        WebDriver getDriver() {
            return this.driver;
        }

        @Override
        public void close() {
            if (Objects.nonNull(driver)) {
                try {
                    driver.quit();
                    logger.fine("WebDriver quit successfully");
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error while quitting WebDriver", e);
                }
            }
        }
    }
}