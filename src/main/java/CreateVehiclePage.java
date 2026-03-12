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

            // Populate fields
            setElementValue(numberElement, number, "vehicleNumber");
            setElementValue(typeElement, type, "vehicleType");
            setElementValue(capacityElement, cap, "capacity");

            // Submit the form
            clickElement(submitEl, "submitButton");

            logger.info("createVehicle completed for number='{}'", number);
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Rethrow known exceptions after logging
            logger.error("createVehicle failed due to: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // Wrap unexpected exceptions to provide a consistent API
            logger.error("Unexpected error in createVehicle: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to create vehicle due to unexpected error", e);
        }
    }

    /**
     * Ensures that a given WebElement is present (non-null), displayed and enabled (interactable).
     * Returns an Optional containing the element if checks pass, or an empty Optional otherwise.
     *
     * <p>This method catches transient Selenium exceptions like {@link StaleElementReferenceException}
     * and treats them as non-interactable.</p>
     *
     * @param element element to check (may be null)
     * @param name    logical name of the element for logging
     * @return Optional containing the element when present and interactable; empty otherwise
     */
    private Optional<WebElement> ensureElementPresentAndInteractable(WebElement element, String name) {
        if (Objects.isNull(element)) {
            logger.warn("Element '{}' is null (not injected).", name);
            return Optional.empty();
        }
        try {
            boolean displayed = element.isDisplayed();
            boolean enabled = element.isEnabled();
            if (!displayed) {
                logger.warn("Element '{}' is present but not displayed.", name);
                return Optional.empty();
            }
            if (!enabled) {
                logger.warn("Element '{}' is present but not enabled.", name);
                return Optional.empty();
            }
            return Optional.of(element);
        } catch (StaleElementReferenceException sere) {
            logger.warn("Element '{}' is stale: {}", name, sere.getMessage(), sere);
            return Optional.empty();
        } catch (WebDriverException wde) {
            logger.error("WebDriver error while checking element '{}': {}", name, wde.getMessage(), wde);
            return Optional.empty();
        } catch (RuntimeException re) {
            logger.error("Unexpected runtime error while checking element '{}': {}", name, re.getMessage(), re);
            return Optional.empty();
        }
    }

    /**
     * Safely sets the value of an input element. Clears existing content and sends keys.
     *
     * @param element the target WebElement (assumed non-null and interactable)
     * @param value   the value to set
     * @param name    logical name for logging
     * @throws IllegalStateException if interaction with the element fails
     */
    private void setElementValue(WebElement element, String value, String name) {
        try {
            logger.debug("Setting value for element '{}'", name);
            element.clear();
            element.sendKeys(value);
            logger.debug("Value set for element '{}'", name);
        } catch (ElementNotInteractableException enie) {
            String msg = String.format("Element '%s' not interactable when setting value", name);
            logger.error(msg, enie);
            throw new IllegalStateException(msg, enie);
        } catch (StaleElementReferenceException sere) {
            String msg = String.format("Element '%s' became stale when setting value", name);
            logger.error(msg, sere);
            throw new IllegalStateException(msg, sere);
        } catch (WebDriverException wde) {
            String msg = String.format("WebDriver error when setting value for element '%s'", name);
            logger.error(msg, wde);
            throw new IllegalStateException(msg, wde);
        } catch (RuntimeException re) {
            String msg = String.format("Unexpected error when setting value for element '%s'", name);
            logger.error(msg, re);
            throw new IllegalStateException(msg, re);
        }
    }

    /**
     * Safely clicks the given element.
     *
     * @param element the target WebElement (assumed non-null and interactable)
     * @param name    logical name for logging
     * @throws IllegalStateException if clicking the element fails
     */
    private void clickElement(WebElement element, String name) {
        try {
            logger.debug("Clicking element '{}'", name);
            element.click();
            logger.debug("Clicked element '{}'", name);
        } catch (ElementNotInteractableException enie) {
            String msg = String.format("Element '%s' not interactable when clicking", name);
            logger.error(msg, enie);
            throw new IllegalStateException(msg, enie);
        } catch (StaleElementReferenceException sere) {
            String msg = String.format("Element '%s' became stale when clicking", name);
            logger.error(msg, sere);
            throw new IllegalStateException(msg, sere);
        } catch (WebDriverException wde) {
            String msg = String.format("WebDriver error when clicking element '%s'", name);
            logger.error(msg, wde);
            throw new IllegalStateException(msg, wde);
        } catch (RuntimeException re) {
            String msg = String.format("Unexpected error when clicking element '%s'", name);
            logger.error(msg, re);
            throw new IllegalStateException(msg, re);
        }
    }
}