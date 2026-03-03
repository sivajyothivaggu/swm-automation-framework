package com.swm.tests.integration;

import org.testng.annotations.Test;
import io.restassured.response.Response;
import com.swm.core.base.BaseTest;
import com.swm.api.endpoints.RouteEndpoints;
import com.swm.api.payloads.RoutePayload;
import com.swm.ui.pages.transport.route.RouteManagementPage;
import com.swm.database.RouteQueries;
import static org.testng.Assert.*;

public class RouteEndToEndTest extends BaseTest {
    private RouteEndpoints routeEndpoints = new RouteEndpoints();
    private RouteQueries routeQueries = new RouteQueries();
    
    @Test
    public void testRouteEndToEnd() throws Exception {
        // Create via API
        RoutePayload payload = new RoutePayload("Route 999", "Start", "End");
        Response response = routeEndpoints.createRoute(payload);
        assertEquals(response.getStatusCode(), 201);
        
        // Verify in UI
        RouteManagementPage page = new RouteManagementPage();
        page.enterRouteName("Route 999");
        
        // Verify in DB
        assertNotNull(routeQueries.getRouteById("Route 999"));
    }
}
