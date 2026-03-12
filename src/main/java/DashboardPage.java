package com.swm.ui.pages.dashboard;

import com.swm.core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
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
     * A JavaScript click is used as a fallback for stubborn or obscured elements.
     * </p>
     *
     * @throws IllegalStateException if WebDriver is null or the profile icon cannot be clicked.
     */
    public void clickProfileIcon() {
        if (Objects.isNull(driver)) {
            logger.error("WebDriver is null when attempting to click the profile icon");
            throw new IllegalStateException("WebDriver is not initialized");
        }

        try {
            // Prefer waiting utilities when available
            Optional<WebElement> elementOpt = findVisibleElement(profileIconLocator);
            if (elementOpt.isPresent()) {
                WebElement element = elementOpt.get();
                try {
                    element.click();
                    logger.debug("Clicked profile icon using standard click");
                    return;
                } catch (WebDriverException | StaleElementReferenceException clickEx) {
                    logger.warn("Standard click on profile icon failed; attempting JavaScript click", clickEx);
                    // fallback to JS click
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                        logger.debug("Clicked profile icon using JavaScript executor");
                        return;
                    } catch (Exception jsEx) {
                        logger.warn("JavaScript click on profile icon also failed", jsEx);
                    }
                }
            } else {
                logger.debug("Profile icon not found with primary locator; attempting alternate strategies");
                // Try searching broadly for clickable elements that might represent profile icon
                List<WebElement> candidates = driver.findElements(By.cssSelector("button, a, div[role='button']"));
                for (WebElement candidate : candidates) {
                    try {
                        String text = candidate.getText();
                        if (text != null && (text.trim().equalsIgnoreCase("s") || text.trim().equalsIgnoreCase("S"))) {
                            if (candidate.isDisplayed()) {
                                try {
                                    candidate.click();
                                    logger.debug("Clicked profile icon candidate using standard click");
                                    return;
                                } catch (Exception e) {
                                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", candidate);
                                    logger.debug("Clicked profile icon candidate using JavaScript click");
                                    return;
                                }
                            }
                        }
                    } catch (StaleElementReferenceException sere) {
                        logger.debug("Stale element while iterating candidates for profile icon", sere);
                    } catch (Exception e) {
                        logger.debug("Ignoring candidate while searching for profile icon", e);
                    }
                }
            }

            // If reached here, all strategies failed
            logger.error("Unable to click profile icon after trying multiple strategies");
            throw new IllegalStateException("Profile icon could not be clicked");
        } catch (IllegalStateException ise) {
            throw ise;
        } catch (Exception e) {
            logger.error("Unexpected error while attempting to click profile icon", e);
            throw new IllegalStateException("Unexpected error clicking profile icon", e);
        }
    }

    /**
     * Opens the profile dropdown by clicking the profile icon and waiting for the dropdown to appear.
     *
     * @return true if dropdown opened successfully; false otherwise.
     */
    public boolean openProfileDropdown() {
        try {
            clickProfileIcon();
            if (Objects.isNull(wait)) {
                logger.warn("Wait utility is null; cannot wait for profile dropdown visibility");
                // Try a best-effort check
                return findVisibleElement(profileDropdownLocator).isPresent();
            }
            WebElement dropdown = wait.waitForElementVisible(profileDropdownLocator);
            boolean visible = dropdown != null && dropdown.isDisplayed();
            logger.debug("Profile dropdown visibility after opening: {}", visible);
            return visible;
        } catch (TimeoutException e) {
            logger.debug("Profile dropdown did not become visible in expected time", e);
            return false;
        } catch (IllegalStateException e) {
            logger.error("Unable to open profile dropdown because profile icon couldn't be clicked", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while opening profile dropdown", e);
            return false;
        }
    }

    /**
     * Attempts to locate the profile option element from the dropdown.
     *
     * @return Optional containing the WebElement if found and visible; Optional.empty() otherwise.
     */
    public Optional<WebElement> getProfileOptionElement() {
        try {
            return findVisibleElement(profileOptionLocator);
        } catch (Exception e) {
            logger.error("Unexpected error while locating profile option element", e);
            return Optional.empty();
        }
    }

    /**
     * Attempts to click the "Profile" option in the profile dropdown.
     *
     * @return true if clicked successfully; false otherwise.
     */
    public boolean clickProfileOption() {
        try {
            if (!openProfileDropdown()) {
                logger.warn("Profile dropdown could not be opened to click Profile option");
                return false;
            }

            Optional<WebElement> profileOpt = getProfileOptionElement();
            if (profileOpt.isPresent()) {
                WebElement profileEl = profileOpt.get();
                try {
                    profileEl.click();
                    logger.debug("Clicked Profile option");
                    return true;
                } catch (WebDriverException e) {
                    logger.warn("Standard click on Profile option failed; attempting JavaScript click", e);
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", profileEl);
                        logger.debug("Clicked Profile option using JavaScript executor");
                        return true;
                    } catch (Exception jsEx) {
                        logger.error("JavaScript click on Profile option failed", jsEx);
                        return false;
                    }
                }
            } else {
                logger.debug("Profile option not present in dropdown");
                return false;
            }
        } catch (Exception e) {
            logger.error("Unexpected error while attempting to click Profile option", e);
            return false;
        }
    }

    /**
     * Attempts to click the "Logout" option in the profile dropdown.
     *
     * @return true if clicked successfully; false otherwise.
     */
    public boolean clickLogoutOption() {
        try {
            if (!openProfileDropdown()) {
                logger.warn("Profile dropdown could not be opened to click Logout option");
                return false;
            }

            Optional<WebElement> logoutOpt = findVisibleElement(logoutOptionLocator);
            if (logoutOpt.isPresent()) {
                WebElement logoutEl = logoutOpt.get();
                try {
                    logoutEl.click();
                    logger.debug("Clicked Logout option");
                    return true;
                } catch (WebDriverException e) {
                    logger.warn("Standard click on Logout option failed; attempting JavaScript click", e);
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutEl);
                        logger.debug("Clicked Logout option using JavaScript executor");
                        return true;
                    } catch (Exception jsEx) {
                        logger.error("JavaScript click on Logout option failed", jsEx);
                        return false;
                    }
                }
            } else {
                logger.debug("Logout option not present in dropdown");
                return false;
            }
        } catch (Exception e) {
            logger.error("Unexpected error while attempting to click Logout option", e);
            return false;
        }
    }

    /**
     * Retrieves textual options from the profile dropdown. This is a best-effort method that attempts to collect
     * texts from common child controls (links, buttons, list items) inside the dropdown element.
     *
     * @return List of option texts; empty list if none found or an error occurs.
     */
    public List<String> getDropdownOptions() {
        try {
            Optional<WebElement> dropdownOpt = findVisibleElement(profileDropdownLocator);
            if (!dropdownOpt.isPresent()) {
                logger.debug("Profile dropdown not present when attempting to list options");
                return Collections.emptyList();
            }
            WebElement dropdown = dropdownOpt.get();
            List<WebElement> optionElements = dropdown.findElements(By.xpath(".//a | .//button | .//li | .//div[@role='menuitem']"));
            return optionElements.stream()
                    .filter(el -> {
                        try {
                            return el.isDisplayed();
                        } catch (StaleElementReferenceException sere) {
                            logger.debug("Stale element while filtering dropdown options", sere);
                            return false;
                        }
                    })
                    .map(el -> {
                        try {
                            String txt = el.getText();
                            return txt != null ? txt.trim() : "";
                        } catch (StaleElementReferenceException sere) {
                            logger.debug("Stale element while reading dropdown option text", sere);
                            return "";
                        }
                    })
                    .filter(txt -> txt != null && !txt.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving dropdown options", e);
            return Collections.emptyList();
        }
    }

    /**
     * Utility method to safely find a visible element by locator.
     *
     * @param locator the By locator to search for
     * @return Optional containing the visible WebElement or empty if not found
     */
    private Optional<WebElement> findVisibleElement(By locator) {
        try {
            if (Objects.isNull(driver)) {
                logger.error("WebDriver is null in findVisibleElement");
                return Optional.empty();
            }

            // Prefer wait utilities when available
            if (Objects.nonNull(wait)) {
                try {
                    WebElement el = wait.waitForElementVisible(locator);
                    if (el != null && el.isDisplayed()) {
                        return Optional.of(el);
                    }
                } catch (TimeoutException te) {
                    logger.debug("Timed out waiting for element to become visible: {}", locator, te);
                } catch (StaleElementReferenceException sere) {
                    logger.debug("Stale element when waiting for visibility: {}", locator, sere);
                } catch (Exception e) {
                    logger.debug("Non-fatal exception while waiting for element visibility: {}", locator, e);
                }
            }

            // Fallback: find among elements and return first displayed
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement el : elements) {
                try {
                    if (el != null && el.isDisplayed()) {
                        return Optional.of(el);
                    }
                } catch (StaleElementReferenceException sere) {
                    logger.debug("Stale element while iterating results for locator: {}", locator, sere);
                }
            }
            return Optional.empty();
        } catch (WebDriverException e) {
            logger.error("WebDriver exception while finding element: {}", locator, e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected exception while finding element: {}", locator, e);
            return Optional.empty();
        }
    }
}