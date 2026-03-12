package com.swm.core.config;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConfigManager is a centralized, thread-safe utility for accessing environment-specific configuration values.
 *
 * Responsibilities:
 * - Initialize EnvironmentConfig based on the "env" system property (default "qa").
 * - Expose accessor methods for URL, API URL, DB credentials and browser type.
 *
 * Behavior:
 * - Initialization problems are logged. Accessing configuration before successful initialization
 *   results in a clear IllegalStateException for strict getters, or Optional.empty() for optional getters.
 * - Supports explicit re-initialization via {@link #reload(String, String)}.
 *
 * This class is final and has a private constructor to prevent instantiation.
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
            final String rawEnv = System.getProperty("env", DEFAULT_ENV);
            final String rawBrowser = System.getProperty("browser", DEFAULT_BROWSER);

            final String envName = (rawEnv == null || rawEnv.trim().isEmpty()) ? DEFAULT_ENV : rawEnv.trim();
            final String browserName = (rawBrowser == null || rawBrowser.trim().isEmpty()) ? DEFAULT_BROWSER : rawBrowser.trim();

            final EnvironmentConfig localConfig = new EnvironmentConfig(envName);
            config = localConfig;
            browser = browserName;

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
            final EnvironmentConfig newConfig = new EnvironmentConfig(envName.trim());
            config = newConfig;
            browser = (browserName == null || browserName.trim().isEmpty()) ? DEFAULT_BROWSER : browserName.trim();
            LOGGER.log(Level.INFO, "ConfigManager reloaded for environment: {0}, browser: {1}",
                    new Object[]{envName.trim(), browser});
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
     * @throws RuntimeException      if retrieving the URL from the config fails or value is null
     */
    public static String getUrl() {
        ensureConfigAvailable("getUrl");
        try {
            final String value = config.getUrl();
            if (Objects.isNull(value) || value.trim().isEmpty()) {
                final String msg = "Environment URL is not set in configuration.";
                LOGGER.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            return value;
        } catch (RuntimeException e) {
            // rethrow runtime exceptions as-is
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve URL from configuration", e);
            throw new RuntimeException("Failed to retrieve URL from configuration", e);
        }
    }

    /**
     * Optional accessor for the environment URL.
     *
     * @return Optional containing the URL if present and retrievable; otherwise Optional.empty()
     */
    public static Optional<String> getUrlOptional() {
        if (Objects.isNull(config)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(config.getUrl()).filter(s -> !s.trim().isEmpty());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve URL from configuration (optional getter)", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the API URL for the current environment.
     *
     * @return the API URL
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the API URL from the config fails or value is null
     */
    public static String getApiUrl() {
        ensureConfigAvailable("getApiUrl");
        try {
            final String value = config.getApiUrl();
            if (Objects.isNull(value) || value.trim().isEmpty()) {
                final String msg = "API URL is not set in configuration.";
                LOGGER.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            return value;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve API URL from configuration", e);
            throw new RuntimeException("Failed to retrieve API URL from configuration", e);
        }
    }

    /**
     * Optional accessor for the API URL.
     *
     * @return Optional containing the API URL if present and retrievable; otherwise Optional.empty()
     */
    public static Optional<String> getApiUrlOptional() {
        if (Objects.isNull(config)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(config.getApiUrl()).filter(s -> !s.trim().isEmpty());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve API URL from configuration (optional getter)", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the database username for the current environment.
     *
     * @return the DB username
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB username from the config fails or value is null
     */
    public static String getDbUser() {
        ensureConfigAvailable("getDbUser");
        try {
            final String value = config.getDbUser();
            if (Objects.isNull(value) || value.trim().isEmpty()) {
                final String msg = "DB user is not set in configuration.";
                LOGGER.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            return value;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve DB user from configuration", e);
            throw new RuntimeException("Failed to retrieve DB user from configuration", e);
        }
    }

    /**
     * Optional accessor for the DB user.
     *
     * @return Optional containing the DB user if present and retrievable; otherwise Optional.empty()
     */
    public static Optional<String> getDbUserOptional() {
        if (Objects.isNull(config)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(config.getDbUser()).filter(s -> !s.trim().isEmpty());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve DB user from configuration (optional getter)", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the database password for the current environment.
     *
     * @return the DB password
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB password from the config fails or value is null
     */
    public static String getDbPassword() {
        ensureConfigAvailable("getDbPassword");
        try {
            final String value = config.getDbPassword();
            if (Objects.isNull(value) || value.trim().isEmpty()) {
                final String msg = "DB password is not set in configuration.";
                LOGGER.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            return value;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve DB password from configuration", e);
            throw new RuntimeException("Failed to retrieve DB password from configuration", e);
        }
    }

    /**
     * Optional accessor for the DB password.
     *
     * @return Optional containing the DB password if present and retrievable; otherwise Optional.empty()
     */
    public static Optional<String> getDbPasswordOptional() {
        if (Objects.isNull(config)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(config.getDbPassword()).filter(s -> !s.trim().isEmpty());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve DB password from configuration (optional getter)", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the browser name currently set for the runtime.
     *
     * @return browser name (never null)
     */
    public static String getBrowser() {
        // browser field always has a sensible default; ensure a non-null return
        final String current = browser;
        return (Objects.isNull(current) || current.trim().isEmpty()) ? DEFAULT_BROWSER : current;
    }

    /**
     * Optional accessor for the browser name.
     *
     * @return Optional containing the browser name if present; otherwise Optional.of(DEFAULT_BROWSER)
     */
    public static Optional<String> getBrowserOptional() {
        final String current = browser;
        if (Objects.isNull(current) || current.trim().isEmpty()) {
            return Optional.of(DEFAULT_BROWSER);
        }
        return Optional.of(current);
    }
}