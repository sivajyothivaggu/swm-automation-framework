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
 * from a properties file named &lt;env&gt;.properties available on the classpath.
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
     * located on the classpath.
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
        LOGGER.debug("Attempting to load environment properties file '{}'", fileName);

        Properties props = new Properties();
        // Use classloader resource lookup with try-with-resources to ensure stream is closed
        try (InputStream in = locateResourceAsStream(fileName)) {
            if (Objects.isNull(in)) {
                String msg = String.format("Properties file '%s' was not found on the classpath", fileName);
                LOGGER.error(msg);
                throw new IllegalStateException(msg);
            }
            props.load(in);
            LOGGER.info("Loaded properties for environment '{}'", env.trim());
        } catch (IOException e) {
            String msg = String.format("Failed to load properties file '%s': %s", fileName, e.getMessage());
            LOGGER.error(msg, e);
            throw new IllegalStateException(msg, e);
        }

        this.url = normalize(props.getProperty(PROP_APP_URL));
        this.apiUrl = normalize(props.getProperty(PROP_API_URL));
        this.dbUrl = normalize(props.getProperty(PROP_DB_URL));
        this.dbUser = normalize(props.getProperty(PROP_DB_USER));
        this.dbPassword = normalize(props.getProperty(PROP_DB_PASSWORD));

        // Log presence of critical properties without exposing sensitive values
        if (Objects.isNull(this.url)) {
            LOGGER.warn("Property '{}' is missing for environment '{}'", PROP_APP_URL, env.trim());
        } else {
            LOGGER.debug("Property '{}' loaded", PROP_APP_URL);
        }

        if (Objects.isNull(this.apiUrl)) {
            LOGGER.warn("Property '{}' is missing for environment '{}'", PROP_API_URL, env.trim());
        } else {
            LOGGER.debug("Property '{}' loaded", PROP_API_URL);
        }

        if (Objects.isNull(this.dbUrl)) {
            LOGGER.warn("Property '{}' is missing for environment '{}'", PROP_DB_URL, env.trim());
        } else {
            LOGGER.debug("Property '{}' loaded", PROP_DB_URL);
        }

        if (Objects.isNull(this.dbUser)) {
            LOGGER.info("Database user property '{}' is missing for environment '{}'", PROP_DB_USER, env.trim());
        } else {
            LOGGER.debug("Database user property '{}' loaded", PROP_DB_USER);
        }

        if (Objects.isNull(this.dbPassword)) {
            LOGGER.info("Database password property '{}' is missing for environment '{}'", PROP_DB_PASSWORD, env.trim());
        } else {
            LOGGER.debug("Database password property '{}' is present (value suppressed)", PROP_DB_PASSWORD);
        }
    }

    /**
     * Helper to locate a resource on the classpath as an InputStream.
     * Tries the context classloader first then the class's classloader as a fallback.
     *
     * @param resource the resource name
     * @return InputStream or null if not found
     */
    private InputStream locateResourceAsStream(String resource) {
        InputStream in = null;
        try {
            ClassLoader ctx = Thread.currentThread().getContextClassLoader();
            if (ctx != null) {
                in = ctx.getResourceAsStream(resource);
            }
        } catch (Throwable t) {
            LOGGER.debug("Context classloader failed to load resource '{}': {}", resource, t.getMessage());
            // continue to fallback
        }
        if (Objects.isNull(in)) {
            in = EnvironmentConfig.class.getClassLoader().getResourceAsStream(resource);
        }
        return in;
    }

    /**
     * Normalizes a property String by trimming it and converting empty strings to null.
     *
     * @param value raw property value
     * @return trimmed value or null
     */
    private String normalize(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Returns the application URL if present.
     *
     * @return Optional containing app URL or empty if not configured
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    /**
     * Returns the API URL if present.
     *
     * @return Optional containing API URL or empty if not configured
     */
    public Optional<String> getApiUrl() {
        return Optional.ofNullable(apiUrl);
    }

    /**
     * Returns the database URL if present.
     *
     * @return Optional containing DB URL or empty if not configured
     */
    public Optional<String> getDbUrl() {
        return Optional.ofNullable(dbUrl);
    }

    /**
     * Returns the database user if present.
     *
     * @return Optional containing DB user or empty if not configured
     */
    public Optional<String> getDbUser() {
        return Optional.ofNullable(dbUser);
    }

    /**
     * Returns the database password if present.
     *
     * <p>Note: Callers must handle this sensitive value securely.</p>
     *
     * @return Optional containing DB password or empty if not configured
     */
    public Optional<String> getDbPassword() {
        return Optional.ofNullable(dbPassword);
    }
}