package TestNG;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * DreamPortalAutomationTest contains UI automation tests for the Dream Portal demo site.
 *
 * <p>This class initializes a Chrome WebDriver, performs a set of validations on the page,
 * and ensures resources are properly cleaned up. Logging and robust error handling are included.
 */
public class DreamPortalAutomationTest {

    private static final Logger logger = LoggerFactory.getLogger(DreamPortalAutomationTest.class);

    // WebDriver instance used across tests
    private WebDriver driver;

    // Default wait used across operations
    private WebDriverWait wait;

    /**
     * Setup method executed once before any tests.
     * Initializes WebDriver and WebDriverWait.
     */
    @BeforeClass
    public void setup() {
        try {
            WebDriverManager.chromedriver().setup();
            this.driver = new ChromeDriver();
            this.driver.manage().window().maximize();
            this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(20));
            logger.info("WebDriver initialized and browser window maximized");
        } catch (Exception e) {
            logger.error("Failed to initialize WebDriver", e);
            // Fail fast if driver initialization fails
            Assert.fail("Failed to initialize WebDriver: " + e.getMessage());
        }
    }

    /**
     * Main test that validates:
     * 1. Opening the Dream Portal home page
     * 2. Loader visibility on page load
     * 3. Loader disappears
     * 4. My Dreams button is visible
     */
    @Test
    public void testDreamPortal() {
        final String url = "https://arjitnigam.github.io/myDreams/";

        try {
            // 1. Open Dream Portal Home page
            driver.get(url);
            logger.info("Dream Portal Home page opened: {}", url);

            // 2. Check loader appears
            Optional<WebElement> loaderOpt = findElementIfVisible(By.id("loadingAnimation"), 5);
            Assert.assertTrue(loaderOpt.isPresent() && loaderOpt.get().isDisplayed(),
                    "Loader should be visible on page load");
            logger.info("Loader is visible on page load");

            // 3. Wait for loader to disappear
            wait.until(ExpectedConditions.invisibilityOf(loaderOpt.get()));
            logger.info("Loader disappeared after wait");

            // 4. Verify My Dreams button visible
            Optional<WebElement> myDreamsBtnOpt = findElementIfVisible(By.xpath("//button[contains(.,'My Dreams')]"), 10);
            Assert.assertTrue(myDreamsBtnOpt.isPresent() && myDreamsBtnOpt.get().isDisplayed(),
                    "My Dreams button should be visible");
            logger.info("My Dreams button is visible");
        } catch (AssertionError ae) {
            logger.error("Assertion failed during testDreamPortal: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            logger.error("Unexpected error during testDreamPortal", e);
            Assert.fail("Unexpected error during testDreamPortal: " + e.getMessage());
        }
    }

    /**
     * Tear down method executed once after all tests.
     * Ensures WebDriver is quit and references cleared.
     */
    @AfterClass
    public void tearDown() {
        // Use try-with-resources style by using a small AutoCloseable wrapper to ensure quit is invoked
        if (Objects.isNull(driver)) {
            logger.warn("tearDown invoked but WebDriver was null");
            return;
        }

        try (DriverCloser closer = new DriverCloser(this.driver)) {
            // DriverCloser#close will be invoked automatically
            logger.info("Closing WebDriver via DriverCloser");
        } catch (Exception e) {
            logger.warn("Exception while closing WebDriver", e);
        } finally {
            // Clear references
            this.driver = null;
            this.wait = null;
            logger.info("Driver and wait references cleared in tearDown");
        }
    }

    /**
     * Finds a visible element located by the given locator within the specified timeout.
     *
     * @param locator the By locator to find the element
     * @param timeoutSeconds timeout in seconds to wait for visibility
     * @return Optional containing the WebElement if found and visible; Optional.empty() otherwise
     */
    private Optional<WebElement> findElementIfVisible(By locator, long timeoutSeconds) {
        if (Objects.isNull(locator)) {
            logger.debug("Locator provided to findElementIfVisible is null");
            return Optional.empty();
        }
        try {
            WebDriverWait localWait = (Objects.isNull(this.wait) || timeoutSeconds != this.wait.getDuration().getSeconds())
                    ? new WebDriverWait(this.driver, Duration.ofSeconds(timeoutSeconds))
                    : this.wait;
            WebElement element = localWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return Optional.ofNullable(element);
        } catch (Exception e) {
            logger.debug("Element not found or not visible for locator: {}. Exception: {}", locator, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Helper AutoCloseable wrapper that ensures WebDriver.quit() is called when closed.
     * This allows using try-with-resources semantics in tearDown.
     */
    private static class DriverCloser implements AutoCloseable {

        private final WebDriver driverToClose;

        DriverCloser(WebDriver driver) {
            this.driverToClose = driver;
        }

        @Override
        public void close() {
            if (Objects.isNull(this.driverToClose)) {
                logger.debug("DriverCloser.close called but driverToClose is null");
                return;
            }
            try {
                this.driverToClose.quit();
                logger.info("WebDriver quit successfully");
            } catch (Exception e) {
                logger.warn("Error while quitting WebDriver", e);
            }
        }
    }
}