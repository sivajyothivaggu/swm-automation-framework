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
 * <p>
 * This class delegates HTTP calls to a RestClient and adds error handling,
 * logging and documentation. It preserves existing functionality while providing
 * safer defaults and helper overloads that return Optional for nullable responses.
 * </p>
 */
public class AuthEndpoints extends BaseAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthEndpoints.class);

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
    public AuthEndpoints(RestClient client) {
        if (Objects.isNull(client)) {
            throw new IllegalArgumentException("RestClient must not be null");
        }
        this.client = client;
    }

    /**
     * Performs login with the provided payload.
     *
     * This method preserves the original behavior of returning the raw Response.
     * It logs useful contextual information, validates input where appropriate,
     * and wraps unexpected exceptions in a RuntimeException to make failures explicit.
     *
     * @param payload request payload (may be null depending on API contract)
     * @return Response from the /auth/login endpoint (may be null if RestClient returns null)
     * @throws RuntimeException if an unexpected error occurs while performing the request
     */
    public Response login(Object payload) {
        try {
            if (Objects.isNull(payload)) {
                LOGGER.debug("login called with null payload");
            } else {
                LOGGER.debug("login called with payload of type: {}", payload.getClass().getSimpleName());
            }

            Response response = client.post("/auth/login", payload, getRequestSpec());
            if (Objects.isNull(response)) {
                LOGGER.warn("Received null Response from POST /auth/login");
            } else {
                LOGGER.info("POST /auth/login completed with status code: {}", response.getStatusCode());
            }
            return response;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while calling POST /auth/login", e);
            throw new RuntimeException("Failed to perform login operation", e);
        }
    }

    /**
     * Performs login with the provided payload and returns an Optional-wrapped Response.
     * This helper provides a null-safe alternative without changing the original method signature.
     *
     * @param payload request payload (may be null depending on API contract)
     * @return Optional containing the Response, or empty if the response was null
     */
    public Optional<Response> loginOptional(Object payload) {
        return Optional.ofNullable(login(payload));
    }

    /**
     * Performs logout.
     *
     * Preserves original behavior of returning the raw Response. Adds logging and error
     * handling to make issues easier to diagnose in production.
     *
     * @return Response from the /auth/logout endpoint (may be null if RestClient returns null)
     * @throws RuntimeException if an unexpected error occurs while performing the request
     */
    public Response logout() {
        try {
            LOGGER.debug("logout called");
            Response response = client.post("/auth/logout", null, getRequestSpec());
            if (Objects.isNull(response)) {
                LOGGER.warn("Received null Response from POST /auth/logout");
            } else {
                LOGGER.info("POST /auth/logout completed with status code: {}", response.getStatusCode());
            }
            return response;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while calling POST /auth/logout", e);
            throw new RuntimeException("Failed to perform logout operation", e);
        }
    }

    /**
     * Performs logout and returns an Optional-wrapped Response for callers that prefer null-safety.
     *
     * @return Optional containing the Response, or empty if the response was null
     */
    public Optional<Response> logoutOptional() {
        return Optional.ofNullable(logout());
    }
}