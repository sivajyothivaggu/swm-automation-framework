package com.swm.core.driver;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * DriverManager is responsible for managing WebDriver instances on a per-thread basis.
 *
 * <p>
 * It lazily initializes a driver for the current thread when requested, provides access
 * to the current thread's driver, and ensures proper shutdown and cleanup of the driver.
 * </p>
 *
 * <p>
 * This class is thread-safe due to the usage of ThreadLocal to isolate WebDriver instances
 * between threads.
 * </p>
 */
public final class DriverManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverManager.class);

    /**
     * Thread-local storage for WebDriver instances.
     * Declared as static final since the reference to the ThreadLocal is constant.
     */
    private static final ThreadLocal<WebDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();

    // Prevent instantiation
    private DriverManager() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }

    /**
     * Initialize a WebDriver for the current thread if one is not already present.
     *
     * @param browserName the browser identifier used by DriverFactory to create a WebDriver
     *                    (e.g., "chrome", "firefox"). Must not be null or empty.
     * @throws IllegalArgumentException if browserName is null or empty
     * @throws RuntimeException         if driver creation fails
     */
    public static void initDriver(String browserName) {
        if (Objects.isNull(browserName) || browserName.trim().isEmpty()) {
            throw new IllegalArgumentException("browserName must not be null or empty");
        }

        try {
            if (Objects.isNull(DRIVER_THREAD_LOCAL.get())) {
                LOGGER.debug("Initializing WebDriver for browser: {}", browserName);
                WebDriver webDriver = DriverFactory.createDriver(browserName);
                if (Objects.isNull(webDriver)) {
                    String msg = "DriverFactory.createDriver returned null for browser: " + browserName;
                    LOGGER.error(msg);
                    throw new RuntimeException(msg);
                }
                DRIVER_THREAD_LOCAL.set(webDriver);
                LOGGER.info("WebDriver initialized for thread: {}", Thread.currentThread().getName());
            } else {
                LOGGER.debug("WebDriver already initialized for thread: {}", Thread.currentThread().getName());
            }
        } catch (RuntimeException rte) {
            LOGGER.error("Failed to initialize WebDriver for browser {}: {}", browserName, rte.getMessage(), rte);
            throw rte;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while initializing WebDriver for browser {}: {}", browserName, ex.getMessage(), ex);
            throw new RuntimeException("Failed to initialize WebDriver", ex);
        }
    }

    /**
     * Returns the WebDriver instance for the current thread, or null if none has been initialized.
     *
     * <p>
     * Note: For safer null handling, use {@link #getDriverOptional()} which returns an Optional.
     * </p>
     *
     * @return the current thread's WebDriver, or null if not initialized
     */
    public static WebDriver getDriver() {
        return DRIVER_THREAD_LOCAL.get();
    }

    /**
     * Returns an Optional wrapping the current thread's WebDriver.
     *
     * @return Optional of WebDriver; empty if not initialized
     */
    public static Optional<WebDriver> getDriverOptional() {
        return Optional.ofNullable(DRIVER_THREAD_LOCAL.get());
    }

    /**
     * Attempts to quit the WebDriver for the current thread and removes it from thread-local storage.
     * Any exceptions during quit are logged but do not prevent removal from ThreadLocal.
     */
    public static void quitDriver() {
        WebDriver webDriver = DRIVER_THREAD_LOCAL.get();
        String threadName = Thread.currentThread().getName();

        if (Objects.isNull(webDriver)) {
            LOGGER.debug("No WebDriver to quit for thread: {}", threadName);
            // Ensure we still attempt removal in case of unexpected state
            try {
                DRIVER_THREAD_LOCAL.remove();
            } catch (Exception removeEx) {
                LOGGER.warn("Failed to remove WebDriver from ThreadLocal for thread {}: {}", threadName, removeEx.getMessage(), removeEx);
            }
            return;
        }

        try {
            LOGGER.debug("Attempting to quit WebDriver for thread: {}", threadName);
            webDriver.quit();
            LOGGER.info("WebDriver quit successfully for thread: {}", threadName);
        } catch (Exception ex) {
            // Log the exception and continue with cleanup to avoid thread-local leaks
            LOGGER.error("Error while quitting WebDriver for thread {}: {}", threadName, ex.getMessage(), ex);
        } finally {
            try {
                DRIVER_THREAD_LOCAL.remove();
                LOGGER.debug("WebDriver removed from ThreadLocal for thread: {}", threadName);
            } catch (Exception removeEx) {
                LOGGER.warn("Failed to remove WebDriver from ThreadLocal for thread {}: {}", threadName, removeEx.getMessage(), removeEx);
            }
        }
    }
}