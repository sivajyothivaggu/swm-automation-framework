package com.swm.ui.pages.transport.VehicleManagement;

import java.util.Objects;
import java.util.Optional;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swm.core.base.BasePage;

/**
 * Page object representing the "Create Vehicle" page.
 *
 * <p>This class encapsulates interactions with the Create Vehicle UI. It performs
 * input validation, verifies that required elements are present and interactable,
 * and performs the actions necessary to create a vehicle (populate fields and submit).</p>
 *
 * <p>All WebDriver-related exceptions are translated into {@link IllegalStateException}
 * to provide a consistent failure mode for callers. Detailed logging is performed for
 * observability.</p>
 *
 * <p>Thread-safety: this page object assumes single-threaded access per WebDriver/session
 * as is typical in Selenium-based tests.</p>
 */
public class CreateVehiclePage extends BasePage {
    private static final Logger logger = LoggerFactory.getLogger(CreateVehiclePage.class);

    /**
     * Input field for vehicle number/identifier.
     */
    @FindBy(id = "vehicle-number")
    private WebElement vehicleNumber;

    /**
     * Input field for vehicle type.
     */
    @FindBy(id = "vehicle-type")
    private WebElement vehicleType;

    /**
     * Input field for capacity.
     */
    @FindBy(id = "capacity")
    private WebElement capacity;

    /**
     * Submit button for the create vehicle form.
     */
    @FindBy(id = "submit-btn")
    private WebElement submitButton;

    /**
     * Populate the Create Vehicle form and submit it.
     *
     * <p>This method validates inputs, checks that page elements are present and interactable,
     * fills out the form fields and clicks the submit button. Any parameter validation failure
     * results in {@link IllegalArgumentException}. Any problem interacting with the UI results
     * in {@link IllegalStateException} being thrown with the original exception as the cause.</p>
     *
     * @param number the vehicle number/identifier; must not be null or blank
     * @param type   the vehicle type; must not be null or blank
     * @param cap    the capacity value; must not be null or blank
     * @throws IllegalArgumentException if any input parameter is null or blank
     * @throws IllegalStateException    if required UI elements are missing, not interactable,
     *                                  or an unexpected WebDriver error occurs
     */
    public void createVehicle(String number, String type, String cap) {
        logger.debug("createVehicle called with number='{}', type='{}', cap='{}'", number, type, cap);

        // Validate parameters using Objects.isNull for null checks
        if (Objects.isNull(number) || number.isBlank()) {
            logger.error("createVehicle called with invalid 'number': {}", number);
            throw new IllegalArgumentException("Parameter 'number' must not be null or blank");
        }
        if (Objects.isNull(type) || type.isBlank()) {
            logger.error("createVehicle called with invalid 'type': {}", type);
            throw new IllegalArgumentException("Parameter 'type' must not be null or blank");
        }
        if (Objects.isNull(cap) || cap.isBlank()) {
            logger.error("createVehicle called with invalid 'cap': {}", cap);
            throw new IllegalArgumentException("Parameter 'cap' must not be null or blank");
        }

        try {
            // Verify elements are injected and interactable; obtain references via Optional
            WebElement numberElement = ensureElementPresentAndInteractable(vehicleNumber, "vehicleNumber")
                    .orElseThrow(() -> {
                        String msg = "vehicleNumber element is not present or not interactable";
                        logger.error(msg);
                        return new NoSuchElementException(msg);
                    });

            WebElement typeElement = ensureElementPresentAndInteractable(vehicleType, "vehicleType")
                    .orElseThrow(() -> {
                        String msg = "vehicleType element is not present or not interactable";
                        logger.error(msg);
                        return new NoSuchElementException(msg);
                    });

            WebElement capacityElement = ensureElementPresentAndInteractable(capacity, "capacity")
                    .orElseThrow(() -> {
                        String msg = "capacity element is not present or not interactable";
                        logger.error(msg);
                        return new NoSuchElementException(msg);
                    });

            WebElement submitEl = ensureElementPresentAndInteractable(submitButton, "submitButton")
                    .orElseThrow(() -> {
                        String msg = "submitButton element is not present or not interactable";
                        logger.error(msg);
                        return new NoSuchElementException(msg);
                    });

            // Interact with the elements safely
            safeClearAndSendKeys(numberElement, number, "vehicleNumber");
            safeClearAndSendKeys(typeElement, type, "vehicleType");
            safeClearAndSendKeys(capacityElement, cap, "capacity");

            safeClick(submitEl, "submitButton");

            logger.info("createVehicle completed successfully for number='{}'", number);
        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions unchanged after logging
            logger.error("Validation error in createVehicle: {}", e.getMessage());
            throw e;
        } catch (NoSuchElementException | ElementNotInteractableException | StaleElementReferenceException e) {
            String msg = "Element interaction failure while creating vehicle: " + e.getMessage();
            logger.error(msg, e);
            throw new IllegalStateException(msg, e);
        } catch (WebDriverException e) {
            String msg = "WebDriver encountered an error while creating vehicle: " + e.getMessage();
            logger.error(msg, e);
            throw new IllegalStateException(msg, e);
        } catch (RuntimeException e) {
            // Catch-all to ensure callers receive consistent exceptions and we log unexpected runtime issues.
            String msg = "Unexpected error while creating vehicle: " + e.getMessage();
            logger.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    /**
     * Ensure the provided WebElement reference is non-null and interactable.
     *
     * <p>If the element reference is null or accessing its state throws a Selenium-related
     * exception (stale, not present, etc.), an empty Optional is returned.</p>
     *
     * @param element the WebElement to verify
     * @param name    logical name for logging
     * @return Optional containing the element if present and interactable, otherwise Optional.empty()
     */
    private Optional<WebElement> ensureElementPresentAndInteractable(WebElement element, String name) {
        if (Objects.isNull(element)) {
            logger.warn("Element '{}' is null (not injected).", name);
            return Optional.empty();
        }

        try {
            // Accessing displayed/enabled can throw StaleElementReferenceException or NoSuchElementException
            boolean displayed = element.isDisplayed();
            boolean enabled = element.isEnabled();
            if (!displayed) {
                logger.warn("Element '{}' is not displayed.", name);
                return Optional.empty();
            }
            if (!enabled) {
                logger.warn("Element '{}' is not enabled.", name);
                return Optional.empty();
            }
            return Optional.of(element);
        } catch (StaleElementReferenceException | NoSuchElementException | WebDriverException e) {
            logger.warn("Element '{}' not present or stale: {}", name, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Safely clears a field and sends keys to it. Wraps common Selenium exceptions into
     * ElementNotInteractableException so callers can handle uniformly.
     *
     * @param element the WebElement to interact with
     * @param value   the value to send
     * @param name    logical name for logging
     */
    private void safeClearAndSendKeys(WebElement element, String value, String name) {
        try {
            element.clear();
            element.sendKeys(value);
            logger.debug("Set value for '{}': {}", name, value);
        } catch (ElementNotInteractableException | StaleElementReferenceException | WebDriverException e) {
            String msg = "Unable to set value for '" + name + "': " + e.getMessage();
            logger.error(msg, e);
            throw new ElementNotInteractableException(msg, e);
        } catch (RuntimeException e) {
            String msg = "Unexpected error while setting value for '" + name + "': " + e.getMessage();
            logger.error(msg, e);
            throw new ElementNotInteractableException(msg, e);
        }
    }

    /**
     * Safely clicks an element. Wraps Selenium exceptions with context.
     *
     * @param element the WebElement to click
     * @param name    logical name for logging
     */
    private void safeClick(WebElement element, String name) {
        try {
            element.click();
            logger.debug("Clicked element '{}'.", name);
        } catch (ElementNotInteractableException | StaleElementReferenceException | WebDriverException e) {
            String msg = "Unable to click element '" + name + "': " + e.getMessage();
            logger.error(msg, e);
            throw new ElementNotInteractableException(msg, e);
        } catch (RuntimeException e) {
            String msg = "Unexpected error while clicking element '" + name + "': " + e.getMessage();
            logger.error(msg, e);
            throw new ElementNotInteractableException(msg, e);
        }
    }
}