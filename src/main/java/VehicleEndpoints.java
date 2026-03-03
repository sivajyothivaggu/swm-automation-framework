package com.swm.api.endpoints;

import io.restassured.response.Response;
import com.swm.api.client.RestClient;
import com.swm.core.base.BaseAPI;

public class VehicleEndpoints extends BaseAPI {
    private RestClient client = new RestClient();
    
    public Response getAllVehicles() {
        return client.get("/vehicles", getRequestSpec());
    }
    
    public Response getVehicleById(String id) {
        return client.get("/vehicles/" + id, getRequestSpec());
    }
    
    public Response createVehicle(Object payload) {
        return client.post("/vehicles", payload, getRequestSpec());
    }
    
    public Response updateVehicle(String id, Object payload) {
        return client.put("/vehicles/" + id, payload, getRequestSpec());
    }
    
    public Response deleteVehicle(String id) {
        return client.delete("/vehicles/" + id, getRequestSpec());
    }
}
