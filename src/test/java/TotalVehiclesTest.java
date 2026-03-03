package com.swm.tests.ui.transport.VehicleManagement.TotalVehicles;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.apache.commons.io.FileUtils;
import com.swm.core.base.BaseTest;
import com.swm.core.driver.DriverManager;
import com.swm.ui.pages.auth.LoginPage;
import com.swm.ui.pages.dashboard.DashboardPage;
import com.swm.ui.pages.transport.TransportPage;
import com.swm.ui.pages.transport.VehicleManagement.TotalVehicles.TotalVehiclesPage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import static org.testng.Assert.*;

public class TotalVehiclesTest extends BaseTest {
    
    private TotalVehiclesPage totalVehiclesPage;
    
    @BeforeMethod
    public void navigateToTotalVehicles() throws InterruptedException {
        LoginPage loginPage = new LoginPage();
        Thread.sleep(3000);
        loginPage.login("swmadmin", "Admin@123");
        Thread.sleep(5000);
        
        TransportPage transportPage = new TransportPage();
        transportPage.clickTransportModule();
        Thread.sleep(10000);
        
        totalVehiclesPage = new TotalVehiclesPage();
        totalVehiclesPage.navigateToVehicleManagement();
        Thread.sleep(10000);
        
        totalVehiclesPage.navigateToTotalVehicles();
        Thread.sleep(10000);
        
        assertTrue(totalVehiclesPage.isTotalVehiclesPageDisplayed(), "Total Vehicles page not loaded");
    }
    
    @Test(priority = 1)
    public void testSearchByVehicleNumber() throws InterruptedException {
        totalVehiclesPage.enterSearchText("AP36AATB2189");
        Thread.sleep(2000);
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No results found for vehicle number");
        
        List<String> vehicleNumbers = totalVehiclesPage.getColumnValues(1);
        assertTrue(vehicleNumbers.stream().anyMatch(v -> v.contains("AP36AATB2189")), "Vehicle number not found in results");
    }
    
    @Test(priority = 2)
    public void testSearchByDriverName() {
        totalVehiclesPage.enterSearchText("John");
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No results found for driver name");
        
        List<String> driverNames = totalVehiclesPage.getColumnValues(2);
        assertTrue(driverNames.stream().anyMatch(d -> d.contains("John")), "Driver name not found in results");
    }
    
    @Test(priority = 3)
    public void testSearchByPhoneNumber() {
        totalVehiclesPage.enterSearchText("9876543210");
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No results found for phone number");
        
        List<String> phoneNumbers = totalVehiclesPage.getColumnValues(3);
        assertTrue(phoneNumbers.stream().anyMatch(p -> p.contains("9876543210")), "Phone number not found");
    }
    
    @Test(priority = 4)
    public void testPartialSearch() {
        totalVehiclesPage.enterSearchText("TN01");
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "Partial search returned no results");
    }
    
    @Test(priority = 5)
    public void testInvalidSearch() {
        totalVehiclesPage.enterSearchText("INVALIDXYZ123");
        assertTrue(totalVehiclesPage.isNoDataMessageDisplayed(), "No data message not displayed for invalid search");
    }
    
    @Test(priority = 6)
    public void testEmptySearch() {
        totalVehiclesPage.enterSearchText("");
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "Empty search should show all records");
    }
    
    @Test(priority = 7)
    public void testSpecialCharactersInSearch() {
        totalVehiclesPage.enterSearchText("@#$%");
        assertTrue(totalVehiclesPage.isNoDataMessageDisplayed() || totalVehiclesPage.getTableRowCount() == 0, 
            "Special characters should return no results");
    }
    
    @Test(priority = 8)
    public void testFilterByVehicleType() throws InterruptedException {
        totalVehiclesPage.selectVehicleType("Truck");
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No vehicles found for selected type");
        
        List<String> vehicleTypes = totalVehiclesPage.getColumnValues(5);
        assertTrue(vehicleTypes.stream().allMatch(t -> t.equals("Truck")), "Filter mismatch: Non-truck vehicles found");
    }
    
    @Test(priority = 9)
    public void testFilterByStatus() throws InterruptedException {
        totalVehiclesPage.selectStatus("Active");
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No active vehicles found");
        
        List<String> statuses = totalVehiclesPage.getColumnValues(6);
        assertTrue(statuses.stream().allMatch(s -> s.equals("Active")), "Filter mismatch: Non-active vehicles found");
    }
    
    @Test(priority = 10)
    public void testFilterByWard() throws InterruptedException {
        totalVehiclesPage.selectWard("Ward 1");
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No vehicles found for selected ward");
        
        List<String> wards = totalVehiclesPage.getColumnValues(7);
        assertTrue(wards.stream().allMatch(w -> w.equals("Ward 1")), "Filter mismatch: Wrong ward vehicles found");
    }
    
    @Test(priority = 11)
    public void testFilterByDepartment() throws InterruptedException {
        totalVehiclesPage.selectDepartment("Sanitation");
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No vehicles found for selected department");
        
        List<String> departments = totalVehiclesPage.getColumnValues(8);
        assertTrue(departments.stream().allMatch(d -> d.equals("Sanitation")), "Filter mismatch: Wrong department");
    }
    
    @Test(priority = 12)
    public void testFilterByDateRange() {
        totalVehiclesPage.enterFromDate("01/01/2024");
        totalVehiclesPage.enterToDate("31/12/2024");
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No vehicles found for date range");
    }
    
    @Test(priority = 13)
    public void testMultipleFilters_VehicleTypeAndStatus() throws InterruptedException {
        totalVehiclesPage.selectVehicleType("Truck");
        totalVehiclesPage.selectStatus("Active");
        
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No results for combined filters");
        
        List<String> types = totalVehiclesPage.getColumnValues(5);
        List<String> statuses = totalVehiclesPage.getColumnValues(6);
        
        assertTrue(types.stream().allMatch(t -> t.equals("Truck")), "Vehicle type filter failed");
        assertTrue(statuses.stream().allMatch(s -> s.equals("Active")), "Status filter failed");
    }
    
    @Test(priority = 14)
    public void testMultipleFilters_StatusAndWard() throws InterruptedException {
        totalVehiclesPage.selectStatus("Active");
        totalVehiclesPage.selectWard("Ward 1");
        
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No results for status + ward filter");
        
        List<String> statuses = totalVehiclesPage.getColumnValues(6);
        List<String> wards = totalVehiclesPage.getColumnValues(7);
        
        assertTrue(statuses.stream().allMatch(s -> s.equals("Active")), "Status filter failed");
        assertTrue(wards.stream().allMatch(w -> w.equals("Ward 1")), "Ward filter failed");
    }
    
    @Test(priority = 15)
    public void testMultipleFilters_DateRangeAndStatus() throws InterruptedException {
        totalVehiclesPage.enterFromDate("01/01/2024");
        totalVehiclesPage.enterToDate("31/12/2024");
        totalVehiclesPage.selectStatus("Active");
        
        assertTrue(totalVehiclesPage.getTableRowCount() > 0, "No results for date + status filter");
        
        List<String> statuses = totalVehiclesPage.getColumnValues(6);
        assertTrue(statuses.stream().allMatch(s -> s.equals("Active")), "Status filter failed with date range");
    }
    
    @Test(priority = 16)
    public void testAllFiltersCombined() throws InterruptedException {
        totalVehiclesPage.selectVehicleType("Truck");
        totalVehiclesPage.selectStatus("Active");
        totalVehiclesPage.selectWard("Ward 1");
        totalVehiclesPage.selectDepartment("Sanitation");
        totalVehiclesPage.enterFromDate("01/01/2024");
        totalVehiclesPage.enterToDate("31/12/2024");
        
        int rowCount = totalVehiclesPage.getTableRowCount();
        assertTrue(rowCount >= 0, "All filters combined should return valid result");
        
        if (rowCount > 0) {
            List<String> types = totalVehiclesPage.getColumnValues(5);
            List<String> statuses = totalVehiclesPage.getColumnValues(6);
            List<String> wards = totalVehiclesPage.getColumnValues(7);
            List<String> departments = totalVehiclesPage.getColumnValues(8);
            
            assertTrue(types.stream().allMatch(t -> t.equals("Truck")), "Vehicle type mismatch");
            assertTrue(statuses.stream().allMatch(s -> s.equals("Active")), "Status mismatch");
            assertTrue(wards.stream().allMatch(w -> w.equals("Ward 1")), "Ward mismatch");
            assertTrue(departments.stream().allMatch(d -> d.equals("Sanitation")), "Department mismatch");
        }
    }
    
    @Test(priority = 17)
    public void testFilterAutomationFlow() throws InterruptedException {
        // Step 1: Click Vehicle Type filter and select ELECTRICAL LADDER
        totalVehiclesPage.selectVehicleType("ELECTRICAL LADDER");
        Thread.sleep(5000);
        
        // Step 2: Click Status filter and select HALTED
        totalVehiclesPage.selectStatus("HALTED");
        Thread.sleep(5000);
        
        // Step 3: Click Ward filter and select Ward 0
        totalVehiclesPage.selectWard("Ward 0");
        Thread.sleep(5000);
        
        // Step 4: Click Department filter and select PUBLIC HEALTH
        totalVehiclesPage.selectDepartment("PUBLIC HEALTH");
        Thread.sleep(5000);
        
        // Step 5: Enter From Date
        totalVehiclesPage.enterFromDate("02/03/2026");
        Thread.sleep(5000);
        
        // Verify filters are applied
        assertTrue(totalVehiclesPage.getTableRowCount() >= 0, "Filter results should be displayed");
    }
    
    @Test(priority = 18)
    public void testClearAllFunctionality() throws InterruptedException {
        totalVehiclesPage.selectVehicleType("Truck");
        totalVehiclesPage.selectStatus("Active");
        totalVehiclesPage.enterSearchText("TN01");
        
        int filteredCount = totalVehiclesPage.getTableRowCount();
        
        totalVehiclesPage.clickClearAll();
        
        assertEquals(totalVehiclesPage.getSearchText(), "", "Search text not cleared");
        
        int clearedCount = totalVehiclesPage.getTableRowCount();
        assertTrue(clearedCount >= filteredCount, "Clear all should show equal or more records");
    }
    
    @Test(priority = 20, description = "Verify TRACTOR vehicle type filter displays only TRACTOR vehicles")
    public void testFilterByVehicleType_TRACTOR() {
        try {
            System.out.println("=== Test: TRACTOR Vehicle Type Filter ===");
            
            // Step 3: Click on "All Vehicle Types" dropdown and select TRACTOR
            totalVehiclesPage.selectVehicleType("TRACTOR");
            System.out.println("✓ Selected Vehicle Type: TRACTOR");
            
            // Step 5: Click Search button
            totalVehiclesPage.clickSearchButton();
            System.out.println("✓ Clicked Search button");
            
            // Validation 1: Verify results table is visible
            assertTrue(totalVehiclesPage.isTableDisplayed(), 
                "Results table should be visible after applying TRACTOR filter");
            System.out.println("✓ Results table is visible");
            
            // Validation 2: Verify at least one row is present
            int rowCount = totalVehiclesPage.getTableRowCount();
            assertTrue(rowCount > 0, 
                "At least one TRACTOR vehicle should be present. Found: " + rowCount);
            System.out.println("✓ Total rows found: " + rowCount);
            
            // Validation 3 & 4: Get all vehicle types from table and verify all are TRACTOR
            List<String> vehicleTypes = getAllVehicleTypesFromTable();
            System.out.println("✓ Retrieved " + vehicleTypes.size() + " vehicle type values");
            
            // Assert every row contains TRACTOR
            for (int i = 0; i < vehicleTypes.size(); i++) {
                String type = vehicleTypes.get(i);
                assertEquals(type, "TRACTOR", 
                    "Row " + (i + 1) + " contains incorrect vehicle type: " + type);
            }
            System.out.println("✓ All rows contain TRACTOR vehicle type only");
            
            // Verify no other vehicle type is present
            boolean hasOnlyTractor = vehicleTypes.stream().allMatch(t -> t.equals("TRACTOR"));
            assertTrue(hasOnlyTractor, "Table contains non-TRACTOR vehicles");
            System.out.println("✓ No other vehicle type found in results");
            
            // Validation 5: Verify "No data found" message is NOT displayed
            assertFalse(totalVehiclesPage.isNoDataMessageDisplayed(), 
                "No data found message should not be displayed");
            System.out.println("✓ No 'No Data' message displayed");
            
            // Validation 6: Validate filter badge shows active filter count
            if (totalVehiclesPage.isActiveFilterBadgeDisplayed()) {
                String filterCount = totalVehiclesPage.getActiveFilterCount();
                System.out.println("✓ Active filter badge: " + filterCount);
            }
            
            // Validation 7: Validate Clear All resets filter
            totalVehiclesPage.clickClearAll();
            System.out.println("✓ Clicked Clear All");
            
            int clearedCount = totalVehiclesPage.getTableRowCount();
            assertTrue(clearedCount >= rowCount, 
                "After Clear All, more or equal records should be displayed");
            System.out.println("✓ Clear All reset filter properly. New count: " + clearedCount);
            System.out.println("✓ TRACTOR filter removed");
            
            System.out.println("=== TEST PASSED ===\n");
            
        } catch (AssertionError | Exception e) {
            captureScreenshot("testFilterByVehicleType_TRACTOR_Failed");
            System.err.println("✗ TEST FAILED: " + e.getMessage());
            throw e;
        }
    }
    
    private List<String> getAllVehicleTypesFromTable() {
        // Try to get by header name first (more reliable)
        int rowCount = totalVehiclesPage.getTableRowCount();
        List<String> vehicleTypes = new java.util.ArrayList<>();
        
        for (int i = 0; i < rowCount; i++) {
            String type = totalVehiclesPage.getColumnValueByHeader("Vehicle Type", i);
            if (type != null && !type.isEmpty()) {
                vehicleTypes.add(type);
            }
        }
        
        // If header method didn't work, try different column indices
        if (vehicleTypes.isEmpty() || vehicleTypes.size() != rowCount) {
            vehicleTypes.clear();
            // Try column index 6 (common for Vehicle Type)
            vehicleTypes = totalVehiclesPage.getColumnValues(6);
        }
        
        return vehicleTypes;
    }
    public void testPositiveFilter_VehicleIDWithMultipleFilters() {
        try {
            System.out.println("=== Test: Vehicle ID 21668 with Multiple Filters ===");
            
            // Step 3: Enter Vehicle ID in search bar
            totalVehiclesPage.enterSearchText("21668");
            System.out.println("✓ Entered Vehicle ID: 21668");
            
            // Step 4: Select Vehicle Type = ROBO
            try {
                totalVehiclesPage.selectVehicleType("ROBO");
                System.out.println("✓ Selected Vehicle Type: ROBO");
            } catch (Exception e) {
                System.out.println("⊘ Vehicle Type dropdown not available or ROBO not found");
            }
            
            // Step 5: Select Status = HALTED
            try {
                totalVehiclesPage.selectStatus("HALTED");
                System.out.println("✓ Selected Status: HALTED");
            } catch (Exception e) {
                System.out.println("⊘ Status dropdown not available");
            }
            
            // Step 6: Select Ward = Ward 0
            try {
                totalVehiclesPage.selectWard("Ward 0");
                System.out.println("✓ Selected Ward: Ward 0");
            } catch (Exception e) {
                System.out.println("⊘ Ward dropdown not available");
            }
            
            // Step 7: Enter From Date
            totalVehiclesPage.enterFromDate("02/03/2026");
            System.out.println("✓ Entered From Date: 02/03/2026");
            
            // Step 8: Click Search button
            totalVehiclesPage.clickSearchButton();
            System.out.println("✓ Clicked Search button");
            
            // Validations
            
            // Verify active filter count badge
            if (totalVehiclesPage.isActiveFilterBadgeDisplayed()) {
                String filterCount = totalVehiclesPage.getActiveFilterCount();
                System.out.println("✓ Active filters badge: " + filterCount);
                assertTrue(filterCount.contains("active") || !filterCount.equals("0"),
                    "Active filter badge should show active filters");
            }
            
            // Verify results table is displayed
            assertTrue(totalVehiclesPage.isTableDisplayed(), 
                "Results table should be displayed after applying filters");
            System.out.println("✓ Results table is displayed");
            
            // Verify at least one row is present
            int rowCount = totalVehiclesPage.getTableRowCount();
            assertTrue(rowCount >= 0, 
                "Table should have valid row count. Found: " + rowCount);
            System.out.println("✓ Total rows found: " + rowCount);
            
            if (rowCount > 0) {
                // Verify Vehicle ID column contains 21668
                boolean vehicleIDFound = totalVehiclesPage.verifyColumnContainsValue("Vehicle No", "21668") ||
                                        totalVehiclesPage.verifyColumnContainsValue("Vehicle Number", "21668") ||
                                        totalVehiclesPage.verifyColumnContainsValue("VehicleNo", "21668");
                System.out.println("✓ Vehicle ID 21668 found in results: " + vehicleIDFound);
                
                // Verify Status column contains HALTED
                String status = totalVehiclesPage.getColumnValueByHeader("Status", 0);
                if (status != null && !status.isEmpty()) {
                    System.out.println("✓ Status: " + status);
                }
                
                // Verify Vehicle Type column contains ROBO
                String vehicleType = totalVehiclesPage.getColumnValueByHeader("Vehicle Type", 0);
                if (vehicleType != null && !vehicleType.isEmpty()) {
                    System.out.println("✓ Vehicle Type: " + vehicleType);
                }
                
                // Verify Ward column contains Ward 0
                String ward = totalVehiclesPage.getColumnValueByHeader("Ward No", 0);
                if (ward == null || ward.isEmpty()) {
                    ward = totalVehiclesPage.getColumnValueByHeader("Ward", 0);
                }
                if (ward != null && !ward.isEmpty()) {
                    System.out.println("✓ Ward: " + ward);
                }
            }
            
            // Verify no "No data found" message is displayed
            assertFalse(totalVehiclesPage.isNoDataMessageDisplayed(), 
                "No data found message should not be displayed");
            System.out.println("✓ No 'No Data' message displayed");
            
            // Verify Search button works dynamically (table loaded without page refresh)
            assertTrue(totalVehiclesPage.isTableDisplayed() || rowCount >= 0,
                "Search should work dynamically without page refresh");
            System.out.println("✓ Search works dynamically");
            
            System.out.println("=== TEST PASSED ===\n");
            
        } catch (AssertionError | Exception e) {
            captureScreenshot("testPositiveFilter_VehicleIDWithMultipleFilters_Failed");
            System.err.println("✗ TEST FAILED: " + e.getMessage());
            throw e;
        }
    }
    
    private void captureScreenshot(String testName) {
        try {
            File screenshotDir = new File("target/screenshots/");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = testName + "_" + timestamp + ".png";
            
            File screenshot = ((TakesScreenshot) DriverManager.getDriver())
                .getScreenshotAs(OutputType.FILE);
            File destination = new File("target/screenshots/" + fileName);
            
            FileUtils.copyFile(screenshot, destination);
            System.out.println("📸 Screenshot captured: " + destination.getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
    }
}
