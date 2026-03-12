package com.swm.ui.pages.transport.VehicleManagement.TotalVehicles;

import com.swm.ui.pages.transport.VehicleManagement.BaseVehiclePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Page object representing the Total Vehicles page within Vehicle Management.
 * <p>
 * Provides high-level interactions such as searching vehicles, applying filters,
 * clearing filters and retrieving table data. All actions are logged and guarded
 * with robust error handling.
 */
public class TotalVehiclesPage extends BaseVehiclePage {

    private static final Logger LOGGER = LoggerFactory.getLogger(TotalVehiclesPage.class);
    private static final Duration DEFAULT_WAIT = Duration.ofSeconds(10);

    @FindBy(xpath = "//*[contains(text(), 'Vehicle Management')]")
    private WebElement vehicleManagementCard;

    @FindBy(xpath = "//input[@placeholder='AP36AATB2189' or contains(@placeholder, 'Search') or contains(@placeholder, 'vehicle')]")
    private WebElement searchBox;

    @FindBy(xpath = "//button[normalize-space()='Search'] | //button[contains(@class, 'search')] | //button[@type='submit']")
    private WebElement searchButton;

    @FindBy(xpath = "//span[contains(text(), 'active') or contains(@class, 'badge')]")
    private WebElement activeFilterBadge;

    @FindBy(xpath = "//input[@placeholder='dd/mm/yyyy'][1] | //input[contains(@placeholder, 'dd/mm/yyyy')][1] | //label[contains(text(), 'From')]/following::input[1]")
    private WebElement fromDateField;

    @FindBy(xpath = "//input[@placeholder='dd/mm/yyyy'][2] | //input[contains(@placeholder, 'dd/mm/yyyy')][2] | //label[contains(text(), 'To')]/following::input[1]")
    private WebElement toDateField;

    @FindBy(xpath = "//button[contains(text(), 'Clear All') or contains(@class, 'clear')]")
    private WebElement clearAllButton;

    @FindBy(xpath = "//table//tbody//tr")
    private List<WebElement> tableRows;

    @FindBy(xpath = "//*[contains(text(), 'No Data Found') or contains(text(), 'No records') or contains(text(), 'No data')]")
    private WebElement noDataMessage;

    /**
     * Opens the Vehicle Management card if present.
     *
     * @return true if the card was opened or already visible, false otherwise
     */
    public boolean openVehicleManagementCard() {
        if (Objects.isNull(vehicleManagementCard)) {
            LOGGER.warn("Vehicle Management card WebElement is null; cannot open card.");
            return false;
        }
        try {
            waitForVisibility(vehicleManagementCard, DEFAULT_WAIT);
            if (vehicleManagementCard.isDisplayed()) {
                safeClick(vehicleManagementCard);
                LOGGER.info("Vehicle Management card clicked/opened.");
                return true;
            } else {
                LOGGER.info("Vehicle Management card is not displayed.");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to open Vehicle Management card.", e);
            return false;
        }
    }

    /**
     * Searches for the given vehicle text using the search box and search button.
     * Preserves previous functionality but adds robust logging and error handling.
     *
     * @param vehicleIdentifier text to search for (e.g., registration number)
     * @return true if search action was initiated successfully, false otherwise
     */
    public boolean searchVehicle(String vehicleIdentifier) {
        if (Objects.isNull(searchBox) || Objects.isNull(searchButton)) {
            LOGGER.warn("Search box or search button WebElement is null; cannot perform search.");
            return false;
        }
        try {
            waitForVisibility(searchBox, DEFAULT_WAIT);
            searchBox.clear();
            if (!Objects.isNull(vehicleIdentifier)) {
                searchBox.sendKeys(vehicleIdentifier);
            }
            // Some UIs require ENTER; others require clicking Search
            try {
                safeClick(searchButton);
            } catch (Exception clickEx) {
                LOGGER.debug("Click on search button failed; attempting ENTER key as fallback.", clickEx);
                searchBox.sendKeys(Keys.ENTER);
            }
            // Wait for either data to appear or a no-data message
            WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT);
            try {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//table//tbody//tr")),
                        ExpectedConditions.visibilityOf(noDataMessage)
                ));
            } catch (TimeoutException te) {
                LOGGER.debug("Timeout waiting for search results or no-data message.", te);
            }
            LOGGER.info("Search initiated for identifier '{}'.", vehicleIdentifier);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to perform search for '{}'.", vehicleIdentifier, e);
            return false;
        }
    }

    /**
     * Applies or toggles the "active" filter badge if present.
     *
     * @return true if filter was applied/toggled, false otherwise
     */
    public boolean applyActiveFilter() {
        if (Objects.isNull(activeFilterBadge)) {
            LOGGER.warn("Active filter badge WebElement is null; cannot apply filter.");
            return false;
        }
        try {
            waitForVisibility(activeFilterBadge, DEFAULT_WAIT);
            safeClick(activeFilterBadge);
            LOGGER.info("Active filter badge clicked/toggled.");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to click active filter badge.", e);
            return false;
        }
    }

    /**
     * Sets the 'From' date field.
     *
     * @param dateText date string in expected format (e.g., dd/mm/yyyy)
     * @return true if set successfully, false otherwise
     */
    public boolean setFromDate(String dateText) {
        if (Objects.isNull(fromDateField)) {
            LOGGER.warn("From date field WebElement is null; cannot set date.");
            return false;
        }
        try {
            waitForVisibility(fromDateField, DEFAULT_WAIT);
            fromDateField.clear();
            if (!Objects.isNull(dateText)) {
                fromDateField.sendKeys(dateText);
                // some date pickers require ENTER or TAB to close
                fromDateField.sendKeys(Keys.TAB);
            }
            LOGGER.info("From date set to '{}'.", dateText);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to set From date to '{}'.", dateText, e);
            return false;
        }
    }

    /**
     * Sets the 'To' date field.
     *
     * @param dateText date string in expected format (e.g., dd/mm/yyyy)
     * @return true if set successfully, false otherwise
     */
    public boolean setToDate(String dateText) {
        if (Objects.isNull(toDateField)) {
            LOGGER.warn("To date field WebElement is null; cannot set date.");
            return false;
        }
        try {
            waitForVisibility(toDateField, DEFAULT_WAIT);
            toDateField.clear();
            if (!Objects.isNull(dateText)) {
                toDateField.sendKeys(dateText);
                toDateField.sendKeys(Keys.TAB);
            }
            LOGGER.info("To date set to '{}'.", dateText);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to set To date to '{}'.", dateText, e);
            return false;
        }
    }

    /**
     * Clicks Clear All to reset filters.
     *
     * @return true if clear action was successful, false otherwise
     */
    public boolean clearAll() {
        if (Objects.isNull(clearAllButton)) {
            LOGGER.warn("Clear All button WebElement is null; cannot clear filters.");
            return false;
        }
        try {
            waitForVisibility(clearAllButton, DEFAULT_WAIT);
            safeClick(clearAllButton);
            LOGGER.info("Clear All button clicked; filters should be reset.");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to click Clear All button.", e);
            return false;
        }
    }

    /**
     * Retrieves the visible rows' combined text from the vehicles table.
     *
     * @return non-null List of row texts; empty list if no rows present or an error occurs
     */
    public List<String> getTableRowTexts() {
        try {
            if (Objects.isNull(tableRows) || tableRows.isEmpty()) {
                LOGGER.debug("No table rows found or tableRows WebElement list is null/empty.");
                return Collections.emptyList();
            }
            // Handle potential stale elements by retrying once
            try {
                return tableRows.stream()
                        .filter(Objects::nonNull)
                        .map(WebElement::getText)
                        .map(String::trim)
                        .collect(Collectors.toList());
            } catch (StaleElementReferenceException stale) {
                LOGGER.warn("StaleElementReferenceException encountered when reading table rows; retrying once.", stale);
                return tableRows.stream()
                        .filter(Objects::nonNull)
                        .map(elem -> {
                            try {
                                return elem.getText().trim();
                            } catch (Exception ex) {
                                LOGGER.debug("Failed to read a row element during retry.", ex);
                                return "";
                            }
                        })
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error while retrieving table rows.", e);
            return Collections.emptyList();
        }
    }

    /**
     * Returns the "No Data" message text when present.
     *
     * @return Optional containing message if present and visible; Optional.empty() otherwise
     */
    public Optional<String> getNoDataMessage() {
        if (Objects.isNull(noDataMessage)) {
            LOGGER.debug("No data message WebElement is null.");
            return Optional.empty();
        }
        try {
            waitForVisibility(noDataMessage, DEFAULT_WAIT);
            if (noDataMessage.isDisplayed()) {
                String text = noDataMessage.getText();
                LOGGER.info("No data message found: {}", text);
                return Optional.ofNullable(text);
            } else {
                LOGGER.debug("No data message not displayed.");
                return Optional.empty();
            }
        } catch (TimeoutException te) {
            LOGGER.debug("No data message did not appear within timeout.", te);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Error while retrieving no data message.", e);
            return Optional.empty();
        }
    }

    /**
     * Waits for the given element to be visible within the specified duration.
     *
     * @param element element to wait for
     * @param timeout maximum wait duration
     */
    private void waitForVisibility(WebElement element, Duration timeout) {
        if (Objects.isNull(element)) {
            throw new IllegalArgumentException("Element to wait for cannot be null.");
        }
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            wait.until(ExpectedConditions.visibilityOf(element));
        } catch (TimeoutException te) {
            LOGGER.debug("Timeout while waiting for element visibility: {}", element, te);
            throw te;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while waiting for element visibility.", e);
            throw e;
        }
    }

    /**
     * Safely clicks a WebElement with error handling for common Selenium issues.
     *
     * @param element element to click
     */
    private void safeClick(WebElement element) {
        if (Objects.isNull(element)) {
            LOGGER.warn("Attempted to click a null WebElement.");
            return;
        }
        try {
            waitForVisibility(element, DEFAULT_WAIT);
            element.click();
        } catch (StaleElementReferenceException stale) {
            LOGGER.warn("StaleElementReferenceException on click; attempting to re-locate and click.", stale);
            // Attempt to find and click via its locator if possible
            try {
                String xpath = buildXPathFromElement(element);
                if (xpath != null) {
                    WebElement fresh = driver.findElement(By.xpath(xpath));
                    waitForVisibility(fresh, DEFAULT_WAIT);
                    fresh.click();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to recover from stale element by re-locating via xpath.", e);
                throw e;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to click element.", e);
            throw e;
        }
    }

    /**
     * Attempts to derive an XPath for a given WebElement for recovery in stale scenarios.
     * This method is a best-effort helper and may return null if derivation is not possible.
     *
     * @param element element whose XPath is to be derived
     * @return xpath string or null when not derivable
     */
    private String buildXPathFromElement(WebElement element) {
        try {
            // Many WebDriver implementations expose a toString that contains the locator.
            // Example: "Proxy element for: DefaultElementLocator 'By.xpath: //button...'"
            String desc = element.toString();
            if (Objects.isNull(desc)) {
                return null;
            }
            // Heuristic parsing for "By.xpath: <xpath>"
            int idx = desc.indexOf("By.xpath:");
            if (idx != -1) {
                String sub = desc.substring(idx + "By.xpath:".length()).trim();
                // Remove trailing characters like ']' or ')'
                sub = sub.replaceAll("[\\)\\]]+$", "").trim();
                return sub;
            }
            // Add more heuristics if necessary for other locator types
        } catch (Exception e) {
            LOGGER.debug("Failed to derive xpath from element description.", e);
        }
        return null;
    }
}