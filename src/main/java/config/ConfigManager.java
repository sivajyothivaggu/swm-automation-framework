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
 * - Provide Optional-based accessors for callers that prefer to handle absence explicitly.
 *
 * <p>Notes:
 * - Initialization problems are logged. Accessing mandatory configuration before successful initialization
 *   will result in a clear IllegalStateException from the non-Optional accessors.
 * - Optional-based accessors return Optional.empty() if configuration is not available or an error occurs.
 *
 * <p>This class is thread-safe for reads and supports explicit re-initialization via reload().
 */
public final class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    // Holds the environment configuration instance; may be null if initialization failed.
    // Volatile ensures visibility across threads for reads/writes.
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
     * @param envName     environment name to load (non-null, non-blank)
     * @param browserName browser name to set (nullable). If null or blank, defaults to "chrome".
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
            browser = (Objects.isNull(browserName) || browserName.trim().isEmpty()) ? "chrome" : browserName;
            LOGGER.log(Level.INFO, "ConfigManager reloaded for environment: {0}, browser: {1}",
                    new Object[]{envName, browser});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to reload EnvironmentConfig for environment: " + envName, e);
            throw new RuntimeException("Failed to reload configuration for environment: " + envName, e);
        }
    }

    /**
     * Ensure that configuration is available. Throws IllegalStateException if not initialized.
     *
     * @param callerName name of the calling method for clearer diagnostics
     */
    private static void ensureConfigAvailable(final String callerName) {
        if (Objects.isNull(config)) {
            final String msg = "Configuration not initialized. Cannot perform: " + callerName;
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Returns the base URL for the current environment.
     *
     * @return the environment URL (may be null if EnvironmentConfig returns null)
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the URL from the config fails
     */
    public static String getUrl() {
        ensureConfigAvailable("getUrl");
        try {
            final String value = config.getUrl();
            if (Objects.isNull(value)) {
                LOGGER.log(Level.WARNING, "Config returned null for URL");
            } else {
                LOGGER.log(Level.FINE, "Retrieved URL from config");
            }
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving URL from config", e);
            throw new RuntimeException("Unable to get URL from config", e);
        }
    }

    /**
     * Returns the API base URL for the current environment.
     *
     * @return the API URL (may be null if EnvironmentConfig returns null)
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the API URL from the config fails
     */
    public static String getApiUrl() {
        ensureConfigAvailable("getApiUrl");
        try {
            final String value = config.getApiUrl();
            if (Objects.isNull(value)) {
                LOGGER.log(Level.WARNING, "Config returned null for API URL");
            } else {
                LOGGER.log(Level.FINE, "Retrieved API URL from config");
            }
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving API URL from config", e);
            throw new RuntimeException("Unable to get API URL from config", e);
        }
    }

    /**
     * Returns the database username for the current environment.
     *
     * @return the DB username (may be null if EnvironmentConfig returns null)
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB username from the config fails
     */
    public static String getDbUser() {
        ensureConfigAvailable("getDbUser");
        try {
            final String value = config.getDbUser();
            if (Objects.isNull(value)) {
                LOGGER.log(Level.WARNING, "Config returned null for DB user");
            } else {
                LOGGER.log(Level.FINE, "Retrieved DB user from config");
            }
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB user from config", e);
            throw new RuntimeException("Unable to get DB user from config", e);
        }
    }

    /**
     * Returns the database password for the current environment.
     *
     * @return the DB password (may be null if EnvironmentConfig returns null)
     * @throws IllegalStateException if configuration was not initialized
     * @throws RuntimeException      if retrieving the DB password from the config fails
     */
    public static String getDbPassword() {
        ensureConfigAvailable("getDbPassword");
        try {
            final String value = config.getDbPassword();
            if (Objects.isNull(value)) {
                LOGGER.log(Level.WARNING, "Config returned null for DB password");
            } else {
                LOGGER.log(Level.FINE, "Retrieved DB password from config");
            }
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB password from config", e);
            throw new RuntimeException("Unable to get DB password from config", e);
        }
    }

    /**
     * Returns the configured browser name. If browser is not set, returns the default "chrome".
     *
     * @return the browser name (never null; defaulted to "chrome")
     */
    public static String getBrowser() {
        try {
            final String localBrowser = Objects.isNull(browser) ? "chrome" : browser;
            LOGGER.log(Level.FINE, "Retrieved browser: {0}", new Object[]{localBrowser});
            return localBrowser;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error retrieving browser, defaulting to 'chrome'", e);
            return "chrome";
        }
    }

    /**
     * Optional-based accessor for the current EnvironmentConfig.
     *
     * @return Optional containing EnvironmentConfig if available, otherwise Optional.empty()
     */
    public static Optional<EnvironmentConfig> getConfigOptional() {
        try {
            return Optional.ofNullable(config);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error accessing configuration", e);
            return Optional.empty();
        }
    }

    /**
     * Optional-based accessor for URL. Does not throw if configuration is missing; returns Optional.empty().
     *
     * @return Optional containing URL if available
     */
    public static Optional<String> getOptionalUrl() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getUrl());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error retrieving optional URL from config", e);
            return Optional.empty();
        }
    }

    /**
     * Optional-based accessor for API URL. Does not throw if configuration is missing; returns Optional.empty().
     *
     * @return Optional containing API URL if available
     */
    public static Optional<String> getOptionalApiUrl() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getApiUrl());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error retrieving optional API URL from config", e);
            return Optional.empty();
        }
    }

    /**
     * Optional-based accessor for DB user.
     *
     * @return Optional containing DB user if available
     */
    public static Optional<String> getOptionalDbUser() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getDbUser());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error retrieving optional DB user from config", e);
            return Optional.empty();
        }
    }

    /**
     * Optional-based accessor for DB password.
     *
     * @return Optional containing DB password if available
     */
    public static Optional<String> getOptionalDbPassword() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getDbPassword());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error retrieving optional DB password from config", e);
            return Optional.empty();
        }
    }

    /**
     * Optional-based accessor for browser.
     *
     * @return Optional containing browser if set; otherwise Optional.of("chrome") is returned to represent default.
     */
    public static Optional<String> getOptionalBrowser() {
        try {
            final String localBrowser = Objects.isNull(browser) ? "chrome" : browser;
            return Optional.ofNullable(localBrowser);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error retrieving optional browser", e);
            return Optional.of("chrome");
        }
    }
}