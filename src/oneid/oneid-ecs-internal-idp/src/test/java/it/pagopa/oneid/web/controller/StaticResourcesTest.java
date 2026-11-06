package it.pagopa.oneid.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
class StaticResourcesTest {

  @Inject
  @Location("error.html")
  Template error;

  @TestHTTPResource
  URI baseUri;

  @Test
  @DisplayName("given the error template when rendered then preserve the message and asset URLs")
  void given_errorTemplate_when_rendered_then_preservesMessageAndAssetUrls() {
    String page = error.data("errorMessage", "Regression test message").render();

    assertTrue(page.contains("Regression test message"));
    assertTrue(page.contains("/assets/style/styles.css"));
    assertTrue(page.contains("/assets/js/scripts.js"));
    assertTrue(page.contains("/assets/logo.png"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/assets/style/styles.css", "/assets/js/scripts.js", "/assets/logo.png"})
  @DisplayName("given an existing asset URL when requested then preserve its content")
  void given_assetUrl_when_requested_then_returnsExistingAsset(String path) throws Exception {
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpResponse<byte[]> response = client.send(
          HttpRequest.newBuilder(baseUri.resolve(path)).GET().build(),
          HttpResponse.BodyHandlers.ofByteArray());

      assertEquals(200, response.statusCode());
      assertTrue(response.body().length > 0);
    }
  }
}
