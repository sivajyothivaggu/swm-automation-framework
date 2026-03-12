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
 * - Provide both direct accessors (which throw IllegalStateException when configuration is missing)
 *   and Optional-based accessors for callers that prefer to handle absence explicitly.
 *
 * Thread-safety:
 * - Reads are safe due to use of volatile fields.
 * - Reload operations are synchronized to ensure atomic replacement of configuration state.
 *
 * Error handling and logging:
 * - Initialization and reload failures are logged and surfaced as runtime exceptions where appropriate.
 * - Accessors validate presence of configuration and log failures with context.
 */
public final class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    /**
     * Holds the environment configuration instance; may be null if initialization failed.
     * Volatile ensures visibility across threads.
     */
    private static volatile EnvironmentConfig config;

    /**
     * Browser name as provided by system properties or defaulted to "chrome".
     * Volatile ensures visibility across threads.
     */
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
     * Any exception is logged; config remains null so callers fail fast with an appropriate error.
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
     * @param envName     environment name to load (non-null, non-blank)
     * @param browserName browser name to set (nullable or blank -> defaults to "chrome")
     * @throws IllegalArgumentException if envName is null or blank
     * @throws RuntimeException         if initialization of EnvironmentConfig fails
     */
    public static synchronized void reload(final String envName, final String browserName) {
        if (Objects.isNull(envName) || envName.trim().isEmpty()) {
            throw new IllegalArgumentException("envName must not be null or empty");
        }
        try {
            final EnvironmentConfig newConfig = new EnvironmentConfig(envName);
            config = newConfig;
            if (Objects.isNull(browserName) || browserName.trim().isEmpty()) {
                browser = "chrome";
            } else {
                browser = browserName;
            }
            LOGGER.log(Level.INFO, "ConfigManager reloaded for environment: {0}, browser: {1}",
                    new Object[]{envName, browser});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to reload EnvironmentConfig for environment: " + envName, e);
            throw new RuntimeException("Failed to reload configuration for environment: " + envName, e);
        }
    }

    /**
     * Ensures that configuration is available and throws an informative IllegalStateException otherwise.
     *
     * @param caller name of the caller method for better diagnostics
     * @throws IllegalStateException if configuration is not available
     */
    private static void ensureConfigAvailable(final String caller) {
        if (Objects.isNull(config)) {
            final String msg = "Configuration not initialized. Called by: " + caller;
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
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
     * Returns the database username for the current environment.
     *
     * @return DB username
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
     * Returns the database password for the current environment.
     *
     * Note: callers should handle this sensitive information securely and avoid logging it.
     *
     * @return DB password
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB password from the config fails
     */
    public static String getDbPassword() {
        ensureConfigAvailable("getDbPassword");
        try {
            final String value = config.getDbPassword();
            LOGGER.log(Level.FINE, "Retrieved DB password from config (value suppressed in logs)");
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB password from config", e);
            throw new RuntimeException("Unable to get DB password from config", e);
        }
    }

    /**
     * Returns the configured browser name. If no explicit browser is set, returns "chrome".
     *
     * @return browser name (never null)
     */
    public static String getBrowser() {
        final String b = browser;
        return (Objects.isNull(b) || b.trim().isEmpty()) ? "chrome" : b;
    }

    /**
     * Optional-based accessor for the environment URL.
     *
     * @return Optional containing the URL if available
     */
    public static Optional<String> getUrlOptional() {
        try {
            return Optional.ofNullable(config).map(EnvironmentConfig::getUrl);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve URL via Optional accessor", e);
            return Optional.empty();
        }
    }

    /**
     * Optional-based accessor for the API URL.
     *
     * @return Optional containing the API URL if available
     */
    public static Optional<String> getApiUrlOptional() {
        try {
            return Optional.ofNullable(config).map(EnvironmentConfig::getApiUrl);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve API URL via Optional accessor", e);
            return Optional.empty();
        }
    }

    /**
     * Optional-based accessor for the DB username.
     *
     * @return Optional containing the DB username if available
     */
    public static Optional<String> getDbUserOptional() {
        try {
            return Optional.ofNullable(config).map(EnvironmentConfig::getDbUser);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve DB user via Optional accessor", e);
            return Optional.empty();
        }
    }

    /**
     * Optional-based accessor for the DB password.
     *
     * @return Optional containing the DB password if available
     */
    public static Optional<String> getDbPasswordOptional() {
        try {
            return Optional.ofNullable(config).map(EnvironmentConfig::getDbPassword);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve DB password via Optional accessor", e);
            return Optional.empty();
        }
    }

    /**
     * Returns an Optional of the underlying EnvironmentConfig for advanced use-cases.
     *
     * @return Optional containing the EnvironmentConfig if initialized
     */
    public static Optional<EnvironmentConfig> getConfigOptional() {
        return Optional.ofNullable(config);
    }
}