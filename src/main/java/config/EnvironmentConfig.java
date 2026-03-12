package com.swm.core.config;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EnvironmentConfig is responsible for loading environment specific configuration
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
     * @throws IllegalStateException if properties cannot be loaded
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

            // If PropertyReader implements AutoCloseable we will close it via try-with-resources.
            // If not, we'll attempt to close it via reflection in the finally block.
            try (AutoCloseable ignored = reader instanceof AutoCloseable ? (AutoCloseable) reader : null) {
                try {
                    tmpUrl = safeTrim(reader.getProperty(PROP_APP_URL));
                } catch (Exception e) {
                    LOGGER.warn("Failed to read property '{}' from {}: {}", PROP_APP_URL, fileName, e.getMessage(), e);
                    tmpUrl = null;
                }

                try {
                    tmpApiUrl = safeTrim(reader.getProperty(PROP_API_URL));
                } catch (Exception e) {
                    LOGGER.warn("Failed to read property '{}' from {}: {}", PROP_API_URL, fileName, e.getMessage(), e);
                    tmpApiUrl = null;
                }

                try {
                    tmpDbUrl = safeTrim(reader.getProperty(PROP_DB_URL));
                } catch (Exception e) {
                    LOGGER.warn("Failed to read property '{}' from {}: {}", PROP_DB_URL, fileName, e.getMessage(), e);
                    tmpDbUrl = null;
                }

                try {
                    tmpDbUser = safeTrim(reader.getProperty(PROP_DB_USER));
                } catch (Exception e) {
                    LOGGER.warn("Failed to read property '{}' from {}: {}", PROP_DB_USER, fileName, e.getMessage(), e);
                    tmpDbUser = null;
                }

                try {
                    tmpDbPassword = safeTrim(reader.getProperty(PROP_DB_PASSWORD));
                } catch (Exception e) {
                    LOGGER.warn("Failed to read property '{}' from {}: {}", PROP_DB_PASSWORD, fileName, e.getMessage(), e);
                    tmpDbPassword = null;
                }

                // Log missing important properties at WARN level to help diagnostics
                if (Objects.isNull(tmpUrl)) {
                    LOGGER.warn("Property '{}' is missing or empty in {}", PROP_APP_URL, fileName);
                }
                if (Objects.isNull(tmpApiUrl)) {
                    LOGGER.warn("Property '{}' is missing or empty in {}", PROP_API_URL, fileName);
                }

                LOGGER.debug("Loaded properties from {}: app.url={}, api.url={}, db.url={}, db.user={}",
                        fileName,
                        tmpUrl == null ? "<null>" : tmpUrl,
                        tmpApiUrl == null ? "<null>" : tmpApiUrl,
                        tmpDbUrl == null ? "<null>" : tmpDbUrl,
                        Objects.isNull(tmpDbUser) ? "<null>" : "<removed-for-security>");
                // dbPassword intentionally not logged for security reasons
            } // try-with-resources will close reader if it is AutoCloseable
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
                        LOGGER.debug("Invoked close() on PropertyReader for {}", fileName);
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
     * Safely trims the input string and returns null if the result is empty or input is null.
     *
     * @param value the input string
     * @return trimmed string or null if empty
     */
    private static String safeTrim(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Returns the application URL if available.
     *
     * @return Optional containing app.url or empty if not present
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    /**
     * Returns the API base URL if available.
     *
     * @return Optional containing api.url or empty if not present
     */
    public Optional<String> getApiUrl() {
        return Optional.ofNullable(apiUrl);
    }

    /**
     * Returns the database URL if available.
     *
     * @return Optional containing db.url or empty if not present
     */
    public Optional<String> getDbUrl() {
        return Optional.ofNullable(dbUrl);
    }

    /**
     * Returns the database username if available.
     *
     * @return Optional containing db.user or empty if not present
     */
    public Optional<String> getDbUser() {
        return Optional.ofNullable(dbUser);
    }

    /**
     * Returns the database password if available.
     *
     * <p>Callers should handle this value securely and avoid logging it or exposing it
     * in diagnostics.</p>
     *
     * @return Optional containing db.password or empty if not present
     */
    public Optional<String> getDbPassword() {
        return Optional.ofNullable(dbPassword);
    }

    /**
     * Returns a redacted string representation useful for debugging without exposing secrets.
     *
     * @return redacted string representation
     */
    @Override
    public String toString() {
        return "EnvironmentConfig{" +
                "url=" + (url == null ? "<null>" : url) +
                ", apiUrl=" + (apiUrl == null ? "<null>" : apiUrl) +
                ", dbUrl=" + (dbUrl == null ? "<null>" : dbUrl) +
                ", dbUser=" + (dbUser == null ? "<null>" : "<removed-for-security>") +
                ", dbPassword=<removed-for-security>" +
                '}';
    }
}