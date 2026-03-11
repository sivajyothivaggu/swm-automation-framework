package com.swm.ui.pages.dashboard;

import com.swm.core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Page object representing the Executive Dashboard page.
 * <p>
 * This class provides methods to interact with and query the dashboard UI elements
 * such as the dashboard title, profile icon and profile/logout dropdown options.
 * </p>
 *
 * Notes:
 * - This class relies on BasePage to provide access to 'driver' and 'wait' utilities.
 * - Methods are defensive: they log and handle exceptions rather than propagating raw Selenium exceptions.
 */
public class DashboardPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(DashboardPage.class);

    // Locators for dashboard elements
    private final By dashboardTitle = By.xpath("//*[contains(text(), 'Executive Dashboard')]");
    private final By profileIcon = By.xpath("//button[contains(text(),'s') or contains(text(),'S')]");
    private final By profileDropdown = By.xpath("//*[contains(@class, 'dropdown') or contains(@class, 'menu')]");
    private final By profileOption = By.xpath("//*[text()='Profile']");
    private final By logoutOption = By.xpath("//*[text()='Logout']");

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
        Optional<String> optional = getCurrentUrlOptional();
        return optional.orElse("");
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
     * <p>
     * Logs each attempt and throws an IllegalStateException if none of the strategies succeed.
     * </p>
     */
    public void clickProfileIcon() {
        // Try multiple strategies to find the profile icon
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
                WebElement element = wait.waitForElementClickable(locator);
                if (element != null) {
                    element.click();
                    logger.debug("Clicked profile icon using locator: {}", locator);
                    return;
                }
            } catch (Exception e) {
                lastException = e;
                logger.debug("Locator {} did not work for profile icon: {}", locator, e.getMessage());
            }
        }

        logger.error("Could not find profile icon with any locator strategy");
        if (lastException != null) {
            throw new IllegalStateException("Could not find profile icon with any locator strategy", lastException);
        } else {
            throw new IllegalStateException("Could not find profile icon with any locator strategy");
        }
    }

    /**
     * Checks whether the dropdown (logout option) is visible.
     *
     * @return true if visible, false otherwise.
     */
    public boolean isDropdownVisible() {
        try {
            WebElement el = wait.waitForElementVisible(logoutOption);
            return el != null && el.isDisplayed();
        } catch (Exception e) {
            logger.debug("Dropdown not visible or error occurred while checking dropdown visibility", e);
            return false;
        }
    }

    /**
     * Waits until the dropdown (logout option) is visible.
     * <p>
     * If the wait utility throws an exception, it will be logged and rethrown as an IllegalStateException.
     * </p>
     */
    public void waitForDropdownVisible() {
        try {
            wait.waitForElementVisible(logoutOption);
        } catch (Exception e) {
            logger.error("Error while waiting for dropdown to be visible", e);
            throw new IllegalStateException("Error while waiting for dropdown to be visible", e);
        }
    }

    /**
     * Checks whether the "Profile" option is displayed in the dropdown.
     *
     * @return true if displayed, false otherwise.
     */
    public boolean isProfileOptionDisplayed() {
        try {
            WebElement el = wait.waitForElementVisible(profileOption);
            return el != null && el.isDisplayed();
        } catch (Exception e) {
            logger.debug("Profile option not visible or error occurred while checking profile option visibility", e);
            return false;
        }
    }

    /**
     * Checks whether the "Logout" option is displayed in the dropdown.
     *
     * @return true if displayed, false otherwise.
     */
    public boolean isLogoutOptionDisplayed() {
        try {
            WebElement el = wait.waitForElementVisible(logoutOption);
            return el != null && el.isDisplayed();
        } catch (Exception e) {
            logger.debug("Logout option not visible or error occurred while checking logout option visibility", e);
            return false;
        }
    }

    /**
     * Clicks the logout option from the dropdown.
     * <p>
     * If clicking fails, the error is logged and rethrown as an IllegalStateException.
     * </p>
     */
    public void clickLogoutFromDropdown() {
        try {
            WebElement el = wait.waitForElementClickable(logoutOption);
            if (el == null) {
                logger.error("Logout option element was not found or not clickable");
                throw new IllegalStateException("Logout option element was not found or not clickable");
            }
            el.click();
            logger.debug("Clicked logout option from dropdown");
        } catch (Exception e) {
            logger.error("Failed to click logout option from dropdown", e);
            throw new IllegalStateException("Failed to click logout option from dropdown", e);
        }
    }
}