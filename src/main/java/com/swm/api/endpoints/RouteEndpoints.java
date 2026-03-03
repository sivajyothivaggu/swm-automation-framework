package com.swm.api.endpoints;

import io.restassured.response.Response;
import com.swm.api.client.RestClient;
import com.swm.core.base.BaseAPI;

public class RouteEndpoints extends BaseAPI {
    private RestClient client = new RestClient();
    
    public Response getAllRoutes() {
        return client.get("/routes", getRequestSpec());
    }
    
    public Response createRoute(Object payload) {
        return client.post("/routes", payload, getRequestSpec());
    }
    
    public Response deleteRoute(String id) {
        return client.delete("/routes/" + id, getRequestSpec());
    }
}
