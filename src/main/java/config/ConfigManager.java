package com.swm.core.config;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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

            browser = (Objects.isNull(browserName) || browserName.trim().isEmpty()) ? DEFAULT_BROWSER : browserName.trim();

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
            browser = (Objects.isNull(browserName) || browserName.trim().isEmpty()) ? DEFAULT_BROWSER : browserName.trim();
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
     * Generic accessor helper that applies the accessor function against the current EnvironmentConfig.
     *
     * @param accessor accessor function to retrieve a value from EnvironmentConfig
     * @param name     logical name of the value for logging/error messages
     * @return the retrieved value (may be null)
     * @throws RuntimeException if accessor throws or returns an invalid result for strict callers
     */
    private static String getValue(final Function<EnvironmentConfig, String> accessor, final String name) {
        ensureConfigAvailable(name);
        try {
            final String value = accessor.apply(config);
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving '" + name + "' from EnvironmentConfig", e);
            throw new RuntimeException("Unable to retrieve " + name + " from configuration", e);
        }
    }

    /**
     * Returns the base URL for the current environment (strict).
     *
     * @return the environment URL
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the URL from the config fails or URL is absent
     */
    public static String getUrl() {
        final String value = getValue(EnvironmentConfig::getUrl, "url");
        if (Objects.isNull(value) || value.trim().isEmpty()) {
            final String msg = "URL is not configured for the current environment";
            LOGGER.log(Level.SEVERE, msg);
            throw new RuntimeException(msg);
        }
        return value;
    }

    /**
     * Returns the base URL for the current environment as an Optional.
     *
     * @return Optional containing the URL if present, otherwise Optional.empty()
     */
    public static Optional<String> getUrlOptional() {
        try {
            final String value = getValue(EnvironmentConfig::getUrl, "url");
            return Optional.ofNullable(value).map(String::trim).filter(s -> !s.isEmpty());
        } catch (IllegalStateException e) {
            // If configuration isn't available, return empty instead of throwing for the optional variant
            LOGGER.log(Level.FINE, "getUrlOptional called when configuration is not available", e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "getUrlOptional encountered an error", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the API URL for the current environment (strict).
     *
     * @return the API URL
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the API URL fails or API URL is absent
     */
    public static String getApiUrl() {
        final String value = getValue(EnvironmentConfig::getApiUrl, "apiUrl");
        if (Objects.isNull(value) || value.trim().isEmpty()) {
            final String msg = "API URL is not configured for the current environment";
            LOGGER.log(Level.SEVERE, msg);
            throw new RuntimeException(msg);
        }
        return value;
    }

    /**
     * Returns the API URL for the current environment as an Optional.
     *
     * @return Optional containing the API URL if present, otherwise Optional.empty()
     */
    public static Optional<String> getApiUrlOptional() {
        try {
            final String value = getValue(EnvironmentConfig::getApiUrl, "apiUrl");
            return Optional.ofNullable(value).map(String::trim).filter(s -> !s.isEmpty());
        } catch (IllegalStateException e) {
            LOGGER.log(Level.FINE, "getApiUrlOptional called when configuration is not available", e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "getApiUrlOptional encountered an error", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the database username for the current environment (strict).
     *
     * @return the DB username
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the username fails or username is absent
     */
    public static String getDbUsername() {
        final String value = getValue(EnvironmentConfig::getDbUsername, "dbUsername");
        if (Objects.isNull(value) || value.trim().isEmpty()) {
            final String msg = "Database username is not configured for the current environment";
            LOGGER.log(Level.SEVERE, msg);
            throw new RuntimeException(msg);
        }
        return value;
    }

    /**
     * Returns the database username as an Optional.
     *
     * @return Optional containing the DB username if present, otherwise Optional.empty()
     */
    public static Optional<String> getDbUsernameOptional() {
        try {
            final String value = getValue(EnvironmentConfig::getDbUsername, "dbUsername");
            return Optional.ofNullable(value).map(String::trim).filter(s -> !s.isEmpty());
        } catch (IllegalStateException e) {
            LOGGER.log(Level.FINE, "getDbUsernameOptional called when configuration is not available", e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "getDbUsernameOptional encountered an error", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the database password for the current environment (strict).
     *
     * @return the DB password
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the password fails or password is absent
     */
    public static String getDbPassword() {
        final String value = getValue(EnvironmentConfig::getDbPassword, "dbPassword");
        if (Objects.isNull(value) || value.trim().isEmpty()) {
            final String msg = "Database password is not configured for the current environment";
            LOGGER.log(Level.SEVERE, msg);
            throw new RuntimeException(msg);
        }
        return value;
    }

    /**
     * Returns the database password as an Optional.
     *
     * @return Optional containing the DB password if present, otherwise Optional.empty()
     */
    public static Optional<String> getDbPasswordOptional() {
        try {
            final String value = getValue(EnvironmentConfig::getDbPassword, "dbPassword");
            return Optional.ofNullable(value).map(String::trim).filter(s -> !s.isEmpty());
        } catch (IllegalStateException e) {
            LOGGER.log(Level.FINE, "getDbPasswordOptional called when configuration is not available", e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "getDbPasswordOptional encountered an error", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the configured browser name (strict).
     *
     * @return the browser name (never null)
     */
    public static String getBrowser() {
        if (Objects.isNull(browser) || browser.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Browser was not set; returning default: {0}", DEFAULT_BROWSER);
            return DEFAULT_BROWSER;
        }
        return browser;
    }

    /**
     * Returns the configured browser name as an Optional.
     *
     * @return Optional containing the browser name if present, otherwise Optional.empty()
     */
    public static Optional<String> getBrowserOptional() {
        return Optional.ofNullable(browser).map(String::trim).filter(s -> !s.isEmpty());
    }

    /**
     * Returns whether the ConfigManager has a valid configuration loaded.
     *
     * @return true if configuration is available; false otherwise
     */
    public static boolean isInitialized() {
        return Objects.nonNull(config);
    }
}