package io.github.yuokada;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class GreetingTest {

    @Test
    public void testCallback() {
        String requestBody = "\"Hello World 123\"";
        RestAssured
            .given()
            .contentType(ContentType.JSON)
            //.header("X-Line-Signature", "foo bar")
            .body(requestBody)
            .when().post("/callback")
            .then()
            .contentType("application/json")
            .body(containsString("X-Line-Signature header is required"));
    }

    @Test
    public void testCallbackWithEmptyHeader() {
        String requestBody = "\"Hello World 123\"";
        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("X-Line-Signature", "")
            .body(requestBody)
            .when().post("/callback")
            .then()
            .contentType("application/json")
            .body(containsString("X-Line-Signature header is empty"));
    }

    @Test
    public void testCallbackAdd() {
        String requestBody = "{\"left\": 10, \"right\": 100}";
        RestAssured
            .given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .when().post("/callback/add")
            .then()
            .contentType("application/json")
            .body(containsString("\"result\":110"));
    }

    @Test
    public void testCallbackAddNegative() {
        String requestBody = "{\"left\": -5, \"right\": 3}";
        RestAssured
            .given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .when().post("/callback/add")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body(containsString("\"result\":-2"));
    }

    @Test
    public void testCallbackAddZero() {
        String requestBody = "{\"left\": 0, \"right\": 0}";
        RestAssured
            .given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .when().post("/callback/add")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body(containsString("\"result\":0"));
    }

    @Test
    public void testXSuperHeaderBlocked() {
        // Filter priority 200 (signature check) runs before priority 100 (X-Super-Header check).
        // X-Line-Signature must be non-empty to pass filter 200; then filter 100 sees
        // X-Super-Header in the request and stops propagation with 400.
        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("X-Line-Signature", "non-empty-signature")
            .header("X-Super-Header", "test-value")
            .body("\"test\"")
            .when().post("/callback")
            .then()
            .statusCode(400)
            .body(containsString("Stop propagation"));
    }

    @Disabled
    @Test
    public void testVertx() {
        RestAssured.when().get("/vertx/hello").then()
            .contentType("text/plain")
            .body(equalTo("hello vertx"));
    }
}
