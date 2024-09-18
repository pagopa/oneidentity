package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.ClientFE;
import it.pagopa.oneid.service.ClientServiceImpl;
import it.pagopa.oneid.service.OIDCServiceImpl;
import jakarta.inject.Inject;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class OneIDControllerTest {

  @Inject
  OneIDController oneIDController;
  @InjectMock
  ClientServiceImpl clientServiceImpl;
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

  @Test
  void findClientByIdTest() {

    //given
    String clientID = "test";
    ClientFE clientMock = Mockito.mock(ClientFE.class);
    Mockito.when(clientServiceImpl.getClientInformation(Mockito.anyString()))
        .thenReturn(Optional.of(clientMock));

    String response =
        given()
            .pathParam("client_id", clientID)
            .when().get("/client/{client_id}")
            .then()
            .statusCode(200)
            .extract()
            .asString();
    assertNotNull(response);

  }

  @Test
  void findClientByIdTest_error() {

    //given
    String clientID = "test";
    Mockito.when(clientServiceImpl.getClientInformation(Mockito.anyString()))
        .thenReturn(Optional.empty());

    String response =
        given()
            .pathParams("client_id", clientID)
            .when().get("/client/{client_id}")
            .then()
            .statusCode(404)
            .extract()
            .asString();
    assertNotNull(response);

  }
}