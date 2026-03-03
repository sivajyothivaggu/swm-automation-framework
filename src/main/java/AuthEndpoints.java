package com.swm.api.endpoints;

import io.restassured.response.Response;
import com.swm.api.client.RestClient;
import com.swm.core.base.BaseAPI;

public class AuthEndpoints extends BaseAPI {
    private RestClient client = new RestClient();
    
    public Response login(Object payload) {
        return client.post("/auth/login", payload, getRequestSpec());
    }
    
    public Response logout() {
        return client.post("/auth/logout", null, getRequestSpec());
    }
}
