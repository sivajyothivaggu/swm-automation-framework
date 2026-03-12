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
 *   will result in a clear IllegalStateException.
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
        String envName = "qa";
        String browserName = "chrome";
        try {
            final String rawEnv = System.getProperty("env");
            final String rawBrowser = System.getProperty("browser");
            if (!Objects.isNull(rawEnv) && !rawEnv.trim().isEmpty()) {
                envName = rawEnv.trim().toLowerCase();
            }
            if (!Objects.isNull(rawBrowser) && !rawBrowser.trim().isEmpty()) {
                browserName = rawBrowser.trim().toLowerCase();
            }

            EnvironmentConfig localConfig = null;
            try {
                // Constructing EnvironmentConfig may throw; isolate to catch and log specific failures.
                localConfig = new EnvironmentConfig(envName);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to construct EnvironmentConfig for env: " + envName, e);
                // Ensure localConfig remains null to indicate failed initialization.
                localConfig = null;
            }

            if (!Objects.isNull(localConfig)) {
                config = localConfig;
                browser = browserName;
                LOGGER.log(Level.INFO, "ConfigManager initialized for env: {0}, browser: {1}", new Object[]{envName, browserName});
            } else {
                // Explicitly set defaults when initialization failed to avoid undefined state.
                config = null;
                browser = browserName;
                LOGGER.log(Level.WARNING, "EnvironmentConfig is null after initialization attempt for env: {0}", envName);
            }
        } catch (SecurityException se) {
            // Security manager may prevent reading system properties.
            LOGGER.log(Level.SEVERE, "SecurityManager prevented access to system properties during initialization.", se);
            config = null;
            browser = browserName;
        } catch (Exception e) {
            // Catch-all to prevent class initialization from failing.
            LOGGER.log(Level.SEVERE, "Unexpected error during ConfigManager initialization.", e);
            config = null;
            browser = browserName;
        }
    }

    /**
     * Reload configuration from system properties. Thread-safe and will replace the current configuration.
     *
     * @return true if reload produced a non-null configuration, false otherwise.
     */
    public static boolean reload() {
        synchronized (ConfigManager.class) {
            try {
                initializeFromSystemProperties();
                if (!Objects.isNull(config)) {
                    LOGGER.log(Level.INFO, "ConfigManager reloaded successfully.");
                    return true;
                } else {
                    LOGGER.log(Level.WARNING, "ConfigManager reload completed but configuration is null.");
                    return false;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error reloading configuration.", e);
                return false;
            }
        }
    }

    /**
     * Returns the EnvironmentConfig if available.
     *
     * @return Optional containing the EnvironmentConfig or empty if not initialized.
     */
    public static Optional<EnvironmentConfig> getConfigOptional() {
        return Optional.ofNullable(config);
    }

    /**
     * Returns the current EnvironmentConfig or throws IllegalStateException if not initialized.
     *
     * @return EnvironmentConfig instance
     * @throws IllegalStateException if configuration is not initialized
     */
    public static EnvironmentConfig getConfig() {
        if (Objects.isNull(config)) {
            final String msg = "Configuration has not been initialized. Ensure system property 'env' is set and valid, or call ConfigManager.reload().";
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }
        return config;
    }

    /**
     * Returns the base URL from the configuration if present.
     *
     * @return Optional containing the URL string or empty if not available.
     */
    public static Optional<String> getUrlOptional() {
        try {
            return getConfigOptional().map(EnvironmentConfig::getUrl);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve URL from config.", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the base URL from the configuration or throws IllegalStateException if config is missing.
     *
     * @return URL string
     */
    public static String getUrl() {
        return getConfig().getUrl();
    }

    /**
     * Returns the API URL from the configuration if present.
     *
     * @return Optional containing the API URL string or empty if not available.
     */
    public static Optional<String> getApiUrlOptional() {
        try {
            return getConfigOptional().map(EnvironmentConfig::getApiUrl);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve API URL from config.", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the API URL from the configuration or throws IllegalStateException if config is missing.
     *
     * @return API URL string
     */
    public static String getApiUrl() {
        return getConfig().getApiUrl();
    }

    /**
     * Returns the DB user from the configuration if present.
     *
     * @return Optional containing DB user or empty if not available.
     */
    public static Optional<String> getDbUserOptional() {
        try {
            return getConfigOptional().map(EnvironmentConfig::getDbUser);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve DB user from config.", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the DB password from the configuration if present.
     *
     * Note: Caller is responsible for handling secrets securely.
     *
     * @return Optional containing DB password or empty if not available.
     */
    public static Optional<String> getDbPasswordOptional() {
        try {
            return getConfigOptional().map(EnvironmentConfig::getDbPassword);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve DB password from config.", e);
            return Optional.empty();
        }
    }

    /**
     * Returns the browser being used as configured or system default.
     *
     * @return Optional containing browser name or empty if not set.
     */
    public static Optional<String> getBrowserOptional() {
        return Optional.ofNullable(browser);
    }

    /**
     * Returns the configured browser or the default ("chrome") if none was set.
     *
     * @return browser name string
     */
    public static String getBrowser() {
        final String b = browser == null ? "chrome" : browser;
        return b;
    }
}