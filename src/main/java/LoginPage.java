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
                        logger.warn("Failed to click 'Login' button: {}", e.getMessage());
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
                        logger.error("Failed to click sign-in button '{}': {}", buttonText, e.getMessage());
                        throw new IllegalStateException("Unable to click sign-in button", e);
                    }
                    break;
                }
            }

            if (!signInClicked) {
                logger.warn("Sign In button was not found or not clickable after attempting login. loginButtonClicked={}", loginButtonClicked);
            }
        } catch (Exception e) {
            logger.error("An error occurred during login attempt: {}", e.getMessage(), e);
            throw new IllegalStateException("Login failed due to an unexpected error", e);
        }
    }

    /**
     * Check whether the username field (or initial Login button) is visible.
     *
     * @return true if the username field or initial Login button is visible; false otherwise
     */
    public boolean isUsernameFieldVisible() {
        try {
            // If a visible "Login" button exists, consider the login entry point visible
            for (WebElement button : Optional.ofNullable(buttons).orElse(Collections.emptyList())) {
                if (Objects.isNull(button)) {
                    continue;
                }
                String text = Optional.ofNullable(button.getText()).orElse("").trim();
                if ("Login".equals(text) && button.isDisplayed()) {
                    logger.debug("'Login' button is visible.");
                    return true;
                }
            }
            boolean emailVisible = driver.findElement(By.id("email")).isDisplayed();
            logger.debug("Email field visibility: {}", emailVisible);
            return emailVisible;
        } catch (Exception e) {
            logger.debug("isUsernameFieldVisible encountered an exception: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check whether the password field is visible on the page.
     *
     * @return true if the password field is visible; false otherwise
     */
    public boolean isPasswordFieldVisible() {
        try {
            boolean visible = driver.findElement(By.id("password")).isDisplayed();
            logger.debug("Password field visibility: {}", visible);
            return visible;
        } catch (Exception e) {
            logger.debug("isPasswordFieldVisible encountered an exception: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check whether a login-related button (either initial "Login" or "Sign In") is visible.
     *
     * @return true if a login-related button is visible; false otherwise
     */
    public boolean isLoginButtonVisible() {
        try {
            for (WebElement button : Optional.ofNullable(buttons).orElse(Collections.emptyList())) {
                if (Objects.isNull(button)) {
                    continue;
                }
                String text = Optional.ofNullable(button.getText()).orElse("").trim();
                if (("Login".equals(text) || text.contains("Sign")) && button.isDisplayed()) {
                    logger.debug("Login-related button '{}' is visible.", text);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.debug("isLoginButtonVisible encountered an exception: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Determines if an error message is displayed on the page by checking a set of common locators.
     *
     * @return true if an error element is displayed; false otherwise
     */
    public boolean isErrorMessageDisplayed() {
        try {
            for (By locator : ERROR_LOCATORS) {
                List<WebElement> elements = driver.findElements(locator);
                if (elements == null || elements.isEmpty()) {
                    continue;
                }
                for (WebElement element : elements) {
                    if (element != null && element.isDisplayed()) {
                        logger.debug("Error message element found using locator: {}", locator);
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            logger.debug("isErrorMessageDisplayed encountered an exception: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to interact with a field: ensure visibility, clear existing content and type new value.
     *
     * @param field   the WebElement field to interact with
     * @param value   the value to type
     * @param fldName friendly name used for logging
     */
    private void interactAndClearAndType(WebElement field, String value, String fldName) {
        if (Objects.isNull(field)) {
            String msg = String.format("Field '%s' is not present on the page.", fldName);
            logger.error(msg);
            throw new IllegalStateException(msg);
        }

        try {
            if (!field.isDisplayed()) {
                // attempt to scroll into view via JavaScript if supported by BasePage
                try {
                    executeJsScrollIntoView(field);
                } catch (Exception jsEx) {
                    logger.debug("Unable to scroll field '{}' into view: {}", fldName, jsEx.getMessage());
                }
            }

            field.click();
            // Try using clear(), then fall back to Ctrl+A + Delete to ensure value is removed
            try {
                field.clear();
            } catch (Exception clearEx) {
                logger.debug("field.clear() failed for '{}': {}", fldName, clearEx.getMessage());
            }

            try {
                field.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                field.sendKeys(Keys.DELETE);
            } catch (Exception e) {
                logger.debug("Alternative clear via keyboard failed for '{}': {}", fldName, e.getMessage());
            }

            field.sendKeys(value);
            logger.debug("Entered value into '{}'.", fldName);
        } catch (WebDriverException e) {
            logger.error("WebDriver exception while interacting with '{}': {}", fldName, e.getMessage(), e);
            throw new IllegalStateException("Failed to interact with field: " + fldName, e);
        } catch (Exception e) {
            logger.error("Unexpected exception while interacting with '{}': {}", fldName, e.getMessage(), e);
            throw new IllegalStateException("Failed to interact with field: " + fldName, e);
        }
    }

    /**
     * Execute a JavaScript scrollIntoView for the provided element if the BasePage provides driver access.
     * This method is defensive - if JavaScript execution is not available it will silently return.
     *
     * Note: The BasePage is expected to expose a protected 'driver' member of type WebDriver.
     *
     * @param element the WebElement to scroll into view
     */
    private void executeJsScrollIntoView(WebElement element) {
        try {
            if (Objects.nonNull(element) && Objects.nonNull(driver)) {
                // Use JavaScript executor if available
                if (driver instanceof org.openqa.selenium.JavascriptExecutor) {
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
                }
            }
        } catch (Exception e) {
            logger.debug("executeJsScrollIntoView failed: {}", e.getMessage());
        }
    }
}