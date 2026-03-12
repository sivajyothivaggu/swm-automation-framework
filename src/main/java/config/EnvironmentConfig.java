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

        final String fileName = env.trim() + PROPERTIES_SUFFIX;
        PropertyReader reader = null;

        String tmpUrl = null;
        String tmpApiUrl = null;
        String tmpDbUrl = null;
        String tmpDbUser = null;
        String tmpDbPassword = null;

        try {
            // Instantiate reader - this may throw; let it bubble as Exception caught below
            reader = new PropertyReader(fileName);

            // If PropertyReader implements AutoCloseable we use try-with-resources to ensure it is closed.
            // If not, the resource will be null and nothing will be closed here; the finally block will attempt a reflective close.
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
            if (reader != null && !(reader instanceof AutoCloseable)) {
                try {
                    Method closeMethod = reader.getClass().getMethod("close");
                    if (closeMethod != null) {
                        closeMethod.setAccessible(true);
                        closeMethod.invoke(reader);
                    }
                } catch (NoSuchMethodException nsme) {
                    // No close method available - nothing to do.
                    LOGGER.debug("PropertyReader for {} does not implement AutoCloseable and has no close() method", fileName);
                } catch (InvocationTargetException ite) {
                    LOGGER.warn("Close method of PropertyReader for {} threw an exception: {}", fileName, ite.getTargetException().getMessage(), ite.getTargetException());
                } catch (IllegalAccessException iae) {
                    LOGGER.warn("Unable to access close method of PropertyReader for {}: {}", fileName, iae.getMessage(), iae);
                } catch (Exception e) {
                    LOGGER.warn("Unexpected error while closing PropertyReader for {}: {}", fileName, e.getMessage(), e);
                }
            }
        }

        // Assign to final fields (preserve values even if null)
        this.url = tmpUrl;
        this.apiUrl = tmpApiUrl;
        this.dbUrl = tmpDbUrl;
        this.dbUser = tmpDbUser;
        this.dbPassword = tmpDbPassword;

        LOGGER.info("EnvironmentConfig initialized for '{}' (app.url present={}, api.url present={})",
                fileName,
                this.url != null,
                this.apiUrl != null);
    }

    /**
     * Return application URL if present.
     *
     * @return Optional containing app URL or empty if not present
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    /**
     * Return API URL if present.
     *
     * @return Optional containing API URL or empty if not present
     */
    public Optional<String> getApiUrl() {
        return Optional.ofNullable(apiUrl);
    }

    /**
     * Return database URL if present.
     *
     * @return Optional containing DB URL or empty if not present
     */
    public Optional<String> getDbUrl() {
        return Optional.ofNullable(dbUrl);
    }

    /**
     * Return database username if present.
     *
     * @return Optional containing DB username or empty if not present
     */
    public Optional<String> getDbUser() {
        return Optional.ofNullable(dbUser);
    }

    /**
     * Return database password if present.
     *
     * WARNING: callers should treat this value sensitively. Prefer using getMaskedDbPassword()
     * for logging or diagnostic output.
     *
     * @return Optional containing DB password or empty if not present
     */
    public Optional<String> getDbPassword() {
        return Optional.ofNullable(dbPassword);
    }

    /**
     * Return a masked representation of the DB password suitable for logs/diagnostics.
     *
     * @return Optional containing masked DB password or empty if not present
     */
    public Optional<String> getMaskedDbPassword() {
        return Optional.ofNullable(maskValue(dbPassword));
    }

    /**
     * Mask a sensitive value for safe logging. Preserves up to first and last character(s)
     * and replaces middle characters with asterisks. If the input is null, returns null.
     *
     * @param value input value that may be sensitive
     * @return masked value or null if input was null
     */
    private static String maskValue(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        final int len = value.length();
        if (len <= 2) {
            return "*".repeat(len);
        }
        int numStars = Math.max(1, len - 2);
        StringBuilder sb = new StringBuilder();
        sb.append(value.charAt(0));
        for (int i = 0; i < numStars; i++) {
            sb.append('*');
        }
        sb.append(value.charAt(len - 1));
        return sb.toString();
    }

    /**
     * Safely trim a string, returning null if the input is null or consists only of whitespace.
     *
     * @param s input string
     * @return trimmed string or null
     */
    private static String safeTrim(String s) {
        if (Objects.isNull(s)) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    public String toString() {
        return "EnvironmentConfig{" +
                "url=" + (url == null ? "<null>" : url) +
                ", apiUrl=" + (apiUrl == null ? "<null>" : apiUrl) +
                ", dbUrl=" + (dbUrl == null ? "<null>" : dbUrl) +
                ", dbUser=" + (dbUser == null ? "<null>" : dbUser) +
                ", dbPassword=" + (dbPassword == null ? "<null>" : maskValue(dbPassword)) +
                '}';
    }
}