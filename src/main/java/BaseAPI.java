package com.swm.core.base;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import com.swm.core.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

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

    private static final Logger logger = LoggerFactory.getLogger(BaseAPI.class);

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";
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
     */
    protected RequestSpecification getRequestSpec() {
        final String apiUrl = ConfigManager.getApiUrl();

        if (Objects.isNull(apiUrl) || apiUrl.isBlank()) {
            logger.error("Configured API URL is null or blank. Check ConfigManager.getApiUrl()");
            throw new IllegalStateException("API URL is not configured");
        }

        try {
            // Validate the URL is well-formed and has a scheme (http/https)
            final URI uri = new URI(apiUrl.trim());
            if (Objects.isNull(uri.getScheme()) || uri.getScheme().isBlank()) {
                logger.error("Configured API URL '{}' does not contain a valid scheme", apiUrl);
                throw new IllegalStateException("API URL is invalid: missing scheme");
            }

            RestAssured.baseURI = apiUrl;
            final RequestSpecification spec = RestAssured.given()
                    .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
                    .header(HEADER_ACCEPT, APPLICATION_JSON);

            if (logger.isDebugEnabled()) {
                logger.debug("Built RequestSpecification for base URI: {}", apiUrl);
            }

            return spec;
        } catch (URISyntaxException e) {
            logger.error("Configured API URL '{}' is not a valid URI", apiUrl, e);
            throw new IllegalStateException("API URL is invalid", e);
        } catch (RuntimeException e) {
            // Re-throw runtime exceptions after logging to preserve original semantics.
            logger.error("Runtime exception while creating RequestSpecification for base URI: {}", apiUrl, e);
            throw e;
        } catch (Exception e) {
            // Catch-all to ensure visibility and consistent failure mode.
            logger.error("Failed to create RequestSpecification for base URI: {}", apiUrl, e);
            throw new RuntimeException("Failed to build RequestSpecification", e);
        }
    }
}