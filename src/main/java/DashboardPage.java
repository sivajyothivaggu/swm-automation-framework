package com.swm.ui.pages.dashboard;

import com.swm.core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    // Locators for dashboard elements (using snake_case for field names to satisfy static analysis)
    private final By dashboard_title = By.xpath("//*[contains(text(), 'Executive Dashboard')]");
    private final By profile_icon_loc = By.xpath("//button[contains(text(),'s') or contains(text(),'S')]");
    private final By profile_dropdown_loc = By.xpath("//*[contains(@class, 'dropdown') or contains(@class, 'menu')]");
    private final By profile_option_loc = By.xpath("//*[text()='Profile']");
    private final By logout_option_loc = By.xpath("//*[text()='Logout']");

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
            WebElement el = wait.waitForElementVisible(dashboard_title);
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

        List<By> profile_icon_locators = List.of(
            By.xpath("//button[contains(text(),'s') or contains(text(),'S')]"),
            By.xpath("//button[contains(., 'superadmin')]"),
            By.xpath("//button[contains(., 'SUPERADMIN')]"),
            By.xpath("//*[contains(@class, 'profile')]//button"),
            By.xpath("//div[contains(text(), 'superadmin')]//ancestor::button"),
            By.xpath("//div[contains(text(), 'SUPERADMIN')]//ancestor::button"),
            By.xpath("//button[contains(@aria-label, 'profile') or contains(@aria-label, 'Profile')]"),
            profile_icon_loc
        );

        for (By loc : profile_icon_locators) {
            try {
                WebElement el = wait.waitForElementClickable(loc);
                if (el != null && el.isDisplayed()) {
                    el.click();
                    logger.debug("Clicked profile icon using locator: {}", loc);
                    return;
                }
            } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
                logger.debug("Profile icon locator not clickable/stale with locator {}: {}", loc, e.getMessage());
                // continue to next locator
            } catch (Exception e) {
                logger.warn("Unexpected error when attempting to click profile icon with locator {}: {}", loc, e.getMessage(), e);
            }
        }
        logger.error("Unable to locate and click the profile icon using known strategies");
        throw new IllegalStateException("Profile icon could not be clicked");
    }

    /**
     * Opens the profile dropdown by clicking the profile icon and waiting for the dropdown to be visible.
     *
     * @throws IllegalStateException if opening the dropdown fails.
     */
    public void openProfileDropdown() {
        clickProfileIcon();
        try {
            WebElement dropdown = wait.waitForElementVisible(profile_dropdown_loc);
            if (dropdown == null || !dropdown.isDisplayed()) {
                logger.error("Profile dropdown is not visible after clicking profile icon");
                throw new IllegalStateException("Profile dropdown did not appear");
            }
            logger.debug("Profile dropdown is visible");
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.error("Failed to open profile dropdown", e);
            throw new IllegalStateException("Profile dropdown did not appear", e);
        } catch (Exception e) {
            logger.error("Unexpected error while opening profile dropdown", e);
            throw new IllegalStateException("Unexpected error opening profile dropdown", e);
        }
    }

    /**
     * Returns the list of option texts present in the profile dropdown.
     *
     * @return Optional containing list of option texts; Optional.empty() if unable to retrieve.
     */
    public Optional<List<String>> getProfileDropdownOptions() {
        try {
            openProfileDropdown();
            List<WebElement> options = wait.waitForElementsVisible(profile_dropdown_loc.findElements(By.xpath(".//*")));
            if (options == null || options.isEmpty()) {
                logger.debug("No options found in profile dropdown");
                return Optional.of(new ArrayList<>());
            }
            List<String> texts = options.stream()
                .filter(Objects::nonNull)
                .map(el -> {
                    try {
                        return el.getText();
                    } catch (StaleElementReferenceException sere) {
                        logger.debug("Stale element while reading dropdown option text", sere);
                        return "";
                    } catch (Exception ex) {
                        logger.debug("Unexpected error reading dropdown option text", ex);
                        return "";
                    }
                })
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toList());
            return Optional.of(texts);
        } catch (IllegalStateException e) {
            logger.warn("Cannot get profile dropdown options because dropdown could not be opened", e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving profile dropdown options", e);
            return Optional.empty();
        }
    }

    /**
     * Clicks the "Profile" option in the profile dropdown.
     *
     * @throws IllegalStateException if the action cannot be completed.
     */
    public void clickProfileOption() {
        try {
            openProfileDropdown();
            WebElement profileEl = wait.waitForElementClickable(profile_option_loc);
            if (profileEl == null) {
                logger.error("Profile option not found in dropdown");
                throw new IllegalStateException("Profile option not found");
            }
            profileEl.click();
            logger.info("Clicked Profile option");
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.error("Failed to click Profile option", e);
            throw new IllegalStateException("Failed to click Profile option", e);
        } catch (Exception e) {
            logger.error("Unexpected error when clicking Profile option", e);
            throw new IllegalStateException("Unexpected error when clicking Profile option", e);
        }
    }

    /**
     * Clicks the "Logout" option in the profile dropdown.
     *
     * @throws IllegalStateException if the action cannot be completed.
     */
    public void clickLogoutOption() {
        try {
            openProfileDropdown();
            WebElement logoutEl = wait.waitForElementClickable(logout_option_loc);
            if (logoutEl == null) {
                logger.error("Logout option not found in dropdown");
                throw new IllegalStateException("Logout option not found");
            }
            logoutEl.click();
            logger.info("Clicked Logout option");
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.error("Failed to click Logout option", e);
            throw new IllegalStateException("Failed to click Logout option", e);
        } catch (Exception e) {
            logger.error("Unexpected error when clicking Logout option", e);
            throw new IllegalStateException("Unexpected error when clicking Logout option", e);
        }
    }
}