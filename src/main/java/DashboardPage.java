package com.swm.ui.pages.dashboard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import com.swm.core.base.BasePage;
import java.util.List;

public class DashboardPage extends BasePage {
    
    private By dashboardTitle = By.xpath("//*[contains(text(), 'Executive Dashboard')]");
    private By profileIcon = By.xpath("//button[contains(text(),'s') or contains(text(),'S')]");
    private By profileDropdown = By.xpath("//*[contains(@class, 'dropdown') or contains(@class, 'menu')]");
    private By profileOption = By.xpath("//*[text()='Profile']");
    private By logoutOption = By.xpath("//*[text()='Logout']");
    
    public void waitForDashboardLoad() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    public boolean isDashboardTitleVisible() {
        try {
            return wait.waitForElementVisible(dashboardTitle).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public void clickProfileIcon() {
        // Try multiple strategies to find the profile icon
        List<By> locators = List.of(
            By.xpath("//button[contains(text(),'s') or contains(text(),'S')]"),
            By.xpath("//button[contains(., 'superadmin')]"),
            By.xpath("//button[contains(., 'SUPERADMIN')]"),
            By.xpath("//*[contains(@class, 'profile')]//button"),
            By.xpath("//div[contains(text(), 'superadmin')]//ancestor::button"),
            By.xpath("//div[contains(text(), 'SUPERADMIN')]//ancestor::button"),
            By.xpath("//button[contains(@aria-label, 'profile')]"),
            By.cssSelector("button[class*='profile']"),
            By.xpath("//button[.//div[text()='s' or text()='S']]"),
            By.xpath("//div[text()='superadmin']/parent::*/parent::button")
        );
        
        for (By locator : locators) {
            try {
                WebElement element = wait.waitForElementClickable(locator);
                element.click();
                return;
            } catch (Exception e) {
                // Try next locator
            }
        }
        throw new RuntimeException("Could not find profile icon with any locator strategy");
    }
    
    public boolean isDropdownVisible() {
        try {
            wait.waitForElementVisible(logoutOption);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void waitForDropdownVisible() {
        wait.waitForElementVisible(logoutOption);
    }
    
    public boolean isProfileOptionDisplayed() {
        try {
            return wait.waitForElementVisible(profileOption).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isLogoutOptionDisplayed() {
        try {
            return wait.waitForElementVisible(logoutOption).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public void clickLogoutFromDropdown() {
        wait.waitForElementClickable(logoutOption).click();
    }
}
