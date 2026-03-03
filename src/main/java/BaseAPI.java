package com.swm.core.base;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import com.swm.core.config.ConfigManager;

public class BaseAPI {
    
    protected RequestSpecification getRequestSpec() {
        RestAssured.baseURI = ConfigManager.getApiUrl();
        return RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }
}
