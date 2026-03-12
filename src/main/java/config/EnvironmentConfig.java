package com.swm.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EnvironmentConfig is responsible for loading environment specific configuration
 * from a properties file named &lt;env&gt;.properties located on the classpath.
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
     * Construct an EnvironmentConfig by loading properties from &lt;env&gt;.properties
     * found on the classpath.
     *
     * @param env environment identifier (must not be null/empty)
     * @throws IllegalArgumentException if env is null or empty
     * @throws IllegalStateException if properties cannot be loaded
     */
    public EnvironmentConfig(String env) {
        if (Objects.isNull(env) || env.trim().isEmpty()) {
            LOGGER.error("Environment identifier must be a non-empty string");
            throw new IllegalArgumentException("env must be a non-empty string");
        }

        String fileName = env.trim() + PROPERTIES_SUFFIX;
        Properties props = new Properties();

        // Load properties from classpath using try-with-resources
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream is = cl.getResourceAsStream(fileName)) {
            if (Objects.isNull(is)) {
                String msg = "Properties file not found on classpath: " + fileName;
                LOGGER.error(msg);
                throw new IllegalStateException(msg);
            }
            props.load(is);
            LOGGER.info("Loaded environment properties from {}", fileName);
        } catch (IOException e) {
            String msg = "I/O error while loading properties file: " + fileName;
            LOGGER.error(msg, e);
            throw new IllegalStateException(msg, e);
        } catch (RuntimeException e) {
            // Defensive: catch other runtime exceptions that may occur during loading
            String msg = "Unexpected error while loading properties file: " + fileName;
            LOGGER.error(msg, e);
            throw new IllegalStateException(msg, e);
        }

        this.url = normalize(props.getProperty(PROP_APP_URL));
        this.apiUrl = normalize(props.getProperty(PROP_API_URL));
        this.dbUrl = normalize(props.getProperty(PROP_DB_URL));
        this.dbUser = normalize(props.getProperty(PROP_DB_USER));
        this.dbPassword = normalize(props.getProperty(PROP_DB_PASSWORD));

        // Log missing properties at debug level to avoid leaking sensitive info at info level
        logMissingProperties(fileName);
    }

    /**
     * Returns the application URL if configured.
     *
     * @return Optional containing the app.url property, or empty if not present
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    /**
     * Returns the API URL if configured.
     *
     * @return Optional containing the api.url property, or empty if not present
     */
    public Optional<String> getApiUrl() {
        return Optional.ofNullable(apiUrl);
    }

    /**
     * Returns the database URL if configured.
     *
     * @return Optional containing the db.url property, or empty if not present
     */
    public Optional<String> getDbUrl() {
        return Optional.ofNullable(dbUrl);
    }

    /**
     * Returns the database username if configured.
     *
     * @return Optional containing the db.user property, or empty if not present
     */
    public Optional<String> getDbUser() {
        return Optional.ofNullable(dbUser);
    }

    /**
     * Returns the database password if configured.
     *
     * @return Optional containing the db.password property, or empty if not present
     */
    public Optional<String> getDbPassword() {
        return Optional.ofNullable(dbPassword);
    }

    /**
     * Normalize property values: trim and convert empty strings to null.
     *
     * @param value raw property value
     * @return trimmed value or null if blank
     */
    private static String normalize(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Log which expected properties are missing. Uses debug level to avoid
     * leaking sensitive information in normal logs.
     *
     * @param fileName the properties file that was loaded
     */
    private void logMissingProperties(String fileName) {
        if (LOGGER.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Missing properties in ").append(fileName).append(":");
            boolean anyMissing = false;
            if (Objects.isNull(url)) {
                sb.append(' ').append(PROP_APP_URL);
                anyMissing = true;
            }
            if (Objects.isNull(apiUrl)) {
                sb.append(' ').append(PROP_API_URL);
                anyMissing = true;
            }
            if (Objects.isNull(dbUrl)) {
                sb.append(' ').append(PROP_DB_URL);
                anyMissing = true;
            }
            if (Objects.isNull(dbUser)) {
                sb.append(' ').append(PROP_DB_USER);
                anyMissing = true;
            }
            if (Objects.isNull(dbPassword)) {
                sb.append(' ').append(PROP_DB_PASSWORD);
                anyMissing = true;
            }
            if (anyMissing) {
                LOGGER.debug(sb.toString());
            } else {
                LOGGER.debug("All expected properties present in {}", fileName);
            }
        }
    }

    @Override
    public String toString() {
        // Avoid exposing sensitive values such as db.password
        return "EnvironmentConfig{" +
                "url='" + url + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", dbUrl='" + dbUrl + '\'' +
                ", dbUser='" + dbUser + '\'' +
                ", dbPassword='" + (Objects.isNull(dbPassword) ? "null" : "*****") + '\'' +
                '}';
    }
}