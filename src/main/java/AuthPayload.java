package com.swm.api.payloads;

import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Payload object representing authentication credentials.
 *
 * <p>This immutable-aware class holds a username and password for authentication operations.
 * It performs validation to ensure values are non-null and non-blank and provides both
 * Optional-based accessors for safer null handling and traditional getters/setters for
 * backward compatibility.</p>
 *
 * <p>Usage notes:
 * - Constructor and setters will throw IllegalArgumentException when supplied with null or blank values.
 * - Use getUsernameOptional() / getPasswordOptional() to obtain Optional-wrapped values.
 * - toString() and logs mask sensitive values to avoid leaking secrets.</p>
 *
 * @since 1.0
 */
public final class AuthPayload {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthPayload.class);

    /**
     * Replacement suffix used when masking values for logs.
     */
    private static final String MASK_SUFFIX = "****";

    /**
     * Username for authentication.
     */
    private String username;

    /**
     * Password for authentication.
     */
    private String password;

    /**
     * Constructs a new AuthPayload with the provided username and password.
     *
     * @param username the username; must not be null or blank
     * @param password the password; must not be null or blank
     * @throws IllegalArgumentException if username or password is null or blank
     */
    public AuthPayload(String username, String password) {
        // Validate inputs and initialize fields. Use explicit logging for failures.
        validateNotBlank(username, "username");
        validateNotBlank(password, "password");
        this.username = username;
        this.password = password;
        LOGGER.debug("AuthPayload created for username='{}'", maskForLogs(username));
    }

    /**
     * Returns the username wrapped in an Optional.
     *
     * @return Optional containing the username if present, otherwise an empty Optional
     */
    public Optional<String> getUsernameOptional() {
        return Optional.ofNullable(username);
    }

    /**
     * Returns the password wrapped in an Optional.
     *
     * @return Optional containing the password if present, otherwise an empty Optional
     */
    public Optional<String> getPasswordOptional() {
        return Optional.ofNullable(password);
    }

    /**
     * Traditional getter for username for backwards compatibility.
     *
     * @return the username string (may be null)
     * @deprecated Prefer getUsernameOptional() to avoid null handling.
     */
    @Deprecated
    public String getUsername() {
        return username;
    }

    /**
     * Traditional setter for username.
     *
     * @param username the new username; must not be null or blank
     * @throws IllegalArgumentException if username is null or blank
     */
    public void setUsername(String username) {
        validateNotBlank(username, "username");
        LOGGER.debug("Updating username from '{}' to '{}'", maskForLogs(this.username), maskForLogs(username));
        this.username = username;
    }

    /**
     * Traditional getter for password for backwards compatibility.
     *
     * @return the password string (may be null)
     * @deprecated Prefer getPasswordOptional() to avoid null handling.
     */
    @Deprecated
    public String getPassword() {
        return password;
    }

    /**
     * Traditional setter for password.
     *
     * @param password the new password; must not be null or blank
     * @throws IllegalArgumentException if password is null or blank
     */
    public void setPassword(String password) {
        validateNotBlank(password, "password");
        LOGGER.debug("Updating password for username='{}'", maskForLogs(this.username));
        this.password = password;
    }

    /**
     * Validates that a provided string is neither null nor blank.
     *
     * @param value     the value to validate
     * @param fieldName the name of the field (used in exception messages and logs)
     * @throws IllegalArgumentException when the value is null or blank
     */
    private static void validateNotBlank(String value, String fieldName) {
        if (Objects.isNull(value) || value.trim().isEmpty()) {
            String message = String.format("Field '%s' must not be null or blank", fieldName);
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Masks a value for safe logging. If the value is null returns "null".
     *
     * @param value the value to mask
     * @return masked representation suitable for logs
     */
    private static String maskForLogs(String value) {
        if (Objects.isNull(value)) {
            return "null";
        }
        int len = value.length();
        if (len <= 2) {
            return MASK_SUFFIX;
        }
        // Keep first 2 characters and append mask suffix to avoid leaking full secret
        return value.substring(0, 2) + MASK_SUFFIX;
    }

    /**
     * Returns a string representation of this object with sensitive fields masked.
     *
     * @return string representation with masked sensitive values
     */
    @Override
    public String toString() {
        return "AuthPayload{username='" + maskForLogs(username) + "', password='" + MASK_SUFFIX + "'}";
    }

    /**
     * Equality based on username and password values.
     *
     * @param o other object
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthPayload)) {
            return false;
        }
        AuthPayload that = (AuthPayload) o;
        return Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    /**
     * Hash code derived from username and password.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}