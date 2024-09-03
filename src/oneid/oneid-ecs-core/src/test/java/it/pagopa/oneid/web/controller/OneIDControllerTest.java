package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.service.OIDCServiceImpl;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class OneIDControllerTest {

  @Inject
  OneIDController oneIDController;
  @InjectMock
  private OIDCServiceImpl oidcServiceImpl;


  @Test
  void ping() {
    String response =
        given()
            .when().get("/ping")
            .then()
            .statusCode(200)
            .body(is("Ping"))
            .extract()
            .asString();
    assertNotNull(response);

  }

  @Test
  void openIDConfig() {
    OIDCProviderMetadata oidcProviderMetadata = Mockito.mock(OIDCProviderMetadata.class);
    Mockito.when(oidcServiceImpl.buildOIDCProviderMetadata()).thenReturn(oidcProviderMetadata);

    String response =
        given()
            .when().get("/.well-known/openid-configuration")
            .then()
            .statusCode(200)
            .extract()
            .asString();
    assertNotNull(response);

  }
}