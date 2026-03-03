package com.swm.api.validators;

import io.restassured.response.Response;
import static org.testng.Assert.*;

public class ResponseValidator {
    
    public static void validateStatusCode(Response response, int expectedCode) {
        assertEquals(response.getStatusCode(), expectedCode);
    }
    
    public static void validateResponseTime(Response response, long maxTime) {
        assertTrue(response.getTime() < maxTime, "Response time exceeded");
    }
    
    public static void validateField(Response response, String field, Object expectedValue) {
        assertEquals(response.jsonPath().get(field), expectedValue);
    }
}
