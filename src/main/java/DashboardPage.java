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
 * Methods that return values use Optional where nullability is possible.
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
            } catch (TimeoutException | NoSuchElementException e) {
                logger.debug("Profile icon not found/clickable for locator {}: {}", locator, e.getMessage());
                lastException = e;
            } catch (StaleElementReferenceException e) {
                logger.debug("Stale element encountered while clicking profile icon for locator {}: {}", locator, e.getMessage());
                lastException = e;
            } catch (Exception e) {
                logger.error("Unexpected error while clicking profile icon for locator " + locator, e);
                lastException = e;
            }
        }

        logger.error("Failed to click profile icon with any known locator");
        if (Objects.nonNull(lastException)) {
            throw new IllegalStateException("Unable to click profile icon", lastException);
        } else {
            throw new IllegalStateException("Unable to click profile icon: unknown reason");
        }
    }

    /**
     * Opens the profile dropdown by clicking the profile icon and waiting for the dropdown to be visible.
     *
     * @return true if the dropdown became visible, false otherwise.
     */
    public boolean openProfileDropdown() {
        try {
            clickProfileIcon();
            WebElement dropdown = wait.waitForElementVisible(profileDropdownLoc);
            boolean visible = dropdown != null && dropdown.isDisplayed();
            if (visible) {
                logger.info("Profile dropdown is visible after clicking the profile icon");
            } else {
                logger.warn("Profile dropdown not visible after clicking profile icon");
            }
            return visible;
        } catch (IllegalStateException e) {
            logger.error("Cannot open profile dropdown because profile icon could not be clicked", e);
            return false;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Profile dropdown not found or stale: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while opening profile dropdown", e);
            return false;
        }
    }

    /**
     * Checks whether the profile dropdown is visible.
     *
     * @return true if visible, false otherwise.
     */
    public boolean isProfileDropdownVisible() {
        try {
            WebElement el = wait.waitForElementVisible(profileDropdownLoc);
            return el != null && el.isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Profile dropdown not visible or stale while checking visibility", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while checking profile dropdown visibility", e);
            return false;
        }
    }

    /**
     * Clicks the "Profile" option from the profile dropdown.
     *
     * @return true if the action succeeded, false otherwise.
     */
    public boolean clickProfileOption() {
        try {
            if (!isProfileDropdownVisible()) {
                boolean opened = openProfileDropdown();
                if (!opened) {
                    logger.warn("Cannot click Profile option because profile dropdown could not be opened");
                    return false;
                }
            }

            WebElement profileOption = wait.waitForElementClickable(profileOptionLoc);
            if (profileOption != null) {
                profileOption.click();
                logger.info("Clicked Profile option in profile dropdown");
                return true;
            } else {
                logger.warn("Profile option element was null after waiting to become clickable");
                return false;
            }
        } catch (TimeoutException | NoSuchElementException e) {
            logger.debug("Profile option not found or clickable", e);
            return false;
        } catch (StaleElementReferenceException e) {
            logger.debug("Stale element when attempting to click Profile option", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while clicking Profile option", e);
            return false;
        }
    }

    /**
     * Clicks the "Logout" option from the profile dropdown.
     *
     * @return true if the action succeeded, false otherwise.
     */
    public boolean clickLogoutOption() {
        try {
            if (!isProfileDropdownVisible()) {
                boolean opened = openProfileDropdown();
                if (!opened) {
                    logger.warn("Cannot click Logout option because profile dropdown could not be opened");
                    return false;
                }
            }

            WebElement logoutOption = wait.waitForElementClickable(logoutOptionLoc);
            if (logoutOption != null) {
                logoutOption.click();
                logger.info("Clicked Logout option in profile dropdown");
                return true;
            } else {
                logger.warn("Logout option element was null after waiting to become clickable");
                return false;
            }
        } catch (TimeoutException | NoSuchElementException e) {
            logger.debug("Logout option not found or clickable", e);
            return false;
        } catch (StaleElementReferenceException e) {
            logger.debug("Stale element when attempting to click Logout option", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while clicking Logout option", e);
            return false;
        }
    }

    /**
     * Retrieves the visible text of profile dropdown options.
     *
     * @return Optional containing a list of option texts if available, otherwise Optional.empty()
     */
    public Optional<List<String>> getProfileDropdownOptions() {
        try {
            WebElement dropdown = wait.waitForElementVisible(profileDropdownLoc);
            if (dropdown == null) {
                logger.debug("Profile dropdown is not visible; cannot retrieve options");
                return Optional.empty();
            }

            List<WebElement> options = dropdown.findElements(By.xpath(".//li|.//button|.//a"));
            if (options == null || options.isEmpty()) {
                logger.debug("No options found inside profile dropdown");
                return Optional.empty();
            }

            List<String> texts = options.stream()
                    .filter(Objects::nonNull)
                    .map(el -> {
                        try {
                            return el.getText().trim();
                        } catch (StaleElementReferenceException sere) {
                            logger.debug("Stale element encountered while getting option text", sere);
                            return "";
                        } catch (Exception ex) {
                            logger.debug("Unexpected error while getting option text", ex);
                            return "";
                        }
                    })
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.toList());

            return texts.isEmpty() ? Optional.empty() : Optional.of(texts);
        } catch (TimeoutException | NoSuchElementException e) {
            logger.debug("Profile dropdown options not retrievable", e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving profile dropdown options", e);
            return Optional.empty();
        }
    }
}