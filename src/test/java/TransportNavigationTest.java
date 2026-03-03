package com.swm.tests.ui.transport;

import org.testng.annotations.Test;
import com.swm.core.base.BaseTest;
import com.swm.ui.pages.auth.LoginPage;
import com.swm.ui.pages.dashboard.DashboardPage;
import com.swm.ui.pages.transport.TransportPage;
import com.swm.core.driver.DriverManager;

public class TransportNavigationTest extends BaseTest {
    
    @Test
    public void testNavigateToTransport() throws InterruptedException {
        // Login
        LoginPage loginPage = new LoginPage();
        loginPage.login("swmadmin", "Admin@123");
        
        // Wait for dashboard
        DashboardPage dashboardPage = new DashboardPage();
        dashboardPage.waitForDashboardLoad();
        
        // Additional wait to ensure page is fully loaded
        Thread.sleep(2000);
        
        // Click on Transport module
        TransportPage transportPage = new TransportPage();
        transportPage.clickTransportModule();
        
        // Wait to see Transport page
        Thread.sleep(3000);
        
        // Refresh the page
        DriverManager.getDriver().navigate().refresh();
    }
}
