package com.swm.core.config;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConfigManager is a centralized utility for accessing environment-specific configuration values.
 * <p>
 * Responsibilities:
 * - Initialize EnvironmentConfig based on the "env" system property (default "qa").
 * - Expose accessor methods for URL, API URL, DB credentials and browser type.
 * <p>
 * Notes:
 * - Initialization problems are logged. Accessing configuration before successful initialization
 *   will result in a clear IllegalStateException.
 * - Additional Optional-based accessor is provided for callers that prefer to handle absence explicitly.
 */
public final class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    // Holds the environment configuration instance; may be null if initialization failed.
    private static EnvironmentConfig config;

    // Browser name as provided by system properties or defaulted to "chrome".
    private static String browser;

    static {
        try {
            String envName = System.getProperty("env", "qa");
            browser = System.getProperty("browser", "chrome");
            config = new EnvironmentConfig(envName);
            LOGGER.log(Level.CONFIG, "ConfigManager initialized for environment: {0}, browser: {1}", new Object[]{envName, browser});
        } catch (Exception e) {
            // Log full exception and leave config as null to allow callers to fail fast with a clear message.
            LOGGER.log(Level.SEVERE, "Failed to initialize EnvironmentConfig", e);
            config = null;
        }
    }

    // Private constructor to prevent instantiation.
    private ConfigManager() {
        throw new UnsupportedOperationException("Utility class, do not instantiate.");
    }

    /**
     * Returns the base URL for the current environment.
     *
     * @return the environment URL
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the URL from the config fails
     */
    public static String getUrl() {
        ensureConfigAvailable("getUrl");
        try {
            return config.getUrl();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving URL from config", e);
            throw new RuntimeException("Unable to get URL from config", e);
        }
    }

    /**
     * Returns the API base URL for the current environment.
     *
     * @return the API URL
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the API URL from the config fails
     */
    public static String getApiUrl() {
        ensureConfigAvailable("getApiUrl");
        try {
            return config.getApiUrl();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving API URL from config", e);
            throw new RuntimeException("Unable to get API URL from config", e);
        }
    }

    /**
     * Returns the database connection URL for the current environment.
     *
     * @return the DB URL
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB URL from the config fails
     */
    public static String getDbUrl() {
        ensureConfigAvailable("getDbUrl");
        try {
            return config.getDbUrl();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB URL from config", e);
            throw new RuntimeException("Unable to get DB URL from config", e);
        }
    }

    /**
     * Returns the database username for the current environment.
     *
     * @return the DB user
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB user from the config fails
     */
    public static String getDbUser() {
        ensureConfigAvailable("getDbUser");
        try {
            return config.getDbUser();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB user from config", e);
            throw new RuntimeException("Unable to get DB user from config", e);
        }
    }

    /**
     * Returns the database password for the current environment.
     *
     * @return the DB password
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB password from the config fails
     */
    public static String getDbPassword() {
        ensureConfigAvailable("getDbPassword");
        try {
            return config.getDbPassword();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB password from config", e);
            throw new RuntimeException("Unable to get DB password from config", e);
        }
    }

    /**
     * Returns the browser name configured via system properties or the default ("chrome").
     *
     * @return the browser name; never null (defaults to "chrome" if not provided)
     */
    public static String getBrowser() {
        if (Objects.isNull(browser)) {
            LOGGER.warning("Browser not initialized; defaulting to 'chrome'.");
            browser = "chrome";
        }
        return browser;
    }

    /**
     * Returns an Optional containing the browser name if present.
     *
     * @return Optional of browser name
     */
    public static Optional<String> getBrowserOptional() {
        return Optional.ofNullable(browser);
    }

    /**
     * Helper to ensure config is available before attempting to access it.
     *
     * @param caller name of the calling accessor for better logging
     * @throws IllegalStateException when config is not initialized
     */
    private static void ensureConfigAvailable(String caller) {
        if (Objects.isNull(config)) {
            String message = "EnvironmentConfig is not initialized. Cannot perform: " + caller;
            LOGGER.severe(message);
            throw new IllegalStateException(message);
        }
    }
}