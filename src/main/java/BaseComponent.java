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
 * BaseComponent provides common functionality for page components:
 * - Obtains a WebDriver from DriverManager
 * - Initializes a WaitUtils instance for explicit waits
 * - Initializes PageFactory elements for subclasses
 *
 * <p>This class performs defensive checks during construction to ensure a valid
 * WebDriver and WaitUtils are available. All failures are logged in detail and
 * rethrown as IllegalStateException to fail fast in test initialization.</p>
 */
public class BaseComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseComponent.class);

    /**
     * The WebDriver instance used by this component. Guaranteed non-null after successful construction.
     */
    protected final WebDriver driver;

    /**
     * The WaitUtils helper for this component. Guaranteed non-null after successful construction.
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
        } catch (RuntimeException e) {
            LOGGER.error("Exception while retrieving WebDriver from DriverManager.", e);
            throw new IllegalStateException("Unable to obtain WebDriver from DriverManager.", e);
        }

        if (Objects.isNull(tmpDriver)) {
            LOGGER.error("DriverManager returned null WebDriver during BaseComponent construction.");
            throw new IllegalStateException("WebDriver is not initialized in DriverManager.");
        }
        this.driver = tmpDriver;

        final WaitUtils tmpWait;
        try {
            tmpWait = new WaitUtils(this.driver);
        } catch (RuntimeException e) {
            LOGGER.error("Failed to initialize WaitUtils for component {}.", this.getClass().getName(), e);
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
        } catch (RuntimeException e) {
            LOGGER.error("PageFactory initialization failed for component {}.", this.getClass().getName(), e);
            throw new IllegalStateException("PageFactory initialization failed for " + this.getClass().getName(), e);
        }
    }

    /**
     * Returns an Optional containing the WebDriver associated with this component.
     * The driver is expected to be non-null after construction; however, Optional
     * is returned to provide a safe access pattern for callers.
     *
     * @return Optional containing the WebDriver
     */
    public Optional<WebDriver> getDriver() {
        if (Objects.isNull(driver)) {
            LOGGER.warn("getDriver() called but driver is null for component {}.", this.getClass().getName());
            return Optional.empty();
        }
        LOGGER.trace("getDriver() called for component {}.", this.getClass().getName());
        return Optional.of(driver);
    }

    /**
     * Returns an Optional containing the WaitUtils associated with this component.
     *
     * @return Optional containing the WaitUtils
     */
    public Optional<WaitUtils> getWait() {
        if (Objects.isNull(wait)) {
            LOGGER.warn("getWait() called but wait is null for component {}.", this.getClass().getName());
            return Optional.empty();
        }
        LOGGER.trace("getWait() called for component {}.", this.getClass().getName());
        return Optional.of(wait);
    }
}