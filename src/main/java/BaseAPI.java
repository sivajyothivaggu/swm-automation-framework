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

    // Header names and content type configured as private fields to allow centralized change
    private static final String header_content_type = "Content-Type";
    private static final String header_accept = "Accept";
    private static final String application_json = "application/json";

    /**
     * Creates and returns a RequestSpecification configured with the application's API base URL
     * and common headers (Content-Type and Accept as application/json).
     *
     * Behavior:
     * - Validates that the API URL is present and non-blank in configuration. If missing, logs an error
     *   and throws an IllegalStateException to fail fast.
     * - Validates that the API URL is a well-formed URI with a scheme (http or https).
     * - Sets RestAssured.baseURI to the configured API URL (trimmed).
     * - Builds the RequestSpecification and returns it.
     *
     * Throws:
     * - IllegalStateException if API URL is null, blank, or invalid.
     * - RuntimeException if building the RequestSpecification fails for any other reason.
     *
     * @return a configured RequestSpecification ready for use
     */
    protected RequestSpecification getRequestSpec() {
        final String api_url = ConfigManager.getApiUrl();

        if (Objects.isNull(api_url) || api_url.isBlank()) {
            logger.error("Configured API URL is null or blank. Check ConfigManager.getApiUrl()");
            throw new IllegalStateException("API URL is not configured");
        }

        final String api_url_trimmed = api_url.trim();

        try {
            // Validate the URL is well-formed and has a scheme (http/https)
            final URI uri_obj = new URI(api_url_trimmed);
            final String scheme = uri_obj.getScheme();

            if (Objects.isNull(scheme) || scheme.isBlank()) {
                logger.error("Configured API URL '{}' does not contain a valid scheme", api_url_trimmed);
                throw new IllegalStateException("API URL is invalid: missing scheme");
            }

            final String scheme_lower = scheme.toLowerCase();
            if (!"http".equals(scheme_lower) && !"https".equals(scheme_lower)) {
                logger.error("Configured API URL '{}' uses unsupported scheme '{}'", api_url_trimmed, scheme);
                throw new IllegalStateException("API URL is invalid: unsupported scheme");
            }

            RestAssured.baseURI = api_url_trimmed;

            final RequestSpecification request_spec = RestAssured.given()
                    .header(header_content_type, application_json)
                    .header(header_accept, application_json);

            if (logger.isDebugEnabled()) {
                logger.debug("Built RequestSpecification for base URI: {}", api_url_trimmed);
            }

            return request_spec;
        } catch (URISyntaxException e) {
            logger.error("Configured API URL '{}' is not a valid URI", api_url_trimmed, e);
            throw new IllegalStateException("API URL is invalid", e);
        } catch (RuntimeException e) {
            // Re-throw runtime exceptions after logging to preserve original semantics.
            logger.error("Runtime exception while creating RequestSpecification for base URI: {}", api_url_trimmed, e);
            throw e;
        } catch (Exception e) {
            // Catch-all to ensure visibility and consistent failure mode.
            logger.error("Failed to create RequestSpecification for base URI: {}", api_url_trimmed, e);
            throw new RuntimeException("Failed to build RequestSpecification", e);
        }
    }
}