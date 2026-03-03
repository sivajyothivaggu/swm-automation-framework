package com.swm.ui.pages.auth;

import org.openqa.selenium.By;
import com.swm.core.base.BasePage;

public class LogoutPopupPage extends BasePage {
    
    private By confirmLogoutTitle = By.xpath("//*[contains(text(), 'Confirm Logout')]");
    private By currentUser = By.xpath("//*[contains(text(), 'Current User')]");
    private By sessionStarted = By.xpath("//*[contains(text(), 'Session Started')]");
    private By cancelButton = By.xpath("//button[text()='Cancel']");
    private By logoutButton = By.xpath("//button[text()='Logout']");
    
    public boolean isPopupVisible() {
        try {
            wait.waitForElementVisible(confirmLogoutTitle);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void waitForPopupVisible() {
        wait.waitForElementVisible(confirmLogoutTitle);
    }
    
    public boolean isConfirmLogoutTitleDisplayed() {
        try {
            return wait.waitForElementVisible(confirmLogoutTitle).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isCurrentUserDisplayed() {
        try {
            return wait.waitForElementVisible(currentUser).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isSessionStartedDisplayed() {
        try {
            return wait.waitForElementVisible(sessionStarted).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isCancelButtonVisible() {
        try {
            return wait.waitForElementVisible(cancelButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isLogoutButtonVisible() {
        try {
            return wait.waitForElementVisible(logoutButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public void clickLogoutButton() {
        wait.waitForElementClickable(logoutButton).click();
    }
}
