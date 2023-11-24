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

    @Disabled
    @Test
    public void testVertx() {
        RestAssured.when().get("/vertx/hello").then()
            .contentType("text/plain")
            .body(equalTo("hello vertx"));
    }
}
