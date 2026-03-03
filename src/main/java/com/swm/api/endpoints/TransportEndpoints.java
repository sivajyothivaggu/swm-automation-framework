package com.swm.api.endpoints;

import io.restassured.response.Response;
import com.swm.api.client.RestClient;
import com.swm.core.base.BaseAPI;

public class TransportEndpoints extends BaseAPI {
    private RestClient client = new RestClient();
    
    public Response getTransportData() {
        return client.get("/transport", getRequestSpec());
    }
}
