package com.swm.core.driver;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * DriverManager is responsible for managing WebDriver instances on a per-thread basis.
 * <p>
 * It lazily initializes a driver for the current thread when requested, provides access
 * to the current thread's driver, and ensures proper shutdown and cleanup of the driver.
 * <p>
 * This class is thread-safe due to the usage of ThreadLocal to isolate WebDriver instances
 * between threads.
 */
public final class DriverManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverManager.class);

    /**
     * Thread-local storage for WebDriver instances. Variable name follows snake_case as required.
     */
    private static final ThreadLocal<WebDriver> driver_thread_local = new ThreadLocal<>();

    // Prevent instantiation
    private DriverManager() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }

    /**
     * Initialize a WebDriver for the current thread if one is not already present.
     *
     * @param browser_name the browser identifier used by DriverFactory to create a WebDriver
     *                     (e.g., "chrome", "firefox"). Must not be null or empty.
     * @throws IllegalArgumentException if browser_name is null or empty
     * @throws RuntimeException         if driver creation fails
     */
    public static void initDriver(String browser_name) {
        if (browser_name == null || browser_name.trim().isEmpty()) {
            throw new IllegalArgumentException("browser_name must not be null or empty");
        }

        try {
            if (Objects.isNull(driver_thread_local.get())) {
                LOGGER.debug("Initializing WebDriver for browser: {}", browser_name);
                WebDriver web_driver = DriverFactory.createDriver(browser_name);
                if (Objects.isNull(web_driver)) {
                    String msg = "DriverFactory.createDriver returned null for browser: " + browser_name;
                    LOGGER.error(msg);
                    throw new RuntimeException(msg);
                }
                driver_thread_local.set(web_driver);
                LOGGER.info("WebDriver initialized for thread: {}", Thread.currentThread().getName());
            } else {
                LOGGER.debug("WebDriver already initialized for thread: {}", Thread.currentThread().getName());
            }
        } catch (RuntimeException rte) {
            LOGGER.error("Failed to initialize WebDriver for browser {}: {}", browser_name, rte.getMessage(), rte);
            throw rte;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while initializing WebDriver for browser {}: {}", browser_name, ex.getMessage(), ex);
            throw new RuntimeException("Failed to initialize WebDriver", ex);
        }
    }

    /**
     * Returns the WebDriver instance for the current thread, or null if none has been initialized.
     * <p>
     * Note: For safer null handling, use {@link #getDriverOptional()} which returns an Optional.
     *
     * @return the current thread's WebDriver, or null if not initialized
     */
    public static WebDriver getDriver() {
        return driver_thread_local.get();
    }

    /**
     * Returns an Optional wrapping the current thread's WebDriver.
     *
     * @return Optional of WebDriver; empty if not initialized
     */
    public static Optional<WebDriver> getDriverOptional() {
        return Optional.ofNullable(driver_thread_local.get());
    }

    /**
     * Attempts to quit the WebDriver for the current thread and removes it from thread-local storage.
     * Any exceptions during quit are logged but do not prevent removal from ThreadLocal.
     */
    public static void quitDriver() {
        WebDriver web_driver = driver_thread_local.get();
        if (Objects.isNull(web_driver)) {
            LOGGER.debug("No WebDriver to quit for thread: {}", Thread.currentThread().getName());
            return;
        }

        try {
            LOGGER.debug("Attempting to quit WebDriver for thread: {}", Thread.currentThread().getName());
            web_driver.quit();
            LOGGER.info("WebDriver quit successfully for thread: {}", Thread.currentThread().getName());
        } catch (Exception ex) {
            // Log the exception and continue with cleanup to avoid thread-local leaks
            LOGGER.error("Error while quitting WebDriver for thread {}: {}", Thread.currentThread().getName(), ex.getMessage(), ex);
        } finally {
            try {
                driver_thread_local.remove();
                LOGGER.debug("WebDriver removed from ThreadLocal for thread: {}", Thread.currentThread().getName());
            } catch (Exception removeEx) {
                LOGGER.warn("Failed to remove WebDriver from ThreadLocal for thread {}: {}", Thread.currentThread().getName(), removeEx.getMessage(), removeEx);
            }
        }
    }
}