package com.swm.api.endpoints;

import com.swm.api.client.RestClient;
import com.swm.core.base.BaseAPI;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * AuthEndpoints provides wrapper methods for authentication-related API calls.
 *
 * <p>
 * This class delegates HTTP calls to a RestClient and adds error handling,
 * logging and documentation. It preserves existing functionality while providing
 * safer defaults and helper overloads that return Optional for nullable responses.
 * </p>
 *
 * <p>
 * Usage example:
 * AuthEndpoints endpoints = new AuthEndpoints();
 * Response resp = endpoints.login(payload);
 * Optional<Response> maybeResp = endpoints.loginOptional(payload);
 * </p>
 */
public class AuthEndpoints extends BaseAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthEndpoints.class);

    // Endpoint path constants - follow UPPER_CASE naming convention for constants.
    private static final String LOGIN_ENDPOINT = "/auth/login";
    private static final String LOGOUT_ENDPOINT = "/auth/logout";

    /**
     * Rest client used to perform HTTP operations. Marked final to make the
     * intent clear that the reference should not change once constructed.
     */
    private final RestClient client;

    /**
     * Default constructor that creates an internal RestClient instance.
     * Prefer injecting a RestClient via the overloaded constructor for easier testing.
     */
    public AuthEndpoints() {
        this(new RestClient());
    }

    /**
     * Constructor allowing dependency injection of RestClient. Useful for unit tests
     * or for providing custom configured clients.
     *
     * @param client RestClient instance to use; must not be null
     * @throws IllegalArgumentException if client is null
     */
    public AuthEndpoints(final RestClient client) {
        if (Objects.isNull(client)) {
            throw new IllegalArgumentException("RestClient must not be null");
        }
        this.client = client;
    }

    /**
     * Performs login with the provided payload.
     *
     * <p>
     * This method preserves the original behavior of returning the raw Response.
     * It logs useful contextual information, validates input where appropriate,
     * and wraps unexpected exceptions in a RuntimeException to make failures explicit.
     * </p>
     *
     * @param payload request payload (may be null depending on API contract)
     * @return Response from the /auth/login endpoint (may be null if RestClient returns null)
     * @throws RuntimeException if an unexpected error occurs while performing the request
     */
    public Response login(final Object payload) {
        try {
            if (Objects.isNull(payload)) {
                LOGGER.debug("login called with null payload");
            } else {
                LOGGER.debug("login called with payload of type: {}", payload.getClass().getSimpleName());
            }

            final Response response = client.post(LOGIN_ENDPOINT, payload, getRequestSpec());
            if (Objects.isNull(response)) {
                LOGGER.warn("Received null Response from POST {}", LOGIN_ENDPOINT);
            } else {
                try {
                    LOGGER.info("POST {} completed with status code: {}", LOGIN_ENDPOINT, response.getStatusCode());
                } catch (Exception e) {
                    // Defensive logging in case response.getStatusCode() throws for some implementations
                    LOGGER.warn("Unable to obtain status code from Response for POST {}", LOGIN_ENDPOINT, e);
                }
            }
            return response;
        } catch (RuntimeException e) {
            // Re-throw runtime exceptions after logging
            LOGGER.error("Runtime error while calling POST {}", LOGIN_ENDPOINT, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while calling POST {}", LOGIN_ENDPOINT, e);
            throw new RuntimeException("Failed to perform login operation", e);
        }
    }

    /**
     * Performs login with the provided payload and returns an Optional-wrapped Response.
     * This helper provides a null-safe alternative without changing the original method signature.
     *
     * @param payload request payload (may be null depending on API contract)
     * @return Optional containing the Response, or empty if the response was null or an error occurred
     */
    public Optional<Response> loginOptional(final Object payload) {
        try {
            return Optional.ofNullable(login(payload));
        } catch (RuntimeException e) {
            LOGGER.error("loginOptional encountered an error", e);
            return Optional.empty();
        }
    }

    /**
     * Performs logout.
     *
     * <p>
     * Preserves original behavior of returning the raw Response. Adds logging and error
     * handling to make issues easier to diagnose.
     * </p>
     *
     * @return Response from the /auth/logout endpoint (may be null if RestClient returns null)
     * @throws RuntimeException if an unexpected error occurs while performing the request
     */
    public Response logout() {
        try {
            LOGGER.debug("logout called");

            // Use POST for logout to preserve existing behavior; payload is null for logout.
            final Response response = client.post(LOGOUT_ENDPOINT, null, getRequestSpec());
            if (Objects.isNull(response)) {
                LOGGER.warn("Received null Response from POST {}", LOGOUT_ENDPOINT);
            } else {
                try {
                    LOGGER.info("POST {} completed with status code: {}", LOGOUT_ENDPOINT, response.getStatusCode());
                } catch (Exception e) {
                    LOGGER.warn("Unable to obtain status code from Response for POST {}", LOGOUT_ENDPOINT, e);
                }
            }
            return response;
        } catch (RuntimeException e) {
            LOGGER.error("Runtime error while calling POST {}", LOGOUT_ENDPOINT, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while calling POST {}", LOGOUT_ENDPOINT, e);
            throw new RuntimeException("Failed to perform logout operation", e);
        }
    }

    /**
     * Performs logout and returns an Optional-wrapped Response.
     *
     * @return Optional containing the Response, or empty if the response was null or an error occurred
     */
    public Optional<Response> logoutOptional() {
        try {
            return Optional.ofNullable(logout());
        } catch (RuntimeException e) {
            LOGGER.error("logoutOptional encountered an error", e);
            return Optional.empty();
        }
    }
}