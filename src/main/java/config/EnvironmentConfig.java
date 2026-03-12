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
 * Optional&lt;String&gt; url = cfg.getUrl();
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
                }
            } else {
                // Not AutoCloseable - read without try-with-resources and close reflectively in finally
                tmpUrl = safeTrim(reader.getProperty(PROP_APP_URL));
                tmpApiUrl = safeTrim(reader.getProperty(PROP_API_URL));
                tmpDbUrl = safeTrim(reader.getProperty(PROP_DB_URL));
                tmpDbUser = safeTrim(reader.getProperty(PROP_DB_USER));
                tmpDbPassword = safeTrim(reader.getProperty(PROP_DB_PASSWORD));
            }

            // Log missing important properties at WARN level to help diagnostics
            if (Objects.isNull(tmpUrl)) {
                LOGGER.warn("Property '{}' is missing in {}", PROP_APP_URL, fileName);
            }
            if (Objects.isNull(tmpApiUrl)) {
                LOGGER.warn("Property '{}' is missing in {}", PROP_API_URL, fileName);
            }

            LOGGER.debug("Loaded properties from {}: app.url={}, api.url={}, db.url={}, db.user={}",
                    fileName,
                    tmpUrl,
                    tmpApiUrl,
                    tmpDbUrl,
                    Objects.isNull(tmpDbUser) ? "<null>" : maskValue(tmpDbUser));

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
                        LOGGER.debug("Closed PropertyReader for {}", fileName);
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
     * Returns the application URL from config.
     *
     * @return Optional containing URL if present, otherwise Optional.empty()
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    /**
     * Returns the API URL from config.
     *
     * @return Optional containing API URL if present, otherwise Optional.empty()
     */
    public Optional<String> getApiUrl() {
        return Optional.ofNullable(apiUrl);
    }

    /**
     * Returns the database URL from config.
     *
     * @return Optional containing DB URL if present, otherwise Optional.empty()
     */
    public Optional<String> getDbUrl() {
        return Optional.ofNullable(dbUrl);
    }

    /**
     * Returns the database user from config.
     *
     * @return Optional containing DB user if present, otherwise Optional.empty()
     */
    public Optional<String> getDbUser() {
        return Optional.ofNullable(dbUser);
    }

    /**
     * Returns the database password from config.
     *
     * Note: caller should handle this secret carefully. This method exposes the
     * raw password that was read from the properties file (if present).
     *
     * @return Optional containing DB password if present, otherwise Optional.empty()
     */
    public Optional<String> getDbPassword() {
        return Optional.ofNullable(dbPassword);
    }

    /**
     * Trim a string safely and treat empty strings as null.
     *
     * @param value input string
     * @return trimmed string or null if input was null/empty after trim
     */
    private static String safeTrim(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Mask a value for logging so secrets or sensitive values are not revealed.
     *
     * This keeps the last 4 characters (if available) and replaces the rest with asterisks.
     *
     * @param value input value
     * @return masked representation suitable for logs
     */
    private static String maskValue(String value) {
        if (Objects.isNull(value)) {
            return "<null>";
        }
        final int visible = 4;
        int length = value.length();
        if (length <= visible) {
            return "****";
        }
        String last = value.substring(length - visible);
        return "****" + last;
    }
}