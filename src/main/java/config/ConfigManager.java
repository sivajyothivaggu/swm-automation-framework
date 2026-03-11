package com.swm.core.config;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConfigManager is a centralized utility for accessing environment-specific configuration values.
 *
 * Responsibilities:
 * - Initialize EnvironmentConfig based on the "env" system property (default "qa").
 * - Expose accessor methods for URL, API URL, DB credentials and browser type.
 *
 * Notes:
 * - Initialization problems are logged. Accessing configuration before successful initialization
 *   will result in a clear IllegalStateException.
 * - Optional-based accessors are provided for callers that prefer to handle absence explicitly.
 *
 * This class is thread-safe for reads and supports explicit re-initialization via reload().
 */
public final class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    // Holds the environment configuration instance; may be null if initialization failed.
    // Volatile ensures visibility across threads.
    private static volatile EnvironmentConfig config;

    // Browser name as provided by system properties or defaulted to "chrome".
    private static volatile String browser;

    static {
        initializeFromSystemProperties();
    }

    // Private constructor to prevent instantiation.
    private ConfigManager() {
        throw new UnsupportedOperationException("Utility class, do not instantiate.");
    }

    /**
     * Initialize configuration from system properties. Called during class loading.
     * Any exception is logged and leaves config as null so callers fail fast with an appropriate error.
     */
    private static void initializeFromSystemProperties() {
        try {
            final String envName = System.getProperty("env", "qa");
            final String browserName = System.getProperty("browser", "chrome");
            final EnvironmentConfig localConfig = new EnvironmentConfig(envName);
            config = localConfig;
            browser = browserName;
            LOGGER.log(Level.CONFIG, "ConfigManager initialized for environment: {0}, browser: {1}",
                    new Object[]{envName, browserName});
        } catch (Exception e) {
            config = null;
            browser = null;
            LOGGER.log(Level.SEVERE, "Failed to initialize EnvironmentConfig from system properties", e);
        }
    }

    /**
     * Re-initialize the configuration for a given environment and browser.
     *
     * @param envName     environment name to load (non-null)
     * @param browserName browser name to set (nullable)
     * @throws IllegalArgumentException if envName is null or blank
     * @throws RuntimeException         if initialization of EnvironmentConfig fails
     */
    public static synchronized void reload(final String envName, final String browserName) {
        if (envName == null || envName.trim().isEmpty()) {
            throw new IllegalArgumentException("envName must not be null or empty");
        }
        try {
            final EnvironmentConfig newConfig = new EnvironmentConfig(envName);
            config = newConfig;
            browser = (browserName == null || browserName.trim().isEmpty()) ? "chrome" : browserName;
            LOGGER.log(Level.INFO, "ConfigManager reloaded for environment: {0}, browser: {1}",
                    new Object[]{envName, browser});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to reload EnvironmentConfig for environment: " + envName, e);
            throw new RuntimeException("Failed to reload configuration for environment: " + envName, e);
        }
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
            final String value = config.getUrl();
            LOGGER.log(Level.FINE, "Retrieved URL from config");
            return value;
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
            final String value = config.getApiUrl();
            LOGGER.log(Level.FINE, "Retrieved API URL from config");
            return value;
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
            final String value = config.getDbUrl();
            LOGGER.log(Level.FINE, "Retrieved DB URL from config");
            return value;
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
            final String value = config.getDbUser();
            LOGGER.log(Level.FINE, "Retrieved DB user from config");
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB user from config", e);
            throw new RuntimeException("Unable to get DB user from config", e);
        }
    }

    /**
     * Returns the database password for the current environment.
     *
     * Note: Password values are sensitive. This method avoids logging the returned value.
     *
     * @return the DB password
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB password from the config fails
     */
    public static String getDbPassword() {
        ensureConfigAvailable("getDbPassword");
        try {
            final String value = config.getDbPassword();
            LOGGER.log(Level.FINE, "Retrieved DB password from config (value suppressed)");
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB password from config", e);
            throw new RuntimeException("Unable to get DB password from config", e);
        }
    }

    /**
     * Returns the browser name currently configured.
     *
     * @return the browser name
     * @throws IllegalStateException if configuration was not initialized
     */
    public static String getBrowser() {
        if (Objects.isNull(browser)) {
            throw new IllegalStateException("Browser not configured. ConfigManager may not have been initialized.");
        }
        return browser;
    }

    /**
     * Returns an Optional containing the EnvironmentConfig if available.
     *
     * @return Optional of EnvironmentConfig
     */
    public static Optional<EnvironmentConfig> getConfigOptional() {
        return Optional.ofNullable(config);
    }

    /**
     * Returns an Optional containing the browser name if available.
     *
     * @return Optional of browser name
     */
    public static Optional<String> getBrowserOptional() {
        return Optional.ofNullable(browser);
    }

    /**
     * Ensures that the configuration has been successfully initialized.
     *
     * @param caller name of the calling method for better error messages
     * @throws IllegalStateException if the configuration is not available
     */
    private static void ensureConfigAvailable(final String caller) {
        if (Objects.isNull(config)) {
            final String msg = "Configuration not initialized. Cannot execute: " + caller;
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }
    }
}