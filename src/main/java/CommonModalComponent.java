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
 * <p>This class provides common interactions for vehicle pages such as searching,
 * opening filters, and exporting. It performs validation and robust error handling
 * for WebElement interactions and logs relevant events. Methods throw unchecked
 * exceptions when the underlying WebElement is not available or an interaction fails,
 * preserving original behavior while providing clearer diagnostics.</p>
 *
 * @since 1.0
 */
public class BaseVehiclePage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(BaseVehiclePage.class);

    @FindBy(id = "search-box")
    protected WebElement search_box;

    @FindBy(id = "filter-btn")
    protected WebElement filter_button;

    @FindBy(id = "export-btn")
    protected WebElement export_button;

    /**
     * Types the provided text into the vehicle search box.
     *
     * @param search_text the text to enter into the search box; must not be null
     * @throws IllegalArgumentException if {@code search_text} is null
     * @throws IllegalStateException    if the search box element is not initialized
     * @throws RuntimeException         if a WebElement interaction error occurs
     */
    public void searchVehicle(String search_text) {
        logger.debug("Attempting to search vehicle with text: {}", search_text);
        try {
            if (Objects.isNull(search_text)) {
                throw new IllegalArgumentException("search_text must not be null");
            }

            WebElement web_element = getSearchBox()
                    .orElseThrow(() -> new IllegalStateException("Search box element is not initialized"));

            // Attempt to clear existing text before sending new keys to avoid unexpected concatenation
            try {
                if (web_element.isDisplayed() && web_element.isEnabled()) {
                    web_element.clear();
                } else {
                    logger.debug("Search box is not displayed or not enabled prior to clearing.");
                }
            } catch (Exception e) {
                // Not fatal; continue to sendKeys but log the event for diagnostics
                logger.debug("Ignored exception while clearing search box: {}", e.getMessage(), e);
            }

            try {
                if (web_element.isDisplayed() && web_element.isEnabled()) {
                    web_element.sendKeys(search_text);
                } else {
                    throw new ElementNotInteractableException("Search box is not interactable");
                }
            } catch (NoSuchElementException | StaleElementReferenceException | ElementNotInteractableException e) {
                logger.error("WebElement interaction failed while sending keys to search box: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to interact with search box", e);
            }

            logger.info("Search input provided successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid argument or state while searching vehicle: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            // Already logged above; rethrow
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
            WebElement web_element = getFilterButton()
                    .orElseThrow(() -> new IllegalStateException("Filter button element is not initialized"));
            safeClick(web_element, "filter button");
            logger.info("Filter button clicked successfully");
        } catch (IllegalStateException e) {
            logger.error("Invalid state while clicking filter button: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            // Already logged inside safeClick or below; rethrow to preserve behavior
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
            WebElement web_element = getExportButton()
                    .orElseThrow(() -> new IllegalStateException("Export button element is not initialized"));
            safeClick(web_element, "export button");
            logger.info("Export button clicked successfully");
        } catch (IllegalStateException e) {
            logger.error("Invalid state while clicking export button: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            // Already logged inside safeClick; rethrow to preserve behavior
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while clicking export button: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during clickExport", e);
        }
    }

    /**
     * Safely clicks a given WebElement with enhanced validation and logging.
     *
     * @param web_element the WebElement to click; must not be null
     * @param element_name human-readable name for logging purposes
     * @throws IllegalArgumentException if {@code web_element} is null
     * @throws RuntimeException         if the click action fails due to selenium issues
     */
    protected void safeClick(WebElement web_element, String element_name) {
        logger.debug("Preparing to click element: {}", element_name);
        if (Objects.isNull(web_element)) {
            logger.error("Attempted to click a null WebElement: {}", element_name);
            throw new IllegalArgumentException("web_element must not be null");
        }

        try {
            if (!web_element.isDisplayed()) {
                logger.warn("{} is not displayed before click attempt", element_name);
            }
            if (!web_element.isEnabled()) {
                logger.warn("{} is not enabled before click attempt", element_name);
            }

            if (web_element.isDisplayed() && web_element.isEnabled()) {
                web_element.click();
            } else {
                // Attempting to click anyway to preserve original behavior but with better diagnostics
                try {
                    web_element.click();
                } catch (Exception e) {
                    logger.error("Element {} is not interactable: {}", element_name, e.getMessage(), e);
                    throw new ElementNotInteractableException("Element " + element_name + " is not interactable");
                }
            }
        } catch (NoSuchElementException | StaleElementReferenceException | ElementNotInteractableException e) {
            logger.error("WebElement interaction failed for {}: {}", element_name, e.getMessage(), e);
            throw new RuntimeException("Failed to interact with " + element_name, e);
        } catch (Exception e) {
            logger.error("Unexpected error while clicking {}: {}", element_name, e.getMessage(), e);
            throw new RuntimeException("Unexpected error while clicking " + element_name, e);
        }
    }

    /**
     * Returns an Optional wrapping the search box WebElement.
     *
     * @return Optional of WebElement for search box
     */
    protected Optional<WebElement> getSearchBox() {
        return Optional.ofNullable(search_box);
    }

    /**
     * Returns an Optional wrapping the filter button WebElement.
     *
     * @return Optional of WebElement for filter button
     */
    protected Optional<WebElement> getFilterButton() {
        return Optional.ofNullable(filter_button);
    }

    /**
     * Returns an Optional wrapping the export button WebElement.
     *
     * @return Optional of WebElement for export button
     */
    protected Optional<WebElement> getExportButton() {
        return Optional.ofNullable(export_button);
    }
}