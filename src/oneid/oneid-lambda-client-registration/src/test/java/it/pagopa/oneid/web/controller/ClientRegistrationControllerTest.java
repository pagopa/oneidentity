package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.service.ClientRegistrationServiceImpl;
import java.util.HashMap;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@TestHTTPEndpoint(ClientRegistrationController.class)
@QuarkusTest
class ClientRegistrationControllerTest {

  @InjectMock
  ClientRegistrationServiceImpl clientRegistrationServiceImpl;

  @Test
  void register() {

    // given
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .userId("test")
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of(Identifier.name))
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
  void testGetClientInfoByClientId() {

    String userId = "testUserId";
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .userId(userId)
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of(Identifier.name))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .localizedContentMap(new HashMap<>())
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .build();

    given()
        .contentType("application/json")
        .pathParam("client_id", "test")
        .body(clientRegistrationDTO)
        .when()
        .get("/register/{client_id}")
        .then()
        .statusCode(200);
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