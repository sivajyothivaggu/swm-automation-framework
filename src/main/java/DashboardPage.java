package com.swm.ui.pages.dashboard;

import com.swm.core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Page object representing the Executive Dashboard page.
 *
 * <p>
 * This class provides methods to interact with and query the dashboard UI elements
 * such as the dashboard title and the profile icon. It depends on BasePage for WebDriver
 * and wait utilities.
 * </p>
 *
 * <p>
 * All public methods are defensive: they log and handle exceptions rather than propagating raw Selenium exceptions.
 * Use Optional-returning methods when a nullable value is expected.
 * </p>
 */
public class DashboardPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(DashboardPage.class);

    // Locators for dashboard elements
    private final By dashboardTitle = By.xpath("//*[contains(text(), 'Executive Dashboard')]");
    private final By profileIconLoc = By.xpath("//button[contains(text(),'s') or contains(text(),'S')]");
    private final By profileDropdownLoc = By.xpath("//*[contains(@class, 'dropdown') or contains(@class, 'menu')]");
    private final By profileOptionLoc = By.xpath("//*[text()='Profile']");
    private final By logoutOptionLoc = By.xpath("//*[text()='Logout']");

    /**
     * Waits for a short period to allow the dashboard to load.
     * <p>
     * This method preserves the original behavior of sleeping for 5 seconds but handles
     * interruptions correctly by restoring the interrupt status and logging.
     * </p>
     */
    public void waitForDashboardLoad() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Thread interrupted while waiting for dashboard load", e);
        }
    }

    /**
     * Returns the current URL from the WebDriver.
     *
     * @return the current URL as a non-null String. If the driver is not available or an error occurs,
     * an empty string is returned and the error is logged.
     */
    public String getCurrentUrl() {
        return getCurrentUrlOptional().orElse("");
    }

    /**
     * Returns the current URL wrapped in an Optional.
     *
     * @return Optional containing the current URL, or Optional.empty() if driver is null or an error occurred.
     */
    public Optional<String> getCurrentUrlOptional() {
        try {
            if (Objects.isNull(driver)) {
                logger.error("WebDriver instance is null when attempting to get current URL");
                return Optional.empty();
            }
            String url = driver.getCurrentUrl();
            return Optional.ofNullable(url);
        } catch (Exception e) {
            logger.error("Failed to retrieve current URL from WebDriver", e);
            return Optional.empty();
        }
    }

    /**
     * Checks whether the dashboard title is visible.
     *
     * @return true if visible, false otherwise.
     */
    public boolean isDashboardTitleVisible() {
        try {
            WebElement el = wait.waitForElementVisible(dashboardTitle);
            return el != null && el.isDisplayed();
        } catch (Exception e) {
            logger.debug("Dashboard title not visible or error occurred while checking visibility", e);
            return false;
        }
    }

    /**
     * Attempts multiple locator strategies to click the profile icon.
     *
     * <p>
     * Logs each attempt and throws an IllegalStateException if none of the strategies succeed.
     * </p>
     *
     * @throws IllegalStateException if the WebDriver is null or no locator could be clicked
     */
    public void clickProfileIcon() {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null, cannot click profile icon");
            throw new IllegalStateException("WebDriver is null");
        }

        List<By> locators = List.of(
            By.xpath("//button[contains(text(),'s') or contains(text(),'S')]"),
            By.xpath("//button[contains(., 'superadmin')]"),
            By.xpath("//button[contains(., 'SUPERADMIN')]"),
            By.xpath("//*[contains(@class, 'profile')]//button"),
            By.xpath("//div[contains(text(), 'superadmin')]//ancestor::button"),
            By.xpath("//div[contains(text(), 'SUPERADMIN')]//ancestor::button"),
            By.xpath("//button[contains(@aria-label, 'profile')]"),
            By.cssSelector("button[class*='profile']"),
            By.xpath("//button[.//div[text()='s' or text()='S']]"),
            By.xpath("//div[text()='superadmin']/parent::*/parent::button")
        );

        Exception lastException = null;
        for (By locator : locators) {
            try {
                logger.debug("Attempting to click profile icon using locator: {}", locator);
                WebElement element = wait.waitForElementClickable(locator);
                if (element != null) {
                    element.click();
                    logger.info("Successfully clicked profile icon using locator: {}", locator);
                    return;
                } else {
                    logger.debug("Element was not clickable for locator: {}", locator);
                }
            } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
                lastException = e;
                logger.debug("Locator did not yield a clickable element: {}", locator, e);
            } catch (Exception e) {
                lastException = e;
                logger.warn("Unexpected exception when attempting to click profile icon with locator: {}", locator, e);
            }
        }

        // As a fallback, attempt the default configured profileIconLoc once more with an explicit log
        try {
            logger.debug("Attempting fallback click using configured profileIconLoc: {}", profileIconLoc);
            WebElement fallback = wait.waitForElementClickable(profileIconLoc);
            if (fallback != null) {
                fallback.click();
                logger.info("Successfully clicked profile icon using fallback locator: {}", profileIconLoc);
                return;
            }
        } catch (Exception e) {
            lastException = e;
            logger.warn("Fallback attempt to click profile icon failed using locator: {}", profileIconLoc, e);
        }

        logger.error("Failed to click profile icon with all known locators");
        if (lastException != null) {
            throw new IllegalStateException("Unable to click profile icon", lastException);
        } else {
            throw new IllegalStateException("Unable to click profile icon: no exception captured");
        }
    }
}