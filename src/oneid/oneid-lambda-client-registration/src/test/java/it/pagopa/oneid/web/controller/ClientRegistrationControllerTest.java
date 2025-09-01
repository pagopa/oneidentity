package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.service.ClientRegistrationServiceImpl;
import java.util.HashMap;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@TestHTTPEndpoint(ClientRegistrationController.class)
@QuarkusTest
class ClientRegistrationControllerTest {

  @InjectMock
  ClientRegistrationServiceImpl clientRegistrationServiceImpl;

  //Register

  @Test
  void register_ok() {
    // given
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .userId("test")
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .localizedContentMap(new HashMap<>())
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .build();

    ClientRegistrationResponseDTO mockResponse = Mockito.mock(ClientRegistrationResponseDTO.class);
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any())).thenReturn(mockResponse);

    given()
        .contentType("application/json")
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(201);
  }

  @Test
  void register_differentContentType() {
    ClientRegistrationResponseDTO mockResponse = Mockito.mock(ClientRegistrationResponseDTO.class);
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any())).thenReturn(mockResponse);

    given()
        .contentType("application/xml") // Testing wrong content-type
        .when()
        .post("/register")
        .then()
        .statusCode(415);
  }

  @Test
  void register_missingRequiredField_ko() {
    // given
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        //.userId("test")
        //.redirectUris(Set.of("http://test.com"))
        //.clientName("test")
        //.defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        //.samlRequestedAttributes(Set.of(Identifier.name))
        .build();

    ClientRegistrationResponseDTO mockResponse = Mockito.mock(ClientRegistrationResponseDTO.class);
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any())).thenReturn(mockResponse);

    given()
        .contentType("application/json")
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(400);
  }

  @Test
  void register_withInvalidSamlAttribute_ko() {
    // given
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .userId("test-user")
        .clientName("Test Client")
        .redirectUris(Set.of("https://valid.uri/callback"))
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue())) // This must be valid
        // The payload contains a string that cannot be mapped to the Identifier enum
        .samlRequestedAttributes(Set.of("this_is_not_a_valid_attribute"))
        .build();

    given()
        .contentType("application/json")
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .log().body()
        .statusCode(400);
  }

  @Test
  void register_withInvalidAcrValues_ko() {
    // given
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .userId("test-user")
        .clientName("Test Client")
        .redirectUris(Set.of("https://valid.uri/callback"))
        .samlRequestedAttributes(Set.of("name"))
        // The payload contains a string that cannot be mapped to the Authlevel enum
        .defaultAcrValues(Set.of("this_is_not_a_valid_attribute"))
        .build();

    given()
        .contentType("application/json")
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .log().body()
        .statusCode(400);
  }


  @Test
  void register_MalformedJson_ko() {
    String malformedJson = "{\"someField\": \"missingEndQuote}"; // invalid JSON

    given()
        .contentType("application/json")
        .body(malformedJson)
        .when()
        .post("/register")
        .then()
        .statusCode(400);
  }

  //Get Client Info

  @Test
  void getClient_ok() {
    Client mockClient = Client.builder()
        .clientId("client_id")
        .userId("user_id")
        .friendlyName("test")
        .callbackURI(Set.of("http://test.com"))
        .requestedParameters(Set.of("name"))
        .authLevel(AuthLevel.L2)
        .acsIndex(0)
        .attributeIndex(0)
        .clientIdIssuedAt(0)
        .build();

    Mockito.when(
            clientRegistrationServiceImpl.getClientByUserId(Mockito.eq("user_id")))
        .thenReturn(mockClient);

    given()
        .contentType("application/json")
        .pathParam("user_id", "user_id")
        .when()
        .get("/register/user_id/{user_id}")
        .then()
        .statusCode(200);
  }

  @Test
  void getClient_missingUserId_ko() {

    given()
        .contentType("application/json")
        .pathParam("user_id", "")
        .when()
        .get("/register/user_id/{user_id}")
        .then()
        .statusCode(400); // Bad Request due to missing userId
  }

  @Test
  void getClient_clientNotFound_ko() {

    Mockito.when(clientRegistrationServiceImpl.getClientByUserId(Mockito.eq("user_id")))
        .thenThrow(ClientNotFoundException.class);

    given()
        .contentType("application/json")
        .pathParam("user_id", "user_id")
        .when()
        .get("/register/user_id/{user_id}")
        .then()
        .statusCode(401); // Unauthorized due to client not found
  }

  //Update Client

  @Test
  void updateClient_ok() {
    String clientId = "testClientId";
    String userId = "testUserId";

    Client existingClient = Client.builder()
        .clientId("testClientId")
        .userId(userId)
        .friendlyName("oldName")
        .callbackURI(Set.of("http://old.com"))
        .requestedParameters(Set.of("name"))
        .authLevel(AuthLevel.L2)
        .acsIndex(0)
        .attributeIndex(0)
        .clientIdIssuedAt(111)
        .build();

    ClientRegistrationDTO updatedDto = ClientRegistrationDTO.builder()
        .userId(userId)
        .defaultAcrValues(Set.of("https://www.spid.gov.it/SpidL2"))
        .clientName("updatedName")
        .redirectUris(Set.of("http://updated.com"))
        .build();

    Mockito.when(clientRegistrationServiceImpl.getClientByClientId(Mockito.eq(clientId)
        ))
        .thenReturn(existingClient);

    given()
        .contentType("application/json")
        .pathParam("client_id", clientId)
        .body(updatedDto)
        .when()
        .patch("/register/client_id/{client_id}")
        .then()
        .statusCode(204);
  }

  @Test
  void updateClient_missingUserId_ko() {
    String clientId = "testClientId";
    String userId = "testUserId";

    Client existingClient = Client.builder()
        .clientId("testClientId")
        .userId(userId)
        .friendlyName("oldName")
        .callbackURI(Set.of("http://old.com"))
        .requestedParameters(Set.of("name"))
        .authLevel(AuthLevel.L2)
        .acsIndex(0)
        .attributeIndex(0)
        .clientIdIssuedAt(0)
        .build();

    ClientRegistrationDTO existingDto = ClientRegistrationDTO.builder()
        .userId(userId)
        .clientName("oldName")
        .redirectUris(Set.of("http://old.com"))
        .build();

    ClientRegistrationDTO updatedDto = ClientRegistrationDTO.builder()
        //.userId(userId) // Intentionally missing userId to simulate error
        .clientName("newName")
        .redirectUris(Set.of("http://new.com"))
        .build();

    Mockito.when(clientRegistrationServiceImpl.getClientByClientId(Mockito.eq(clientId)
        ))
        .thenReturn(existingClient);
    Assertions.assertDoesNotThrow(
        () -> clientRegistrationServiceImpl.patchClientRegistrationDTO(existingDto, updatedDto));

    given()
        .contentType("application/json")
        .pathParam("client_id", clientId)
        .body(updatedDto)
        .when()
        .patch("/register/client_id/{client_id}")
        .then()
        .statusCode(400); // Bad Request due to missing userId in request body
  }

  @Test
  void refreshClientSecret() {
    String clientId = "testClientId";
    String userId = "testUserId";
    Mockito.when(clientRegistrationServiceImpl.refreshClientSecret(clientId, userId))
        .thenReturn("NewSecret");

    given()
        .contentType("application/json")
        .pathParam("client_id", clientId)
        .body("{\"userId\": \"" + userId + "\"}")
        .when()
        .post("/clients/{client_id}/secret/refresh")
        .then()
        .statusCode(200);
  }
}