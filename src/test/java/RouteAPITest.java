package com.swm.tests.api;

import org.testng.annotations.Test;
import io.restassured.response.Response;
import com.swm.api.endpoints.RouteEndpoints;
import com.swm.api.payloads.RoutePayload;
import com.swm.api.validators.ResponseValidator;

public class RouteAPITest {
    private RouteEndpoints routeEndpoints = new RouteEndpoints();
    
    @Test
    public void testGetAllRoutes() {
        Response response = routeEndpoints.getAllRoutes();
        ResponseValidator.validateStatusCode(response, 200);
    }
    
    @Test
    public void testCreateRoute() {
        RoutePayload payload = new RoutePayload("Route 1", "Point A", "Point B");
        Response response = routeEndpoints.createRoute(payload);
        ResponseValidator.validateStatusCode(response, 201);
    }
}
