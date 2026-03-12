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

    /**
     * Holds the environment configuration instance; may be null if initialization failed.
     * Volatile ensures visibility across threads.
     */
    private static volatile EnvironmentConfig config;

    /**
     * Browser name as provided by system properties or defaulted to DEFAULT_BROWSER.
     */
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
     * Returns the base URL for the current environment.
     *
     * @return the environment URL
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the URL from the config fails or URL is absent
     */
    public static String getUrl() {
        ensureConfigAvailable("getUrl");
        try {
            final String value = config.getUrl();
            if (Objects.isNull(value) || value.trim().isEmpty()) {
                final String msg = "URL is not configured for the current environment.";
                LOGGER.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            return value.trim();
        } catch (RuntimeException re) {
            LOGGER.log(Level.SEVERE, "Runtime error while retrieving URL", re);
            throw re;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while retrieving URL", e);
            throw new RuntimeException("Failed to retrieve URL from configuration", e);
        }
    }

    /**
     * Returns the API URL for the current environment.
     *
     * @return the API URL
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the API URL from the config fails or API URL is absent
     */
    public static String getApiUrl() {
        ensureConfigAvailable("getApiUrl");
        try {
            final String value = config.getApiUrl();
            if (Objects.isNull(value) || value.trim().isEmpty()) {
                final String msg = "API URL is not configured for the current environment.";
                LOGGER.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            return value.trim();
        } catch (RuntimeException re) {
            LOGGER.log(Level.SEVERE, "Runtime error while retrieving API URL", re);
            throw re;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while retrieving API URL", e);
            throw new RuntimeException("Failed to retrieve API URL from configuration", e);
        }
    }

    /**
     * Returns the database username for the current environment.
     *
     * @return database username
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB username from the config fails or username is absent
     */
    public static String getDbUsername() {
        ensureConfigAvailable("getDbUsername");
        try {
            final String value = config.getDbUsername();
            if (Objects.isNull(value) || value.trim().isEmpty()) {
                final String msg = "DB username is not configured for the current environment.";
                LOGGER.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            return value.trim();
        } catch (RuntimeException re) {
            LOGGER.log(Level.SEVERE, "Runtime error while retrieving DB username", re);
            throw re;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while retrieving DB username", e);
            throw new RuntimeException("Failed to retrieve DB username from configuration", e);
        }
    }

    /**
     * Returns the database password for the current environment.
     *
     * @return database password
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB password from the config fails or password is absent
     */
    public static String getDbPassword() {
        ensureConfigAvailable("getDbPassword");
        try {
            final String value = config.getDbPassword();
            if (Objects.isNull(value) || value.trim().isEmpty()) {
                final String msg = "DB password is not configured for the current environment.";
                LOGGER.log(Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            return value;
        } catch (RuntimeException re) {
            LOGGER.log(Level.SEVERE, "Runtime error while retrieving DB password", re);
            throw re;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while retrieving DB password", e);
            throw new RuntimeException("Failed to retrieve DB password from configuration", e);
        }
    }

    /**
     * Returns the configured browser name.
     *
     * @return the browser name (never null)
     */
    public static String getBrowser() {
        final String localBrowser = Objects.isNull(browser) ? DEFAULT_BROWSER : browser;
        return localBrowser.trim();
    }

    /**
     * Returns an Optional wrapping the base URL for the current environment.
     * Does not throw if configuration is missing; returns Optional.empty().
     *
     * @return Optional of URL if present; Optional.empty() otherwise
     */
    public static Optional<String> getUrlOptional() {
        try {
            if (Objects.isNull(config)) {
                LOGGER.log(Level.FINE, "getUrlOptional called but configuration is not initialized.");
                return Optional.empty();
            }
            return Optional.ofNullable(config.getUrl()).map(String::trim).filter(s -> !s.isEmpty());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while retrieving optional URL", e);
            return Optional.empty();
        }
    }

    /**
     * Returns an Optional wrapping the API URL for the current environment.
     * Does not throw if configuration is missing; returns Optional.empty().
     *
     * @return Optional of API URL if present; Optional.empty() otherwise
     */
    public static Optional<String> getApiUrlOptional() {
        try {
            if (Objects.isNull(config)) {
                LOGGER.log(Level.FINE, "getApiUrlOptional called but configuration is not initialized.");
                return Optional.empty();
            }
            return Optional.ofNullable(config.getApiUrl()).map(String::trim).filter(s -> !s.isEmpty());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while retrieving optional API URL", e);
            return Optional.empty();
        }
    }

    /**
     * Returns an Optional wrapping the DB username for the current environment.
     *
     * @return Optional of DB username if present; Optional.empty() otherwise
     */
    public static Optional<String> getDbUsernameOptional() {
        try {
            if (Objects.isNull(config)) {
                LOGGER.log(Level.FINE, "getDbUsernameOptional called but configuration is not initialized.");
                return Optional.empty();
            }
            return Optional.ofNullable(config.getDbUsername()).map(String::trim).filter(s -> !s.isEmpty());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while retrieving optional DB username", e);
            return Optional.empty();
        }
    }

    /**
     * Returns an Optional wrapping the DB password for the current environment.
     *
     * @return Optional of DB password if present; Optional.empty() otherwise
     */
    public static Optional<String> getDbPasswordOptional() {
        try {
            if (Objects.isNull(config)) {
                LOGGER.log(Level.FINE, "getDbPasswordOptional called but configuration is not initialized.");
                return Optional.empty();
            }
            return Optional.ofNullable(config.getDbPassword());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while retrieving optional DB password", e);
            return Optional.empty();
        }
    }
}