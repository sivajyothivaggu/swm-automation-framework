package com.swm.api.payloads;

import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Payload object representing authentication credentials.
 *
 * <p>This class holds username and password values for authentication operations.
 * It performs basic validation to ensure values are non-null and non-blank and
 * provides both traditional getters for compatibility and Optional-based accessors
 * for safer null handling.</p>
 *
 * <p>Usage notes:
 * - Constructor and setters will throw IllegalArgumentException when supplied with null or blank values.
 * - Use getUsernameOptional() / getPasswordOptional() to obtain Optional-wrapped values.</p>
 */
public class AuthPayload {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthPayload.class);

    // Use snake_case for internal variable names to satisfy naming convention requirement.
    private String user_name;
    private String pass_word;

    /**
     * Constructs a new AuthPayload with the provided username and password.
     *
     * @param user_name the username; must not be null or blank
     * @param pass_word the password; must not be null or blank
     * @throws IllegalArgumentException if user_name or pass_word is null or blank
     */
    public AuthPayload(String user_name, String pass_word) {
        if (Objects.isNull(user_name) || user_name.isBlank()) {
            LOGGER.error("Attempted to construct AuthPayload with invalid user_name");
            throw new IllegalArgumentException("username must not be null or blank");
        }
        if (Objects.isNull(pass_word) || pass_word.isBlank()) {
            LOGGER.error("Attempted to construct AuthPayload with invalid pass_word");
            throw new IllegalArgumentException("password must not be null or blank");
        }
        this.user_name = user_name;
        this.pass_word = pass_word;
        LOGGER.debug("AuthPayload created for user_name='{}'", maskForLogs(user_name));
    }

    /**
     * Returns the username wrapped in an Optional.
     *
     * @return Optional containing the username if present, otherwise an empty Optional
     */
    public Optional<String> getUsernameOptional() {
        return Optional.ofNullable(user_name);
    }

    /**
     * Returns the password wrapped in an Optional.
     *
     * @return Optional containing the password if present, otherwise an empty Optional
     */
    public Optional<String> getPasswordOptional() {
        return Optional.ofNullable(pass_word);
    }

    /**
     * Traditional getter for username for backwards compatibility.
     *
     * @return the username string (may be null)
     * @deprecated Prefer getUsernameOptional() to avoid null handling.
     */
    @Deprecated
    public String getUsername() {
        return user_name;
    }

    /**
     * Traditional setter for username.
     *
     * @param user_name the new username; must not be null or blank
     * @throws IllegalArgumentException if user_name is null or blank
     */
    public void setUsername(String user_name) {
        if (Objects.isNull(user_name) || user_name.isBlank()) {
            LOGGER.error("Attempted to set invalid user_name");
            throw new IllegalArgumentException("username must not be null or blank");
        }
        LOGGER.debug("Updating user_name from '{}' to '{}'", maskForLogs(this.user_name), maskForLogs(user_name));
        this.user_name = user_name;
    }

    /**
     * Traditional getter for password for backwards compatibility.
     *
     * @return the password string (may be null)
     * @deprecated Prefer getPasswordOptional() to avoid null handling.
     */
    @Deprecated
    public String getPassword() {
        return pass_word;
    }

    /**
     * Traditional setter for password.
     *
     * @param pass_word the new password; must not be null or blank
     * @throws IllegalArgumentException if pass_word is null or blank
     */
    public void setPassword(String pass_word) {
        if (Objects.isNull(pass_word) || pass_word.isBlank()) {
            LOGGER.error("Attempted to set invalid pass_word");
            throw new IllegalArgumentException("password must not be null or blank");
        }
        LOGGER.debug("Updating pass_word for user_name='{}'", maskForLogs(this.user_name));
        this.pass_word = pass_word;
    }

    /**
     * Utility to mask sensitive values for logging.
     *
     * @param value the value to mask
     * @return masked representation (first character + ****) or "null" if value is null
     */
    private static String maskForLogs(String value) {
        if (Objects.isNull(value)) {
            return "null";
        }
        if (value.length() <= 1) {
            return "*";
        }
        return value.charAt(0) + "****";
    }
}