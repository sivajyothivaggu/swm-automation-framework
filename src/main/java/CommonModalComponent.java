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
     * Returns an Optional wrapping the search box WebElement.
     *
     * @return Optional containing search box element if initialized, otherwise empty
     */
    public Optional<WebElement> getSearchBox() {
        return Optional.ofNullable(searchBox);
    }

    /**
     * Returns an Optional wrapping the filter button WebElement.
     *
     * @return Optional containing filter button element if initialized, otherwise empty
     */
    public Optional<WebElement> getFilterButton() {
        return Optional.ofNullable(filterButton);
    }

    /**
     * Returns an Optional wrapping the export button WebElement.
     *
     * @return Optional containing export button element if initialized, otherwise empty
     */
    public Optional<WebElement> getExportButton() {
        return Optional.ofNullable(exportButton);
    }

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

        if (Objects.isNull(searchText)) {
            logger.error("searchText parameter is null");
            throw new IllegalArgumentException("searchText must not be null");
        }

        WebElement element = getSearchBox()
                .orElseThrow(() -> {
                    logger.error("Search box WebElement is not initialized");
                    return new IllegalStateException("Search box element is not initialized");
                });

        try {
            // Attempt to clear if possible
            try {
                if (element.isDisplayed() && element.isEnabled()) {
                    element.clear();
                    logger.debug("Cleared existing text in search box");
                } else {
                    logger.debug("Search box present but not interactable for clearing");
                }
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                logger.debug("Ignored element state issue while clearing search box: {}", e.getMessage(), e);
            } catch (Exception e) {
                logger.debug("Unexpected exception while attempting to clear search box: {}", e.getMessage(), e);
            }

            // Ensure interactable before sending keys
            if (!element.isDisplayed() || !element.isEnabled()) {
                logger.error("Search box is not interactable (displayed: {}, enabled: {})",
                        safeIsDisplayed(element), safeIsEnabled(element));
                throw new ElementNotInteractableException("Search box is not interactable");
            }

            try {
                element.sendKeys(searchText);
                logger.info("Search input provided successfully");
            } catch (StaleElementReferenceException | NoSuchElementException | ElementNotInteractableException e) {
                logger.error("WebElement interaction failed while sending keys to search box: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to interact with search box", e);
            } catch (Exception e) {
                logger.error("Unexpected error while sending keys to search box: {}", e.getMessage(), e);
                throw new RuntimeException("Unexpected error while interacting with search box", e);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Already logged above, rethrow to propagate to caller
            throw e;
        } catch (RuntimeException e) {
            // Already logged above where thrown; preserve stacktrace
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
        WebElement element = getFilterButton()
                .orElseThrow(() -> {
                    logger.error("Filter button WebElement is not initialized");
                    return new IllegalStateException("Filter button element is not initialized");
                });
        safeClick(element, "filter button");
        logger.info("Filter button clicked successfully");
    }

    /**
     * Clicks the export button to trigger exporting functionality.
     *
     * @throws IllegalStateException if the export button element is not initialized
     * @throws RuntimeException      if a WebElement interaction error occurs
     */
    public void clickExport() {
        logger.debug("Attempting to click export button");
        WebElement element = getExportButton()
                .orElseThrow(() -> {
                    logger.error("Export button WebElement is not initialized");
                    return new IllegalStateException("Export button element is not initialized");
                });
        safeClick(element, "export button");
        logger.info("Export button clicked successfully");
    }

    /**
     * Safely clicks a web element with comprehensive error handling and logging.
     *
     * @param element  the web element to click; must not be null
     * @param elementName friendly name for logging purposes
     * @throws IllegalArgumentException if {@code element} is null
     * @throws RuntimeException         if clicking fails due to WebDriver/element issues
     */
    protected void safeClick(WebElement element, String elementName) {
        if (Objects.isNull(element)) {
            logger.error("safeClick received null element for {}", elementName);
            throw new IllegalArgumentException("Element to click must not be null");
        }

        try {
            if (!element.isDisplayed() || !element.isEnabled()) {
                logger.error("Element '{}' is not interactable (displayed: {}, enabled: {})",
                        elementName, safeIsDisplayed(element), safeIsEnabled(element));
                throw new ElementNotInteractableException("Element is not interactable: " + elementName);
            }
            element.click();
            logger.debug("Clicked element '{}'", elementName);
        } catch (StaleElementReferenceException | NoSuchElementException | ElementNotInteractableException e) {
            logger.error("Failed to click element '{}': {}", elementName, e.getMessage(), e);
            throw new RuntimeException("Failed to click element: " + elementName, e);
        } catch (Exception e) {
            logger.error("Unexpected error while clicking element '{}': {}", elementName, e.getMessage(), e);
            throw new RuntimeException("Unexpected error while clicking element: " + elementName, e);
        }
    }

    /**
     * Safe wrapper to check isDisplayed on an element. Returns false if element throws exception.
     *
     * @param element the web element to check
     * @return true if displayed, false otherwise or if checking fails
     */
    private boolean safeIsDisplayed(WebElement element) {
        try {
            return element != null && element.isDisplayed();
        } catch (Exception e) {
            logger.debug("safeIsDisplayed encountered an exception: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Safe wrapper to check isEnabled on an element. Returns false if element throws exception.
     *
     * @param element the web element to check
     * @return true if enabled, false otherwise or if checking fails
     */
    private boolean safeIsEnabled(WebElement element) {
        try {
            return element != null && element.isEnabled();
        } catch (Exception e) {
            logger.debug("safeIsEnabled encountered an exception: {}", e.getMessage(), e);
            return false;
        }
    }
}