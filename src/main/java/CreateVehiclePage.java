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
 * <p>This class encapsulates interactions with the Create Vehicle UI.
 * It performs input validation, element presence checks, and logs
 * errors with contextual information. It preserves the original
 * functionality of entering vehicle details and submitting the form.</p>
 *
 * <p>Thread-safety: This page object assumes it is used in the context
 * of a single webdriver/session thread as typical for Selenium-based tests.</p>
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
     * <p>This method validates the provided inputs and verifies that the expected
     * UI elements are present and interactable before performing actions. Any
     * Selenium/WebDriver exceptions are logged and rethrown as an IllegalStateException
     * to provide a consistent failure mode for callers.</p>
     *
     * @param number the vehicle number/identifier; must not be null or blank
     * @param type   the vehicle type; must not be null or blank
     * @param cap    the capacity value; must not be null or blank
     * @throws IllegalArgumentException if any input parameter is null or blank
     * @throws IllegalStateException    if required UI elements are missing, not interactable,
     *                                  or an unexpected WebDriver error occurs
     */
    public void createVehicle(String number, String type, String cap) {
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

            // Interact with the form elements using safe helpers
            interactWithInputElement(numberElement, number, "vehicleNumber");
            interactWithInputElement(typeElement, type, "vehicleType");
            interactWithInputElement(capacityElement, cap, "capacity");

            try {
                submitEl.click();
                logger.info("Clicked submit button for creating vehicle [{}]", number);
            } catch (ElementNotInteractableException e) {
                String msg = "Submit button was not interactable";
                logger.error(msg, e);
                throw new IllegalStateException(msg, e);
            } catch (WebDriverException e) {
                String msg = "WebDriverException while clicking submit button";
                logger.error(msg, e);
                throw new IllegalStateException(msg, e);
            }

        } catch (NoSuchElementException | ElementNotInteractableException e) {
            // These are expected issues when elements are missing or not interactable
            logger.error("Failed to interact with Create Vehicle page elements", e);
            throw new IllegalStateException("Failed to interact with Create Vehicle page elements", e);
        } catch (WebDriverException e) {
            // Catch broader WebDriver issues and wrap them
            logger.error("Unexpected WebDriver error while creating vehicle", e);
            throw new IllegalStateException("Unexpected WebDriver error while creating vehicle", e);
        } catch (RuntimeException e) {
            // Defensive: log any other runtime exceptions and rethrow
            logger.error("Unexpected error in createVehicle", e);
            throw e;
        }
    }

    /**
     * Ensures a WebElement is present (not null), displayed and enabled (interactable).
     *
     * <p>This method will catch common Selenium exceptions like StaleElementReferenceException
     * and ElementNotInteractableException, log them, and return Optional.empty() in such cases.</p>
     *
     * @param element the WebElement to verify; may be null if injection failed
     * @param name    friendly name of the element for logging
     * @return Optional of the WebElement if present and interactable; Optional.empty() otherwise
     */
    private Optional<WebElement> ensureElementPresentAndInteractable(WebElement element, String name) {
        if (Objects.isNull(element)) {
            logger.warn("Element reference for '{}' is null (likely not injected).", name);
            return Optional.empty();
        }
        try {
            boolean displayed = element.isDisplayed();
            boolean enabled = element.isEnabled();
            if (displayed && enabled) {
                return Optional.of(element);
            } else {
                logger.warn("Element '{}' present but not interactable. displayed={}, enabled={}", name, displayed, enabled);
                return Optional.empty();
            }
        } catch (StaleElementReferenceException e) {
            logger.warn("Element '{}' is stale and cannot be interacted with.", name, e);
            return Optional.empty();
        } catch (ElementNotInteractableException e) {
            logger.warn("Element '{}' exists but is not interactable.", name, e);
            return Optional.empty();
        } catch (NoSuchElementException e) {
            logger.warn("Element '{}' could not be found in the DOM.", name, e);
            return Optional.empty();
        } catch (WebDriverException e) {
            logger.error("WebDriverException when checking element '{}'", name, e);
            return Optional.empty();
        }
    }

    /**
     * Safely clears and sends text to an input element.
     *
     * @param element the input WebElement; expected to be present and interactable
     * @param value   the value to set
     * @param name    friendly name used for logging
     */
    private void interactWithInputElement(WebElement element, String value, String name) {
        try {
            element.clear();
            element.sendKeys(value);
            logger.debug("Set '{}': {}", name, value);
        } catch (IllegalArgumentException e) {
            String msg = String.format("Provided value for '%s' was invalid: %s", name, value);
            logger.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        } catch (ElementNotInteractableException e) {
            String msg = String.format("Element '%s' was not interactable when attempting to enter value.", name);
            logger.error(msg, e);
            throw new IllegalStateException(msg, e);
        } catch (WebDriverException e) {
            String msg = String.format("WebDriver error while interacting with element '%s'.", name);
            logger.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }
}