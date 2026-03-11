package com.swm.ui.pages.auth;

import com.swm.core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Page object representing the Logout confirmation popup.
 * <p>
 * This class exposes helper methods to interact with and query the state of the logout popup.
 * All methods include defensive error handling and logging to make usage in tests more robust.
 */
public class LogoutPopupPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(LogoutPopupPage.class);

    // Locators for elements on the logout confirmation popup
    private final By confirm_logout_title = By.xpath("//*[contains(text(), 'Confirm Logout')]");
    private final By current_user = By.xpath("//*[contains(text(), 'Current User')]");
    private final By session_started = By.xpath("//*[contains(text(), 'Session Started')]");
    private final By cancel_button = By.xpath("//button[text()='Cancel']");
    private final By logout_button = By.xpath("//button[text()='Logout']");

    /**
     * Checks whether the logout popup is currently visible on the page.
     *
     * @return true if the popup is visible, false otherwise
     */
    public boolean isPopupVisible() {
        try {
            return safeFindVisible(confirm_logout_title).isPresent();
        } catch (Exception e) {
            logger.debug("Unexpected error while checking popup visibility", e);
            return false;
        }
    }

    /**
     * Waits for the logout popup to become visible.
     * <p>
     * This method will throw an unchecked exception if the popup does not become visible within
     * the underlying wait timeout. The exception is logged with context for debugging.
     */
    public void waitForPopupVisible() {
        try {
            WebElement el = wait.waitForElementVisible(confirm_logout_title);
            if (Objects.isNull(el)) {
                throw new IllegalStateException("waitForElementVisible returned null for locator: " + confirm_logout_title);
            }
        } catch (Exception e) {
            logger.error("Failed waiting for logout popup to be visible (locator: {}).", confirm_logout_title, e);
            throw new IllegalStateException("Logout popup did not become visible", e);
        }
    }

    /**
     * Checks whether the Confirm Logout title is displayed.
     *
     * @return true if displayed, false otherwise
     */
    public boolean isConfirmLogoutTitleDisplayed() {
        try {
            return safeFindVisible(confirm_logout_title).map(WebElement::isDisplayed).orElse(false);
        } catch (Exception e) {
            logger.debug("Error checking if confirm logout title is displayed", e);
            return false;
        }
    }

    /**
     * Checks whether the Current User element is displayed.
     *
     * @return true if displayed, false otherwise
     */
    public boolean isCurrentUserDisplayed() {
        try {
            return safeFindVisible(current_user).map(WebElement::isDisplayed).orElse(false);
        } catch (Exception e) {
            logger.debug("Error checking if current user is displayed", e);
            return false;
        }
    }

    /**
     * Checks whether the Session Started element is displayed.
     *
     * @return true if displayed, false otherwise
     */
    public boolean isSessionStartedDisplayed() {
        try {
            return safeFindVisible(session_started).map(WebElement::isDisplayed).orElse(false);
        } catch (Exception e) {
            logger.debug("Error checking if session started is displayed", e);
            return false;
        }
    }

    /**
     * Checks whether the Cancel button is visible.
     *
     * @return true if visible, false otherwise
     */
    public boolean isCancelButtonVisible() {
        try {
            return safeFindVisible(cancel_button).map(WebElement::isDisplayed).orElse(false);
        } catch (Exception e) {
            logger.debug("Error checking if cancel button is visible", e);
            return false;
        }
    }

    /**
     * Checks whether the Logout button is visible.
     *
     * @return true if visible, false otherwise
     */
    public boolean isLogoutButtonVisible() {
        try {
            return safeFindVisible(logout_button).map(WebElement::isDisplayed).orElse(false);
        } catch (Exception e) {
            logger.debug("Error checking if logout button is visible", e);
            return false;
        }
    }

    /**
     * Clicks the Logout button on the popup.
     * <p>
     * Logs and throws an IllegalStateException if the logout button cannot be clicked.
     */
    public void clickLogoutButton() {
        try {
            WebElement el = wait.waitForElementClickable(logout_button);
            if (Objects.isNull(el)) {
                logger.error("Clickable logout button element is null (locator: {})", logout_button);
                throw new IllegalStateException("Clickable logout button is null");
            }
            el.click();
        } catch (Exception e) {
            logger.error("Failed to click logout button (locator: {}).", logout_button, e);
            throw new IllegalStateException("Unable to click logout button", e);
        }
    }

    /**
     * Helper that attempts to locate a visible element using the underlying wait utility.
     * Any exceptions are caught and logged; Optional.empty() is returned in failure cases.
     *
     * @param locator the locator to find
     * @return Optional containing the WebElement if found and non-null, otherwise Optional.empty()
     */
    private Optional<WebElement> safeFindVisible(By locator) {
        try {
            WebElement element = wait.waitForElementVisible(locator);
            return Objects.isNull(element) ? Optional.empty() : Optional.of(element);
        } catch (Exception e) {
            logger.debug("Element not found or not visible for locator: {}", locator, e);
            return Optional.empty();
        }
    }
}