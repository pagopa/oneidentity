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
import java.util.Map;
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
        .build();

    ClientRegistrationResponseDTO mockResponse = Mockito.mock(ClientRegistrationResponseDTO.class);
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any())).thenReturn(mockResponse);

    given()
        .contentType("application/x-www-form-urlencoded")
        .formParams(Map.of(
            "redirect_uris", clientRegistrationRequestDTO.getRedirectUris(),
            "client_name", clientRegistrationRequestDTO.getClientName(),
            "logo_uri", clientRegistrationRequestDTO.getLogoUri(),
            "policy_uri", clientRegistrationRequestDTO.getPolicyUri(),
            "tos_uri", clientRegistrationRequestDTO.getTosUri(),
            "default_acr_values", clientRegistrationRequestDTO.getDefaultAcrValues(),
            "saml_requested_attributes", "name"
        ))
        .when()
        .post("/register")
        .then()
        .statusCode(201);
  }

  @Test
  void testRegister() {

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
}