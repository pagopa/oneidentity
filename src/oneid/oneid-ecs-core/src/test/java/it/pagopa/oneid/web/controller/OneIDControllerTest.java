package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit5.virtual.ShouldNotPin;
import io.quarkus.test.junit5.virtual.VirtualThreadUnit;
import it.pagopa.oneid.common.model.ClientFE;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.service.ClientServiceImpl;
import it.pagopa.oneid.service.IdpServiceImpl;
import it.pagopa.oneid.service.OIDCServiceImpl;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@VirtualThreadUnit
@ShouldNotPin
class OneIDControllerTest {

  @Inject
  OneIDController oneIDController;
  @InjectMock
  ClientServiceImpl clientServiceImpl;
  @InjectMock
  OIDCServiceImpl oidcServiceImpl;
  @InjectMock
  IdpServiceImpl idpServiceImpl;


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

    //when
    String response =
        given()
            .pathParam("client_id", clientID)
            .when().get("/clients/{client_id}")
            .then()
            .statusCode(200)
            .extract()
            .asString();

    //then
    assertNotNull(response);

  }

  @Test
  void findClientByIdTest_error() {

    //given
    String clientID = "test";
    Mockito.when(clientServiceImpl.getClientInformation(Mockito.anyString()))
        .thenReturn(Optional.empty());

    //when
    String response =
        given()
            .pathParams("client_id", clientID)
            .when().get("/clients/{client_id}")
            .then()
            .statusCode(404)
            .extract()
            .asString();

    //then
    assertNotNull(response);
  }

  @Test
  void findAllClients() {

    //given
    ArrayList<ClientFE> clients = Mockito.mock(ArrayList.class);
    Mockito.when(clientServiceImpl.getAllClientsInformation())
        .thenReturn(Optional.of(clients));

    //when
    String response =
        given()
            .when().get("/clients")
            .then()
            .statusCode(200)
            .extract()
            .asString();

    //then
    assertNotNull(response);
  }

  @Test
  void findAllClients_error() {

    //given
    Mockito.when(clientServiceImpl.getAllClientsInformation())
        .thenReturn(Optional.empty());

    //when
    String response =
        given()
            .when().get("/clients")
            .then()
            .statusCode(404)
            .extract()
            .asString();

    //then
    assertNotNull(response);
  }

  @Test
  void findAllIdp() {

    //given
    ArrayList<IDP> idps = Mockito.mock(ArrayList.class);
    Mockito.when(idpServiceImpl.findAllIdpByTimestamp())
        .thenReturn(Optional.of(idps));

    //when
    String response =
        given()
            .when().get("/idps")
            .then()
            .statusCode(200)
            .extract()
            .asString();

    //then
    assertNotNull(response);

  }

  @Test
  void findAllIdp_error() {

    //given
    Mockito.when(idpServiceImpl.findAllIdpByTimestamp())
        .thenReturn(Optional.empty());

    //when
    String response =
        given()
            .when().get("/idps")
            .then()
            .statusCode(404)
            .extract()
            .asString();

    //then
    assertNotNull(response);
  }
}