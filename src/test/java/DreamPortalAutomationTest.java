package TestNG;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Automated test for the Dream Portal site.
 *
 * This class uses Selenium WebDriver to navigate to the site, asserts the
 * presence and disappearance of a loading animation and verifies the visibility
 * of the "My Dreams" button.
 *
 * Improvements over the original:
 * - Replaced System.out.println statements with java.util.logging.Logger usage.
 * - Added comprehensive error handling and logging.
 * - Used Optional for nullable WebElement retrieval.
 * - Used Objects.isNull for null checks.
 * - Added JavaDoc comments and improved readability.
 */
public class DreamPortalAutomationTest {
    private static final Logger LOGGER = Logger.getLogger(DreamPortalAutomationTest.class.getName());
    private static final Duration DEFAULT_WAIT = Duration.ofSeconds(20);

    private WebDriver driver;
    private WebDriverWait wait;

    /**
     * Test setup: initializes the ChromeDriver and WebDriverWait.
     */
    @BeforeClass
    public void setup() {
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, DEFAULT_WAIT);
            LOGGER.info("WebDriver initialized successfully.");
        } catch (Exception e) {
            logException("Failed to initialize WebDriver", e);
            throw e;
        }
    }

    /**
     * Tests the Dream Portal page:
     * 1) Opens the portal URL.
     * 2) Verifies loader is present and visible.
     * 3) Waits for loader to disappear.
     * 4) Verifies "My Dreams" button is visible.
     */
    @Test
    public void testDreamPortal() {
        final String url = "https://arjitnigam.github.io/myDreams/";

        try {
            // 1. Open Dream Portal Home page
            driver.get(url);
            LOGGER.info("Dream Portal Home page opened: " + url);

            // 2. Check loader appears
            Optional<WebElement> loaderOpt = findElement(By.id("loadingAnimation"));
            Assert.assertTrue(loaderOpt.isPresent(), "Loader element should be present on the page");
            WebElement loader = loaderOpt.get();
            Assert.assertTrue(loader.isDisplayed(), "Loader should be visible on page load");
            LOGGER.info("Loader is visible on page load");

            // 3. Wait for loader to disappear
            wait.until(ExpectedConditions.invisibilityOf(loader));
            LOGGER.info("Loader disappeared successfully after wait");

            // 4. Verify My Dreams button visible
            Optional<WebElement> myDreamsBtnOpt = findElementWithVisibility(By.xpath("//button[contains(.,'My Dreams')]"));
            Assert.assertTrue(myDreamsBtnOpt.isPresent(), "My Dreams button should be present and visible");
            WebElement myDreamsBtn = myDreamsBtnOpt.get();
            Assert.assertTrue(myDreamsBtn.isDisplayed(), "My Dreams button should be visible");
            LOGGER.info("My Dreams button is visible");
        } catch (AssertionError ae) {
            // Fail the test and log assertion failures
            LOGGER.log(Level.SEVERE, "Assertion failed during testDreamPortal: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            // Log exception details and fail the test
            logException("An unexpected exception occurred during testDreamPortal", e);
            Assert.fail("Unexpected exception during testDreamPortal: " + e.getMessage());
        }
    }

    /**
     * Attempts to find an element by the provided locator using presenceOfElementLocated.
     *
     * @param locator By locator to find the element.
     * @return Optional containing the WebElement if found, otherwise Optional.empty().
     */
    private Optional<WebElement> findElement(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return Optional.ofNullable(element);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Element not found (presence): " + locator.toString(), e);
            return Optional.empty();
        }
    }

    /**
     * Attempts to find an element by the provided locator using visibilityOfElementLocated.
     *
     * @param locator By locator to find the element.
     * @return Optional containing the visible WebElement if found, otherwise Optional.empty().
     */
    private Optional<WebElement> findElementWithVisibility(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return Optional.ofNullable(element);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Element not found (visibility): " + locator.toString(), e);
            return Optional.empty();
        }
    }

    /**
     * Tear down method to quit the WebDriver instance.
     */
    @AfterClass
    public void tearDown() {
        try {
            if (!Objects.isNull(driver)) {
                driver.quit();
                LOGGER.info("WebDriver quit successfully.");
            } else {
                LOGGER.warning("WebDriver was null during teardown. Nothing to quit.");
            }
        } catch (Exception e) {
            logException("Exception while quitting WebDriver", e);
        }
    }

    /**
     * Helper to log exceptions with stack trace using try-with-resources for proper resource handling.
     *
     * @param message contextual message for the log
     * @param e       exception to log
     */
    private void logException(String message, Exception e) {
        // Use try-with-resources to capture the stack trace into a String safely.
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            LOGGER.log(Level.SEVERE, message + ": " + e.getMessage() + "\n" + sw.toString());
        } catch (Exception loggingException) {
            // Fallback in case of unexpected failure during logging
            LOGGER.log(Level.SEVERE, "Failed to capture stacktrace for exception: " + loggingException.getMessage(), loggingException);
            LOGGER.log(Level.SEVERE, message + ": " + e.getMessage(), e);
        }
    }
}