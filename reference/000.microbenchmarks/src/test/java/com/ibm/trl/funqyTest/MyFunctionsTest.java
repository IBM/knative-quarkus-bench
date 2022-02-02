package com.ibm.trl.funqyTest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class MyFunctionsTest {

    // @Test
    // public void testFun() {
    //     given()
    //         .post("/Hello")
    //         .then()
    //         .statusCode(200)
    //         .body(containsString("Hello!"));
    // }

    // @Test
    // public void testFunWithName() {
    //     given()
    //         .contentType(ContentType.JSON)
    //         .body("\"Funqy\"")
    //         .post("/hello")
    //         .then()
    //         .statusCode(200)
    //         .body(containsString("Hello Funqy"));
    // }

}
