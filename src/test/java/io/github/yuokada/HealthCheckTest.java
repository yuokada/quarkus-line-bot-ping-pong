package io.github.yuokada;

import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class HealthCheckTest {

    @Test
    public void testLivenessIsUp() {
        RestAssured.when().get("/q/health/live")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("checks[0].name", equalTo("Simple health check"))
            .body("checks[0].status", equalTo("UP"));
    }
}
