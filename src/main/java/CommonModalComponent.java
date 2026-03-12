package com.swm.ui.pages.transport.VehicleManagement;

import com.swm.core.base.BasePage;
import org.openqa.selenium.ElementClickInterceptedException;
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
     * Clicks the export button to trigger vehicle export.
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
     * Safe click helper that performs validations and robust exception handling.
     *
     * @param element the WebElement to click
     * @param name    a human-readable name for logging
     * @throws IllegalArgumentException if {@code element} is null
     * @throws RuntimeException         if clicking fails due to Selenium related issues
     */
    protected void safeClick(WebElement element, String name) {
        logger.debug("Attempting safe click on element: {}", name);
        if (Objects.isNull(element)) {
            logger.error("Attempted to safeClick a null element: {}", name);
            throw new IllegalArgumentException("WebElement to click must not be null: " + name);
        }

        try {
            if (!element.isDisplayed() || !element.isEnabled()) {
                String msg = String.format("%s is not interactable (displayed=%s, enabled=%s)",
                        name, element.isDisplayed(), element.isEnabled());
                logger.error(msg);
                throw new ElementNotInteractableException(msg);
            }
            element.click();
        } catch (ElementClickInterceptedException | ElementNotInteractableException |
                 NoSuchElementException | StaleElementReferenceException e) {
            logger.error("Failed to click {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Failed to click " + name, e);
        } catch (Exception e) {
            logger.error("Unexpected error while clicking {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Unexpected error while clicking " + name, e);
        }
    }

    /**
     * Returns an Optional wrapping the search box WebElement.
     *
     * @return Optional of the search box element, empty if not initialized
     */
    public Optional<WebElement> getSearchBox() {
        return Optional.ofNullable(this.searchBox);
    }

    /**
     * Returns an Optional wrapping the filter button WebElement.
     *
     * @return Optional of the filter button element, empty if not initialized
     */
    public Optional<WebElement> getFilterButton() {
        return Optional.ofNullable(this.filterButton);
    }

    /**
     * Returns an Optional wrapping the export button WebElement.
     *
     * @return Optional of the export button element, empty if not initialized
     */
    public Optional<WebElement> getExportButton() {
        return Optional.ofNullable(this.exportButton);
    }
}