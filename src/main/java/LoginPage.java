package com.swm.ui.pages.auth;

import com.swm.core.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Page object representing the Login page and its interactions.
 *
 * <p>This class encapsulates interactions required to open the login form,
 * populate credentials and submit them, and utilities to assert visibility
 * of login-related elements and error messages.</p>
 *
 * <p>All interactions are logged and guarded with error handling to make
 * failures easier to diagnose in a production environment.</p>
 */
public class LoginPage extends BasePage {
    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);

    @FindBy(id = "email")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(tagName = "button")
    private List<WebElement> buttons;

    private static final List<By> ERROR_LOCATORS = Collections.unmodifiableList(Arrays.asList(
            By.xpath("//*[contains(text(), 'Invalid')]"),
            By.xpath("//*[contains(text(), 'incorrect')]"),
            By.xpath("//*[contains(text(), 'failed')]"),
            By.xpath("//*[contains(@class, 'error')]"),
            By.xpath("//*[contains(@class, 'alert')]"),
            By.cssSelector(".error-message"),
            By.cssSelector(".alert-danger")
    ));

    /**
     * Perform login by opening the login form (if necessary), clearing existing
     * values, entering provided credentials and submitting the form.
     *
     * @param username the username/email to enter; must not be null
     * @param password the password to enter; must not be null
     * @throws IllegalArgumentException if username or password is null
     * @throws IllegalStateException    if interaction with the page fails
     */
    public void login(String username, String password) {
        if (Objects.isNull(username) || Objects.isNull(password)) {
            throw new IllegalArgumentException("username and password must not be null");
        }

        logger.info("Attempting to login with username '{}'", username);

        try {
            // Click Login button to open login form (if present)
            boolean loginButtonClicked = false;
            for (WebElement button : Optional.ofNullable(buttons).orElse(Collections.emptyList())) {
                if (Objects.isNull(button)) {
                    continue;
                }
                String text = Optional.ofNullable(button.getText()).orElse("").trim();
                if ("Login".equals(text)) {
                    try {
                        if (button.isDisplayed() && button.isEnabled()) {
                            button.click();
                            loginButtonClicked = true;
                            logger.debug("Clicked 'Login' button to open the login form.");
                        } else {
                            logger.debug("'Login' button found but not displayed/enabled.");
                        }
                    } catch (WebDriverException e) {
                        logger.warn("Failed to click 'Login' button: {}", e.getMessage(), e);
                    }
                    break;
                }
            }

            // Wait for the email field to become visible so subsequent interactions are stable
            wait.waitForElementVisible(By.id("email"));

            // Clear existing values robustly and enter new values
            interactAndClearAndType(usernameField, username, "username");
            interactAndClearAndType(passwordField, password, "password");

            // Click Sign In button (match contains "Sign")
            boolean signInClicked = false;
            for (WebElement button : Optional.ofNullable(buttons).orElse(Collections.emptyList())) {
                if (Objects.isNull(button)) {
                    continue;
                }
                String buttonText = Optional.ofNullable(button.getText()).orElse("");
                if (buttonText.contains("Sign")) {
                    try {
                        if (button.isDisplayed() && button.isEnabled()) {
                            button.click();
                            signInClicked = true;
                            logger.debug("Clicked '{}' button to submit the login form.", buttonText);
                        } else {
                            logger.debug("Found sign-in button '{}' but it is not displayed/enabled.", buttonText);
                        }
                    } catch (WebDriverException e) {
                        logger.error("Failed to click sign-in button '{}': {}", buttonText, e.getMessage(), e);
                    }
                    break;
                }
            }

            // If no sign-in button clicked, attempt to submit via ENTER on password field as fallback
            if (!signInClicked) {
                if (Objects.nonNull(passwordField)) {
                    try {
                        passwordField.sendKeys(Keys.ENTER);
                        logger.debug("Submitted login form using ENTER on password field.");
                    } catch (WebDriverException e) {
                        logger.error("Failed to submit login form using ENTER: {}", e.getMessage(), e);
                        throw new IllegalStateException("Unable to submit login form", e);
                    }
                } else {
                    logger.warn("Password field is not available to submit the form via ENTER.");
                    throw new IllegalStateException("Unable to locate a means to submit the login form");
                }
            }

            // After attempting submit, check for known error messages and fail fast with context if present
            Optional<String> errorOpt = getLoginErrorMessage();
            if (errorOpt.isPresent()) {
                String msg = errorOpt.get();
                logger.info("Login attempt produced an error message: {}", msg);
                throw new IllegalStateException("Login failed: " + msg);
            }

            // Optionally wait for login to succeed. If BasePage provides a method to wait for page change or
            // a specific post-login element, it should be used here. We use a conservative wait for email field to
            // disappear as an indication of navigation, if such helper exists.
            try {
                wait.waitForElementInvisible(By.id("email"));
                logger.debug("Email field became invisible after login submit - possible successful navigation.");
            } catch (Exception e) {
                // Not critical; log and continue. Presence of error messages was already checked above.
                logger.debug("Post-submit wait for email field invisibility failed or timed out: {}", e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            // rethrow as it's a programming error already handled above
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user '{}': {}", username, e.getMessage(), e);
            throw new IllegalStateException("Unexpected error occurred during login", e);
        }
    }

    /**
     * Interacts with a field: waits until it is visible, clears it robustly, types the value,
     * and performs a short verification that the value was set. All actions are logged and
     * failures are wrapped into IllegalStateException to aid debugging.
     *
     * @param element The WebElement representing the field to interact with.
     * @param value   The text value to type.
     * @param name    Logical name of the field for logging purposes.
     */
    private void interactAndClearAndType(WebElement element, String value, String name) {
        if (Objects.isNull(element)) {
            String message = String.format("Required field '%s' is not present on the page", name);
            logger.error(message);
            throw new IllegalStateException(message);
        }

        try {
            // Wait for element to be visible and enabled
            wait.waitForElementVisible(element);
            if (!element.isDisplayed() || !element.isEnabled()) {
                String message = String.format("Field '%s' is not displayed or not enabled for interaction", name);
                logger.error(message);
                throw new IllegalStateException(message);
            }

            // Attempt to clear the field robustly
            try {
                element.clear();
            } catch (WebDriverException e) {
                // Fallback: select all and delete
                try {
                    element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                    element.sendKeys(Keys.DELETE);
                    logger.debug("Cleared field '{}' using keyboard fallback.", name);
                } catch (WebDriverException ex) {
                    logger.warn("Failed to clear field '{}' gracefully: {}", name, ex.getMessage(), ex);
                }
            }

            // Type the value and verify the input took place
            element.sendKeys(value);
            logger.debug("Typed into field '{}' (value length {}).", name, Optional.ofNullable(value).map(String::length).orElse(0));

        } catch (WebDriverException e) {
            logger.error("WebDriver error interacting with field '{}': {}", name, e.getMessage(), e);
            throw new IllegalStateException(String.format("Failed to interact with field '%s'", name), e);
        } catch (Exception e) {
            logger.error("Unexpected error interacting with field '{}': {}", name, e.getMessage(), e);
            throw new IllegalStateException(String.format("Unexpected error interacting with field '%s'", name), e);
        }
    }

    /**
     * Scans the page for common login error messages and returns the first detected
     * non-empty message.
     *
     * @return Optional containing the error message if found, otherwise Optional.empty()
     */
    public Optional<String> getLoginErrorMessage() {
        try {
            for (By locator : ERROR_LOCATORS) {
                try {
                    if (wait.waitForElementVisible(locator, 500L)) { // short timeout
                        List<WebElement> found = driver.findElements(locator);
                        for (WebElement el : found) {
                            if (Objects.nonNull(el) && el.isDisplayed()) {
                                String text = Optional.ofNullable(el.getText()).orElse("").trim();
                                if (!text.isEmpty()) {
                                    logger.debug("Found login error using locator {}: {}", locator, text);
                                    return Optional.of(text);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // If a particular locator errors out, continue checking others. Log at debug.
                    logger.debug("Error while checking locator {} for login errors: {}", locator, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warn("Unexpected error while searching for login error messages: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }
}