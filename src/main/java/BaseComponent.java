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
 * <p>This class ensures that a valid WebDriver is present during construction
 * and provides safe accessors that return Optional values.</p>
 *
 * <p>All subclasses should rely on getDriver() and getWait() to access the
 * underlying driver and wait utilities. Construction will fail fast with
 * clear logging if the required runtime dependencies are not available.</p>
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
     * @throws IllegalStateException if DriverManager does not provide a WebDriver
     * @throws RuntimeException if PageFactory initialization fails or WaitUtils cannot be constructed
     */
    public BaseComponent() {
        WebDriver tmpDriver = null;
        try {
            tmpDriver = DriverManager.getDriver();
        } catch (Exception e) {
            LOGGER.error("Exception while obtaining WebDriver from DriverManager.", e);
            throw new IllegalStateException("Failed to obtain WebDriver from DriverManager.", e);
        }

        if (Objects.isNull(tmpDriver)) {
            LOGGER.error("Failed to construct BaseComponent: DriverManager returned null WebDriver.");
            throw new IllegalStateException("WebDriver is not initialized in DriverManager.");
        }

        this.driver = tmpDriver;

        try {
            this.wait = new WaitUtils(this.driver);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize WaitUtils for component {}.", this.getClass().getName(), e);
            throw new RuntimeException("Failed to initialize WaitUtils.", e);
        }

        try {
            PageFactory.initElements(this.driver, this);
            LOGGER.debug("PageFactory initialized for component {}.", this.getClass().getName());
        } catch (RuntimeException e) {
            LOGGER.error("PageFactory initialization failed for component {}.", this.getClass().getName(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected exception during PageFactory initialization for component {}.", this.getClass().getName(), e);
            throw new RuntimeException("Unexpected error during PageFactory initialization.", e);
        }

        LOGGER.info("BaseComponent constructed successfully for {}.", this.getClass().getName());
    }

    /**
     * Returns an Optional containing the WebDriver associated with this component.
     *
     * @return Optional of WebDriver; Optional.empty() if none present
     */
    public Optional<WebDriver> getDriver() {
        return Optional.ofNullable(this.driver);
    }

    /**
     * Returns an Optional containing the WaitUtils associated with this component.
     *
     * @return Optional of WaitUtils; Optional.empty() if none present
     */
    public Optional<WaitUtils> getWait() {
        return Optional.ofNullable(this.wait);
    }
}