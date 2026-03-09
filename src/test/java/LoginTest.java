package com.swm.tests.ui.auth;

import org.testng.annotations.Test;
import com.swm.core.base.BaseTest;
import com.swm.ui.pages.auth.LoginPage;
import com.swm.ui.pages.auth.LogoutPopupPage;


public class LoginTest extends BaseTest 
    
    @Test
    public void testValidLogin
        LoginPage loginPage = new LoginPage()
        loginPage.login("swmadmin", "Admin@123")
        
        DashboardPage dashboardPage = new DashboardPage()
        
        // Step 1: Wait and verify dashboard is loaded
        dashboardPage.waitForDashboardLoad();
        String currentUrl = dashboardPage.getCurrentUrl();
        assertTrue(currentUrl.contains("/dashboard"), "Expected dashboard but not found - URL: " + currentUrl);
        assertTrue(dashboardPage.isDashboardTitleVisible(), "Expected dashboard but not found - Executive Dashboard title not visible");
        
        // Step 2: Click Profile icon and verify dropdown
        dashboardPage.clickProfileIcon();
        assertTrue(dashboardPage.isDropdownVisible(), "Expected dropdown but not visible");
        assertTrue(dashboardPage.isProfileOptionDisplayed(), "Expected dropdown but not visible - Profile option missing");
        assertTrue(dashboardPage.isLogoutOptionDisplayed(), "Expected dropdown but not visible - Logout option missing");
        
        // Step 3: Click Logout from dropdown
        dashboardPage.clickLogoutFromDropdown();
        
        // Step 4: Verify logout popup
        LogoutPopupPage logoutPopup = new LogoutPopupPage();
        assertTrue(logoutPopup.isPopupVisible(), "Expected logout popup but not found");
        assertTrue(logoutPopup.isConfirmLogoutTitleDisplayed(), "Expected logout popup but not found - Title missing");
        assertTrue(logoutPopup.isCurrentUserDisplayed(), "Expected logout popup but not found - Current user missing");
        assertTrue(logoutPopup.isSessionStartedDisplayed(), "Expected logout popup but not found - Session time missing");
        assertTrue(logoutPopup.isCancelButtonVisible(), "Expected logout popup but not found - Cancel button missing");
        assertTrue(logoutPopup.isLogoutButtonVisible(), "Expected logout popup but not found - Logout button missing");
        
        // Step 5: Click Logout button
        logoutPopup.clickLogoutButton();
        
        // Step 6 & 7: Verify redirected to login page
        currentUrl = dashboardPage.getCurrentUrl();
        assertFalse(currentUrl.contains("/dashboard"), "Expected login page after logout but not redirected - Still on dashboard: " + currentUrl);
        assertTrue(loginPage.isLoginButtonVisible(), "Expected login page after logout but not redirected - Login button missing");
    }
}
