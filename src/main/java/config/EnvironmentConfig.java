package com.swm.core.config;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EnvironmentConfig is responsible for loading environment-specific configuration
 * from a properties file named &lt;env&gt;.properties using PropertyReader.
 *
 * <p>This class performs validation of the input environment identifier, loads
 * properties with defensive error handling and logs meaningful messages. All
 * exposed getters return Optional to make nullable values explicit to callers.</p>
 *
 * <p>Usage example:
 * EnvironmentConfig cfg = new EnvironmentConfig("dev");
 * Optional<String> url = cfg.getUrl();
 * </p>
 */
public final class EnvironmentConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentConfig.class);

    // Property keys - constants are UPPER_CASE per Java conventions
    private static final String PROP_APP_URL = "app.url";
    private static final String PROP_API_URL = "api.url";
    private static final String PROP_DB_URL = "db.url";
    private static final String PROP_DB_USER = "db.user";
    private static final String PROP_DB_PASSWORD = "db.password";
    private static final String PROPERTIES_SUFFIX = ".properties";

    private final String url;
    private final String apiUrl;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    /**
     * Construct an EnvironmentConfig by loading properties from &lt;env&gt;.properties.
     *
     * @param env environment identifier (must not be null/empty)
     * @throws IllegalArgumentException if env is null or empty
     * @throws IllegalStateException    if properties cannot be loaded
     */
    public EnvironmentConfig(String env) {
        if (Objects.isNull(env) || env.trim().isEmpty()) {
            throw new IllegalArgumentException("env must be a non-empty string");
        }

        String fileName = env.trim() + PROPERTIES_SUFFIX;
        PropertyReader reader = null;

        String tmpUrl = null;
        String tmpApiUrl = null;
        String tmpDbUrl = null;
        String tmpDbUser = null;
        String tmpDbPassword = null;

        try {
            reader = new PropertyReader(fileName);

            // If PropertyReader implements AutoCloseable we use try-with-resources to ensure it is closed.
            if (reader instanceof AutoCloseable) {
                try (AutoCloseable ac = (AutoCloseable) reader) {
                    tmpUrl = safeTrim(reader.getProperty(PROP_APP_URL));
                    tmpApiUrl = safeTrim(reader.getProperty(PROP_API_URL));
                    tmpDbUrl = safeTrim(reader.getProperty(PROP_DB_URL));
                    tmpDbUser = safeTrim(reader.getProperty(PROP_DB_USER));
                    tmpDbPassword = safeTrim(reader.getProperty(PROP_DB_PASSWORD));

                    logLoadedProperties(fileName, tmpUrl, tmpApiUrl, tmpDbUrl, tmpDbUser);
                }
            } else {
                // Reader is not AutoCloseable: use normal try/finally and attempt reflective close in finally.
                try {
                    tmpUrl = safeTrim(reader.getProperty(PROP_APP_URL));
                    tmpApiUrl = safeTrim(reader.getProperty(PROP_API_URL));
                    tmpDbUrl = safeTrim(reader.getProperty(PROP_DB_URL));
                    tmpDbUser = safeTrim(reader.getProperty(PROP_DB_USER));
                    tmpDbPassword = safeTrim(reader.getProperty(PROP_DB_PASSWORD));

                    logLoadedProperties(fileName, tmpUrl, tmpApiUrl, tmpDbUrl, tmpDbUser);
                } finally {
                    // Attempt reflective close below in outer finally as well; keep this for safety.
                }
            }
        } catch (RuntimeException re) {
            LOGGER.error("Runtime exception while loading properties from {}", fileName, re);
            throw re;
        } catch (Exception e) {
            LOGGER.error("Failed to load properties from {}: {}", fileName, e.getMessage(), e);
            throw new IllegalStateException("Failed to load environment properties from " + fileName, e);
        } finally {
            // If reader existed but did not implement AutoCloseable, attempt to close via reflection if a close method exists.
            if (reader != null && !(reader instanceof AutoCloseable)) {
                try {
                    Method closeMethod = reader.getClass().getMethod("close");
                    if (closeMethod != null) {
                        closeMethod.invoke(reader);
                    }
                } catch (NoSuchMethodException nsme) {
                    // No close method available - nothing to do.
                    LOGGER.debug("PropertyReader for {} does not implement AutoCloseable and has no close() method", fileName);
                } catch (Exception e) {
                    LOGGER.warn("Failed to close PropertyReader for {}: {}", fileName, e.getMessage(), e);
                }
            }
        }

        this.url = tmpUrl;
        this.apiUrl = tmpApiUrl;
        this.dbUrl = tmpDbUrl;
        this.dbUser = tmpDbUser;
        this.dbPassword = tmpDbPassword;
    }

    /**
     * Safely trims input string. Returns null if input is null or trims to empty.
     *
     * @param input the raw property value
     * @return trimmed value or null if empty
     */
    private static String safeTrim(String input) {
        if (Objects.isNull(input)) {
            return null;
        }
        String t = input.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Masks a sensitive value for safe logging. For short values it returns "***".
     *
     * @param value the sensitive value
     * @return masked string for logging
     */
    private static String maskValue(String value) {
        if (Objects.isNull(value)) {
            return "<null>";
        }
        if (value.length() <= 2) {
            return "***";
        }
        // show first and last character to help debugging without exposing secret
        return value.charAt(0) + "***" + value.charAt(value.length() - 1);
    }

    /**
     * Log loaded properties while avoiding printing sensitive data.
     *
     * @param fileName properties file name
     * @param appUrl loaded app url
     * @param apiUrl loaded api url
     * @param dbUrl loaded db url
     * @param dbUser loaded db user
     */
    private void logLoadedProperties(String fileName, String appUrl, String apiUrl, String dbUrl, String dbUser) {
        if (Objects.isNull(appUrl)) {
            LOGGER.warn("Property '{}' is missing in {}", PROP_APP_URL, fileName);
        }
        if (Objects.isNull(apiUrl)) {
            LOGGER.warn("Property '{}' is missing in {}", PROP_API_URL, fileName);
        }
        LOGGER.debug("Loaded properties from {}: app.url={}, api.url={}, db.url={}, db.user={}",
                fileName,
                appUrl,
                apiUrl,
                dbUrl,
                Objects.isNull(dbUser) ? "<null>" : maskValue(dbUser));
    }

    /**
     * Returns the application URL if present.
     *
     * @return Optional containing app url or empty if not present
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(this.url);
    }

    /**
     * Returns the API URL if present.
     *
     * @return Optional containing api url or empty if not present
     */
    public Optional<String> getApiUrl() {
        return Optional.ofNullable(this.apiUrl);
    }

    /**
     * Returns the database URL if present.
     *
     * @return Optional containing db url or empty if not present
     */
    public Optional<String> getDbUrl() {
        return Optional.ofNullable(this.dbUrl);
    }

    /**
     * Returns the database user if present.
     *
     * @return Optional containing db user or empty if not present
     */
    public Optional<String> getDbUser() {
        return Optional.ofNullable(this.dbUser);
    }

    /**
     * Returns the database password if present. Callers should handle this sensitively.
     *
     * @return Optional containing db password or empty if not present
     */
    public Optional<String> getDbPassword() {
        return Optional.ofNullable(this.dbPassword);
    }
}