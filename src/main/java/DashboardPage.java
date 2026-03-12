package com.swm.ui.pages.dashboard;

import com.swm.core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.ElementClickInterceptedException;
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
     *
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
            By.xpath("//button[contains(@aria-label, 'profile') or contains(@aria-label, 'Profile')]"),
            profileIconLoc
        );

        for (By locator : locators) {
            try {
                // Prefer waiting for visibility; if wait utility is not available for clickable, visibility is acceptable then click
                WebElement element = null;
                try {
                    element = wait.waitForElementVisible(locator);
                } catch (TimeoutException te) {
                    // fallback to trying to find element directly if wait returned timeout
                    logger.debug("Timed out waiting for locator {} to be visible; will try direct find", locator, te);
                }

                if (element == null) {
                    List<WebElement> found = driver.findElements(locator);
                    if (!found.isEmpty()) {
                        // get first visible element
                        element = found.stream().filter(WebElement::isDisplayed).findFirst().orElse(found.get(0));
                    }
                }

                if (element != null) {
                    try {
                        element.click();
                        logger.info("Clicked profile icon using locator: {}", locator);
                        return;
                    } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
                        logger.warn("Element found for locator {} but click intercepted or stale. Trying next locator.", locator, e);
                        // continue to next locator
                    } catch (Exception e) {
                        logger.debug("Unexpected exception while clicking element for locator " + locator, e);
                    }
                } else {
                    logger.debug("No element found using locator {}", locator);
                }
            } catch (Exception e) {
                logger.debug("Error trying locator {} while attempting to click profile icon", locator, e);
            }
        }

        // As a final attempt, try clicking by opening dropdown via javascript if available
        try {
            WebElement el = driver.findElement(profileIconLoc);
            if (el != null) {
                el.click();
                logger.info("Clicked profile icon using fallback profileIconLoc");
                return;
            }
        } catch (Exception e) {
            logger.debug("Fallback attempt to click profile icon failed", e);
        }

        logger.error("Unable to click profile icon with any known locator strategies");
        throw new IllegalStateException("Profile icon could not be clicked");
    }

    /**
     * Checks whether the profile dropdown is visible.
     *
     * @return true if the profile dropdown is visible, false otherwise.
     */
    public boolean isProfileDropdownVisible() {
        try {
            WebElement dropdown = wait.waitForElementVisible(profileDropdownLoc);
            return dropdown != null && dropdown.isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Profile dropdown not visible or stale while checking visibility", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while checking profile dropdown visibility", e);
            return false;
        }
    }

    /**
     * Returns the list of profile option texts present in the profile dropdown.
     *
     * @return Optional containing list of option texts if available; Optional.empty() otherwise.
     */
    public Optional<List<String>> getProfileOptions() {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null, cannot retrieve profile options");
            return Optional.empty();
        }

        try {
            WebElement dropdown = null;
            try {
                dropdown = wait.waitForElementVisible(profileDropdownLoc);
            } catch (TimeoutException te) {
                logger.debug("Timed out waiting for profile dropdown to be visible; attempting to locate directly", te);
            }

            if (dropdown == null) {
                List<WebElement> candidates = driver.findElements(profileDropdownLoc);
                if (candidates.isEmpty()) {
                    logger.debug("No profile dropdown elements found directly");
                    return Optional.of(List.of());
                }
                dropdown = candidates.get(0);
            }

            List<WebElement> optionElements = dropdown.findElements(By.xpath(".//button|.//a|.//li|.//*[self::button or self::a]"));
            List<String> options = optionElements.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

            logger.debug("Found profile options: {}", options);
            return Optional.of(options);
        } catch (StaleElementReferenceException e) {
            logger.warn("Profile dropdown became stale while collecting options", e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving profile options", e);
            return Optional.empty();
        }
    }

    /**
     * Clicks the specified option in the profile dropdown.
     *
     * @param optionName option text to click (case-insensitive)
     * @throws IllegalStateException if the option cannot be found or clicked
     */
    public void clickProfileOption(String optionName) {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null, cannot click profile option '{}'", optionName);
            throw new IllegalStateException("WebDriver is null");
        }
        if (optionName == null || optionName.trim().isEmpty()) {
            logger.error("Invalid profile option name provided: '{}'", optionName);
            throw new IllegalArgumentException("optionName must be a non-empty string");
        }

        try {
            // Ensure dropdown is visible
            if (!isProfileDropdownVisible()) {
                logger.debug("Profile dropdown not visible; attempting to click profile icon");
                try {
                    clickProfileIcon();
                } catch (Exception e) {
                    logger.error("Failed to open profile dropdown before selecting option '{}'", optionName, e);
                }
            }

            WebElement dropdown = null;
            try {
                dropdown = wait.waitForElementVisible(profileDropdownLoc);
            } catch (TimeoutException te) {
                logger.debug("Timed out waiting for profile dropdown visible while selecting option '{}'", optionName, te);
            }

            if (dropdown == null) {
                List<WebElement> candidates = driver.findElements(profileDropdownLoc);
                if (!candidates.isEmpty()) {
                    dropdown = candidates.get(0);
                }
            }

            if (dropdown == null) {
                logger.error("Profile dropdown not found while trying to select option '{}'", optionName);
                throw new IllegalStateException("Profile dropdown not found");
            }

            List<WebElement> candidates = dropdown.findElements(By.xpath(".//button|.//a|.//li|.//*[self::button or self::a]"));
            for (WebElement candidate : candidates) {
                try {
                    String text = candidate.getText();
                    if (text != null && text.trim().equalsIgnoreCase(optionName.trim()) && candidate.isDisplayed()) {
                        candidate.click();
                        logger.info("Clicked profile option '{}'", optionName);
                        return;
                    }
                } catch (StaleElementReferenceException sere) {
                    logger.debug("Stale element encountered when evaluating a profile option; continuing", sere);
                } catch (ElementClickInterceptedException eci) {
                    logger.warn("Click intercepted for option '{}', will attempt to scroll and retry", optionName, eci);
                    try {
                        // attempt to click again
                        candidate.click();
                        logger.info("Clicked profile option '{}' after retry", optionName);
                        return;
                    } catch (Exception ex) {
                        logger.debug("Retry clicking profile option '{}' failed", optionName, ex);
                    }
                } catch (Exception e) {
                    logger.debug("Unexpected error while trying option element", e);
                }
            }

            // As fallback, try locating by text globally within dropdown
            List<WebElement> globalMatches = driver.findElements(By.xpath("//*[contains(normalize-space(.), '" + escapeXPath(optionName.trim()) + "')]"));
            for (WebElement gm : globalMatches) {
                try {
                    String text = gm.getText();
                    if (text != null && text.trim().equalsIgnoreCase(optionName.trim()) && gm.isDisplayed()) {
                        gm.click();
                        logger.info("Clicked profile option '{}' using global fallback", optionName);
                        return;
                    }
                } catch (Exception e) {
                    logger.debug("Fallback global click attempt failed for option '{}'", optionName, e);
                }
            }

            logger.error("Profile option '{}' not found or could not be clicked", optionName);
            throw new IllegalStateException("Profile option '" + optionName + "' could not be clicked");
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while attempting to click profile option '{}'", optionName, e);
            throw new IllegalStateException("Error while clicking profile option", e);
        }
    }

    /**
     * Clicks the Logout option from the profile dropdown.
     *
     * @throws IllegalStateException if logout cannot be found or clicked
     */
    public void clickLogout() {
        try {
            clickProfileOption("Logout");
            logger.info("Logout clicked from profile dropdown");
        } catch (IllegalStateException e) {
            // As a fallback attempt, try to click a direct logout locator
            try {
                WebElement logoutEl = null;
                try {
                    logoutEl = wait.waitForElementVisible(logoutOptionLoc);
                } catch (TimeoutException te) {
                    logger.debug("Timed out waiting for logout locator to be visible; attempting direct find", te);
                }

                if (logoutEl == null) {
                    List<WebElement> found = driver.findElements(logoutOptionLoc);
                    if (!found.isEmpty()) {
                        logoutEl = found.get(0);
                    }
                }

                if (logoutEl != null && logoutEl.isDisplayed()) {
                    logoutEl.click();
                    logger.info("Clicked logout using fallback locator");
                    return;
                }
            } catch (Exception ex) {
                logger.debug("Fallback logout click attempt failed", ex);
            }

            logger.error("Failed to click logout via profile dropdown or fallback locators", e);
            throw new IllegalStateException("Unable to click logout", e);
        } catch (Exception e) {
            logger.error("Unexpected error during logout click", e);
            throw new IllegalStateException("Error while clicking logout", e);
        }
    }

    /**
     * Utility to escape single quotes for XPath literal construction.
     *
     * @param input raw string
     * @return escaped version suitable for embedding in XPath contains(..., '...') if possible
     */
    private static String escapeXPath(String input) {
        if (input == null) {
            return "";
        }
        // Simple escape for single quotes by using concat approach is complex; for contains() with provided usage,
        // replace single quotes with double quotes is not feasible inside single-quoted literal.
        // To keep it simple and safe for common cases, replace single quote with HTML apostrophe equivalent.
        return input.replace("'", "\u2019");
    }
}