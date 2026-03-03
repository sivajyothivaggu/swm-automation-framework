package com.swm.api.validators;

import io.restassured.response.Response;
import io.restassured.module.jsv.JsonSchemaValidator;
import java.io.File;

public class SchemaValidator {
    
    public static void validateSchema(Response response, String schemaPath) {
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(new File(schemaPath)));
    }
}
