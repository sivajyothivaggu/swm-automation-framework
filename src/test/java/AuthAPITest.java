package com.swm.tests.api;

import org.testng.annotations.Test;
import io.restassured.response.Response;
import com.swm.api.endpoints.AuthEndpoints;
import com.swm.api.payloads.AuthPayload;
import com.swm.api.validators.ResponseValidator;

public class AuthAPITest {
    private AuthEndpoints authEndpoints = new AuthEndpoints();
    
    @Test
    public void testLogin() {
        AuthPayload payload = new AuthPayload("admin", "password");
        Response response = authEndpoints.login(payload);
        ResponseValidator.validateStatusCode(response, 200);
    }
    
    @Test
    public void testLogout() {
        Response response = authEndpoints.logout();
        ResponseValidator.validateStatusCode(response, 200);
    }
}
