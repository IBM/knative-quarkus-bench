package com.ibm.trl.funqy.cloudevent;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class FunqyTest {

    @Test
    public void testCloudEvent1() {
        RestAssured.given().contentType("application/json")
                .header("ce-specversion", "1.0")
                .header("ce-id", UUID.randomUUID().toString())
                .header("ce-type", "cloudeventbenchmark")
                .header("ce-source", "test")
                .body("{ \"name\": \"test\" }")
                .post("/")
                .then().statusCode(200);
    }

    @Test
    public void testCloudEventJS() {
        RestAssured.given().contentType("application/json")
                .header("ce-specversion", "1.0")
                .header("ce-id", UUID.randomUUID().toString())
                .header("ce-type", "cloudeventbenchmark")
                .header("ce-source", "jsmallcompresss")
                .body("{ \"name\": \"jsmallcompress\" }")
                .post("/")
                .then().statusCode(200);
    }

    @Test
    public void testCloudEventJ110() {
        RestAssured.given().contentType("application/json")
                .header("ce-specversion", "1.0")
                .header("ce-id", UUID.randomUUID().toString())
                .header("ce-type", "cloudeventbenchmark")
                .header("ce-source", "j110")
                .body("{ \"name\": \"j110\" }")
                .post("/")
                .then().statusCode(200);
    }

    public void testCloudEventJM() {
        RestAssured.given().contentType("application/json")
                .header("ce-specversion", "1.0")
                .header("ce-id", UUID.randomUUID().toString())
                .header("ce-type", "cloudeventbenchmark")
                .header("ce-source", "jmediumcompresss")
                .body("{ \"name\": \"jmediumcompress\" }")
                .post("/")
                .then().statusCode(200);
    }

    @Test
    public void testCloudEventPS() {
        RestAssured.given().contentType("application/json")
                .header("ce-specversion", "1.0")
                .header("ce-id", UUID.randomUUID().toString())
                .header("ce-type", "cloudeventbenchmark")
                .header("ce-source", "psmallcompresss")
                .body("{ \"name\": \"psmallcompress\" }")
                .post("/")
                .then().statusCode(200);
    }

    public void testCloudEventPM() {
        RestAssured.given().contentType("application/json")
                .header("ce-specversion", "1.0")
                .header("ce-id", UUID.randomUUID().toString())
                .header("ce-type", "cloudeventbenchmark")
                .header("ce-source", "pmediumcompresss")
                .body("{ \"name\": \"pmediumcompress\" }")
                .post("/")
                .then().statusCode(200);
    }

}
