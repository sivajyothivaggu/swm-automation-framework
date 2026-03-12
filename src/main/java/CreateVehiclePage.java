package com.swm.ui.pages.transport.VehicleManagement;

import java.util.Objects;
import java.util.Optional;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
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
 * of a single WebDriver/session thread as typical for Selenium-based tests.</p>
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
            logger.error("createVehicle called with invalid 'number': '{}'", number);
            throw new IllegalArgumentException("Parameter 'number' must not be null or blank");
        }
        if (Objects.isNull(type) || type.isBlank()) {
            logger.error("createVehicle called with invalid 'type': '{}'", type);
            throw new IllegalArgumentException("Parameter 'type' must not be null or blank");
        }
        if (Objects.isNull(cap) || cap.isBlank()) {
            logger.error("createVehicle called with invalid 'cap': '{}'", cap);
            throw new IllegalArgumentException("Parameter 'cap' must not be null or blank");
        }

        try (LogScope ignored = new LogScope("createVehicle")) {
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

            // Interact with the form elements using robust helper methods
            safeClearAndType(numberElement, number, "vehicleNumber");
            safeClearAndType(typeElement, type, "vehicleType");
            safeClearAndType(capacityElement, cap, "capacity");

            // Click submit
            safeClick(submitEl, "submitButton");

            logger.info("createVehicle completed successfully for number='{}', type='{}', cap='{}'",
                    number, type, cap);
        } catch (NoSuchElementException | ElementNotInteractableException ex) {
            logger.error("Element interaction failed in createVehicle: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Failed to interact with page elements: " + ex.getMessage(), ex);
        } catch (WebDriverException ex) {
            logger.error("WebDriver error in createVehicle: {}", ex.getMessage(), ex);
            throw new IllegalStateException("WebDriver error while creating vehicle: " + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            logger.error("Unexpected error in createVehicle: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Unexpected error while creating vehicle: " + ex.getMessage(), ex);
        }
    }

    /**
     * Ensures that the provided WebElement is present and interactable.
     *
     * @param element the WebElement instance injected by Selenium (may be null)
     * @param name    a descriptive name used for logging
     * @return an Optional containing the WebElement if present and interactable; otherwise Optional.empty()
     */
    private Optional<WebElement> ensureElementPresentAndInteractable(WebElement element, String name) {
        try {
            if (Objects.isNull(element)) {
                logger.warn("Element '{}' is null (not injected)", name);
                return Optional.empty();
            }
            // Some elements can be present but not visible/enabled; check both.
            boolean displayed;
            boolean enabled;
            try {
                displayed = element.isDisplayed();
                enabled = element.isEnabled();
            } catch (WebDriverException e) {
                // If querying the element state throws, treat as not interactable
                logger.warn("Unable to query display/enabled state for '{}': {}", name, e.getMessage());
                return Optional.empty();
            }

            if (!displayed) {
                logger.warn("Element '{}' is not displayed", name);
                return Optional.empty();
            }
            if (!enabled) {
                logger.warn("Element '{}' is not enabled", name);
                return Optional.empty();
            }
            return Optional.of(element);
        } catch (NoSuchElementException e) {
            logger.warn("NoSuchElement when checking '{}': {}", name, e.getMessage());
            return Optional.empty();
        } catch (WebDriverException e) {
            logger.warn("WebDriverException when checking '{}': {}", name, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Unexpected exception when checking '{}': {}", name, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Clears the field and types the provided value into the WebElement in a safe manner.
     *
     * @param element the target input element
     * @param value   the value to enter
     * @param name    a descriptive name used for logging
     * @throws ElementNotInteractableException when the element cannot be interacted with
     * @throws WebDriverException              when a lower-level WebDriver error occurs
     */
    private void safeClearAndType(WebElement element, String value, String name) {
        Objects.requireNonNull(element, "WebElement must not be null for " + name);
        try {
            // Clear existing text if possible
            try {
                element.clear();
            } catch (UnsupportedOperationException | WebDriverException e) {
                // Some elements may not support clear; ignore if typing will overwrite
                logger.debug("clear() not supported or failed for '{}': {}", name, e.getMessage());
            }

            element.sendKeys(value);
            logger.debug("Entered value into '{}'", name);
        } catch (ElementNotInteractableException e) {
            logger.error("Element '{}' not interactable when sending keys: {}", name, e.getMessage(), e);
            throw e;
        } catch (WebDriverException e) {
            logger.error("WebDriverException while typing into '{}': {}", name, e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            logger.error("Unexpected exception while typing into '{}': {}", name, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Performs a safe click on the provided WebElement with logging and error handling.
     *
     * @param element the button/element to click
     * @param name    a descriptive name used for logging
     * @throws ElementNotInteractableException when the element cannot be clicked
     * @throws WebDriverException              when a lower-level WebDriver error occurs
     */
    private void safeClick(WebElement element, String name) {
        Objects.requireNonNull(element, "WebElement must not be null for " + name);
        try {
            element.click();
            logger.debug("Clicked element '{}'", name);
        } catch (ElementNotInteractableException e) {
            logger.error("Element '{}' not interactable when clicking: {}", name, e.getMessage(), e);
            throw e;
        } catch (WebDriverException e) {
            logger.error("WebDriverException while clicking '{}': {}", name, e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            logger.error("Unexpected exception while clicking '{}': {}", name, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Simple AutoCloseable scope used to demonstrate try-with-resources usage for entry/exit logging.
     * This does not manage any external resource; it's solely for structured logging.
     */
    private static final class LogScope implements AutoCloseable {
        private final String name;
        private final long startNanos;

        LogScope(String name) {
            this.name = Objects.requireNonNull(name, "name");
            this.startNanos = System.nanoTime();
            logger.debug("Entering scope '{}'", this.name);
        }

        @Override
        public void close() {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            logger.debug("Exiting scope '{}' after {} ms", this.name, elapsedMs);
        }
    }
}