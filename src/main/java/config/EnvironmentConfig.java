package com.swm.core.config;

import java.lang.reflect.InvocationTargetException;
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

        final String fileName = env.trim() + PROPERTIES_SUFFIX;
        PropertyReader reader = null;

        String tmpUrl = null;
        String tmpApiUrl = null;
        String tmpDbUrl = null;
        String tmpDbUser = null;
        String tmpDbPassword = null;

        try {
            // Instantiate reader - this may throw; exceptions handled below
            reader = new PropertyReader(fileName);

            // If PropertyReader implements AutoCloseable we use try-with-resources to ensure it is closed.
            // If not, the resource variable will be null and finally block will attempt a reflective close.
            try (AutoCloseable ignored = (reader instanceof AutoCloseable) ? (AutoCloseable) reader : null) {
                tmpUrl = safeTrim(reader.getProperty(PROP_APP_URL));
                tmpApiUrl = safeTrim(reader.getProperty(PROP_API_URL));
                tmpDbUrl = safeTrim(reader.getProperty(PROP_DB_URL));
                tmpDbUser = safeTrim(reader.getProperty(PROP_DB_USER));
                tmpDbPassword = safeTrim(reader.getProperty(PROP_DB_PASSWORD));

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
            } // try-with-resources will close reader if it is AutoCloseable
        } catch (RuntimeException re) {
            LOGGER.error("Runtime exception while loading properties from {}", fileName, re);
            throw re;
        } catch (Exception e) {
            LOGGER.error("Failed to load properties from {}: {}", fileName, e.getMessage(), e);
            throw new IllegalStateException("Failed to load environment properties from " + fileName, e);
        } finally {
            // If reader existed but did not implement AutoCloseable, attempt to close via reflection if a close method exists.
            if (!Objects.isNull(reader) && !(reader instanceof AutoCloseable)) {
                try {
                    Method closeMethod = reader.getClass().getMethod("close");
                    if (!Objects.isNull(closeMethod)) {
                        closeMethod.setAccessible(true);
                        try {
                            closeMethod.invoke(reader);
                            LOGGER.debug("Closed PropertyReader for {}", fileName);
                        } catch (IllegalAccessException | InvocationTargetException ite) {
                            LOGGER.warn("Failed to invoke close() on PropertyReader for {}: {}", fileName, ite.getMessage(), ite);
                        }
                    }
                } catch (NoSuchMethodException nsme) {
                    // No close method available; nothing to do.
                    LOGGER.debug("PropertyReader for {} does not have close() method.", fileName);
                } catch (SecurityException se) {
                    LOGGER.warn("Security manager prevented reflective close of PropertyReader for {}: {}", fileName, se.getMessage(), se);
                }
            }
        }

        // Assign to final fields after successful load (if constructor reaches here, either loaded or default nulls)
        this.url = tmpUrl;
        this.apiUrl = tmpApiUrl;
        this.dbUrl = tmpDbUrl;
        this.dbUser = tmpDbUser;
        this.dbPassword = tmpDbPassword;
    }

    /**
     * Returns the application URL if present.
     *
     * @return Optional containing the application URL or empty if not set
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    /**
     * Returns the API URL if present.
     *
     * @return Optional containing the API URL or empty if not set
     */
    public Optional<String> getApiUrl() {
        return Optional.ofNullable(apiUrl);
    }

    /**
     * Returns the database URL if present.
     *
     * @return Optional containing the DB URL or empty if not set
     */
    public Optional<String> getDbUrl() {
        return Optional.ofNullable(dbUrl);
    }

    /**
     * Returns the database username if present.
     *
     * @return Optional containing the DB username or empty if not set
     */
    public Optional<String> getDbUser() {
        return Optional.ofNullable(dbUser);
    }

    /**
     * Returns the database password if present.
     *
     * <p>Note: Consumers of this password should handle it carefully and avoid logging
     * or exposing it. This class will mask the value when logging.</p>
     *
     * @return Optional containing the DB password or empty if not set
     */
    public Optional<String> getDbPassword() {
        return Optional.ofNullable(dbPassword);
    }

    /**
     * Helper to safely trim a string and return null when the trimmed value is empty or input is null.
     *
     * @param value input string
     * @return trimmed string or null
     */
    private static String safeTrim(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Mask a sensitive value for logging purposes.
     *
     * @param val the sensitive value
     * @return masked representation (e.g. ****ed)
     */
    private static String maskValue(String val) {
        if (Objects.isNull(val) || val.isEmpty()) {
            return "<null>";
        }
        int visible = Math.min(3, val.length());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < val.length() - visible; i++) {
            sb.append('*');
        }
        sb.append(val.substring(val.length() - visible));
        return sb.toString();
    }
}