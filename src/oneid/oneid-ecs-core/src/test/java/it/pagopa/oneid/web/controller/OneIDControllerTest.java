package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit5.virtual.ShouldNotPin;
import io.quarkus.test.junit5.virtual.VirtualThreadUnit;
import it.pagopa.oneid.service.OIDCServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@VirtualThreadUnit
@ShouldNotPin
class OneIDControllerTest {

  @InjectMock
  OIDCServiceImpl oidcServiceImpl;


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
