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
 * BaseComponent provides common test component functionality:
 * - Obtains a WebDriver from DriverManager
 * - Initializes WaitUtils for explicit waits
 * - Initializes PageFactory elements for subclasses
 *
 * This class ensures that a valid WebDriver is present during construction
 * and provides safe accessors that return Optional values. Detailed error
 * handling and logging are included to aid diagnostics in production.
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
            LOGGER.error("Failed to retrieve WebDriver from DriverManager due to exception.", e);
            throw new IllegalStateException("Unable to obtain WebDriver from DriverManager.", e);
        }

        if (Objects.isNull(tmpDriver)) {
            LOGGER.error("Failed to construct BaseComponent: DriverManager returned null WebDriver.");
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
     *
     * @return Optional of WebDriver
     */
    public Optional<WebDriver> getDriver() {
        return Optional.ofNullable(driver);
    }

    /**
     * Returns an Optional containing the WaitUtils associated with this component.
     *
     * @return Optional of WaitUtils
     */
    public Optional<WaitUtils> getWait() {
        return Optional.ofNullable(wait);
    }
}