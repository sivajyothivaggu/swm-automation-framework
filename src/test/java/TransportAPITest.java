package com.swm.tests.api;

import org.testng.annotations.Test;
import io.restassured.response.Response;
import com.swm.api.endpoints.TransportEndpoints;
import com.swm.api.validators.ResponseValidator;

public class TransportAPITest {
    private TransportEndpoints transportEndpoints = new TransportEndpoints();
    
    @Test
    public void testGetTransportData() {
        Response response = transportEndpoints.getTransportData();
        ResponseValidator.validateStatusCode(response, 200);
    }
}
