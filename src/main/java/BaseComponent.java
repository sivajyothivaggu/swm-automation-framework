package com.swm.core.base;

import java.util.Objects;
import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swm.core.driver.DriverManager;
import com.swm.core.utils.WaitUtils;

/**
 * BaseComponent provides common functionality for page components and test components.
 *
 * Responsibilities:
 * - Obtain a WebDriver from the DriverManager
 * - Initialize a WaitUtils instance for explicit waits
 * - Initialize PageFactory elements for subclasses
 *
 * This class performs defensive checks during construction and fails fast with detailed
 * logging if required resources are not available. Accessors return Optional wrappers
 * to encourage callers to handle absent values explicitly.
 */
public class BaseComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseComponent.class);

    /**
     * The WebDriver instance used by this component. Non-null after successful construction.
     */
    protected final WebDriver driver;

    /**
     * The WaitUtils helper for this component. Non-null after successful construction.
     */
    protected final WaitUtils wait;

    /**
     * Constructs a BaseComponent by retrieving the WebDriver from DriverManager,
     * initializing WaitUtils, and initializing PageFactory elements.
     *
     * @throws IllegalStateException if DriverManager does not provide a WebDriver or initialization fails
     */
    public BaseComponent() {
        final WebDriver tmpDriver;
        try {
            tmpDriver = DriverManager.getDriver();
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve WebDriver from DriverManager for component {}. Exception: {}",
                    this.getClass().getName(), e.getMessage(), e);
            throw new IllegalStateException("Unable to obtain WebDriver from DriverManager.", e);
        }

        if (Objects.isNull(tmpDriver)) {
            LOGGER.error("DriverManager returned null WebDriver for component {}.", this.getClass().getName());
            throw new IllegalStateException("WebDriver is not initialized in DriverManager.");
        }
        this.driver = tmpDriver;

        final WaitUtils tmpWait;
        try {
            tmpWait = new WaitUtils(this.driver);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize WaitUtils for component {}. Exception: {}",
                    this.getClass().getName(), e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize WaitUtils.", e);
        }

        if (Objects.isNull(tmpWait)) {
            LOGGER.error("WaitUtils initialization returned null for component {}.", this.getClass().getName());
            throw new IllegalStateException("WaitUtils is not initialized.");
        }
        this.wait = tmpWait;

        try {
            PageFactory.initElements(this.driver, this);
            LOGGER.debug("PageFactory initialized for component {}.", this.getClass().getName());
        } catch (Exception e) {
            LOGGER.error("PageFactory initialization failed for component {}. Exception: {}",
                    this.getClass().getName(), e.getMessage(), e);
            throw new IllegalStateException("PageFactory initialization failed for " + this.getClass().getName(), e);
        }

        LOGGER.info("BaseComponent constructed successfully for {}.", this.getClass().getName());
    }

    /**
     * Returns an Optional containing the WebDriver associated with this component.
     * Callers should handle the absent case even though construction guarantees non-null driver.
     *
     * @return Optional of WebDriver
     */
    public Optional<WebDriver> getDriver() {
        try {
            return Optional.ofNullable(driver);
        } catch (Exception e) {
            LOGGER.warn("Exception while accessing WebDriver for component {}: {}",
                    this.getClass().getName(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Returns an Optional containing the WaitUtils associated with this component.
     *
     * @return Optional of WaitUtils
     */
    public Optional<WaitUtils> getWait() {
        try {
            return Optional.ofNullable(wait);
        } catch (Exception e) {
            LOGGER.warn("Exception while accessing WaitUtils for component {}: {}",
                    this.getClass().getName(), e.getMessage(), e);
            return Optional.empty();
        }
    }
}