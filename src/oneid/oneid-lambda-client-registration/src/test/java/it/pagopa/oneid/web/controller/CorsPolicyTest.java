package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CorsPolicyTest {

    @Test
    @DisplayName("An unconfigured cross-origin preflight is rejected")
    void givenUnconfiguredOrigin_whenPreflight_thenForbidden() {
        given()
                .header("Origin", "https://unconfigured.example")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type")
                .when().options("/oidc/register")
                .then().statusCode(403)
                .header("Access-Control-Allow-Origin", nullValue());
    }

    @Test
    @DisplayName("An unconfigured browser origin cannot submit a registration")
    void givenUnconfiguredOrigin_whenRegister_thenForbidden() {
        given()
                .header("Origin", "https://unconfigured.example")
                .contentType("application/json")
                .body("{}")
                .when().post("/oidc/register")
                .then().statusCode(403)
                .header("Access-Control-Allow-Origin", nullValue());
    }

    @Test
    @DisplayName("Registration validation remains active without a browser Origin header")
    void givenInvalidRegistration_whenNoOrigin_thenBadRequest() {
        given()
                .contentType("application/json")
                .body("{}")
                .when().post("/oidc/register")
                .then().statusCode(400)
                .header("Access-Control-Allow-Origin", nullValue());
    }
}
