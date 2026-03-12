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
 *   will result in a clear IllegalStateException from the "requireInitialized" helper.
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

            // Validate created config and assign to volatile fields.
            if (Objects.isNull(localConfig)) {
                LOGGER.log(Level.SEVERE, "EnvironmentConfig construction returned null for env: {0}", envName);
                config = null;
                browser = browserName;
            } else {
                config = localConfig;
                browser = browserName;
                LOGGER.log(Level.INFO, "Configuration initialized for env: {0}, browser: {1}", new Object[]{envName, browserName});
            }
        } catch (Exception ex) {
            // Catch all to ensure static init does not throw; fail-fast for callers accessing config.
            LOGGER.log(Level.SEVERE, "Failed to initialize configuration from system properties", ex);
            config = null;
            browser = System.getProperty("browser", "chrome");
        }
    }

    /**
     * Reload configuration using current system properties. This is synchronized to avoid concurrent
     * re-initializations interfering with each other.
     */
    public static synchronized void reload() {
        try {
            final String envName = System.getProperty("env", "qa");
            final String browserName = System.getProperty("browser", "chrome");
            final EnvironmentConfig localConfig = new EnvironmentConfig(envName);

            if (Objects.isNull(localConfig)) {
                LOGGER.log(Level.WARNING, "Reload produced null EnvironmentConfig for env: {0}. Keeping previous config.", envName);
                // keep previous config but update browser preference
                browser = browserName;
            } else {
                config = localConfig;
                browser = browserName;
                LOGGER.log(Level.INFO, "Configuration reloaded for env: {0}, browser: {1}", new Object[]{envName, browserName});
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to reload configuration from system properties", ex);
            // Intentionally do not change existing config on reload failure to avoid leaving it null.
            browser = System.getProperty("browser", browser);
        }
    }

    /**
     * Reload configuration explicitly for a given environment and browser.
     *
     * @param envName     The environment identifier (non-null).
     * @param browserName The browser name (non-null).
     */
    public static synchronized void reload(final String envName, final String browserName) {
        Objects.requireNonNull(envName, "envName must not be null");
        Objects.requireNonNull(browserName, "browserName must not be null");
        try {
            final EnvironmentConfig localConfig = new EnvironmentConfig(envName);
            if (Objects.isNull(localConfig)) {
                LOGGER.log(Level.WARNING, "Explicit reload produced null EnvironmentConfig for env: {0}. Keeping previous config.", envName);
                browser = browserName;
            } else {
                config = localConfig;
                browser = browserName;
                LOGGER.log(Level.INFO, "Configuration explicitly reloaded for env: {0}, browser: {1}", new Object[]{envName, browserName});
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to explicitly reload configuration for env: " + envName + ", browser: " + browserName, ex);
            // Do not overwrite existing config on failure
        }
    }

    /**
     * Returns true if a valid EnvironmentConfig is available.
     *
     * @return boolean indicating initialization state
     */
    public static boolean isInitialized() {
        return Objects.nonNull(config);
    }

    /**
     * Ensure that configuration has been initialized. If not, throws IllegalStateException with a helpful message.
     *
     * @return the initialized EnvironmentConfig
     * @throws IllegalStateException if config is not initialized
     */
    private static EnvironmentConfig requireInitialized() {
        if (Objects.isNull(config)) {
            final String msg = "Configuration has not been initialized. Ensure system property 'env' is set and initialization succeeded.";
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }
        return config;
    }

    /**
     * Returns an Optional containing the base URL if present.
     *
     * @return Optional<String> base URL
     */
    public static Optional<String> getUrlOptional() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getUrl());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error retrieving URL from configuration", ex);
            return Optional.empty();
        }
    }

    /**
     * Returns the base URL, or throws IllegalStateException if configuration is not initialized or URL absent.
     *
     * @return String base URL
     */
    public static String getUrl() {
        final EnvironmentConfig cfg = requireInitialized();
        final String url = cfg.getUrl();
        if (Objects.isNull(url)) {
            final String msg = "Configured environment does not provide a URL.";
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }
        return url;
    }

    /**
     * Returns an Optional containing the API URL if present.
     *
     * @return Optional<String> API URL
     */
    public static Optional<String> getApiUrlOptional() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getApiUrl());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error retrieving API URL from configuration", ex);
            return Optional.empty();
        }
    }

    /**
     * Returns the API URL, or throws IllegalStateException if configuration is not initialized or API URL absent.
     *
     * @return String API URL
     */
    public static String getApiUrl() {
        final EnvironmentConfig cfg = requireInitialized();
        final String apiUrl = cfg.getApiUrl();
        if (Objects.isNull(apiUrl)) {
            final String msg = "Configured environment does not provide an API URL.";
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }
        return apiUrl;
    }

    /**
     * Returns an Optional containing the DB username if present.
     *
     * @return Optional<String> DB username
     */
    public static Optional<String> getDbUserOptional() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getDbUser());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB user from configuration", ex);
            return Optional.empty();
        }
    }

    /**
     * Returns the DB username, or throws IllegalStateException if configuration is not initialized or username absent.
     *
     * @return String DB username
     */
    public static String getDbUser() {
        final EnvironmentConfig cfg = requireInitialized();
        final String dbUser = cfg.getDbUser();
        if (Objects.isNull(dbUser)) {
            final String msg = "Configured environment does not provide a DB user.";
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }
        return dbUser;
    }

    /**
     * Returns an Optional containing the DB password if present.
     *
     * @return Optional<String> DB password
     */
    public static Optional<String> getDbPasswordOptional() {
        try {
            if (Objects.isNull(config)) {
                return Optional.empty();
            }
            return Optional.ofNullable(config.getDbPassword());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error retrieving DB password from configuration", ex);
            return Optional.empty();
        }
    }

    /**
     * Returns the DB password, or throws IllegalStateException if configuration is not initialized or password absent.
     *
     * @return String DB password
     */
    public static String getDbPassword() {
        final EnvironmentConfig cfg = requireInitialized();
        final String dbPassword = cfg.getDbPassword();
        if (Objects.isNull(dbPassword)) {
            final String msg = "Configured environment does not provide a DB password.";
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }
        return dbPassword;
    }

    /**
     * Returns an Optional with the configured browser name.
     *
     * @return Optional<String> browser name
     */
    public static Optional<String> getBrowserOptional() {
        return Optional.ofNullable(browser);
    }

    /**
     * Returns the configured browser. If not set, returns the default "chrome".
     *
     * @return String browser name
     */
    public static String getBrowser() {
        return Objects.nonNull(browser) ? browser : "chrome";
    }
}