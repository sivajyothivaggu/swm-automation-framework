package com.swm.core.config;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConfigManager is a centralized, thread-safe utility for accessing environment-specific configuration values.
 *
 * Responsibilities:
 * - Initialize EnvironmentConfig based on the "env" system property (default "qa").
 * - Expose accessor methods for base URL, API URL, DB credentials and browser type.
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
     * Generic helper to safely fetch a value from the configuration and return Optional.empty() on any failure.
     * This method will not throw; it logs errors and returns an empty Optional when config is not available or if the supplier fails.
     *
     * @param supplier   supplier that retrieves the value from config
     * @param callerName method name for diagnostics
     * @param <T>        type of the returned value
     * @return Optional.of(value) if retrieval succeeded and value non-null; Optional.empty() otherwise
     */
    private static <T> Optional<T> safeGet(final Supplier<T> supplier, final String callerName) {
        if (Objects.isNull(supplier)) {
            LOGGER.log(Level.WARNING, "safeGet called with null supplier. Caller: {0}", callerName);
            return Optional.empty();
        }
        if (Objects.isNull(config)) {
            LOGGER.log(Level.FINE, "Configuration not available for optional getter. Caller: {0}", callerName);
            return Optional.empty();
        }
        try {
            final T result = supplier.get();
            return Optional.ofNullable(result);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception while retrieving configuration value. Caller: " + callerName, e);
            return Optional.empty();
        }
    }

    /**
     * Get the base URL from configuration. Throws IllegalStateException if configuration is not initialized or value is missing.
     *
     * @return base URL (non-null, non-empty)
     */
    public static String getBaseUrl() {
        ensureConfigAvailable("getBaseUrl");
        try {
            final String value = config.getBaseUrl();
            if (value == null || value.trim().isEmpty()) {
                final String message = "Base URL is not configured";
                LOGGER.log(Level.SEVERE, message);
                throw new IllegalStateException(message);
            }
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get base URL", e);
            throw new IllegalStateException("Failed to get base URL", e);
        }
    }

    /**
     * Optional-safe getter for base URL. Returns Optional.empty() on missing configuration or errors.
     *
     * @return Optional base URL
     */
    public static Optional<String> getBaseUrlOptional() {
        return safeGet(() -> config.getBaseUrl(), "getBaseUrlOptional");
    }

    /**
     * Get the API URL from configuration. Throws IllegalStateException if configuration is not initialized or value is missing.
     *
     * @return API URL (non-null, non-empty)
     */
    public static String getApiUrl() {
        ensureConfigAvailable("getApiUrl");
        try {
            final String value = config.getApiUrl();
            if (value == null || value.trim().isEmpty()) {
                final String message = "API URL is not configured";
                LOGGER.log(Level.SEVERE, message);
                throw new IllegalStateException(message);
            }
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get API URL", e);
            throw new IllegalStateException("Failed to get API URL", e);
        }
    }

    /**
     * Optional-safe getter for API URL. Returns Optional.empty() on missing configuration or errors.
     *
     * @return Optional API URL
     */
    public static Optional<String> getApiUrlOptional() {
        return safeGet(() -> config.getApiUrl(), "getApiUrlOptional");
    }

    /**
     * Get the database username from configuration. Throws IllegalStateException if configuration is not initialized or value is missing.
     *
     * @return database username (non-null, non-empty)
     */
    public static String getDbUser() {
        ensureConfigAvailable("getDbUser");
        try {
            final String value = config.getDbUser();
            if (value == null || value.trim().isEmpty()) {
                final String message = "Database user is not configured";
                LOGGER.log(Level.SEVERE, message);
                throw new IllegalStateException(message);
            }
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get database user", e);
            throw new IllegalStateException("Failed to get database user", e);
        }
    }

    /**
     * Optional-safe getter for database username. Returns Optional.empty() on missing configuration or errors.
     *
     * @return Optional database username
     */
    public static Optional<String> getDbUserOptional() {
        return safeGet(() -> config.getDbUser(), "getDbUserOptional");
    }

    /**
     * Get the database password from configuration. Throws IllegalStateException if configuration is not initialized or value is missing.
     *
     * @return database password (non-null, non-empty)
     */
    public static String getDbPassword() {
        ensureConfigAvailable("getDbPassword");
        try {
            final String value = config.getDbPassword();
            if (value == null || value.trim().isEmpty()) {
                final String message = "Database password is not configured";
                LOGGER.log(Level.SEVERE, message);
                throw new IllegalStateException(message);
            }
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get database password", e);
            throw new IllegalStateException("Failed to get database password", e);
        }
    }

    /**
     * Optional-safe getter for database password. Returns Optional.empty() on missing configuration or errors.
     *
     * @return Optional database password
     */
    public static Optional<String> getDbPasswordOptional() {
        return safeGet(() -> config.getDbPassword(), "getDbPasswordOptional");
    }

    /**
     * Get the configured browser. This returns a non-null, non-empty value (defaults to DEFAULT_BROWSER when not set).
     *
     * @return browser name
     */
    public static String getBrowser() {
        if (Objects.isNull(browser) || browser.trim().isEmpty()) {
            LOGGER.log(Level.CONFIG, "Browser not configured, defaulting to {0}", DEFAULT_BROWSER);
            return DEFAULT_BROWSER;
        }
        return browser;
    }

    /**
     * Optional-safe getter for browser.
     *
     * @return Optional browser name
     */
    public static Optional<String> getBrowserOptional() {
        return Optional.ofNullable(browser).map(String::trim).filter(s -> !s.isEmpty());
    }
}