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
import java.util.stream.Collectors;

/**
 * Page object representing the Executive Dashboard page.
 *
 * <p>
 * This class provides methods to interact with and query the dashboard UI elements
 * such as the dashboard title, profile icon and profile/logout dropdown options.
 * It depends on BasePage for WebDriver and wait utilities.
 * </p>
 *
 * <p>
 * All public methods are defensive: they log and handle exceptions rather than propagating raw Selenium exceptions.
 * Methods return Optionals or booleans to indicate success/failure while keeping the original behavior.
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
        } catch (TimeoutException te) {
            logger.debug("Timeout while waiting for dashboard title visibility", te);
            return false;
        } catch (NoSuchElementException ne) {
            logger.debug("Dashboard title not found in DOM", ne);
            return false;
        } catch (StaleElementReferenceException se) {
            logger.debug("Dashboard title became stale while checking visibility", se);
            return false;
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
     * @throws IllegalStateException if WebDriver is null or no locator succeeded in clicking.
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
                    logger.info("Clicked profile icon using locator: {}", locator);
                    return;
                } else {
                    logger.debug("Element was null for locator: {}", locator);
                }
            } catch (TimeoutException te) {
                lastException = te;
                logger.debug("Timeout waiting for element clickable with locator: {}", locator, te);
            } catch (NoSuchElementException ne) {
                lastException = ne;
                logger.debug("No such element for locator: {}", locator, ne);
            } catch (StaleElementReferenceException se) {
                lastException = se;
                logger.debug("Stale element for locator: {}", locator, se);
            } catch (Exception e) {
                lastException = e;
                logger.debug("Unexpected error when clicking profile icon with locator: {}", locator, e);
            }
        }

        // As a fallback, try the canonical profileIconLoc if not already tried
        try {
            logger.debug("Attempting fallback click using profileIconLoc: {}", profileIconLoc);
            WebElement el = wait.waitForElementClickable(profileIconLoc);
            if (el != null) {
                el.click();
                logger.info("Clicked profile icon using fallback locator");
                return;
            }
        } catch (Exception e) {
            lastException = e;
            logger.debug("Fallback attempt failed for profile icon", e);
        }

        logger.error("Unable to click profile icon after trying multiple locators", lastException);
        throw new IllegalStateException("Unable to click profile icon", lastException);
    }

    /**
     * Ensures the profile dropdown is visible by clicking the profile icon if necessary
     * and then returns the text of the visible dropdown options.
     *
     * @return Optional containing a list of visible dropdown option texts, or Optional.empty() if not available.
     */
    public Optional<List<String>> getProfileDropdownOptions() {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null, cannot get profile dropdown options");
            return Optional.empty();
        }

        try {
            // If dropdown is not visible, try to open it
            WebElement dropdown = null;
            try {
                dropdown = wait.waitForElementVisible(profileDropdownLoc);
            } catch (Exception e) {
                logger.debug("Profile dropdown not immediately visible, will attempt to open it", e);
            }

            if (dropdown == null || !dropdown.isDisplayed()) {
                logger.debug("Profile dropdown not visible; attempting to click profile icon to open it");
                try {
                    clickProfileIcon();
                } catch (Exception e) {
                    logger.warn("Failed to open profile dropdown by clicking profile icon", e);
                    // Proceed to attempt to read elements in case dropdown opened by other means
                }
            }

            // Attempt to retrieve profile and logout options individually to increase resilience
            List<WebElement> optionElements;
            try {
                // Prefer capturing the whole dropdown's child items if possible
                WebElement visibleDropdown = wait.waitForElementVisible(profileDropdownLoc);
                optionElements = visibleDropdown.findElements(By.xpath(".//*"));
            } catch (Exception e) {
                logger.debug("Could not get full dropdown children; attempting to find known option elements", e);
                optionElements = List.of();
            }

            // If we couldn't find children via dropdown, try to locate the common options directly
            if (optionElements.isEmpty()) {
                try {
                    WebElement profileOption = wait.waitForElementVisible(profileOptionLoc);
                    WebElement logoutOption = wait.waitForElementVisible(logoutOptionLoc);
                    optionElements = List.of(profileOption, logoutOption).stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                } catch (Exception e) {
                    logger.debug("Could not find profile/logout options directly", e);
                }
            }

            List<String> texts = optionElements.stream()
                .filter(Objects::nonNull)
                .map(el -> {
                    try {
                        return el.getText() == null ? "" : el.getText().trim();
                    } catch (StaleElementReferenceException se) {
                        logger.debug("Stale element while reading option text", se);
                        return "";
                    } catch (Exception ex) {
                        logger.debug("Error while reading element text", ex);
                        return "";
                    }
                })
                .filter(t -> !t.isEmpty())
                .distinct()
                .collect(Collectors.toList());

            if (texts.isEmpty()) {
                logger.debug("No profile dropdown option texts were found");
                return Optional.empty();
            }

            logger.info("Profile dropdown options found: {}", texts);
            return Optional.of(texts);
        } catch (TimeoutException te) {
            logger.debug("Timeout while attempting to get profile dropdown options", te);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected error while getting profile dropdown options", e);
            return Optional.empty();
        }
    }

    /**
     * Attempts to select the "Profile" option from the profile dropdown.
     *
     * @return true if the Profile option was clicked successfully, false otherwise.
     */
    public boolean selectProfileOption() {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null, cannot select profile option");
            return false;
        }

        try {
            // Ensure dropdown is open
            Optional<List<String>> options = getProfileDropdownOptions();
            if (options.isEmpty()) {
                logger.debug("Profile dropdown options not available; attempting to click profile icon directly to open dropdown");
                try {
                    clickProfileIcon();
                } catch (Exception e) {
                    logger.warn("Failed to open profile dropdown", e);
                }
            }

            WebElement profileEl = wait.waitForElementClickable(profileOptionLoc);
            if (profileEl != null) {
                profileEl.click();
                logger.info("Clicked 'Profile' option in profile dropdown");
                return true;
            } else {
                logger.debug("'Profile' option element was null or not clickable");
                return false;
            }
        } catch (TimeoutException te) {
            logger.debug("Timeout while attempting to click 'Profile' option", te);
            return false;
        } catch (StaleElementReferenceException se) {
            logger.debug("'Profile' element became stale when attempting to click", se);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error when selecting 'Profile' option", e);
            return false;
        }
    }

    /**
     * Attempts to select the "Logout" option from the profile dropdown.
     *
     * @return true if the Logout option was clicked successfully, false otherwise.
     */
    public boolean selectLogoutOption() {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null, cannot select logout option");
            return false;
        }

        try {
            // Ensure dropdown is open
            Optional<List<String>> options = getProfileDropdownOptions();
            if (options.isEmpty()) {
                logger.debug("Profile dropdown options not available; attempting to click profile icon directly to open dropdown");
                try {
                    clickProfileIcon();
                } catch (Exception e) {
                    logger.warn("Failed to open profile dropdown", e);
                }
            }

            WebElement logoutEl = wait.waitForElementClickable(logoutOptionLoc);
            if (logoutEl != null) {
                logoutEl.click();
                logger.info("Clicked 'Logout' option in profile dropdown");
                return true;
            } else {
                logger.debug("'Logout' option element was null or not clickable");
                return false;
            }
        } catch (TimeoutException te) {
            logger.debug("Timeout while attempting to click 'Logout' option", te);
            return false;
        } catch (StaleElementReferenceException se) {
            logger.debug("'Logout' element became stale when attempting to click", se);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error when selecting 'Logout' option", e);
            return false;
        }
    }
}