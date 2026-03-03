package com.swm.tests.api;

import org.testng.annotations.Test;
import io.restassured.response.Response;
import com.swm.api.endpoints.VehicleEndpoints;
import com.swm.api.payloads.VehiclePayload;
import com.swm.api.validators.ResponseValidator;

public class VehicleAPITest {
    private VehicleEndpoints vehicleEndpoints = new VehicleEndpoints();
    
    @Test
    public void testGetAllVehicles() {
        Response response = vehicleEndpoints.getAllVehicles();
        ResponseValidator.validateStatusCode(response, 200);
    }
    
    @Test
    public void testCreateVehicle() {
        VehiclePayload payload = new VehiclePayload("VEH001", "Bus", 50);
        Response response = vehicleEndpoints.createVehicle(payload);
        ResponseValidator.validateStatusCode(response, 201);
    }
}
