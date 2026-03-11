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
 * of a single webdriver/session thread as typical for Selenium-based tests.</p>
 */
public class CreateVehiclePage extends BasePage {
    private static final Logger logger = LoggerFactory.getLogger(CreateVehiclePage.class);

    @FindBy(id = "vehicle-number")
    private WebElement vehicleNumber;

    @FindBy(id = "vehicle-type")
    private WebElement vehicleType;

    @FindBy(id = "capacity")
    private WebElement capacity;

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
        // Validate parameters
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

            // Interact with the form elements
            try {
                numberElement.clear();
                numberElement.sendKeys(number);

                typeElement.clear();
                typeElement.sendKeys(type);

                capacityElement.clear();
                capacityElement.sendKeys(cap);

                submitEl.click();

                logger.info("CreateVehicle submitted successfully for vehicleNumber='{}', vehicleType='{}', capacity='{}'",
                        number, type, cap);
            } catch (WebDriverException e) {
                // Element became stale or not interactable during interaction
                logger.error("Failed interacting with form elements while creating vehicle: number='{}', type='{}', cap='{}'",
                        number, type, cap, e);
                throw new IllegalStateException("Failed interacting with form elements", e);
            }

        } catch (NoSuchElementException | ElementNotInteractableException e) {
            logger.error("Element issue while attempting to create vehicle: number='{}', type='{}', cap='{}'",
                    number, type, cap, e);
            throw new IllegalStateException("UI element not available or not interactable", e);
        } catch (WebDriverException e) {
            logger.error("WebDriver error while attempting to create vehicle: number='{}', type='{}', cap='{}'",
                    number, type, cap, e);
            throw new IllegalStateException("WebDriver error occurred while creating vehicle", e);
        } catch (RuntimeException e) {
            logger.error("Unexpected runtime error while attempting to create vehicle: number='{}', type='{}', cap='{}'",
                    number, type, cap, e);
            throw e; // rethrow runtime exceptions to preserve calling behavior
        } catch (Exception e) {
            logger.error("Unexpected checked exception while attempting to create vehicle: number='{}', type='{}', cap='{}'",
                    number, type, cap, e);
            throw new IllegalStateException("Unexpected error occurred while creating vehicle", e);
        }
    }

    /**
     * Ensure the provided element is not null and appears to be interactable.
     *
     * <p>Returns an Optional containing the element when it appears usable, or an empty
     * Optional when the element is missing or not enabled/displayed. This method does not
     * swallow WebDriverException thrown by the underlying Selenium calls; such exceptions
     * are propagated to the caller so they can be handled with context.</p>
     *
     * @param element the WebElement to check
     * @param name    logical name used for logging
     * @return Optional of the provided WebElement if present and interactable; otherwise Optional.empty()
     * @throws WebDriverException if underlying WebDriver calls fail unexpectedly
     */
    private Optional<WebElement> ensureElementPresentAndInteractable(WebElement element, String name) {
        if (Objects.isNull(element)) {
            logger.warn("Element '{}' is null (likely not injected by PageFactory)", name);
            return Optional.empty();
        }

        try {
            boolean displayed;
            boolean enabled;

            try {
                displayed = element.isDisplayed();
            } catch (WebDriverException e) {
                logger.warn("Unable to determine visibility for element '{}'", name, e);
                throw e;
            }

            try {
                enabled = element.isEnabled();
            } catch (WebDriverException e) {
                logger.warn("Unable to determine enabled state for element '{}'", name, e);
                throw e;
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
        } catch (WebDriverException e) {
            // Bubble up WebDriver issues for callers to handle and log with context
            logger.error("WebDriverException while validating element '{}'", name, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected exception while validating element '{}'", name, e);
            // Convert to a WebDriverException-like failure so callers see consistent types
            throw new WebDriverException("Unexpected error while validating element: " + name, e);
        }
    }
}