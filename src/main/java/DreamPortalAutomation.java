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

    private static final Logger logger = Logger.getLogger(DreamPortalAutomation.class.getName());

    private DreamPortalAutomation() {
        // Utility class - prevent instantiation.
    }

    /**
     * Entry point for the automation utility.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        // Wrap the WebDriver in an AutoCloseable so try-with-resources will quit it.
        try (AutoCloseableWebDriver auto_closeable_driver = new AutoCloseableWebDriver(new ChromeDriver())) {

            WebDriver web_driver = auto_closeable_driver.getDriver();
            if (Objects.isNull(web_driver)) {
                logger.severe("WebDriver instance is null after initialization.");
                return;
            }

            try {
                web_driver.manage().window().maximize();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to maximize the browser window.", e);
                // continue even if maximize fails
            }

            WebDriverWait web_driver_wait = new WebDriverWait(web_driver, Duration.ofSeconds(20));

            try {
                String url = "https://arjitnigam.github.io/myDreams/";
                logger.info("Navigating to URL: " + url);
                web_driver.get(url);

                By loader_by = By.xpath("//*[@id='loadingAnimation']");

                Optional<WebElement> loader_opt = findElementOptional(web_driver, loader_by, 5);

                if (loader_opt.isPresent() && loader_opt.get().isDisplayed()) {
                    logger.info("Loader is visible on page load");
                } else {
                    logger.info("Loader not visible on initial inspection");
                }

                // Wait for the loader to disappear before proceeding.
                try {
                    web_driver_wait.until(ExpectedConditions.invisibilityOfElementLocated(loader_by));
                    logger.info("Loader disappeared after wait");
                } catch (Exception waitEx) {
                    logger.log(Level.WARNING, "Timeout or error while waiting for loader to disappear.", waitEx);
                    // proceed; the next wait should fail fast if element isn't present
                }

                By my_dreams_by = By.xpath("//button[contains(.,'My Dreams')]");

                WebElement my_dreams_btn = null;
                try {
                    my_dreams_btn = web_driver_wait.until(ExpectedConditions.visibilityOfElementLocated(my_dreams_by));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to locate 'My Dreams' button within the timeout.", e);
                    // Nothing more to do if primary element is not available
                }

                if (!Objects.isNull(my_dreams_btn) && my_dreams_btn.isDisplayed()) {
                    logger.info("My Dreams button is visible");
                    String main_window = web_driver.getWindowHandle();
                    logger.finer("Current window handle captured: " + main_window);

                    try {
                        my_dreams_btn.click();
                        logger.info("Clicked 'My Dreams' button; the page should open dreams-diary.html and dreams-total.html in new tabs/windows.");
                    } catch (Exception clickEx) {
                        logger.log(Level.SEVERE, "Failed to click 'My Dreams' button.", clickEx);
                    }
                } else {
                    logger.severe("'My Dreams' button was not found or not visible; aborting click operation.");
                }

            } catch (Exception innerEx) {
                logger.log(Level.SEVERE, "An error occurred during the page automation", innerEx);
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
    private static Optional<WebElement> findElementOptional(WebDriver driver, By by, long timeoutSeconds) {

        if (Objects.isNull(driver) || Objects.isNull(by)) {
            logger.fine("Driver or locator is null in findElementOptional");
            return Optional.empty();
        }

        try {
            WebDriverWait short_wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement element = short_wait.until(ExpectedConditions.presenceOfElementLocated(by));
            return Optional.ofNullable(element);
        } catch (Exception e) {
            logger.fine("Element not found for locator " + by + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * AutoCloseable wrapper around a Selenium WebDriver. Ensures the driver is
     * quit when closed and hides cleanup logic.
     *
     * <p>
     * This small wrapper enables try-with-resources semantics for WebDriver
     * instances so resources are reliably released.
     * </p>
     */
    private static final class AutoCloseableWebDriver implements AutoCloseable {

        private final WebDriver driver;
        private boolean closed = false;

        /**
         * Construct the wrapper with a non-null WebDriver.
         *
         * @param driver the WebDriver to wrap
         * @throws IllegalArgumentException if driver is null
         */
        AutoCloseableWebDriver(WebDriver driver) {
            if (Objects.isNull(driver)) {
                throw new IllegalArgumentException("WebDriver must not be null");
            }
            this.driver = driver;
        }

        /**
         * Access the wrapped WebDriver.
         *
         * @return the wrapped WebDriver instance (never null)
         */
        WebDriver getDriver() {
            return this.driver;
        }

        /**
         * Close and quit the wrapped WebDriver instance.
         *
         * This method is safe to call multiple times.
         */
        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                if (!Objects.isNull(driver)) {
                    try {
                        driver.quit();
                        logger.fine("WebDriver quit successfully.");
                    } catch (Exception quitEx) {
                        logger.log(Level.WARNING, "Exception while quitting WebDriver.", quitEx);
                        try {
                            // Attempt a best-effort close if quit failed
                            driver.close();
                            logger.fine("WebDriver close() called as a fallback.");
                        } catch (Exception closeEx) {
                            logger.log(Level.SEVERE, "Exception while closing WebDriver as a fallback.", closeEx);
                        }
                    }
                }
            } catch (Throwable t) {
                // Catch Throwable to avoid propagation during shutdown/close
                logger.log(Level.SEVERE, "Unexpected error while closing WebDriver", t);
            }
        }
    }
}