package com.swm.ui.pages.dashboard;

import com.swm.core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
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
    private final By profileIconLocator = By.xpath("//button[contains(text(),'s') or contains(text(),'S')]");
    private final By profileDropdownLocator = By.xpath("//*[contains(@class, 'dropdown') or contains(@class, 'menu')]");
    private final By profileOptionLocator = By.xpath("//*[text()='Profile' or normalize-space()='Profile']");
    private final By logoutOptionLocator = By.xpath("//*[text()='Logout' or normalize-space()='Logout']");

    /**
     * Waits briefly to allow the dashboard to load.
     * <p>
     * This preserves the original behavior of sleeping for ~5 seconds but handles
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
        } catch (WebDriverException e) {
            logger.error("WebDriver error while retrieving current URL", e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving current URL", e);
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
            if (Objects.isNull(wait)) {
                logger.error("Wait utility is null while checking dashboard title visibility");
                return false;
            }
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

        if (Objects.isNull(wait)) {
            logger.error("Wait utility is null, cannot click profile icon");
            throw new IllegalStateException("Wait utility is null");
        }

        List<By> locators = List.of(
            profileIconLocator,
            By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'superadmin')]"),
            By.xpath("//*[contains(@class, 'profile')]//button"),
            By.xpath("//div[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'superadmin')]//ancestor::button"),
            By.xpath("//button[contains(@aria-label, 'profile') or contains(@aria-label, 'Profile') or contains(@aria-label, 'PROF')]")
        );

        for (By locator : locators) {
            try {
                logger.debug("Attempting to find and click profile icon using locator: {}", locator);
                WebElement clickable = wait.waitForElementClickable(locator);
                if (clickable != null) {
                    clickable.click();
                    logger.info("Clicked profile icon using locator: {}", locator);
                    return;
                }
            } catch (TimeoutException e) {
                logger.debug("Timed out waiting for locator {} to be clickable", locator, e);
            } catch (StaleElementReferenceException e) {
                logger.debug("Stale element for locator {}, will try next locator", locator, e);
            } catch (NoSuchElementException e) {
                logger.debug("No such element for locator {}, will try next locator", locator, e);
            } catch (Exception e) {
                logger.warn("Unexpected error when attempting to click profile icon with locator {}. Continuing to next locator.", locator, e);
            }
        }

        String err = "Unable to locate and click the profile icon using any known strategy";
        logger.error(err);
        throw new IllegalStateException(err);
    }

    /**
     * Opens the profile dropdown by clicking the profile icon and waiting for the dropdown to appear.
     *
     * @return true if dropdown became visible, false otherwise.
     */
    public boolean openProfileDropdown() {
        try {
            clickProfileIcon();
            WebElement dropdown = wait.waitForElementVisible(profileDropdownLocator);
            boolean visible = dropdown != null && dropdown.isDisplayed();
            logger.debug("Profile dropdown visible: {}", visible);
            return visible;
        } catch (IllegalStateException e) {
            logger.error("Cannot open profile dropdown due to illegal state", e);
            return false;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Profile dropdown not visible after clicking profile icon", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while opening profile dropdown", e);
            return false;
        }
    }

    /**
     * Retrieves the visible options from the profile dropdown.
     *
     * @return Optional containing list of option text values, or Optional.empty() if dropdown is not present or an error occurred.
     */
    public Optional<List<String>> getProfileDropdownOptions() {
        try {
            if (Objects.isNull(wait)) {
                logger.error("Wait utility is null while retrieving profile dropdown options");
                return Optional.empty();
            }

            WebElement dropdown = wait.waitForElementVisible(profileDropdownLocator);
            if (dropdown == null) {
                logger.debug("Profile dropdown element not present");
                return Optional.empty();
            }

            List<WebElement> items = dropdown.findElements(By.xpath(".//a | .//button | .//li"));
            List<String> options = items.stream()
                .filter(Objects::nonNull)
                .map(el -> {
                    try {
                        return el.getText().trim();
                    } catch (StaleElementReferenceException ex) {
                        logger.debug("Stale element encountered when reading option text", ex);
                        return "";
                    } catch (Exception ex) {
                        logger.warn("Unexpected error while reading option text", ex);
                        return "";
                    }
                })
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList());

            logger.debug("Profile dropdown options found: {}", options);
            return Optional.of(options);
        } catch (TimeoutException | NoSuchElementException e) {
            logger.debug("Timed out or no such element when retrieving profile dropdown options", e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving profile dropdown options", e);
            return Optional.empty();
        }
    }

    /**
     * Clicks the "Profile" option in the profile dropdown if present.
     *
     * @return true if the click was successful, false otherwise.
     */
    public boolean clickProfileOption() {
        try {
            if (!openProfileDropdown()) {
                logger.warn("Profile dropdown did not open; cannot click Profile option");
                return false;
            }
            WebElement profileOpt = wait.waitForElementClickable(profileOptionLocator);
            if (profileOpt == null) {
                logger.debug("Profile option not found or not clickable");
                return false;
            }
            profileOpt.click();
            logger.info("Clicked Profile option");
            return true;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Unable to click Profile option due to element state", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while clicking Profile option", e);
            return false;
        }
    }

    /**
     * Clicks the "Logout" option in the profile dropdown if present.
     *
     * @return true if the click was successful, false otherwise.
     */
    public boolean clickLogoutOption() {
        try {
            if (!openProfileDropdown()) {
                logger.warn("Profile dropdown did not open; cannot click Logout option");
                return false;
            }
            WebElement logoutOpt = wait.waitForElementClickable(logoutOptionLocator);
            if (logoutOpt == null) {
                logger.debug("Logout option not found or not clickable");
                return false;
            }
            logoutOpt.click();
            logger.info("Clicked Logout option");
            return true;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Unable to click Logout option due to element state", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while clicking Logout option", e);
            return false;
        }
    }
}