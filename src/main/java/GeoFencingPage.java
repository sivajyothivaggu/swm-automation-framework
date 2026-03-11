package com.swm.ui.pages.transport.geofencing;

import java.util.Objects;
import java.util.Optional;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swm.core.base.BasePage;

/**
 * Page object representing the GeoFencing page.
 *
 * <p>This class provides interactions with the geofencing UI elements.
 * It encapsulates the WebElement(s) used on the page and exposes safe,
 * documented operations to interact with them.</p>
 *
 * <p>All public methods perform necessary null checks and log meaningful
 * messages on failure. Optional is used for nullable returns to avoid
 * returning raw nulls.</p>
 */
public class GeoFencingPage extends BasePage {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoFencingPage.class);

    @FindBy(id = "create-geofence-btn")
    private WebElement createGeofenceButton;

    /**
     * Clicks the "Create Geofence" button.
     *
     * <p>This method performs a null check on the underlying WebElement and logs
     * both normal operation and error conditions. It also verifies that the
     * element is displayed and enabled before attempting to click. Any failure
     * during the click is logged and an IllegalStateException is thrown to
     * surface the problem to callers in a clear manner.</p>
     *
     * @throws IllegalStateException if the button is not present, not interactable, or the click action fails
     */
    public void clickCreateGeofence() {
        if (Objects.isNull(createGeofenceButton)) {
            LOGGER.error("createGeofenceButton WebElement is null. Cannot perform click.");
            throw new IllegalStateException("Create Geofence button is not available on the page.");
        }

        try {
            LOGGER.debug("Verifying Create Geofence button is displayed and enabled before clicking.");
            boolean displayed;
            boolean enabled;
            try {
                displayed = createGeofenceButton.isDisplayed();
                enabled = createGeofenceButton.isEnabled();
            } catch (StaleElementReferenceException | NoSuchElementException ex) {
                LOGGER.warn("Create Geofence button reference is stale or not present when checking interactability.", ex);
                throw new IllegalStateException("Create Geofence button is not available or stale.", ex);
            } catch (Exception ex) {
                LOGGER.error("Unexpected error while checking Create Geofence button state.", ex);
                throw new IllegalStateException("Failed to verify Create Geofence button state.", ex);
            }

            if (!displayed) {
                LOGGER.error("Create Geofence button is present but not displayed.");
                throw new IllegalStateException("Create Geofence button is not displayed.");
            }
            if (!enabled) {
                LOGGER.error("Create Geofence button is present but not enabled.");
                throw new IllegalStateException("Create Geofence button is not enabled.");
            }

            LOGGER.debug("Attempting to click the Create Geofence button.");
            createGeofenceButton.click();
            LOGGER.info("Create Geofence button clicked successfully.");
        } catch (IllegalStateException ise) {
            // Already logged above with a clear message; rethrow to preserve behaviour.
            throw ise;
        } catch (Exception ex) {
            LOGGER.error("Failed to click the Create Geofence button due to an unexpected error.", ex);
            throw new IllegalStateException("Failed to click the Create Geofence button.", ex);
        }
    }

    /**
     * Returns an Optional containing the createGeofenceButton WebElement if present.
     *
     * <p>Callers should use this to safely interact with the element when direct access is needed.
     * Prefer using high-level operations such as clickCreateGeofence() which include validation and logging.</p>
     *
     * @return Optional<WebElement> containing the createGeofenceButton or empty if not present
     */
    public Optional<WebElement> getCreateGeofenceButton() {
        return Optional.ofNullable(createGeofenceButton);
    }
}