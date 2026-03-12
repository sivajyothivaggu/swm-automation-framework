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
                        closeMethod.invoke(reader);
                        LOGGER.debug("Closed PropertyReader for {}", fileName);
                    }
                } catch (NoSuchMethodException nsme) {
                    // No close method available - nothing to do.
                    LOGGER.debug("PropertyReader for {} does not declare a close() method", fileName);
                } catch (IllegalAccessException | InvocationTargetException iae) {
                    LOGGER.warn("Failed to invoke close() on PropertyReader for {}: {}", fileName, iae.getMessage(), iae);
                } catch (Exception ex) {
                    LOGGER.warn("Unexpected exception while closing PropertyReader for {}: {}", fileName, ex.getMessage(), ex);
                }
            }
        }

        this.url = tmpUrl;
        this.apiUrl = tmpApiUrl;
        this.dbUrl = tmpDbUrl;
        this.dbUser = tmpDbUser;
        this.dbPassword = tmpDbPassword;

        LOGGER.info("EnvironmentConfig initialized for env='{}' (properties file: {})", env, fileName);
    }

    /**
     * Safely trims a string. Returns null if input is null or empty after trimming.
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
     * Mask a value for safe logging (keeps first and last character when possible).
     *
     * @param value original value
     * @return masked representation
     */
    private static String maskValue(String value) {
        if (Objects.isNull(value) || value.length() <= 2) {
            return "<masked>";
        }
        int len = value.length();
        StringBuilder sb = new StringBuilder();
        sb.append(value.charAt(0));
        for (int i = 1; i < len - 1; i++) {
            sb.append('*');
        }
        sb.append(value.charAt(len - 1));
        return sb.toString();
    }

    /**
     * @return Optional containing the application URL if present.
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(this.url);
    }

    /**
     * @return Optional containing the API URL if present.
     */
    public Optional<String> getApiUrl() {
        return Optional.ofNullable(this.apiUrl);
    }

    /**
     * @return Optional containing the database URL if present.
     */
    public Optional<String> getDbUrl() {
        return Optional.ofNullable(this.dbUrl);
    }

    /**
     * @return Optional containing the database user if present.
     */
    public Optional<String> getDbUser() {
        return Optional.ofNullable(this.dbUser);
    }

    /**
     * @return Optional containing the database password if present.
     */
    public Optional<String> getDbPassword() {
        return Optional.ofNullable(this.dbPassword);
    }

    /**
     * Returns a safe string representation for logging or debugging. Sensitive fields are masked.
     *
     * @return string representation with masked sensitive values
     */
    @Override
    public String toString() {
        return "EnvironmentConfig{" +
                "url='" + this.url + '\'' +
                ", apiUrl='" + this.apiUrl + '\'' +
                ", dbUrl='" + this.dbUrl + '\'' +
                ", dbUser='" + (Objects.isNull(this.dbUser) ? "<null>" : maskValue(this.dbUser)) + '\'' +
                ", dbPassword='" + (Objects.isNull(this.dbPassword) ? "<null>" : "<masked>") + '\'' +
                '}';
    }
}