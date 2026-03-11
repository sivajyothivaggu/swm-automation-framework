package com.swm.ui.pages.transport.VehicleManagement;

import java.util.Objects;

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
 * Thread-safety: This page object assumes it is used in the context
 * of a single webdriver/session thread as typical for Selenium-based tests.
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
            // Verify elements are injected
            ensureElementPresentAndInteractable(vehicleNumber, "vehicleNumber");
            ensureElementPresentAndInteractable(vehicleType, "vehicleType");
            ensureElementPresentAndInteractable(capacity, "capacity");
            ensureElementPresentAndInteractable(submitButton, "submitButton");

            // Interact with the form
            vehicleNumber.clear();
            vehicleNumber.sendKeys(number);

            vehicleType.clear();
            vehicleType.sendKeys(type);

            capacity.clear();
            capacity.sendKeys(cap);

            submitButton.click();

            logger.info("CreateVehicle submitted successfully for vehicleNumber='{}', vehicleType='{}', capacity='{}'",
                    number, type, cap);
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            logger.error("Element issue while attempting to create vehicle: number='{}', type='{}', cap='{}'",
                    number, type, cap, e);
            throw new IllegalStateException("UI element not available or not interactable", e);
        } catch (WebDriverException e) {
            logger.error("WebDriver error while attempting to create vehicle: number='{}', type='{}', cap='{}'",
                    number, type, cap, e);
            throw new IllegalStateException("WebDriver error occurred while creating vehicle", e);
        } catch (Exception e) {
            logger.error("Unexpected error while attempting to create vehicle: number='{}', type='{}', cap='{}'",
                    number, type, cap, e);
            throw new IllegalStateException("Unexpected error occurred while creating vehicle", e);
        }
    }

    /**
     * Ensure the provided element is not null and appears to be interactable.
     *
     * @param element the WebElement to check
     * @param name    logical name used for logging
     * @throws NoSuchElementException           if element is null
     * @throws ElementNotInteractableException  if element is present but not displayed or not enabled
     */
    private void ensureElementPresentAndInteractable(WebElement element, String name) {
        if (Objects.isNull(element)) {
            logger.error("Required element '{}' is not present on the page (null).", name);
            throw new NoSuchElementException("Required element '" + name + "' is not present (null).");
        }
        try {
            if (!element.isDisplayed() || !element.isEnabled()) {
                logger.error("Required element '{}' is not interactable (displayed={}, enabled={}).", name,
                        element.isDisplayed(), element.isEnabled());
                throw new ElementNotInteractableException("Element '" + name + "' is not interactable.");
            }
        } catch (WebDriverException e) {
            logger.warn("Unable to determine state for element '{}'; treating as not interactable.", name, e);
            throw new ElementNotInteractableException("Unable to access element state for '" + name + "'.");
        }
    }
}