package com.swm.ui.pages.dashboard;

import java.util.Objects;
import java.util.Optional;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swm.core.base.BaseComponent;

/**
 * HeaderComponent represents the header area of the dashboard UI that contains
 * user-related controls such as the profile menu and logout button.
 *
 * This class provides a safe, logged, and well-documented way to perform a
 * logout operation. It performs defensive null checks and captures common
 * Selenium exceptions, logging details for troubleshooting.
 */
public class HeaderComponent extends BaseComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderComponent.class);

    @FindBy(id = "user-profile")
    private WebElement user_profile;

    @FindBy(id = "logout-btn")
    private WebElement logout_button;

    /**
     * Performs logout by clicking the user profile control to open the menu and
     * then clicking the logout button.
     *
     * This method logs progress and errors, validates that required elements are
     * initialized, and throws an unchecked exception when the action cannot be
     * completed so callers can handle failures as needed.
     *
     * Note: This method preserves the original behavior of clicking the two
     * elements. It adds validation, logging and error handling for production use.
     */
    public void logout() {
        // Defensive null checks using Objects.isNull per best practices
        if (Objects.isNull(user_profile) || Objects.isNull(logout_button)) {
            LOGGER.error("Logout failed: required web elements are not initialized (user_profile={}, logout_button={})",
                    user_profile, logout_button);
            throw new IllegalStateException("Required web elements for logout are not initialized.");
        }

        try {
            LOGGER.debug("Attempting to open user profile menu by clicking user_profile element.");
            user_profile.click();

            LOGGER.debug("Attempting to click logout button.");
            logout_button.click();

            LOGGER.info("Logout action invoked successfully.");
        } catch (NoSuchElementException | StaleElementReferenceException | WebDriverException ex) {
            LOGGER.error("Selenium error occurred while performing logout action.", ex);
            throw new RuntimeException("Failed to perform logout due to Selenium error.", ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error occurred while performing logout.", ex);
            throw new RuntimeException("Unexpected error while performing logout.", ex);
        }
    }

    /**
     * Returns an Optional wrapping the user profile element. Useful for callers
     * that prefer Optional-based handling of nullable elements.
     *
     * @return Optional<WebElement> user profile element if present, otherwise Optional.empty()
     */
    public Optional<WebElement> getUserProfileElement() {
        return Optional.ofNullable(user_profile);
    }

    /**
     * Returns an Optional wrapping the logout button element. Useful for callers
     * that prefer Optional-based handling of nullable elements.
     *
     * @return Optional<WebElement> logout button element if present, otherwise Optional.empty()
     */
    public Optional<WebElement> getLogoutButtonElement() {
        return Optional.ofNullable(logout_button);
    }
}