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
        // Validate parameters using Objects.isNull as a best practice
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

        logger.debug("createVehicle called with number='{}', type='{}', cap='{}'", number, type, cap);

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

                logger.info("createVehicle completed successfully for number='{}'", number);
            } catch (ElementNotInteractableException | StaleElementReferenceException e) {
                String msg = "Element not interactable while filling the Create Vehicle form";
                logger.error(msg, e);
                throw new IllegalStateException(msg, e);
            } catch (WebDriverException e) {
                String msg = "WebDriver error occurred while interacting with the Create Vehicle form";
                logger.error(msg, e);
                throw new IllegalStateException(msg, e);
            }
        } catch (NoSuchElementException e) {
            // Already logged at the point of failure; wrap and rethrow to provide consistent API
            throw new IllegalStateException("Required UI element missing or not interactable", e);
        } catch (RuntimeException e) {
            // Catch-all to ensure we log unexpected runtime exceptions and present a consistent failure
            logger.error("Unexpected error in createVehicle", e);
            throw new IllegalStateException("Unexpected error while creating vehicle", e);
        }
    }

    /**
     * Ensures the provided WebElement is non-null, displayed, and enabled (interactable).
     *
     * <p>This helper method centralizes the presence and interactability checks and
     * converts exceptions thrown by Selenium into an empty Optional, while logging
     * contextual information for easier debugging.</p>
     *
     * @param element the WebElement reference injected by the PageFactory
     * @param name    a friendly name for logging purposes
     * @return Optional containing the element if present and interactable; otherwise Optional.empty()
     */
    private Optional<WebElement> ensureElementPresentAndInteractable(WebElement element, String name) {
        if (Objects.isNull(element)) {
            logger.warn("Element '{}' is null (not injected).", name);
            return Optional.empty();
        }
        try {
            if (element.isDisplayed() && element.isEnabled()) {
                return Optional.of(element);
            } else {
                logger.warn("Element '{}' is present but not interactable (displayed={}, enabled={}).",
                        name,
                        safeIsDisplayed(element),
                        safeIsEnabled(element));
                return Optional.empty();
            }
        } catch (NoSuchElementException | ElementNotInteractableException | StaleElementReferenceException e) {
            logger.warn("Element '{}' reported an exception when checking interactability: {}", name, e.getMessage());
            logger.debug("Full exception for element '{}':", name, e);
            return Optional.empty();
        } catch (WebDriverException e) {
            logger.error("WebDriver exception while verifying element '{}': {}", name, e.getMessage());
            logger.debug("Full WebDriver exception for element '{}':", name, e);
            return Optional.empty();
        } catch (RuntimeException e) {
            logger.error("Unexpected exception while verifying element '{}': {}", name, e.getMessage());
            logger.debug("Full unexpected exception for element '{}':", name, e);
            return Optional.empty();
        }
    }

    /**
     * Safe wrapper around WebElement.isDisplayed() that returns false if an exception occurs.
     *
     * @param element the WebElement to check
     * @return true if displayed and no exception; false otherwise
     */
    private boolean safeIsDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            logger.debug("safeIsDisplayed caught exception: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Safe wrapper around WebElement.isEnabled() that returns false if an exception occurs.
     *
     * @param element the WebElement to check
     * @return true if enabled and no exception; false otherwise
     */
    private boolean safeIsEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            logger.debug("safeIsEnabled caught exception: {}", e.getMessage());
            return false;
        }
    }
}