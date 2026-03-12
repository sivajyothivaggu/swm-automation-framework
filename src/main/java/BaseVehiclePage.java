package com.swm.ui.pages.transport.VehicleManagement;

import com.swm.core.base.BasePage;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Base page for vehicle-related actions in the Vehicle Management UI.
 *
 * <p>This class centralizes common interactions with vehicle-related UI elements,
 * such as searching, opening filters, and exporting. It validates inputs, wraps
 * WebElement access in Optional, and provides robust error handling and logging
 * to make pages that extend this class more reliable and diagnosable.</p>
 *
 * @since 1.0
 */
public class BaseVehiclePage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(BaseVehiclePage.class);

    @FindBy(id = "search-box")
    protected WebElement searchBox;

    @FindBy(id = "filter-btn")
    protected WebElement filterButton;

    @FindBy(id = "export-btn")
    protected WebElement exportButton;

    /**
     * Types the provided text into the vehicle search box.
     *
     * @param searchText the text to enter into the search box; must not be null
     * @throws IllegalArgumentException if {@code searchText} is null
     * @throws IllegalStateException    if the search box element is not initialized
     * @throws RuntimeException         if a WebElement interaction error occurs
     */
    public void searchVehicle(String searchText) {
        logger.debug("Attempting to search vehicle with text: {}", searchText);
        try {
            if (Objects.isNull(searchText)) {
                throw new IllegalArgumentException("searchText must not be null");
            }

            WebElement element = getSearchBox()
                    .orElseThrow(() -> new IllegalStateException("Search box element is not initialized"));

            // clear existing text before sending new keys to avoid unexpected concatenation
            try {
                if (element.isDisplayed() && element.isEnabled()) {
                    element.clear();
                } else {
                    logger.debug("Search box present but not interactable for clearing");
                }
            } catch (Exception e) {
                // Not fatal; continue to sendKeys but log the event for diagnostics
                logger.debug("Ignored exception while clearing search box: {}", e.getMessage(), e);
            }

            try {
                if (!element.isDisplayed() || !element.isEnabled()) {
                    throw new ElementNotInteractableException("Search box is not interactable");
                }
                element.sendKeys(searchText);
                logger.info("Search input provided successfully");
            } catch (NoSuchElementException | StaleElementReferenceException | ElementNotInteractableException e) {
                logger.error("WebElement interaction failed while sending keys to search box: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to interact with search box", e);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid argument or state while searching vehicle: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            // Already logged above; rethrow to preserve behavior
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while searching vehicle: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during searchVehicle", e);
        }
    }

    /**
     * Clicks the filter button to open filter options.
     *
     * @throws IllegalStateException if the filter button element is not initialized
     * @throws RuntimeException      if a WebElement interaction error occurs
     */
    public void clickFilter() {
        logger.debug("Attempting to click filter button");
        try {
            WebElement element = getFilterButton()
                    .orElseThrow(() -> new IllegalStateException("Filter button element is not initialized"));
            safeClick(element, "filter button");
            logger.info("Filter button clicked successfully");
        } catch (IllegalStateException e) {
            logger.error("Invalid state while clicking filter button: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            // Already logged in safeClick or below; preserve behavior
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while clicking filter button: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during clickFilter", e);
        }
    }

    /**
     * Clicks the export button to trigger export functionality.
     *
     * @throws IllegalStateException if the export button element is not initialized
     * @throws RuntimeException      if a WebElement interaction error occurs
     */
    public void clickExport() {
        logger.debug("Attempting to click export button");
        try {
            WebElement element = getExportButton()
                    .orElseThrow(() -> new IllegalStateException("Export button element is not initialized"));
            safeClick(element, "export button");
            logger.info("Export button clicked successfully");
        } catch (IllegalStateException e) {
            logger.error("Invalid state while clicking export button: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while clicking export button: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during clickExport", e);
        }
    }

    /**
     * Safely clicks the provided WebElement. Performs common checks and translates
     * Selenium exceptions into RuntimeException with contextual logging.
     *
     * @param element the WebElement to click
     * @param name    human-readable name for logging context
     * @throws RuntimeException if the element is not interactable or a WebDriver error occurs
     */
    protected void safeClick(WebElement element, String name) {
        Objects.requireNonNull(element, "WebElement must not be null for safeClick");
        logger.debug("Attempting safe click on: {}", name);
        try {
            if (!element.isDisplayed() || !element.isEnabled()) {
                logger.error("Element '{}' is present but not interactable (displayed={}, enabled={})",
                        name, safeIsDisplayed(element), safeIsEnabled(element));
                throw new ElementNotInteractableException("Element is not interactable: " + name);
            }
            element.click();
            logger.debug("Click action performed on: {}", name);
        } catch (NoSuchElementException | StaleElementReferenceException | ElementNotInteractableException e) {
            logger.error("Failed to click element '{}': {}", name, e.getMessage(), e);
            throw new RuntimeException("Failed to click element: " + name, e);
        } catch (Exception e) {
            logger.error("Unexpected error during click on '{}': {}", name, e.getMessage(), e);
            throw new RuntimeException("Unexpected error clicking element: " + name, e);
        }
    }

    /**
     * Returns an Optional-wrapped search box WebElement.
     *
     * @return Optional of search box WebElement, empty if not present
     */
    protected Optional<WebElement> getSearchBox() {
        return Optional.ofNullable(searchBox);
    }

    /**
     * Returns an Optional-wrapped filter button WebElement.
     *
     * @return Optional of filter button WebElement, empty if not present
     */
    protected Optional<WebElement> getFilterButton() {
        return Optional.ofNullable(filterButton);
    }

    /**
     * Returns an Optional-wrapped export button WebElement.
     *
     * @return Optional of export button WebElement, empty if not present
     */
    protected Optional<WebElement> getExportButton() {
        return Optional.ofNullable(exportButton);
    }

    /**
     * Safely checks displayed state of an element. Returns false on exceptions.
     *
     * @param element the WebElement to query
     * @return true if displayed; false if not displayed or an exception occurs
     */
    private boolean safeIsDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            logger.debug("Ignored exception while checking isDisplayed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Safely checks enabled state of an element. Returns false on exceptions.
     *
     * @param element the WebElement to query
     * @return true if enabled; false if not enabled or an exception occurs
     */
    private boolean safeIsEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            logger.debug("Ignored exception while checking isEnabled: {}", e.getMessage(), e);
            return false;
        }
    }
}