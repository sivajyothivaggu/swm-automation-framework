package com.swm.tests.ui.transport.vehiclemaster;

import org.testng.annotations.Test;
import com.swm.core.base.BaseTest;
import com.swm.ui.pages.auth.LoginPage;
import com.swm.ui.pages.dashboard.DashboardPage;
import com.swm.ui.pages.transport.TransportPage;
import com.swm.ui.pages.transport.vehiclemaster.VehicleMasterManagementPage;
import com.swm.core.driver.DriverManager;

public class VehicleMasterManagementTest extends BaseTest {
    
    @Test
    public void testAddVehicleMaster() throws InterruptedException {
        // Login
        LoginPage loginPage = new LoginPage();
        loginPage.login("swmadmin", "Admin@123");
        
        // Wait for dashboard
        DashboardPage dashboardPage = new DashboardPage();
        dashboardPage.waitForDashboardLoad();
        
        // Navigate to Transport
        TransportPage transportPage = new TransportPage();
        transportPage.clickTransportModule();
        
        Thread.sleep(2000);
        
        // Refresh to load data
        DriverManager.getDriver().navigate().refresh();
        
        Thread.sleep(2000);
        
        // Click Add Master
        VehicleMasterManagementPage page = new VehicleMasterManagementPage();
        page.clickAddMaster();
    }
}
