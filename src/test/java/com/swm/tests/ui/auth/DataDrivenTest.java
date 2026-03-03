package com.swm.tests.ui.auth;

import org.testng.annotations.Test;
import com.swm.core.base.BaseTest;
import com.swm.ui.pages.auth.LoginPage;
import com.swm.ui.pages.auth.LogoutPopupPage;
import com.swm.ui.pages.dashboard.DashboardPage;
import static org.testng.Assert.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class DataDrivenTest extends BaseTest {
    
    @Test
    public void testLoginWithMultipleCredentials() throws Exception {
        String csvFile = "src/test/resources/testdata/login-invalid.csv";
        LoginPage loginPage = new LoginPage();
        DashboardPage dashboardPage = new DashboardPage();
        LogoutPopupPage logoutPopup = new LogoutPopupPage();
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String username = values[0];
                String password = values[1];
                String expectedResult = values[2];
                
                loginPage.login(username, password);
                Thread.sleep(3000);
                
                if ("valid".equals(expectedResult)) {
                    // Verify successful login
                    String currentUrl = dashboardPage.getCurrentUrl();
                    assertTrue(currentUrl.contains("/dashboard"), 
                        "Valid login failed for: " + username + "/" + password);
                    
                    // Logout
                    dashboardPage.clickProfileIcon();
                    dashboardPage.clickLogoutFromDropdown();
                    logoutPopup.clickLogoutButton();
                    Thread.sleep(2000);
                } else {
                    // Verify login failed
                    assertTrue(loginPage.isErrorMessageDisplayed(), 
                        "Expected error for invalid credentials: " + username + "/" + password);
                    assertTrue(loginPage.isLoginButtonVisible(), 
                        "Should remain on login page after invalid login");
                }
            }
        }
    }
}
