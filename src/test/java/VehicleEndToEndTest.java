package com.swm.tests.integration;

import org.testng.annotations.Test;
import io.restassured.response.Response;
import com.swm.core.base.BaseTest;
import com.swm.api.endpoints.VehicleEndpoints;
import com.swm.api.payloads.VehiclePayload;
import com.swm.database.VehicleQueries;
import com.swm.ui.pages.transport.VehicleManagement.VehicleManagementPage;

import static org.testng.Assert.*;

public class VehicleEndToEndTest extends BaseTest {
    private VehicleEndpoints vehicleEndpoints = new VehicleEndpoints();
    private VehicleQueries vehicleQueries = new VehicleQueries();
    
    @Test
    public void testVehicleEndToEnd() throws Exception {
        // Create via API
        VehiclePayload payload = new VehiclePayload("VEH999", "Bus", 50);
        Response response = vehicleEndpoints.createVehicle(payload);
        assertEquals(response.getStatusCode(), 201);
        
        // Verify in UI
        VehicleManagementPage page = new VehicleManagementPage();
        page.searchVehicle("VEH999");
        
        // Verify in DB
        assertNotNull(vehicleQueries.getVehicleById("VEH999"));
    }
}
