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
     * Generic helper to fetch a String value from config and handle exceptions uniformly.
     *
     * @param supplier   supplier that retrieves the value from config
     * @param callerName method name for logging/diagnostics
     * @return non-null value if available
     * @throws IllegalStateException if config is not available
     * @throws RuntimeException      if supplier throws or returns null/blank
     */
    private static String fetchStrict(final Supplier<String> supplier, final String callerName) {
        ensureConfigAvailable(callerName);
        try {
            final String value = supplier.get();
            if (Objects.isNull(value) || value.trim().isEmpty()) {
                final String message = callerName + " returned null or empty value from EnvironmentConfig";
                LOGGER.log(Level.SEVERE, message);
                throw new RuntimeException(message);
            }
            LOGGER.log(Level.FINE, "{0} retrieved successfully", callerName);
            return value;
        } catch (RuntimeException re) {
            LOGGER.log(Level.SEVERE, "Runtime exception while retrieving " + callerName, re);
            throw re;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected exception while retrieving " + callerName, e);
            throw new RuntimeException("Failed to retrieve " + callerName, e);
        }
    }

    /**
     * Generic helper to fetch an Optional String value from config and handle exceptions uniformly.
     *
     * @param supplier   supplier that retrieves the value from config
     * @param callerName method name for logging/diagnostics
     * @return Optional containing the value if available, or Optional.empty() if config not initialized or value absent
     */
    private static Optional<String> fetchOptional(final Supplier<String> supplier, final String callerName) {
        if (Objects.isNull(config)) {
            LOGGER.log(Level.FINE, "Optional access to {0} requested but configuration is not initialized", callerName);
            return Optional.empty();
        }
        try {
            final String value = supplier.get();
            final Optional<String> result = (Objects.isNull(value) || value.trim().isEmpty())
                    ? Optional.empty()
                    : Optional.of(value);
            LOGGER.log(Level.FINE, "{0} optional retrieval result present: {1}", new Object[]{callerName, result.isPresent()});
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception while retrieving optional " + callerName, e);
            return Optional.empty();
        }
    }

    /**
     * Returns the base URL for the current environment.
     *
     * @return the environment URL (never null)
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the URL from the config fails or value is missing
     */
    public static String getUrl() {
        return fetchStrict(() -> config.getUrl(), "getUrl");
    }

    /**
     * Returns the base URL for the current environment as Optional.
     *
     * @return Optional containing the URL if present and configuration initialized, otherwise Optional.empty()
     */
    public static Optional<String> getUrlOptional() {
        return fetchOptional(() -> config.getUrl(), "getUrl");
    }

    /**
     * Returns the API URL for the current environment.
     *
     * @return the API URL (never null)
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the API URL from the config fails or value is missing
     */
    public static String getApiUrl() {
        return fetchStrict(() -> config.getApiUrl(), "getApiUrl");
    }

    /**
     * Returns the API URL for the current environment as Optional.
     *
     * @return Optional containing the API URL if present and configuration initialized, otherwise Optional.empty()
     */
    public static Optional<String> getApiUrlOptional() {
        return fetchOptional(() -> config.getApiUrl(), "getApiUrl");
    }

    /**
     * Returns the database user for the current environment.
     *
     * @return the DB user (never null)
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB user from the config fails or value is missing
     */
    public static String getDbUser() {
        return fetchStrict(() -> config.getDbUser(), "getDbUser");
    }

    /**
     * Returns the database user for the current environment as Optional.
     *
     * @return Optional containing the DB user if present and configuration initialized, otherwise Optional.empty()
     */
    public static Optional<String> getDbUserOptional() {
        return fetchOptional(() -> config.getDbUser(), "getDbUser");
    }

    /**
     * Returns the database password for the current environment.
     *
     * Note: callers should handle this secret carefully. This method returns the password as a String
     * for compatibility; consider retrieving secrets from a secure store in production.
     *
     * @return the DB password (never null)
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB password from the config fails or value is missing
     */
    public static String getDbPassword() {
        return fetchStrict(() -> config.getDbPassword(), "getDbPassword");
    }

    /**
     * Returns the database password for the current environment as Optional.
     *
     * @return Optional containing the DB password if present and configuration initialized, otherwise Optional.empty()
     */
    public static Optional<String> getDbPasswordOptional() {
        return fetchOptional(() -> config.getDbPassword(), "getDbPassword");
    }

    /**
     * Returns the configured browser name. This value is always available (defaults to DEFAULT_BROWSER).
     *
     * @return configured browser name (never null)
     */
    public static String getBrowser() {
        final String current = browser;
        return Objects.isNull(current) ? DEFAULT_BROWSER : current;
    }

    /**
     * Returns an Optional containing the underlying EnvironmentConfig if initialized.
     *
     * @return Optional of EnvironmentConfig
     */
    public static Optional<EnvironmentConfig> getEnvironmentConfigOptional() {
        return Optional.ofNullable(config);
    }
}