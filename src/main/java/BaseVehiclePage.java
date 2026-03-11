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
                element.clear();
            } catch (Exception e) {
                // Not fatal; continue to sendKeys but log the event for diagnostics
                logger.debug("Ignored exception while clearing search box: {}", e.getMessage(), e);
            }

            element.sendKeys(searchText);
            logger.info("Search input provided successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid argument or state while searching vehicle: {}", e.getMessage(), e);
            throw e;
        } catch (NoSuchElementException | StaleElementReferenceException | ElementNotInteractableException e) {
            logger.error("WebElement interaction failed while searching vehicle: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to interact with search box", e);
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
        } catch (NoSuchElementException | StaleElementReferenceException | ElementNotInteractableException e) {
            logger.error("WebElement interaction failed while clicking filter button: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to click filter button", e);
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
        } catch (NoSuchElementException | StaleElementReferenceException | ElementNotInteractableException e) {
            logger.error("WebElement interaction failed while clicking export button: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to click export button", e);
        } catch (Exception e) {
            logger.error("Unexpected error while clicking export button: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during clickExport", e);
        }
    }

    /**
     * Safely clicks a WebElement while converting common Selenium exceptions into
     * runtime exceptions with improved logging.
     *
     * @param element the WebElement to click; assumed non-null
     * @param name    a human-readable name for logging purposes
     */
    private void safeClick(WebElement element, String name) {
        try {
            element.click();
        } catch (NoSuchElementException | StaleElementReferenceException | ElementNotInteractableException e) {
            logger.error("WebElement interaction failed while clicking {}: {}", name, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while clicking {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Unexpected error during click of " + name, e);
        }
    }

    /**
     * Returns the search box WebElement wrapped in an Optional.
     *
     * @return Optional containing searchBox if present, otherwise Optional.empty()
     */
    protected Optional<WebElement> getSearchBox() {
        return Optional.ofNullable(searchBox);
    }

    /**
     * Returns the filter button WebElement wrapped in an Optional.
     *
     * @return Optional containing filterButton if present, otherwise Optional.empty()
     */
    protected Optional<WebElement> getFilterButton() {
        return Optional.ofNullable(filterButton);
    }

    /**
     * Returns the export button WebElement wrapped in an Optional.
     *
     * @return Optional containing exportButton if present, otherwise Optional.empty()
     */
    protected Optional<WebElement> getExportButton() {
        return Optional.ofNullable(exportButton);
    }
}