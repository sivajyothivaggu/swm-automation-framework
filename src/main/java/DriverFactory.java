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
public class DriverFactory {

    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a WebDriver instance for the provided browser name.
     *
     * @param browser the browser name (case-insensitive). Expected values: "chrome", "firefox", "edge"
     * @return initialized WebDriver instance
     * @throws IllegalArgumentException if the browser is null/empty or not supported
     * @throws DriverInitializationException if an error occurs during WebDriver initialization
     */
    public static WebDriver createDriver(String browser) {
        if (Objects.isNull(browser) || browser.trim().isEmpty()) {
            logger.error("createDriver called with null or empty browser value");
            throw new IllegalArgumentException("Browser must not be null or empty");
        }

        String browser_name = browser.trim().toLowerCase(Locale.ROOT);
        WebDriver web_driver = null;

        try {
            switch (browser_name) {
                case "chrome":
                    ChromeOptions chrome_options = new ChromeOptions();
                    chrome_options.addArguments("--start-maximized");
                    web_driver = new ChromeDriver(chrome_options);
                    logger.info("Chrome WebDriver initialized successfully");
                    break;

                case "firefox":
                    web_driver = new FirefoxDriver();
                    logger.info("Firefox WebDriver initialized successfully");
                    break;

                case "edge":
                    web_driver = new EdgeDriver();
                    logger.info("Edge WebDriver initialized successfully");
                    break;

                default:
                    logger.error("Unsupported browser requested: {}", browser_name);
                    throw new IllegalArgumentException("Browser not supported: " + browser);
            }

            if (Objects.isNull(web_driver)) {
                // Defensive check: should not happen, but guard anyway
                logger.error("WebDriver initialization returned null for browser: {}", browser_name);
                throw new DriverInitializationException("WebDriver initialization failed for browser: " + browser);
            }

            return web_driver;
        } catch (WebDriverException wde) {
            logger.error("WebDriverException while initializing browser {}: {}", browser_name, wde.getMessage(), wde);
            throw new DriverInitializationException("Failed to initialize WebDriver for browser: " + browser, wde);
        } catch (IllegalArgumentException iae) {
            // Re-throw after logging for unsupported browser or invalid argument
            logger.error("Invalid argument when creating WebDriver: {}", iae.getMessage(), iae);
            throw iae;
        } catch (Exception ex) {
            logger.error("Unexpected error while creating WebDriver for browser {}: {}", browser_name, ex.getMessage(), ex);
            throw new DriverInitializationException("Unexpected error while creating WebDriver for browser: " + browser, ex);
        }
    }

    /**
     * Runtime exception used to indicate failures during WebDriver initialization.
     */
    public static class DriverInitializationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public DriverInitializationException(String message) {
            super(message);
        }

        public DriverInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}