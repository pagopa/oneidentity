package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.enums.Identifier;
import it.pagopa.oneid.model.dto.ClientMetadataDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationRequestDTO;
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
    ClientRegistrationRequestDTO clientRegistrationRequestDTO = ClientRegistrationRequestDTO.builder()
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
        .body("{\"redirectUris\": " + "[\"http://test.com\"]" + "," +
            "\"clientName\": \"" + clientRegistrationRequestDTO.getClientName() + "\"," +
            "\"logoUri\": \"" + clientRegistrationRequestDTO.getLogoUri() + "\"," +
            "\"policyUri\": \"" + clientRegistrationRequestDTO.getPolicyUri() + "\"," +
            "\"tosUri\": \"" + clientRegistrationRequestDTO.getTosUri() + "\"," +
            "\"defaultAcrValues\": " + "[\"https://www.spid.gov.it/SpidL2\"]" + "," +
            "\"samlRequestedAttributes\": [\"name\"]}" + "\"," +
            "\"a11YUri\": \"" + clientRegistrationRequestDTO.getA11yUri() + "\"," +
            "\"backButtonEnabled\": \"" + clientRegistrationRequestDTO.isBackButtonEnabled()
            + "\"," +
            "\"localizedContentMap\": \"" + clientRegistrationRequestDTO.getLocalizedContentMap()
            + "\"," +
            "\"spidMinors\": \"" + clientRegistrationRequestDTO.isSpidMinors() + "\"," +
            "\"spidProfessionals\": \"" + clientRegistrationRequestDTO.isSpidProfessionals()
            + "\"," +
            "\"pairwise\": \"" + clientRegistrationRequestDTO.isPairwise())
        .when()
        .post("/register")
        .then()
        .statusCode(201);
  }

  @Test
  void register_differentContentType() {

    // given
    ClientRegistrationRequestDTO clientRegistrationRequestDTO = ClientRegistrationRequestDTO.builder()
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
        .contentType("application/xml") // Testing wrong content-type
        .when()
        .post("/register")
        .then()
        .statusCode(415);
  }

  @Test
  void testGetClientInfoByClientId() {

    ClientMetadataDTO mockResponse = Mockito.mock(ClientMetadataDTO.class);
    Mockito.when(clientRegistrationServiceImpl.getClientMetadataDTO(Mockito.anyString()))
        .thenReturn(mockResponse);

    given()
        .contentType("application/x-www-form-urlencoded")
        .pathParam("client_id", "test")
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