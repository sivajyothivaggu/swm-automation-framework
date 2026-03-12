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

            // Interact with the form elements
            try {
                numberElement.clear();
                numberElement.sendKeys(number);

                typeElement.clear();
                typeElement.sendKeys(type);

                capacityElement.clear();
                capacityElement.sendKeys(cap);

                submitEl.click();

                logger.info("Submitted create vehicle form with number='{}', type='{}', cap='{}'", number, type, cap);
            } catch (ElementNotInteractableException | WebDriverException e) {
                logger.error("Failed while interacting with form elements: number='{}', type='{}', cap='{}' - error: {}",
                        number, type, cap, e.getMessage(), e);
                throw new IllegalStateException("Failed to interact with form elements", e);
            }
        } catch (NoSuchElementException e) {
            // Wrap and rethrow with contextual information
            logger.error("Required element missing or not interactable when creating vehicle: {}", e.getMessage(), e);
            throw new IllegalStateException("Required element missing or not interactable", e);
        } catch (WebDriverException e) {
            logger.error("WebDriverException encountered while creating vehicle: {}", e.getMessage(), e);
            throw new IllegalStateException("WebDriver error while creating vehicle", e);
        } catch (RuntimeException e) {
            // Catch any other unexpected runtime exceptions to provide context
            logger.error("Unexpected error while creating vehicle: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Ensures that the given WebElement reference is not null and is interactable.
     *
     * <p>This is defensive because PageFactory injection might fail or elements
     * might not be attached to the DOM. Any exceptions are logged and an empty
     * Optional is returned to allow the caller to decide how to react.</p>
     *
     * @param element the WebElement to check
     * @param name    human-readable name of the element used for logging
     * @return Optional containing the element if present and interactable, otherwise Optional.empty()
     */
    private Optional<WebElement> ensureElementPresentAndInteractable(WebElement element, String name) {
        if (Objects.isNull(element)) {
            logger.error("Element reference '{}' is null (possibly not injected by PageFactory)", name);
            return Optional.empty();
        }
        try {
            if (!element.isDisplayed()) {
                logger.error("Element '{}' is present but not displayed", name);
                return Optional.empty();
            }
            if (!element.isEnabled()) {
                logger.error("Element '{}' is present but not enabled", name);
                return Optional.empty();
            }
            return Optional.of(element);
        } catch (NoSuchElementException e) {
            logger.error("NoSuchElementException while checking '{}': {}", name, e.getMessage(), e);
            return Optional.empty();
        } catch (ElementNotInteractableException e) {
            logger.error("ElementNotInteractableException while checking '{}': {}", name, e.getMessage(), e);
            return Optional.empty();
        } catch (WebDriverException e) {
            logger.error("WebDriverException while checking '{}': {}", name, e.getMessage(), e);
            return Optional.empty();
        } catch (RuntimeException e) {
            // Defensive catch-all for unexpected runtime exceptions during checks
            logger.error("Unexpected exception while checking element '{}': {}", name, e.getMessage(), e);
            return Optional.empty();
        }
    }
}