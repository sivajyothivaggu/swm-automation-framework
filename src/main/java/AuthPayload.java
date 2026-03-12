package com.swm.api.payloads;

import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Payload object representing authentication credentials.
 *
 * <p>This class holds a username and password for authentication operations.
 * It performs validation to ensure values are non-null and non-blank and provides both
 * Optional-based accessors for safer null handling and traditional getters/setters for
 * backward compatibility.</p>
 *
 * <p>Usage notes:
 * - Constructor and setters will throw IllegalArgumentException when supplied with null or blank values.
 * - Use getUsernameOptional() / getPasswordOptional() to obtain Optional-wrapped values.
 * - toString() and logs mask sensitive values to avoid leaking secrets.</p>
 */
public final class AuthPayload {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthPayload.class);

    /**
     * Replacement string used when masking values for logs.
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
        try {
            validateNotBlank(username, "username");
            validateNotBlank(password, "password");
            this.username = username;
            this.password = password;
            LOGGER.debug("AuthPayload created for username='{}'", maskForLogs(username));
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Failed to create AuthPayload: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while creating AuthPayload", ex);
            throw ex;
        }
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
        try {
            validateNotBlank(username, "username");
            LOGGER.debug("Updating username from '{}' to '{}'", maskForLogs(this.username), maskForLogs(username));
            this.username = username;
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Failed to update username: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating username", ex);
            throw ex;
        }
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
        try {
            validateNotBlank(password, "password");
            LOGGER.debug("Updating password for username='{}'", maskForLogs(this.username));
            this.password = password;
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Failed to update password: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while updating password", ex);
            throw ex;
        }
    }

    /**
     * Validates that a provided string is neither null nor blank.
     *
     * @param value     the value to validate
     * @param fieldName the name of the field (used in exception messages and logs)
     * @throws IllegalArgumentException if value is null or blank
     */
    private static void validateNotBlank(String value, String fieldName) {
        if (Objects.isNull(value) || value.isBlank()) {
            LOGGER.error("Validation failed for {}: value is null or blank", fieldName);
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
    }

    /**
     * Utility to mask sensitive values for logging.
     *
     * @param value the value to mask
     * @return masked representation (first character + MASK_SUFFIX) or "null" if value is null
     */
    private static String maskForLogs(String value) {
        if (Objects.isNull(value)) {
            return "null";
        }
        if (value.length() <= 1) {
            return "*";
        }
        return value.charAt(0) + MASK_SUFFIX;
    }

    /**
     * Standard equals implementation using username and password.
     *
     * @param o other object
     * @return true if same username and password
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
     * Standard hashCode implementation consistent with equals.
     *
     * @return computed hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    /**
     * toString implementation that avoids revealing sensitive details.
     *
     * @return string representation with masked sensitive fields
     */
    @Override
    public String toString() {
        return "AuthPayload{" +
                "username='" + maskForLogs(username) + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}