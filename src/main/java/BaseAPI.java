package com.swm.core.base;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import com.swm.core.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * BaseAPI provides a centralized place to create and configure the RestAssured RequestSpecification
 * used across the application for API interactions.
 *
 * Responsibilities:
 * - Read API base URL from configuration.
 * - Validate the configuration.
 * - Create and return a RequestSpecification pre-configured with common headers.
 *
 * This class includes robust error handling and logging to ensure issues are visible in production.
 */
public class BaseAPI {

    /**
     * Logger for instrumentation and error reporting.
     */
    private static final Logger logger = LoggerFactory.getLogger(BaseAPI.class);

    /**
     * Header name for Content-Type.
     */
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * Header name for Accept.
     */
    private static final String HEADER_ACCEPT = "Accept";

    /**
     * Media type for JSON payloads.
     */
    private static final String APPLICATION_JSON = "application/json";

    /**
     * Creates and returns a RequestSpecification configured with the application's API base URL
     * and common headers (Content-Type and Accept as application/json).
     *
     * Behavior:
     * - Validates that the API URL is present and non-blank in configuration. If missing, logs an error
     *   and throws an IllegalStateException to fail fast.
     * - Validates that the API URL is a well-formed URI with a scheme.
     * - Sets RestAssured.baseURI to the configured API URL.
     * - Builds the RequestSpecification and returns it.
     *
     * Throws:
     * - IllegalStateException if API URL is null, blank, or invalid.
     * - RuntimeException if building the RequestSpecification fails for any other reason.
     *
     * @return a configured RequestSpecification ready for use
     * @throws IllegalStateException when the API URL is missing or invalid
     * @throws RuntimeException for unexpected failures while building the RequestSpecification
     */
    protected RequestSpecification getRequestSpec() {
        final String rawApiUrl;
        try {
            rawApiUrl = ConfigManager.getApiUrl();
        } catch (Exception e) {
            logger.error("Failed to retrieve API URL from ConfigManager", e);
            throw new IllegalStateException("Unable to retrieve API URL from configuration", e);
        }

        final String apiUrl = Optional.ofNullable(rawApiUrl)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> {
                    logger.error("Configured API URL is null or blank. Check ConfigManager.getApiUrl()");
                    return new IllegalStateException("API URL is not configured");
                });

        try {
            // Validate the URL is well-formed and has a scheme (http/https)
            final URI uri = new URI(apiUrl);
            if (Objects.isNull(uri.getScheme()) || uri.getScheme().isBlank()) {
                logger.error("Configured API URL '{}' does not contain a valid scheme", apiUrl);
                throw new IllegalStateException("API URL is invalid: missing scheme");
            }

            final String schemeLower = uri.getScheme().toLowerCase();
            if (!"http".equals(schemeLower) && !"https".equals(schemeLower)) {
                logger.error("Configured API URL '{}' has unsupported scheme '{}'", apiUrl, uri.getScheme());
                throw new IllegalStateException("API URL has unsupported scheme: " + uri.getScheme());
            }

            // Normalize the URI to avoid unexpected whitespace or encoding issues
            final String normalizedBaseUri = uri.normalize().toASCIIString();
            RestAssured.baseURI = normalizedBaseUri;

            final RequestSpecification requestSpec;
            try {
                requestSpec = RestAssured.given()
                        .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
                        .header(HEADER_ACCEPT, APPLICATION_JSON);
            } catch (Exception e) {
                logger.error("Failed to build RequestSpecification for base URI: {}", RestAssured.baseURI, e);
                throw new RuntimeException("Failed to build RequestSpecification", e);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Built RequestSpecification for base URI: {}", RestAssured.baseURI);
            } else {
                logger.info("RequestSpecification created for base URI: {}", RestAssured.baseURI);
            }

            return requestSpec;
        } catch (URISyntaxException e) {
            logger.error("Configured API URL '{}' is not a valid URI", apiUrl, e);
            throw new IllegalStateException("API URL is invalid", e);
        } catch (IllegalStateException e) {
            // preserve IllegalStateException semantics after logging
            logger.error("Invalid API configuration detected: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime exception while creating RequestSpecification for API URL '{}'", apiUrl, e);
            throw e;
        } catch (Exception e) {
            // Catch-all to ensure no unexpected exceptions escape without logging
            logger.error("Unexpected exception while creating RequestSpecification for API URL '{}'", apiUrl, e);
            throw new RuntimeException("Unexpected error while creating RequestSpecification", e);
        }
    }
}