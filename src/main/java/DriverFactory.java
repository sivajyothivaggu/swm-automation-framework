package com.swm.core.driver;

import java.util.Locale;
import java.util.Objects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory responsible for instantiating WebDriver instances for supported browsers.
 *
 * <p>This factory validates inputs, logs actions and errors, and ensures that any
 * initialization errors are propagated as runtime exceptions with useful context.
 *
 * <p>Supported browsers: "chrome", "firefox", "edge"
 */
public final class DriverFactory {

    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a WebDriver instance for the provided browser name.
     *
     * @param browser the browser name (case-insensitive). Expected values: "chrome", "firefox", "edge"
     * @return initialized WebDriver instance
     * @throws IllegalArgumentException         if the browser is null/empty or not supported
     * @throws DriverInitializationException if an error occurs during WebDriver initialization
     */
    public static WebDriver createDriver(String browser) {
        if (Objects.isNull(browser) || browser.trim().isEmpty()) {
            logger.error("createDriver called with null or empty browser value");
            throw new IllegalArgumentException("Browser must not be null or empty");
        }

        final String browserName = browser.trim().toLowerCase(Locale.ROOT);
        WebDriver webDriver = null;

        logger.debug("Request received to create WebDriver for browser '{}'", browserName);

        try {
            switch (browserName) {
                case "chrome": {
                    final ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--start-maximized");
                    webDriver = new ChromeDriver(chromeOptions);
                    logger.info("Chrome WebDriver initialized successfully");
                    break;
                }

                case "firefox": {
                    webDriver = new FirefoxDriver();
                    logger.info("Firefox WebDriver initialized successfully");
                    break;
                }

                case "edge": {
                    webDriver = new EdgeDriver();
                    logger.info("Edge WebDriver initialized successfully");
                    break;
                }

                default:
                    logger.error("Unsupported browser requested: {}", browserName);
                    throw new IllegalArgumentException("Browser not supported: " + browser);
            }

            if (Objects.isNull(webDriver)) {
                // Defensive check: should not happen, but guard anyway
                logger.error("WebDriver initialization returned null for browser: {}", browserName);
                throw new DriverInitializationException(
                        "WebDriver initialization failed for browser: " + browser);
            }

            return webDriver;
        } catch (WebDriverException wde) {
            logger.error("WebDriverException while initializing browser '{}': {}",
                    browserName, wde.getMessage(), wde);
            throw new DriverInitializationException(
                    "Failed to initialize WebDriver for browser: " + browser, wde);
        } catch (IllegalArgumentException iae) {
            // Re-throw after logging for unsupported browser or invalid argument
            logger.error("Invalid argument when creating WebDriver: {}", iae.getMessage(), iae);
            throw iae;
        } catch (SecurityException se) {
            logger.error("Security exception while creating WebDriver for browser '{}': {}",
                    browserName, se.getMessage(), se);
            throw new DriverInitializationException(
                    "Security exception while creating WebDriver for browser: " + browser, se);
        } catch (Exception ex) {
            logger.error("Unexpected error while creating WebDriver for browser '{}': {}",
                    browserName, ex.getMessage(), ex);
            throw new DriverInitializationException(
                    "Unexpected error while creating WebDriver for browser: " + browser, ex);
        }
    }

    /**
     * Runtime exception used to indicate failures during WebDriver initialization.
     *
     * <p>This wrapper provides a clear exception type for callers to catch if they
     * want to handle driver initialization failures specifically.
     */
    public static class DriverInitializationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Construct a new DriverInitializationException with the specified message.
         *
         * @param message detailed message describing the initialization failure
         */
        public DriverInitializationException(String message) {
            super(message);
        }

        /**
         * Construct a new DriverInitializationException with the specified message and cause.
         *
         * @param message detailed message describing the initialization failure
         * @param cause   the underlying cause of the exception
         */
        public DriverInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}