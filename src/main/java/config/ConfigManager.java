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
     * @param callerName method name for logging/diagnostics
     * @param <T>        type of value to return
     * @return Optional containing the value or Optional.empty() if unavailable or an error occurred
     */
    private static <T> Optional<T> fetchOptional(final Supplier<T> supplier, final String callerName) {
        if (Objects.isNull(config)) {
            LOGGER.log(Level.FINE, "Config not available for optional fetch. Caller: {0}", callerName);
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching configuration value. Caller: " + callerName, e);
            return Optional.empty();
        }
    }

    /**
     * Generic helper to fetch a value from the configuration and throw RuntimeException on failure.
     * This is intended for strict getters where callers expect a value and any failure is fatal.
     *
     * @param supplier   supplier that retrieves the value from config
     * @param callerName method name for logging/diagnostics
     * @param <T>        type of value to return
     * @return non-null value from the supplier
     * @throws IllegalStateException if configuration is not available
     * @throws RuntimeException      if supplier throws or returns null
     */
    private static <T> T fetchOrThrow(final Supplier<T> supplier, final String callerName) {
        ensureConfigAvailable(callerName);
        try {
            final T value = supplier.get();
            if (value == null) {
                final String message = "Configuration value is null. Caller: " + callerName;
                LOGGER.log(Level.SEVERE, message);
                throw new RuntimeException(message);
            }
            return value;
        } catch (RuntimeException re) {
            LOGGER.log(Level.SEVERE, "Runtime error while fetching configuration. Caller: " + callerName, re);
            throw re;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while fetching configuration. Caller: " + callerName, e);
            throw new RuntimeException("Failed to fetch configuration value for: " + callerName, e);
        }
    }

    /**
     * Get the base URL for the current environment if available.
     *
     * @return Optional containing the base URL or Optional.empty() if unavailable
     */
    public static Optional<String> getBaseUrlOptional() {
        return fetchOptional(() -> config.getUrl(), "getBaseUrlOptional");
    }

    /**
     * Get the base URL for the current environment.
     *
     * @return base URL string
     * @throws IllegalStateException if configuration is not initialized
     * @throws RuntimeException      if the value cannot be retrieved
     */
    public static String getBaseUrl() {
        return fetchOrThrow(() -> config.getUrl(), "getBaseUrl");
    }

    /**
     * Get the API URL for the current environment if available.
     *
     * @return Optional containing the API URL or Optional.empty() if unavailable
     */
    public static Optional<String> getApiUrlOptional() {
        return fetchOptional(() -> config.getApiUrl(), "getApiUrlOptional");
    }

    /**
     * Get the API URL for the current environment.
     *
     * @return API URL string
     * @throws IllegalStateException if configuration is not initialized
     * @throws RuntimeException      if the value cannot be retrieved
     */
    public static String getApiUrl() {
        return fetchOrThrow(() -> config.getApiUrl(), "getApiUrl");
    }

    /**
     * Get the database username for the current environment if available.
     *
     * @return Optional containing the DB username or Optional.empty() if unavailable
     */
    public static Optional<String> getDbUserOptional() {
        return fetchOptional(() -> config.getDbUser(), "getDbUserOptional");
    }

    /**
     * Get the database username for the current environment.
     *
     * @return DB username string
     * @throws IllegalStateException if configuration is not initialized
     * @throws RuntimeException      if the value cannot be retrieved
     */
    public static String getDbUser() {
        return fetchOrThrow(() -> config.getDbUser(), "getDbUser");
    }

    /**
     * Get the database password for the current environment if available.
     *
     * @return Optional containing the DB password or Optional.empty() if unavailable
     */
    public static Optional<String> getDbPasswordOptional() {
        return fetchOptional(() -> config.getDbPassword(), "getDbPasswordOptional");
    }

    /**
     * Get the database password for the current environment.
     *
     * @return DB password string
     * @throws IllegalStateException if configuration is not initialized
     * @throws RuntimeException      if the value cannot be retrieved
     */
    public static String getDbPassword() {
        return fetchOrThrow(() -> config.getDbPassword(), "getDbPassword");
    }

    /**
     * Get the configured browser name if available.
     *
     * @return Optional containing the browser name or Optional.empty() if unavailable
     */
    public static Optional<String> getBrowserOptional() {
        return Optional.ofNullable(browser);
    }

    /**
     * Get the configured browser name. Always returns a non-null value (defaults to {@link #DEFAULT_BROWSER}).
     *
     * @return browser name
     */
    public static String getBrowser() {
        return Optional.ofNullable(browser).orElse(DEFAULT_BROWSER);
    }

    /**
     * Get the name of the currently loaded environment if available.
     *
     * @return Optional containing the environment name or Optional.empty() if unavailable
     */
    public static Optional<String> getEnvironmentNameOptional() {
        return fetchOptional(() -> config.getEnvironmentName(), "getEnvironmentNameOptional");
    }

    /**
     * Get the name of the currently loaded environment.
     *
     * @return environment name
     * @throws IllegalStateException if configuration is not initialized
     * @throws RuntimeException      if the value cannot be retrieved
     */
    public static String getEnvironmentName() {
        return fetchOrThrow(() -> config.getEnvironmentName(), "getEnvironmentName");
    }
}