package com.swm.ui.pages.dashboard;

import com.swm.core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
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
 * Methods that return values use Optional where nullability is possible.
 * </p>
 */
public class DashboardPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(DashboardPage.class);

    // Locators for dashboard elements
    private final By dashboardTitle = By.xpath("//*[contains(text(), 'Executive Dashboard')]");
    private final By profileIconLoc = By.xpath("//button[contains(text(),'s') or contains(text(),'S')]");
    private final By profileDropdownLoc = By.xpath("//*[contains(@class, 'dropdown') or contains(@class, 'menu')]");
    private final By profileOptionLoc = By.xpath("//*[text()='Profile' or normalize-space(.)='Profile']");
    private final By logoutOptionLoc = By.xpath("//*[text()='Logout' or normalize-space(.)='Logout']");

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
     * Returns the current URL from the WebDriver as a non-null String.
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
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Dashboard title not visible or stale while checking visibility", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while checking dashboard title visibility", e);
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
     * @throws IllegalStateException if WebDriver is null or the profile icon cannot be clicked.
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
            By.xpath("//button[contains(@aria-label, 'profile') or contains(@aria-label, 'Profile') or contains(@aria-label, 'USER')]"),
            profileIconLoc
        );

        Exception lastException = null;
        for (By locator : locators) {
            try {
                logger.debug("Attempting to click profile icon using locator: {}", locator);
                WebElement el;
                try {
                    // Prefer explicit wait if available
                    el = wait.waitForElementClickable(locator);
                } catch (Exception e) {
                    // Fallback to direct find if wait isn't successful
                    logger.debug("waitForElementClickable failed for locator {}, falling back to findElement: {}", locator, e.getMessage());
                    el = driver.findElement(locator);
                }
                if (el != null && el.isDisplayed()) {
                    el.click();
                    logger.info("Clicked profile icon using locator: {}", locator);
                    return;
                }
            } catch (StaleElementReferenceException | NoSuchElementException | TimeoutException e) {
                logger.debug("Profile icon not found/clickable with locator {}: {}", locator, e.getMessage());
                lastException = e;
            } catch (Exception e) {
                logger.warn("Unexpected error when attempting to click profile icon with locator {}: {}", locator, e.getMessage(), e);
                lastException = e;
            }
        }

        logger.error("Failed to click profile icon using any strategy");
        if (lastException != null) {
            throw new IllegalStateException("Unable to click profile icon", lastException);
        } else {
            throw new IllegalStateException("Unable to click profile icon");
        }
    }

    /**
     * Opens the profile dropdown by clicking the profile icon and waiting for the dropdown to appear.
     *
     * @return true if the dropdown became visible, false otherwise
     */
    public boolean openProfileDropdown() {
        try {
            clickProfileIcon();
            WebElement dropdown = wait.waitForElementVisible(profileDropdownLoc);
            boolean visible = dropdown != null && dropdown.isDisplayed();
            logger.debug("Profile dropdown visibility after click: {}", visible);
            return visible;
        } catch (Exception e) {
            logger.error("Failed to open profile dropdown", e);
            return false;
        }
    }

    /**
     * Retrieves the visible options (text) from the profile dropdown.
     *
     * @return Optional containing a list of option texts. Returns Optional.empty() if driver is null or an error occurred.
     */
    public Optional<List<String>> getProfileDropdownOptions() {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null while attempting to get profile dropdown options");
            return Optional.empty();
        }

        try {
            WebElement dropdown = wait.waitForElementVisible(profileDropdownLoc);
            if (dropdown == null) {
                logger.debug("Profile dropdown not visible when attempting to retrieve options");
                return Optional.of(Collections.emptyList());
            }

            // Find common clickable/text elements inside dropdown
            List<WebElement> items = dropdown.findElements(By.xpath(".//a | .//button | .//li | .//*[@role='menuitem']"));
            List<String> texts = items.stream()
                .map(e -> {
                    try {
                        return e.getText();
                    } catch (StaleElementReferenceException sere) {
                        logger.debug("StaleElementReference while reading dropdown option text", sere);
                        return "";
                    }
                })
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

            logger.debug("Profile dropdown options found: {}", texts);
            return Optional.of(texts);
        } catch (Exception e) {
            logger.error("Error while retrieving profile dropdown options", e);
            return Optional.empty();
        }
    }

    /**
     * Clicks the "Profile" option in the profile dropdown.
     *
     * @throws IllegalStateException if driver is null or the option cannot be clicked
     */
    public void clickProfileOption() {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null, cannot click profile option");
            throw new IllegalStateException("WebDriver is null");
        }
        try {
            // Ensure dropdown open
            if (!openProfileDropdown()) {
                logger.warn("Profile dropdown did not open prior to clicking Profile option");
            }

            WebElement el = wait.waitForElementClickable(profileOptionLoc);
            if (el == null) {
                // try locating within dropdown as fallback
                WebElement dropdown = wait.waitForElementVisible(profileDropdownLoc);
                if (dropdown != null) {
                    List<WebElement> candidates = dropdown.findElements(profileOptionLoc);
                    if (!candidates.isEmpty()) {
                        el = candidates.get(0);
                    }
                }
            }

            if (el == null) {
                logger.error("Profile option element not found or not clickable");
                throw new IllegalStateException("Profile option not found");
            }

            el.click();
            logger.info("Clicked Profile option");
        } catch (Exception e) {
            logger.error("Failed to click Profile option", e);
            throw new IllegalStateException("Failed to click Profile option", e);
        }
    }

    /**
     * Clicks the "Logout" option in the profile dropdown.
     *
     * @throws IllegalStateException if driver is null or the option cannot be clicked
     */
    public void clickLogoutOption() {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null, cannot click logout option");
            throw new IllegalStateException("WebDriver is null");
        }
        try {
            // Ensure dropdown open
            if (!openProfileDropdown()) {
                logger.warn("Profile dropdown did not open prior to clicking Logout option");
            }

            WebElement el = wait.waitForElementClickable(logoutOptionLoc);
            if (el == null) {
                // try locating within dropdown as fallback
                WebElement dropdown = wait.waitForElementVisible(profileDropdownLoc);
                if (dropdown != null) {
                    List<WebElement> candidates = dropdown.findElements(logoutOptionLoc);
                    if (!candidates.isEmpty()) {
                        el = candidates.get(0);
                    }
                }
            }

            if (el == null) {
                logger.error("Logout option element not found or not clickable");
                throw new IllegalStateException("Logout option not found");
            }

            el.click();
            logger.info("Clicked Logout option");
        } catch (Exception e) {
            logger.error("Failed to click Logout option", e);
            throw new IllegalStateException("Failed to click Logout option", e);
        }
    }

    /**
     * Utility to check if a specific text option exists in the profile dropdown.
     *
     * @param optionText the visible text of the option to check
     * @return true if the option exists and is visible, false otherwise
     */
    public boolean isProfileOptionVisible(String optionText) {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null while checking profile option visibility");
            return false;
        }
        if (optionText == null || optionText.trim().isEmpty()) {
            logger.warn("Option text is null or empty when checking profile option visibility");
            return false;
        }

        try {
            if (!openProfileDropdown()) {
                logger.debug("Profile dropdown not open when checking for option '{}'", optionText);
            }
            By optionLocator = By.xpath(String.format("//*[normalize-space(text())='%s']", optionText.trim()));
            WebElement el = wait.waitForElementVisible(optionLocator);
            boolean visible = el != null && el.isDisplayed();
            logger.debug("Visibility for profile option '{}' is {}", optionText, visible);
            return visible;
        } catch (Exception e) {
            logger.error("Error while checking visibility of profile option '{}'", optionText, e);
            return false;
        }
    }
}