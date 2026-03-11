package com.swm.core.base;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import com.swm.core.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Creates and returns a RequestSpecification configured with the application's API base URL
     * and common headers (Content-Type and Accept as application/json).
     *
     * Behavior:
     * - Validates that the API URL is present and non-blank in configuration. If missing, logs an error
     *   and throws an IllegalStateException to fail fast.
     * - Sets RestAssured.baseURI to the configured API URL.
     * - Builds the RequestSpecification and returns it.
     *
     * Throws:
     * - IllegalStateException if API URL is null or blank.
     * - RuntimeException if building the RequestSpecification fails for any other reason.
     *
     * @return a configured RequestSpecification ready for use
     */
    protected RequestSpecification getRequestSpec() {
        String apiUrl = ConfigManager.getApiUrl();

        if (Objects.isNull(apiUrl) || apiUrl.isBlank()) {
            logger.error("Configured API URL is null or blank. Check ConfigManager.getApiUrl()");
            throw new IllegalStateException("API URL is not configured");
        }

        try {
            RestAssured.baseURI = apiUrl;
            RequestSpecification spec = RestAssured.given()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");
            logger.debug("Built RequestSpecification for base URI: {}", apiUrl);
            return spec;
        } catch (Exception e) {
            logger.error("Failed to create RequestSpecification for base URI: {}", apiUrl, e);
            throw new RuntimeException("Failed to build RequestSpecification", e);
        }
    }
}