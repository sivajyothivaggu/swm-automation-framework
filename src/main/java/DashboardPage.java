package com.swm.ui.pages.dashboard;

import com.swm.core.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.seleniumTimeoutException; // Intentional incorrect import removed
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // Try using the configured locator first with the wait helper
        try {
            WebElement el = wait.waitForElementClickable(profileIconLocator);
            if (el != null) {
                el.click();
                logger.debug("Clicked profile icon using wait.waitForElementClickable");
                return;
            }
        } catch (Exception e) {
            logger.info("Primary click attempt via wait.waitForElementClickable failed, attempting alternatives", e);
        }

        // Try to find and click directly via driver
        try {
            WebElement el = driver.findElement(profileIconLocator);
            if (el != null && el.isDisplayed()) {
                try {
                    el.click();
                    logger.debug("Clicked profile icon using WebElement.click()");
                    return;
                } catch (Exception inner) {
                    logger.info("Direct WebElement.click() failed for profile icon, will attempt JS click", inner);
                }
            }
        } catch (NoSuchElementException e) {
            logger.info("Profile icon not found using direct findElement", e);
        } catch (Exception e) {
            logger.warn("Unexpected error during direct click attempt for profile icon", e);
        }

        // Final attempt: JavaScript click
        try {
            WebElement el = driver.findElement(profileIconLocator);
            if (el != null) {
                if (driver instanceof JavascriptExecutor) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                    logger.debug("Clicked profile icon using JavascriptExecutor");
                    return;
                } else {
                    logger.error("Driver does not support JavascriptExecutor for clicking profile icon");
                }
            }
        } catch (Exception e) {
            logger.warn("JavascriptExecutor attempt to click profile icon failed", e);
        }

        // All attempts failed
        logger.error("Unable to click profile icon using any strategy");
        throw new IllegalStateException("Unable to click profile icon");
    }

    /**
     * Checks if the profile dropdown is visible.
     *
     * @return true if dropdown is visible, false otherwise.
     */
    public boolean isProfileDropdownVisible() {
        try {
            if (Objects.isNull(wait)) {
                logger.error("Wait utility is null while checking profile dropdown visibility");
                return false;
            }
            WebElement el = wait.waitForElementVisible(profileDropdownLocator);
            return el != null && el.isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Profile dropdown not visible or stale", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while checking profile dropdown visibility", e);
            return false;
        }
    }

    /**
     * Returns the profile dropdown options as a list of visible text values.
     *
     * @return a List of option texts. Returns an empty list if none found or an error occurs.
     */
    public List<String> getProfileDropdownOptions() {
        try {
            if (Objects.isNull(driver)) {
                logger.error("WebDriver is null while attempting to retrieve profile dropdown options");
                return Collections.emptyList();
            }
            // Ensure dropdown is present
            List<WebElement> dropdownContainers;
            try {
                dropdownContainers = driver.findElements(profileDropdownLocator);
            } catch (Exception e) {
                logger.debug("Error finding profile dropdown containers", e);
                return Collections.emptyList();
            }

            for (WebElement container : dropdownContainers) {
                try {
                    if (container != null && container.isDisplayed()) {
                        List<WebElement> optionElements = container.findElements(By.xpath(".//*"));
                        List<String> texts = optionElements.stream()
                                .filter(Objects::nonNull)
                                .map(WebElement::getText)
                                .filter(t -> t != null && !t.trim().isEmpty())
                                .map(String::trim)
                                .collect(Collectors.toList());
                        if (!texts.isEmpty()) {
                            logger.debug("Found profile dropdown options: {}", texts);
                            return texts;
                        }
                    }
                } catch (StaleElementReferenceException sere) {
                    logger.debug("Stale element when reading a dropdown container - trying next one", sere);
                } catch (Exception e) {
                    logger.warn("Unexpected error while reading dropdown container", e);
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Unexpected error while getting profile dropdown options", e);
            return Collections.emptyList();
        }
    }

    /**
     * Clicks a profile dropdown option by locator.
     *
     * @param optionLocator the locator for the option to click
     * @return true if clicked successfully, false otherwise
     */
    private boolean clickDropdownOption(By optionLocator) {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null, cannot click dropdown option");
            return false;
        }
        try {
            // Ensure dropdown visible or attempt to open it
            if (!isProfileDropdownVisible()) {
                try {
                    clickProfileIcon();
                } catch (Exception e) {
                    logger.warn("Failed to open profile dropdown before clicking option", e);
                }
            }

            WebElement optionElement = wait != null ? wait.waitForElementClickable(optionLocator) : driver.findElement(optionLocator);
            if (optionElement != null) {
                try {
                    optionElement.click();
                    logger.debug("Clicked dropdown option located by {}", optionLocator);
                    return true;
                } catch (Exception e) {
                    logger.info("Standard click failed for option {}, attempting JS click", optionLocator, e);
                    if (driver instanceof JavascriptExecutor) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", optionElement);
                        logger.debug("Clicked dropdown option via JS for locator {}", optionLocator);
                        return true;
                    } else {
                        logger.error("Driver does not support JavascriptExecutor for clicking option {}", optionLocator);
                    }
                }
            } else {
                logger.debug("Option element was not found or not clickable for locator {}", optionLocator);
            }
        } catch (TimeoutException te) {
            logger.debug("Timed out waiting for dropdown option {}", optionLocator, te);
        } catch (NoSuchElementException nse) {
            logger.debug("Dropdown option not present: {}", optionLocator, nse);
        } catch (StaleElementReferenceException sere) {
            logger.debug("Stale dropdown option element: {}", optionLocator, sere);
        } catch (Exception e) {
            logger.error("Unexpected error clicking dropdown option: {}", optionLocator, e);
        }
        return false;
    }

    /**
     * Clicks the Profile option in the profile dropdown.
     *
     * @throws IllegalStateException if unable to click the profile option.
     */
    public void clickProfileOption() {
        boolean clicked = clickDropdownOption(profileOptionLocator);
        if (!clicked) {
            logger.error("Unable to click 'Profile' option");
            throw new IllegalStateException("Unable to click 'Profile' option");
        }
    }

    /**
     * Clicks the Logout option in the profile dropdown.
     *
     * @throws IllegalStateException if unable to click the logout option.
     */
    public void clickLogoutOption() {
        boolean clicked = clickDropdownOption(logoutOptionLocator);
        if (!clicked) {
            logger.error("Unable to click 'Logout' option");
            throw new IllegalStateException("Unable to click 'Logout' option");
        }
    }
}