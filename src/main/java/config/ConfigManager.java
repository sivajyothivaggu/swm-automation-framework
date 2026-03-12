package com.swm.core.config;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConfigManager is a centralized, thread-safe utility for accessing environment-specific configuration values.
 *
 * <p>Responsibilities:
 * - Initialize EnvironmentConfig based on the "env" system property (default "qa").
 * - Expose accessor methods for URL, API URL, DB credentials and browser type.
 *
 * <p>Behavior:
 * - Initialization problems are logged. Accessing configuration before successful initialization
 *   results in a clear IllegalStateException for strict getters, or Optional.empty() for optional getters.
 * - Supports explicit re-initialization via {@link #reload(String, String)}.
 *
 * <p>This class is final and has a private constructor to prevent instantiation.
 */
public final class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    // Defaults
    private static final String DEFAULT_ENV = "qa";
    private static final String DEFAULT_BROWSER = "chrome";

    // Holds the environment configuration instance; may be null if initialization failed.
    // Volatile ensures visibility across threads.
    private static volatile EnvironmentConfig config;

    // Browser name as provided by system properties or defaulted to DEFAULT_BROWSER.
    private static volatile String browser;

    static {
        initializeFromSystemProperties();
    }

    // Prevent instantiation.
    private ConfigManager() {
        throw new UnsupportedOperationException("Utility class, do not instantiate.");
    }

    /**
     * Initialize configuration from system properties. Called during class loading.
     * Any exception is logged and leaves config as null so callers fail fast with an appropriate error.
     */
    private static void initializeFromSystemProperties() {
        try {
            final String envName = System.getProperty("env", DEFAULT_ENV);
            final String browserName = System.getProperty("browser", DEFAULT_BROWSER);
            final EnvironmentConfig localConfig = new EnvironmentConfig(envName);
            config = localConfig;
            browser = (browserName == null || browserName.trim().isEmpty()) ? DEFAULT_BROWSER : browserName;
            LOGGER.log(Level.CONFIG, "ConfigManager initialized for environment: {0}, browser: {1}",
                    new Object[]{envName, browser});
        } catch (Exception e) {
            config = null;
            browser = DEFAULT_BROWSER;
            LOGGER.log(Level.SEVERE, "Failed to initialize EnvironmentConfig from system properties", e);
        }
    }

    /**
     * Re-initialize the configuration for a given environment and browser.
     *
     * @param envName     environment name to load (non-null, non-empty)
     * @param browserName browser name to set (nullable)
     * @throws IllegalArgumentException if envName is null or blank
     * @throws RuntimeException         if initialization of EnvironmentConfig fails
     */
    public static synchronized void reload(final String envName, final String browserName) {
        Objects.requireNonNull(envName, "envName must not be null");
        if (envName.trim().isEmpty()) {
            throw new IllegalArgumentException("envName must not be empty");
        }
        try {
            final EnvironmentConfig newConfig = new EnvironmentConfig(envName);
            config = newConfig;
            browser = (browserName == null || browserName.trim().isEmpty()) ? DEFAULT_BROWSER : browserName;
            LOGGER.log(Level.INFO, "ConfigManager reloaded for environment: {0}, browser: {1}",
                    new Object[]{envName, browser});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to reload EnvironmentConfig for environment: " + envName, e);
            throw new RuntimeException("Failed to reload configuration for environment: " + envName, e);
        }
    }

    /**
     * Ensure that the configuration has been initialized.
     *
     * @param callerName name of the calling method for better diagnostics
     * @throws IllegalStateException if configuration has not been initialized
     */
    private static void ensureConfigAvailable(final String callerName) {
        if (Objects.isNull(config)) {
            final String message = "Configuration is not initialized. Caller: " + callerName;
            LOGGER.log(Level.SEVERE, message);
            throw new IllegalStateException(message);
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
     * Returns an Optional containing the base URL for the current environment, or Optional.empty() if not available.
     *
     * @return Optional of environment URL
     */
    public static Optional<String> getOptionalUrl() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getUrl());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving optional URL from config", e);
            return Optional.empty();
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
     * Returns an Optional containing the API base URL for the current environment, or Optional.empty() if not available.
     *
     * @return Optional of API URL
     */
    public static Optional<String> getOptionalApiUrl() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getApiUrl());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving optional API URL from config", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the database username for the current environment.
     *
     * @return the DB username
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB username from the config fails
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
     * Returns an Optional containing the database username for the current environment, or Optional.empty() if not available.
     *
     * @return Optional of DB username
     */
    public static Optional<String> getOptionalDbUser() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getDbUser());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving optional DB user from config", e);
            return Optional.empty();
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
            final String value = config.getDbPassword();
            LOGGER.log(Level.FINE, "Retrieved DB password from config");
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB password from config", e);
            throw new RuntimeException("Unable to get DB password from config", e);
        }
    }

    /**
     * Returns an Optional containing the database password for the current environment, or Optional.empty() if not available.
     *
     * @return Optional of DB password
     */
    public static Optional<String> getOptionalDbPassword() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getDbPassword());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving optional DB password from config", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the configured browser name. If not set, returns the default browser.
     *
     * @return browser name (never null)
     */
    public static String getBrowser() {
        try {
            final String result = (Objects.isNull(browser) || browser.trim().isEmpty()) ? DEFAULT_BROWSER : browser;
            LOGGER.log(Level.FINE, "Retrieved browser: {0}", result);
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving browser", e);
            // Fall back to default to keep behavior safe
            return DEFAULT_BROWSER;
        }
    }

    /**
     * Returns an Optional containing the current EnvironmentConfig instance, or Optional.empty() if not initialized.
     *
     * @return Optional of EnvironmentConfig
     */
    public static Optional<EnvironmentConfig> getOptionalConfig() {
        return Optional.ofNullable(config);
    }
}