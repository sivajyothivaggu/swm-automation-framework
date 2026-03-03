package com.swm.api.client;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RestClient {
    
    public Response get(String endpoint, RequestSpecification spec) {
        return spec.when().get(endpoint);
    }
    
    public Response post(String endpoint, Object body, RequestSpecification spec) {
        return spec.body(body).when().post(endpoint);
    }
    
    public Response put(String endpoint, Object body, RequestSpecification spec) {
        return spec.body(body).when().put(endpoint);
    }
    
    public Response delete(String endpoint, RequestSpecification spec) {
        return spec.when().delete(endpoint);
    }
}
