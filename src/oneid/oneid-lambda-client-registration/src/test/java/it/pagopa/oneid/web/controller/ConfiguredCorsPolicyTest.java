package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(ConfiguredCorsPolicyTest.AllowedOriginProfile.class)
class ConfiguredCorsPolicyTest {

    public static class AllowedOriginProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.http.cors.origins", "http://localhost:5123");
        }
    }

    @Test
    @DisplayName("The configured frontend origin can preflight registration")
    void givenConfiguredOrigin_whenPreflight_thenAllowed() {
        given()
                .header("Origin", "http://localhost:5123")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type,authorization")
                .when().options("/oidc/register")
                .then().statusCode(200)
                .header("Access-Control-Allow-Origin", "http://localhost:5123")
                .header("Access-Control-Allow-Methods", containsString("POST"))
                .header("Access-Control-Allow-Headers", containsString("authorization"));
    }

    @Test
    @DisplayName("A different origin cannot use the configured frontend allowance")
    void givenDifferentOrigin_whenPreflight_thenForbidden() {
        given()
                .header("Origin", "https://unconfigured.example")
                .header("Access-Control-Request-Method", "POST")
                .when().options("/oidc/register")
                .then().statusCode(403)
                .header("Access-Control-Allow-Origin", nullValue());
    }

    @Test
    @DisplayName("A configured origin does not allow an unconfigured HTTP method")
    void givenConfiguredOrigin_whenPatchPreflight_thenPatchNotAllowed() {
        given()
                .header("Origin", "http://localhost:5123")
                .header("Access-Control-Request-Method", "PATCH")
                .when().options("/oidc/register")
                .then().statusCode(200)
                .header("Access-Control-Allow-Methods", not(containsString("PATCH")));
    }
}
