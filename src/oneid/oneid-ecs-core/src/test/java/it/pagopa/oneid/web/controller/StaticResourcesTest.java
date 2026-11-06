package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StaticResourcesTest {

  @Test
  @DisplayName("given the sample index URL when requested then preserve the existing static page")
  void given_sampleIndexUrl_when_requested_then_returnsExistingPage() {
    given()
        .when().get("/static/sample_index.html")
        .then()
        .statusCode(200)
        .contentType("text/html")
        .body(containsString("<h1>HTML di esempio</h1>"));
  }

  @Test
  @DisplayName("given the sample error URL when requested then preserve the existing static page")
  void given_sampleErrorUrl_when_requested_then_returnsExistingPage() {
    given()
        .when().get("/static/sample_error.html")
        .then()
        .statusCode(200)
        .contentType("text/html")
        .body(containsString("<h1>Error Page di esempio</h1>"));
  }
}
