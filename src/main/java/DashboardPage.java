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
                WebElement element = wait.waitForElementClickable(locator);
                if (Objects.nonNull(element)) {
                    element.click();
                    logger.debug("Clicked profile icon using locator: {}", locator);
                    return;
                } else {
                    logger.debug("Locator returned null element (not clickable): {}", locator);
                }
            } catch (StaleElementReferenceException | NoSuchElementException | TimeoutException e) {
                lastException = e;
                logger.debug("Attempt to click profile icon failed for locator {}: {}", locator, e.getMessage());
            } catch (Exception e) {
                lastException = e;
                logger.warn("Unexpected exception while attempting to click profile icon with locator {}: {}", locator, e.toString());
            }
        }

        logger.error("Failed to click profile icon using any known locator");
        if (Objects.nonNull(lastException)) {
            throw new IllegalStateException("Unable to click profile icon", lastException);
        } else {
            throw new IllegalStateException("Unable to click profile icon: no matching element found");
        }
    }

    /**
     * Opens the profile dropdown by clicking the profile icon and waiting for the dropdown to appear.
     *
     * @return true if the dropdown became visible, false otherwise.
     */
    public boolean openProfileDropdown() {
        try {
            clickProfileIcon();
            WebElement dropdown = wait.waitForElementVisible(profileDropdownLoc);
            boolean visible = dropdown != null && dropdown.isDisplayed();
            if (!visible) {
                logger.debug("Profile dropdown not visible after clicking profile icon");
            }
            return visible;
        } catch (Exception e) {
            logger.warn("Failed to open profile dropdown", e);
            return false;
        }
    }

    /**
     * Checks if profile dropdown is visible.
     *
     * @return true if visible, false otherwise.
     */
    public boolean isProfileDropdownVisible() {
        try {
            WebElement dropdown = wait.waitForElementVisible(profileDropdownLoc);
            return dropdown != null && dropdown.isDisplayed();
        } catch (Exception e) {
            logger.debug("Error checking profile dropdown visibility", e);
            return false;
        }
    }

    /**
     * Clicks the "Profile" option from the profile dropdown.
     *
     * @throws IllegalStateException if the option cannot be clicked.
     */
    public void clickProfileOption() {
        if (!openProfileDropdown()) {
            logger.error("Cannot open profile dropdown to click Profile option");
            throw new IllegalStateException("Profile dropdown not open");
        }

        try {
            WebElement option = wait.waitForElementClickable(profileOptionLoc);
            if (Objects.nonNull(option)) {
                option.click();
                logger.info("Clicked Profile option from dropdown");
            } else {
                logger.error("Profile option element not found or not clickable");
                throw new IllegalStateException("Profile option not found");
            }
        } catch (Exception e) {
            logger.error("Failed to click Profile option", e);
            throw new IllegalStateException("Failed to click Profile option", e);
        }
    }

    /**
     * Clicks the "Logout" option from the profile dropdown.
     *
     * @throws IllegalStateException if the option cannot be clicked.
     */
    public void clickLogoutOption() {
        if (!openProfileDropdown()) {
            logger.error("Cannot open profile dropdown to click Logout option");
            throw new IllegalStateException("Profile dropdown not open");
        }

        try {
            WebElement option = wait.waitForElementClickable(logoutOptionLoc);
            if (Objects.nonNull(option)) {
                option.click();
                logger.info("Clicked Logout option from dropdown");
            } else {
                logger.error("Logout option element not found or not clickable");
                throw new IllegalStateException("Logout option not found");
            }
        } catch (Exception e) {
            logger.error("Failed to click Logout option", e);
            throw new IllegalStateException("Failed to click Logout option", e);
        }
    }

    /**
     * Returns a list of visible texts for options found inside the profile dropdown.
     *
     * @return list of option texts, empty list if none found or on error.
     */
    public List<String> getProfileDropdownOptions() {
        try {
            WebElement dropdown = wait.waitForElementVisible(profileDropdownLoc);
            if (Objects.isNull(dropdown)) {
                logger.debug("Profile dropdown element not found when retrieving options");
                return List.of();
            }
            List<WebElement> items = dropdown.findElements(By.xpath(".//li | .//*[self::a or self::button or self::div]"));
            return items.stream()
                        .filter(Objects::nonNull)
                        .map(e -> {
                            try {
                                return e.getText() != null ? e.getText().trim() : "";
                            } catch (StaleElementReferenceException sere) {
                                logger.debug("Stale element when reading dropdown option text", sere);
                                return "";
                            }
                        })
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Failed to retrieve profile dropdown options", e);
            return List.of();
        }
    }
}