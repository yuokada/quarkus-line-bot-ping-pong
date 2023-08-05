package io.github.yuokada;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class GreetingTest {

    @Test
    public void testJaxrs() {
        RestAssured.when().get("/hello").then()
            .contentType("text/plain")
            .header("X-Super-Header", "intercepting the request")
            .body(equalTo("hello jaxrs"));
    }

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
            .body(equalTo("X-Line-Signature does not exist."));
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
    public void testVertx() {
        RestAssured.when().get("/vertx/hello").then()
            .contentType("text/plain")
            .body(equalTo("hello vertx"));
    }
}
