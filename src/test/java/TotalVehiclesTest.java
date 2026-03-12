package com.swm.tests.ui.transport.VehicleManagement.TotalVehicles;

import com.swm.core.base.BaseTest;
import com.swm.core.driver.DriverManager;
import com.swm.ui.pages.auth.LoginPage;
import com.swm.ui.pages.transport.TransportPage;
import com.swm.ui.pages.transport.VehicleManagement.TotalVehicles.TotalVehiclesPage;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.testng.Assert.assertTrue;

/**
 * Test class for Total Vehicles functionality.
 *
 * <p>This class handles navigation to the Total Vehicles page and provides tests that operate on that page.
 * It includes robust error handling and logging and uses best practices for readability and maintainability.</p>
 */
public class TotalVehiclesTest extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TotalVehiclesTest.class);

    private static final String SCREENSHOT_DIR = "target/screenshots";
    private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss_SSS";

    private TotalVehiclesPage totalVehiclesPage;

    /**
     * Navigates the application to the Total Vehicles page. Uses the application's UI flows:
     * login -> transport module -> vehicle management -> total vehicles.
     *
     * Any unexpected exceptions are logged and rethrown as RuntimeException to fail the setup.
     */
    @BeforeMethod
    public void navigateToTotalVehicles() {
        try {
            LOGGER.info("Starting navigation to Total Vehicles page");

            LoginPage loginPage = new LoginPage();
            Objects.requireNonNull(loginPage, "LoginPage instance cannot be null");
            // Preserve original timing behavior but handle InterruptedException appropriately
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Thread was interrupted during initial wait", ie);
            }

            loginPage.login("swmadmin", "Admin@123");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Thread was interrupted after login wait", ie);
            }

            TransportPage transportPage = new TransportPage();
            Objects.requireNonNull(transportPage, "TransportPage instance cannot be null");
            transportPage.clickTransportModule();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Thread was interrupted after clicking transport module", ie);
            }

            totalVehiclesPage = new TotalVehiclesPage();
            Objects.requireNonNull(totalVehiclesPage, "TotalVehiclesPage instance cannot be null");
            totalVehiclesPage.navigateToVehicleManagement();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Thread was interrupted after navigating to vehicle management", ie);
            }

            totalVehiclesPage.navigateToTotalVehicles();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Thread was interrupted after navigating to total vehicles", ie);
            }

            assertTrue(totalVehiclesPage.isTotalVehiclesPageDisplayed(), "Total Vehicles page not loaded");
            LOGGER.info("Successfully navigated to Total Vehicles page");
        } catch (RuntimeException rte) {
            LOGGER.error("Failed to navigate to Total Vehicles page", rte);
            // Optionally capture a screenshot to aid debugging
            try {
                saveScreenshot("navigateToTotalVehicles_failure");
            } catch (Exception e) {
                LOGGER.warn("Failed to capture screenshot after navigation failure", e);
            }
            throw rte;
        } catch (Exception e) {
            LOGGER.error("Unexpected error during navigation to Total Vehicles page", e);
            try {
                saveScreenshot("navigateToTotalVehicles_unexpected_error");
            } catch (Exception ex) {
                LOGGER.warn("Failed to capture screenshot after unexpected navigation error", ex);
            }
            throw new RuntimeException("Navigation to Total Vehicles failed", e);
        }
    }

    /**
     * Verifies that searching by a vehicle number returns at least one result and that the expected vehicle number is present.
     * Any failure will be logged and a screenshot will be attempted.
     */
    @Test(priority = 1)
    public void testSearchByVehicleNumber() {
        final String expectedVehicleNumber = "AP36AATB2189";
        try {
            LOGGER.info("Starting test: testSearchByVehicleNumber with search text '{}'", expectedVehicleNumber);

            if (Objects.isNull(totalVehiclesPage)) {
                throw new IllegalStateException("TotalVehiclesPage is not initialized. Ensure navigateToTotalVehicles() completed successfully.");
            }

            totalVehiclesPage.enterSearchText(expectedVehicleNumber);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Thread was interrupted during wait after entering search text", ie);
            }

            int rowCount = totalVehiclesPage.getTableRowCount();
            LOGGER.debug("Search returned {} rows for vehicle number '{}'", rowCount, expectedVehicleNumber);
            assertTrue(rowCount > 0, "No results found for vehicle number");

            List<String> vehicleNumbers = totalVehiclesPage.getColumnValues(1);
            Objects.requireNonNull(vehicleNumbers, "Vehicle numbers list returned by page object cannot be null");

            boolean found = vehicleNumbers.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(v -> v.contains(expectedVehicleNumber));

            LOGGER.info("Vehicle number '{}' present in results: {}", expectedVehicleNumber, found);
            assertTrue(found, "Expected vehicle number not present in the results");
        } catch (AssertionError ae) {
            LOGGER.error("Assertion failed in testSearchByVehicleNumber", ae);
            saveScreenshot("testSearchByVehicleNumber_assertion_failure").ifPresent(file ->
                    LOGGER.info("Saved failure screenshot to {}", file.getAbsolutePath()));
            throw ae;
        } catch (Exception e) {
            LOGGER.error("Unexpected error in testSearchByVehicleNumber", e);
            saveScreenshot("testSearchByVehicleNumber_unexpected_error").ifPresent(file ->
                    LOGGER.info("Saved unexpected error screenshot to {}", file.getAbsolutePath()));
            throw new RuntimeException("testSearchByVehicleNumber failed unexpectedly", e);
        }
    }

    /**
     * Captures a screenshot using the current WebDriver instance and saves it to the configured screenshot directory.
     *
     * @param namePrefix prefix to use for the screenshot file name
     * @return Optional<File> pointing to the saved screenshot file or Optional.empty() on failure
     */
    private Optional<File> saveScreenshot(String namePrefix) {
        if (Objects.isNull(namePrefix) || namePrefix.trim().isEmpty()) {
            namePrefix = "screenshot";
        }

        try {
            if (Objects.isNull(DriverManager.getDriver())) {
                LOGGER.warn("WebDriver instance is null. Skipping screenshot capture.");
                return Optional.empty();
            }

            if (!(DriverManager.getDriver() instanceof TakesScreenshot)) {
                LOGGER.warn("WebDriver does not support screenshots. Skipping screenshot capture.");
                return Optional.empty();
            }

            File srcFile = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.FILE);
            if (Objects.isNull(srcFile) || !srcFile.exists()) {
                LOGGER.warn("No screenshot file was created by the driver.");
                return Optional.empty();
            }

            String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
            String fileName = String.format("%s_%s.png", namePrefix.replaceAll("[^a-zA-Z0-9_-]", "_"), timestamp);
            File destDir = new File(SCREENSHOT_DIR);
            if (!destDir.exists() && !destDir.mkdirs()) {
                LOGGER.warn("Failed to create screenshot directory: {}", destDir.getAbsolutePath());
                // Attempt to proceed; File may still be created by parent process (unlikely)
            }
            File destFile = new File(destDir, fileName);

            // Use try-with-resources to copy file content safely
            try (var in = Files.newInputStream(srcFile.toPath());
                 var out = Files.newOutputStream(destFile.toPath())) {
                in.transferTo(out);
            }

            LOGGER.info("Screenshot saved to {}", destFile.getAbsolutePath());
            return Optional.of(destFile);
        } catch (IOException ioe) {
            LOGGER.error("IO error while saving screenshot", ioe);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while saving screenshot", e);
            return Optional.empty();
        }
    }
}