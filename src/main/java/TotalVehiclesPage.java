package com.swm.ui.pages.transport.VehicleManagement.TotalVehicles;

import com.swm.ui.pages.transport.VehicleManagement.BaseVehiclePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Page object representing the "Total Vehicles" view in Vehicle Management.
 * <p>
 * This class encapsulates interactions with the Total Vehicles page such as searching,
 * filtering, reading table rows, and clearing filters.
 * Methods provide safe null handling and logging. Exceptions are logged and rethrown
 * as RuntimeException to ensure test frameworks handle failures explicitly.
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

    @FindBy(xpath = "//button[contains(text(), 'Create') or contains(@class, 'create') or //button[@id='createVehicle']]")
    private WebElement createButton;

    /**
     * Waits until an element is visible.
     *
     * @param element the element to wait for
     */
    private void waitForVisibility(WebElement element) {
        if (Objects.isNull(element)) {
            LOGGER.warn("waitForVisibility called with null element");
            throw new IllegalArgumentException("Element must not be null");
        }
        try {
            WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT);
            wait.until(ExpectedConditions.visibilityOf(element));
        } catch (Exception e) {
            LOGGER.error("Element not visible within timeout: {}", element, e);
            throw new RuntimeException("Timed out waiting for element visibility", e);
        }
    }

    /**
     * Clicks a given element after waiting for visibility.
     *
     * @param element the element to click
     */
    private void safeClick(WebElement element) {
        try {
            waitForVisibility(element);
            element.click();
        } catch (Exception e) {
            LOGGER.error("Failed to click element: {}", element, e);
            throw new RuntimeException("Failed to click element", e);
        }
    }

    /**
     * Safely clears and sends text to an input element.
     *
     * @param element the input element
     * @param text    the text to send
     */
    private void safeType(WebElement element, String text) {
        if (Objects.isNull(text)) {
            LOGGER.warn("safeType called with null text for element {}", element);
            throw new IllegalArgumentException("Text must not be null");
        }
        try {
            waitForVisibility(element);
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            LOGGER.error("Failed to type into element: {}", element, e);
            throw new RuntimeException("Failed to type into element", e);
        }
    }

    /**
     * Opens the Vehicle Management card/view.
     */
    public void openVehicleManagementCard() {
        try {
            LOGGER.debug("Opening Vehicle Management card");
            safeClick(vehicleManagementCard);
        } catch (Exception e) {
            LOGGER.error("Unable to open Vehicle Management card", e);
            throw e;
        }
    }

    /**
     * Performs a search for a vehicle or text using the search box and triggers the search action.
     *
     * @param query the search query; must not be null or empty
     */
    public void searchVehicle(String query) {
        if (Objects.isNull(query) || query.trim().isEmpty()) {
            LOGGER.warn("searchVehicle called with null or empty query");
            throw new IllegalArgumentException("Query must not be null or empty");
        }
        try {
            LOGGER.debug("Searching for vehicle/query: {}", query);
            safeType(searchBox, query);
            safeClick(searchButton);
            // Allow UI to reflect search results
            WebDriverWait wait = new WebDriverWait(driver, DEFAULT_WAIT);
            wait.until(driver1 -> {
                // Wait for either results or no data indicator
                boolean rowsLoaded = (Objects.nonNull(tableRows) && !tableRows.isEmpty());
                boolean noData = isNoDataPresent().orElse(false);
                return rowsLoaded || noData;
            });
            LOGGER.info("Search executed for query: {}", query);
        } catch (Exception e) {
            LOGGER.error("Error executing search for query: {}", query, e);
            throw new RuntimeException("Error executing search", e);
        }
    }

    /**
     * Clears all filters by clicking the 'Clear All' button.
     */
    public void clearAllFilters() {
        try {
            LOGGER.debug("Clearing all filters");
            safeClick(clearAllButton);
            LOGGER.info("Cleared all filters");
        } catch (Exception e) {
            LOGGER.error("Failed to clear filters", e);
            throw new RuntimeException("Failed to clear filters", e);
        }
    }

    /**
     * Toggles or applies the active filter badge.
     */
    public void applyActiveFilter() {
        try {
            LOGGER.debug("Applying active filter");
            safeClick(activeFilterBadge);
            LOGGER.info("Active filter applied");
        } catch (Exception e) {
            LOGGER.error("Failed to apply active filter", e);
            throw new RuntimeException("Failed to apply active filter", e);
        }
    }

    /**
     * Sets the 'From' date filter.
     *
     * @param fromDate the date string to set in the from date field (format expected by UI)
     */
    public void setFromDate(String fromDate) {
        if (Objects.isNull(fromDate) || fromDate.trim().isEmpty()) {
            LOGGER.warn("setFromDate called with null or empty date");
            throw new IllegalArgumentException("fromDate must not be null or empty");
        }
        try {
            LOGGER.debug("Setting from date: {}", fromDate);
            safeType(fromDateField, fromDate);
            fromDateField.sendKeys(Keys.ENTER);
            LOGGER.info("From date set to {}", fromDate);
        } catch (Exception e) {
            LOGGER.error("Failed to set from date: {}", fromDate, e);
            throw new RuntimeException("Failed to set from date", e);
        }
    }

    /**
     * Sets the 'To' date filter.
     *
     * @param toDate the date string to set in the to date field (format expected by UI)
     */
    public void setToDate(String toDate) {
        if (Objects.isNull(toDate) || toDate.trim().isEmpty()) {
            LOGGER.warn("setToDate called with null or empty date");
            throw new IllegalArgumentException("toDate must not be null or empty");
        }
        try {
            LOGGER.debug("Setting to date: {}", toDate);
            safeType(toDateField, toDate);
            toDateField.sendKeys(Keys.ENTER);
            LOGGER.info("To date set to {}", toDate);
        } catch (Exception e) {
            LOGGER.error("Failed to set to date: {}", toDate, e);
            throw new RuntimeException("Failed to set to date", e);
        }
    }

    /**
     * Returns the total number of vehicle rows currently visible in the table.
     *
     * @return number of vehicle rows (0 if none or on error)
     */
    public int getTotalVehiclesCount() {
        try {
            if (Objects.isNull(tableRows) || tableRows.isEmpty()) {
                LOGGER.debug("No table rows found - returning count 0");
                return 0;
            }
            int count = tableRows.size();
            LOGGER.debug("Total vehicles count: {}", count);
            return count;
        } catch (Exception e) {
            LOGGER.error("Failed to get total vehicles count", e);
            throw new RuntimeException("Failed to get total vehicles count", e);
        }
    }

    /**
     * Returns a list of registration numbers (or first cell text) from the vehicle table.
     *
     * @return list of registration strings; empty list if none
     */
    public List<String> getAllVehicleRegistrations() {
        try {
            if (Objects.isNull(tableRows) || tableRows.isEmpty()) {
                LOGGER.debug("No table rows to extract registrations from; returning empty list");
                return List.of();
            }
            // Extract first cell text for each row; handle missing cells gracefully
            List<String> registrations = tableRows.stream()
                    .map(row -> {
                        try {
                            WebElement firstCell = row.findElement(By.xpath("./td[1] | ./th[1]"));
                            return Optional.ofNullable(firstCell.getText()).orElse("").trim();
                        } catch (Exception inner) {
                            LOGGER.debug("Failed to extract first cell text for row: {}", row, inner);
                            return "";
                        }
                    })
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            LOGGER.debug("Extracted {} vehicle registrations", registrations.size());
            return registrations;
        } catch (Exception e) {
            LOGGER.error("Failed to extract vehicle registrations", e);
            throw new RuntimeException("Failed to extract vehicle registrations", e);
        }
    }

    /**
     * Returns whether a "No Data" message is present on the page.
     *
     * @return Optional<Boolean> true if present, false if not, empty Optional if element not available
     */
    public Optional<Boolean> isNoDataPresent() {
        try {
            if (Objects.isNull(noDataMessage)) {
                LOGGER.debug("noDataMessage element is null");
                return Optional.empty();
            }
            boolean displayed = noDataMessage.isDisplayed();
            LOGGER.debug("No data message displayed: {}", displayed);
            return Optional.of(displayed);
        } catch (Exception e) {
            LOGGER.debug("No data message not present or not visible", e);
            return Optional.of(false);
        }
    }

    /**
     * Returns the no-data message text if present.
     *
     * @return Optional containing the no-data message text, or empty Optional if not present
     */
    public Optional<String> getNoDataMessageText() {
        try {
            if (Objects.isNull(noDataMessage) || !noDataMessage.isDisplayed()) {
                LOGGER.debug("No data message element null or not displayed");
                return Optional.empty();
            }
            String text = noDataMessage.getText();
            return Optional.ofNullable(text).map(String::trim);
        } catch (Exception e) {
            LOGGER.debug("Failed to retrieve no data message text", e);
            return Optional.empty();
        }
    }

    /**
     * Clicks the Create button to initiate creating a new vehicle (if present).
     */
    public void clickCreateButton() {
        try {
            if (Objects.isNull(createButton)) {
                LOGGER.warn("Create button element not initialized");
                throw new IllegalStateException("Create button not available on the page");
            }
            LOGGER.debug("Clicking create button");
            safeClick(createButton);
            LOGGER.info("Create button clicked");
        } catch (Exception e) {
            LOGGER.error("Failed to click create button", e);
            throw new RuntimeException("Failed to click create button", e);
        }
    }
}