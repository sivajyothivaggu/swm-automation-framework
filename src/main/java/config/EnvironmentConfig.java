package com.swm.core.config;

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
 * Usage example:
 * EnvironmentConfig cfg = new EnvironmentConfig("dev");
 * Optional<String> url = cfg.getUrl();
 */
public final class EnvironmentConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentConfig.class);

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

        String fileName = env.trim() + ".properties";
        PropertyReader reader = null;
        String tmpUrl = null;
        String tmpApiUrl = null;
        String tmpDbUrl = null;
        String tmpDbUser = null;
        String tmpDbPassword = null;

        try {
            reader = new PropertyReader(fileName);

            tmpUrl = safeTrim(reader.getProperty("app.url"));
            tmpApiUrl = safeTrim(reader.getProperty("api.url"));
            tmpDbUrl = safeTrim(reader.getProperty("db.url"));
            tmpDbUser = safeTrim(reader.getProperty("db.user"));
            tmpDbPassword = safeTrim(reader.getProperty("db.password"));

            // Log missing important properties at WARN level to help diagnostics
            if (Objects.isNull(tmpUrl)) {
                LOGGER.warn("Property 'app.url' is missing in {}", fileName);
            }
            if (Objects.isNull(tmpApiUrl)) {
                LOGGER.warn("Property 'api.url' is missing in {}", fileName);
            }
        } catch (RuntimeException re) {
            LOGGER.error("Runtime exception while loading properties from {}", fileName, re);
            throw re;
        } catch (Exception e) {
            LOGGER.error("Failed to load properties from {}: {}", fileName, e.getMessage(), e);
            throw new IllegalStateException("Failed to load environment properties from " + fileName, e);
        } finally {
            // Attempt to close the reader if it implements AutoCloseable
            if (reader instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) reader).close();
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
     * Returns the application URL if present.
     *
     * @return Optional containing the application URL or empty if not present
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    /**
     * Returns the API URL if present.
     *
     * @return Optional containing the API URL or empty if not present
     */
    public Optional<String> getApiUrl() {
        return Optional.ofNullable(apiUrl);
    }

    /**
     * Returns the database URL if present.
     *
     * @return Optional containing the database URL or empty if not present
     */
    public Optional<String> getDbUrl() {
        return Optional.ofNullable(dbUrl);
    }

    /**
     * Returns the database user if present.
     *
     * @return Optional containing the database user or empty if not present
     */
    public Optional<String> getDbUser() {
        return Optional.ofNullable(dbUser);
    }

    /**
     * Returns the database password if present.
     *
     * @return Optional containing the database password or empty if not present
     */
    public Optional<String> getDbPassword() {
        return Optional.ofNullable(dbPassword);
    }

    /**
     * Helper to safely trim a string value. Uses Objects.isNull for null checks to follow codebase practice.
     *
     * @param value input string
     * @return trimmed string or null if input was null
     */
    private static String safeTrim(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public String toString() {
        return "EnvironmentConfig{"
                + "url=" + (url == null ? "null" : "<hidden>")
                + ", apiUrl=" + (apiUrl == null ? "null" : "<hidden>")
                + ", dbUrl=" + (dbUrl == null ? "null" : "<hidden>")
                + ", dbUser=" + (dbUser == null ? "null" : "<hidden>")
                + ", dbPassword=" + (dbPassword == null ? "null" : "<hidden>")
                + '}';
    }
}