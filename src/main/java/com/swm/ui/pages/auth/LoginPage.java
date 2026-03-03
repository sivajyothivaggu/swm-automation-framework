package com.swm.ui.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BasePage;
import java.util.List;

public class LoginPage extends BasePage {
    
    @FindBy(id = "email")
    private WebElement usernameField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    @FindBy(tagName = "button")
    private List<WebElement> buttons;
    
    public void login(String username, String password) {
        // Click Login button to open login form
        for (WebElement button : buttons) {
            if ("Login".equals(button.getText())) {
                button.click();
                break;
            }
        }
        
        // Wait for and fill login form
        wait.waitForElementVisible(By.id("email"));
        
        // Clear existing values using Ctrl+A and Delete
        usernameField.click();
        usernameField.sendKeys(org.openqa.selenium.Keys.CONTROL + "a");
        usernameField.sendKeys(org.openqa.selenium.Keys.DELETE);
        
        passwordField.click();
        passwordField.sendKeys(org.openqa.selenium.Keys.CONTROL + "a");
        passwordField.sendKeys(org.openqa.selenium.Keys.DELETE);
        
        // Enter new credentials
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        
        // Click Sign In button
        for (WebElement button : buttons) {
            String buttonText = button.getText();
            if (buttonText != null && buttonText.contains("Sign")) {
                button.click();
                break;
            }
        }
    }
    
    public boolean isUsernameFieldVisible() {
        try {
            // First check if Login button is visible (initial state)
            for (WebElement button : buttons) {
                if ("Login".equals(button.getText()) && button.isDisplayed()) {
                    return true; // Login page is visible
                }
            }
            // Then check if email field is visible (form opened)
            return driver.findElement(By.id("email")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isPasswordFieldVisible() {
        try {
            return driver.findElement(By.id("password")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isLoginButtonVisible() {
        try {
            for (WebElement button : buttons) {
                if ("Login".equals(button.getText()) && button.isDisplayed()) {
                    return true;
                }
            }
            // Also check for Sign In button
            for (WebElement button : buttons) {
                String buttonText = button.getText();
                if (buttonText != null && buttonText.contains("Sign") && button.isDisplayed()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isErrorMessageDisplayed() {
        try {
            List<By> errorLocators = List.of(
                By.xpath("//*[contains(text(), 'Invalid')]"),
                By.xpath("//*[contains(text(), 'incorrect')]"),
                By.xpath("//*[contains(text(), 'failed')]"),
                By.xpath("//*[contains(@class, 'error')]"),
                By.xpath("//*[contains(@class, 'alert')]"),
                By.cssSelector(".error-message"),
                By.cssSelector(".alert-danger")
            );
            
            for (By locator : errorLocators) {
                List<WebElement> elements = driver.findElements(locator);
                for (WebElement element : elements) {
                    if (element.isDisplayed()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
