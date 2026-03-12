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

                logger.info("createVehicle completed: number='{}', type='{}', cap='{}'", number, type, cap);
            } catch (ElementNotInteractableException | StaleElementReferenceException e) {
                String msg = "Failed to interact with one or more form elements while creating vehicle";
                logger.error(msg + ": {}", e.getMessage(), e);
                throw new IllegalStateException(msg, e);
            } catch (WebDriverException e) {
                String msg = "WebDriver error occurred while interacting with the Create Vehicle form";
                logger.error(msg + ": {}", e.getMessage(), e);
                throw new IllegalStateException(msg, e);
            }
        } catch (NoSuchElementException e) {
            // Already logged above, but log again with context and rethrow as IllegalStateException
            String msg = "Required element missing or not interactable on Create Vehicle page";
            logger.error(msg + ": {}", e.getMessage(), e);
            throw new IllegalStateException(msg, e);
        } catch (RuntimeException e) {
            // Catch any unexpected runtime exception, log it, and wrap it to provide a consistent API.
            logger.error("Unexpected error in createVehicle: {}", e.getMessage(), e);
            throw new IllegalStateException("Unexpected error while creating vehicle", e);
        }
    }

    /**
     * Ensure that the provided WebElement reference is present and interactable.
     *
     * <p>Returns an Optional containing the element when it is non-null, displayed and enabled.
     * If the element is null or a Selenium call indicates it is not present/usable, an empty
     * Optional is returned. Exceptions during checks are caught and result in an empty Optional.</p>
     *
     * @param element the WebElement reference (may be a proxied element via @FindBy)
     * @param name    human-readable name for logging context
     * @return Optional containing the element if it is present and interactable, otherwise Optional.empty()
     */
    private Optional<WebElement> ensureElementPresentAndInteractable(WebElement element, String name) {
        if (Objects.isNull(element)) {
            logger.warn("Element '{}' is null (not injected)", name);
            return Optional.empty();
        }
        try {
            // Accessing isDisplayed/isEnabled may throw if the element is not present in DOM or stale.
            if (element.isDisplayed() && element.isEnabled()) {
                return Optional.of(element);
            } else {
                logger.warn("Element '{}' is present but not interactable (displayed: {}, enabled: {})",
                        name, safeBoolean(() -> element.isDisplayed()), safeBoolean(() -> element.isEnabled()));
                return Optional.empty();
            }
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            logger.warn("Element '{}' is not present or stale: {}", name, e.getMessage());
            return Optional.empty();
        } catch (WebDriverException e) {
            // Log and return empty so caller can handle/translate as needed.
            logger.error("WebDriver exception while checking element '{}': {}", name, e.getMessage(), e);
            return Optional.empty();
        } catch (RuntimeException e) {
            logger.error("Unexpected exception while verifying element '{}': {}", name, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Safely evaluate a boolean supplier that may throw an exception.
     *
     * @param supplier the supplier that returns a boolean
     * @return the boolean result if available; false if an exception occurs
     */
    private boolean safeBoolean(BooleanSupplierWithException supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Functional interface mirroring BooleanSupplier but allowing checked exceptions.
     */
    @FunctionalInterface
    private interface BooleanSupplierWithException {
        boolean get() throws Exception;
    }
}