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

            if (reader instanceof AutoCloseable) {
                // If PropertyReader implements AutoCloseable, use try-with-resources to ensure close.
                try (AutoCloseable resource = (AutoCloseable) reader) {
                    tmpUrl = safeTrim(reader.getProperty(PROP_APP_URL));
                    tmpApiUrl = safeTrim(reader.getProperty(PROP_API_URL));
                    tmpDbUrl = safeTrim(reader.getProperty(PROP_DB_URL));
                    tmpDbUser = safeTrim(reader.getProperty(PROP_DB_USER));
                    tmpDbPassword = safeTrim(reader.getProperty(PROP_DB_PASSWORD));
                }
            } else {
                // Reader does not implement AutoCloseable; use it and attempt to close in finally block.
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

            // Redact sensitive info in logs
            LOGGER.debug("Loaded properties from {}: app.url={}, api.url={}, db.url={}, db.user={}, db.password={}",
                    fileName,
                    tmpUrl == null ? "<null>" : tmpUrl,
                    tmpApiUrl == null ? "<null>" : tmpApiUrl,
                    tmpDbUrl == null ? "<null>" : tmpDbUrl,
                    tmpDbUser == null ? "<null>" : tmpDbUser,
                    tmpDbPassword == null ? "<null>" : "<redacted>");

        } catch (RuntimeException re) {
            LOGGER.error("Runtime exception while loading properties from {}: {}", fileName, re.getMessage(), re);
            throw re;
        } catch (Exception e) {
            LOGGER.error("Failed to load properties from {}: {}", fileName, e.getMessage(), e);
            throw new IllegalStateException("Failed to load environment properties from " + fileName, e);
        } finally {
            // If reader existed but did not implement AutoCloseable, attempt to close via reflection if a close method exists.
            if (!Objects.isNull(reader) && !(reader instanceof AutoCloseable)) {
                try {
                    Method closeMethod = reader.getClass().getMethod("close");
                    if (closeMethod != null) {
                        closeMethod.invoke(reader);
                    }
                } catch (NoSuchMethodException nsme) {
                    // No close method available - nothing to do.
                    LOGGER.debug("PropertyReader for {} does not implement AutoCloseable and has no close() method", fileName);
                } catch (Exception e) {
                    LOGGER.warn("Failed to close PropertyReader for {}: {}", fileName, String.valueOf(e.getMessage()), e);
                }
            }
        }

        // Assign to final fields (preserve values even if null)
        this.url = tmpUrl;
        this.apiUrl = tmpApiUrl;
        this.dbUrl = tmpDbUrl;
        this.dbUser = tmpDbUser;
        this.dbPassword = tmpDbPassword;
    }

    /**
     * Safely trims a string and treats empty strings as null.
     *
     * @param value input value
     * @return trimmed value or null if input was null/empty after trimming
     */
    private static String safeTrim(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * @return Optional containing application URL if present
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    /**
     * @return Optional containing API URL if present
     */
    public Optional<String> getApiUrl() {
        return Optional.ofNullable(apiUrl);
    }

    /**
     * @return Optional containing database URL if present
     */
    public Optional<String> getDbUrl() {
        return Optional.ofNullable(dbUrl);
    }

    /**
     * @return Optional containing database user if present
     */
    public Optional<String> getDbUser() {
        return Optional.ofNullable(dbUser);
    }

    /**
     * @return Optional containing database password if present. Note: callers should handle this
     * value carefully and avoid logging it.
     */
    public Optional<String> getDbPassword() {
        return Optional.ofNullable(dbPassword);
    }

    /**
     * Returns a redacted representation suitable for logs and diagnostics.
     *
     * @return redacted string
     */
    @Override
    public String toString() {
        return "EnvironmentConfig{" +
                "url=" + (url == null ? "<null>" : url) +
                ", apiUrl=" + (apiUrl == null ? "<null>" : apiUrl) +
                ", dbUrl=" + (dbUrl == null ? "<null>" : dbUrl) +
                ", dbUser=" + (dbUser == null ? "<null>" : dbUser) +
                ", dbPassword=" + (dbPassword == null ? "<null>" : "<redacted>") +
                '}';
    }
}