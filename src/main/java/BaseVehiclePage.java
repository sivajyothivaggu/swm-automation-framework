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
     * Clicks the export button to start export operation.
     *
     * @throws IllegalStateException if the export button element is not initialized
     * @throws RuntimeException      if a WebElement interaction error occurs
     * @since 1.0
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
     * Safely clicks a WebElement with robust error handling and logging.
     *
     * @param element     the WebElement to click; must not be null
     * @param elementName friendly name for logging purposes
     * @throws IllegalArgumentException if {@code element} is null
     * @throws RuntimeException         if the click interaction fails
     */
    protected void safeClick(WebElement element, String elementName) {
        if (Objects.isNull(element)) {
            logger.error("Attempted to safeClick a null element: {}", elementName);
            throw new IllegalArgumentException("Element to click must not be null: " + elementName);
        }
        try {
            if (!element.isDisplayed() || !element.isEnabled()) {
                String msg = String.format("%s is not interactable (displayed=%s, enabled=%s)",
                        elementName, safeIsDisplayed(element), safeIsEnabled(element));
                logger.error(msg);
                throw new ElementNotInteractableException(msg);
            }
            element.click();
            logger.debug("Clicked element: {}", elementName);
        } catch (NoSuchElementException | StaleElementReferenceException | ElementNotInteractableException e) {
            logger.error("Failed to click {}: {}", elementName, e.getMessage(), e);
            throw new RuntimeException("Failed to click " + elementName, e);
        } catch (Exception e) {
            logger.error("Unexpected error while clicking {}: {}", elementName, e.getMessage(), e);
            throw new RuntimeException("Unexpected error while clicking " + elementName, e);
        }
    }

    /**
     * Returns an Optional wrapping the search box WebElement.
     *
     * @return Optional of searchBox element
     */
    public Optional<WebElement> getSearchBox() {
        return Optional.ofNullable(searchBox);
    }

    /**
     * Returns an Optional wrapping the filter button WebElement.
     *
     * @return Optional of filterButton element
     */
    public Optional<WebElement> getFilterButton() {
        return Optional.ofNullable(filterButton);
    }

    /**
     * Returns an Optional wrapping the export button WebElement.
     *
     * @return Optional of exportButton element
     */
    public Optional<WebElement> getExportButton() {
        return Optional.ofNullable(exportButton);
    }

    /**
     * Safely checks whether the element is displayed. Any exception during check will be logged and the method
     * will return false.
     *
     * @param element element to check
     * @return true if displayed and accessible, false otherwise
     */
    protected boolean safeIsDisplayed(WebElement element) {
        try {
            return element != null && element.isDisplayed();
        } catch (Exception e) {
            logger.debug("Ignored exception while checking isDisplayed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Safely checks whether the element is enabled. Any exception during check will be logged and the method
     * will return false.
     *
     * @param element element to check
     * @return true if enabled and accessible, false otherwise
     */
    protected boolean safeIsEnabled(WebElement element) {
        try {
            return element != null && element.isEnabled();
        } catch (Exception e) {
            logger.debug("Ignored exception while checking isEnabled: {}", e.getMessage(), e);
            return false;
        }
    }
}