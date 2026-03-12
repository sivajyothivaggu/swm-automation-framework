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
                logger.debug("Set vehicle number to '{}'", number);

                typeElement.clear();
                typeElement.sendKeys(type);
                logger.debug("Set vehicle type to '{}'", type);

                capacityElement.clear();
                capacityElement.sendKeys(cap);
                logger.debug("Set vehicle capacity to '{}'", cap);

                submitEl.click();
                logger.info("Clicked submit button to create vehicle '{}'", number);
            } catch (ElementNotInteractableException enie) {
                String msg = "One or more elements were not interactable while creating vehicle";
                logger.error(msg, enie);
                throw new IllegalStateException(msg, enie);
            } catch (WebDriverException wde) {
                String msg = "WebDriver error occurred while interacting with create vehicle form";
                logger.error(msg, wde);
                throw new IllegalStateException(msg, wde);
            } catch (RuntimeException re) {
                String msg = "Unexpected error occurred while interacting with create vehicle form";
                logger.error(msg, re);
                throw new IllegalStateException(msg, re);
            }
        } catch (NoSuchElementException nse) {
            String msg = "Required form element missing or not ready for createVehicle";
            logger.error(msg, nse);
            throw new IllegalStateException(msg, nse);
        } catch (IllegalStateException ise) {
            // Already logged above with context; propagate
            throw ise;
        } catch (RuntimeException re) {
            String msg = "Unexpected runtime error in createVehicle";
            logger.error(msg, re);
            throw new IllegalStateException(msg, re);
        }
    }

    /**
     * Ensure that the provided WebElement reference is non-null and interactable.
     *
     * <p>This helper method centralizes the null and state checks and returns an Optional
     * containing the element if it is safe to interact with, or an empty Optional
     * otherwise. Any WebDriver related exceptions are logged and result in an empty Optional.</p>
     *
     * @param element the WebElement reference injected by Selenium
     * @param name    human-friendly name for logging
     * @return Optional containing the element if present and interactable; otherwise Optional.empty()
     */
    private Optional<WebElement> ensureElementPresentAndInteractable(WebElement element, String name) {
        if (Objects.isNull(element)) {
            logger.warn("Element '{}' is null (not injected or not present in DOM).", name);
            return Optional.empty();
        }
        try {
            if (!element.isDisplayed()) {
                logger.warn("Element '{}' is present but not displayed.", name);
                return Optional.empty();
            }
            if (!element.isEnabled()) {
                logger.warn("Element '{}' is present but not enabled.", name);
                return Optional.empty();
            }
            return Optional.of(element);
        } catch (NoSuchElementException | ElementNotInteractableException | WebDriverException ex) {
            logger.error("Error while checking element '{}': {}", name, ex.getMessage(), ex);
            return Optional.empty();
        } catch (RuntimeException rte) {
            logger.error("Unexpected error while checking element '{}': {}", name, rte.getMessage(), rte);
            return Optional.empty();
        }
    }
}