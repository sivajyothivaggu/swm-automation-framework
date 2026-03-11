package com.swm.ui.pages.auth;

import com.swm.core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Page object representing the logout confirmation popup.
 *
 * <p>This class provides helper methods to interact with and query the state of the logout
 * confirmation popup. All public methods include defensive error handling and logging to make
 * usage in tests more robust and production-ready.</p>
 */
public class LogoutPopupPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(LogoutPopupPage.class);

    // Locators for elements on the logout confirmation popup
    private final By confirmLogoutTitle = By.xpath("//*[contains(text(), 'Confirm Logout')]");
    private final By currentUser = By.xpath("//*[contains(text(), 'Current User')]");
    private final By sessionStarted = By.xpath("//*[contains(text(), 'Session Started')]");
    private final By cancelButton = By.xpath("//button[text()='Cancel']");
    private final By logoutButton = By.xpath("//button[text()='Logout']");

    /**
     * Checks whether the logout popup is currently visible on the page.
     *
     * @return true if the popup is visible, false otherwise
     */
    public boolean isPopupVisible() {
        try {
            return safeFindVisible(confirmLogoutTitle).isPresent();
        } catch (Exception e) {
            logger.debug("Unexpected error while checking popup visibility.", e);
            return false;
        }
    }

    /**
     * Waits for the logout popup to become visible.
     *
     * <p>This method will throw an unchecked exception if the popup does not become visible within
     * the underlying wait timeout. The exception is logged with context for debugging.</p>
     */
    public void waitForPopupVisible() {
        try {
            WebElement el = wait.waitForElementVisible(confirmLogoutTitle);
            if (Objects.isNull(el)) {
                String msg = "waitForElementVisible returned null for locator: " + confirmLogoutTitle;
                logger.error(msg);
                throw new IllegalStateException(msg);
            }
        } catch (Exception e) {
            logger.error("Failed waiting for logout popup to be visible. Locator: {}", confirmLogoutTitle, e);
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
            Optional<WebElement> opt = safeFindVisible(confirmLogoutTitle);
            return opt.map(WebElement::isDisplayed).orElse(false);
        } catch (Exception e) {
            logger.debug("Error checking if Confirm Logout title is displayed.", e);
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
            Optional<WebElement> opt = safeFindVisible(currentUser);
            return opt.map(WebElement::isDisplayed).orElse(false);
        } catch (Exception e) {
            logger.debug("Error checking if current user is displayed.", e);
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
            Optional<WebElement> opt = safeFindVisible(sessionStarted);
            return opt.map(WebElement::isDisplayed).orElse(false);
        } catch (Exception e) {
            logger.debug("Error checking if session started is displayed.", e);
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
            Optional<WebElement> opt = safeFindVisible(cancelButton);
            return opt.map(WebElement::isDisplayed).orElse(false);
        } catch (Exception e) {
            logger.debug("Error checking if cancel button is visible.", e);
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
            Optional<WebElement> opt = safeFindVisible(logoutButton);
            return opt.map(WebElement::isDisplayed).orElse(false);
        } catch (Exception e) {
            logger.debug("Error checking if logout button is visible.", e);
            return false;
        }
    }

    /**
     * Clicks the Logout button on the popup.
     *
     * <p>Logs and throws an IllegalStateException if the logout button cannot be clicked.</p>
     */
    public void clickLogoutButton() {
        try {
            WebElement el = wait.waitForElementClickable(logoutButton);
            if (Objects.isNull(el)) {
                String msg = "Clickable logout button element is null for locator: " + logoutButton;
                logger.error(msg);
                throw new IllegalStateException(msg);
            }
            el.click();
            logger.info("Clicked logout button.");
        } catch (Exception e) {
            logger.error("Failed to click logout button. Locator: {}", logoutButton, e);
            throw new IllegalStateException("Failed to click logout button", e);
        }
    }

    /**
     * Clicks the Cancel button on the popup.
     *
     * <p>Logs and throws an IllegalStateException if the cancel button cannot be clicked.</p>
     */
    public void clickCancelButton() {
        try {
            WebElement el = wait.waitForElementClickable(cancelButton);
            if (Objects.isNull(el)) {
                String msg = "Clickable cancel button element is null for locator: " + cancelButton;
                logger.error(msg);
                throw new IllegalStateException(msg);
            }
            el.click();
            logger.info("Clicked cancel button.");
        } catch (Exception e) {
            logger.error("Failed to click cancel button. Locator: {}", cancelButton, e);
            throw new IllegalStateException("Failed to click cancel button", e);
        }
    }
}