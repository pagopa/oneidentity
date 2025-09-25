package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.service.ClientRegistrationServiceImpl;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    Map<String, Map<String, Client.LocalizedContent>> localizedContentMap = new HashMap<>();
    Map<String, Client.LocalizedContent> defaultLangs = new HashMap<>();
    defaultLangs.put("en",
        new Client.LocalizedContent("Title of minimum 10 characters",
            "Description of minimum 20 characters to pass the constraint",
            "http://test.com", null, null));
    localizedContentMap.put("default", defaultLangs);
    // Add new theme 'optional'
    Map<String, Client.LocalizedContent> optionalLangs = new HashMap<>();
    optionalLangs.put("de",
        new Client.LocalizedContent("Title of minimum 10 characters",
            "Description of minimum 20 characters to pass the constraint",
            "http://test.com", null,
            ""));
    localizedContentMap.put("optional", optionalLangs);

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        .localizedContentMap(localizedContentMap)
        .build();

    ClientRegistrationResponseDTO mockResponse = Mockito.mock(ClientRegistrationResponseDTO.class);
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any(), Mockito.anyString()))
        .thenReturn(mockResponse);

    given()
        .contentType("application/json")
        .header(new Header("Authorization",
            "Bearer eyJraWQiOiJzSDJhV2NneTNzMGRIcElSK0RGbzJMQmxFek0reU5IWVhhdHR3OG4xOUVNPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjNjFlZDJjMC1mMDAxLTcwMGYtNDE2MC1jOWViZDExNDJlNzkiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LXNvdXRoLTEuYW1hem9uYXdzLmNvbVwvZXUtc291dGgtMV83VmxWUDd5ZFEiLCJjb2duaXRvOnVzZXJuYW1lIjoiYzYxZWQyYzAtZjAwMS03MDBmLTQxNjAtYzllYmQxMTQyZTc5Iiwib3JpZ2luX2p0aSI6IjVkZmQ5MGQ1LWE3NTUtNDI5Yy04ZDY2LWY0ZGU5OTcxZjdiNyIsImF1ZCI6IjJuOTFtMXVtODFxaDNra3JpYmdoZjk1NGVxIiwiZXZlbnRfaWQiOiIzOGU1NDM0YS1jMzk0LTRiMmMtYjAyYy01NTM0ZjkwOGFhM2MiLCJ0b2tlbl91c2UiOiJpZCIsImF1dGhfdGltZSI6MTc1ODAxNjc5OCwiY3VzdG9tOmNsaWVudF9pZCI6IlJIQ3dNdVdXUXpUQnFfQXRKbER1cjdfbkhZMl9YZzlHZ0J1LXlkRF84MlUiLCJleHAiOjE3NTgwMjAzOTgsImlhdCI6MTc1ODAxNjc5OCwianRpIjoiZWVjODlhYTYtNTRjZS00OTViLWJiMWUtMDE4MzFkMDE0OTQxIiwiZW1haWwiOiJnaXVzZXBwZS5nYW5nZW1pQHBhZ29wYS5pdCJ9.IAP4tgWvByHpiP9d72u9aaIh6fRBXVs1eL7TITRXGUUyUJ-Q1EpPFApS2GFj0AaGS4CNDf8OgTZPut9q5ELDBRZeV5ksbbJAcklZ29_OjwrXwkJujNibZX0W4nOGm_-5fRQV20ihNiMnCOa63Oqo7DUlIFgbC9CT3Spm10pDjawipR_SlsjsTekgT66Wp_Nl3RhTE_D7LXm9l3W1ucpnVSy_x9gSyziK3cyjnH98HHBfLCmjH5ZzwHLmZhUlEsEpaseE8jEX0mRpQVaC3IAv9yWnPVD3Wdn59uHUT03ozRvrRZO0vTXMTLN93l5alcqfQv4teFdrcc80wEWTTchMWg"))
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(201);
  }

  @Test
  void register_differentContentType() {
    ClientRegistrationResponseDTO mockResponse = Mockito.mock(ClientRegistrationResponseDTO.class);
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any(), Mockito.anyString()))
        .thenReturn(mockResponse);

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
        //.redirectUris(Set.of("http://test.com"))
        //.clientName("test")
        //.defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        //.samlRequestedAttributes(Set.of(Identifier.name))
        .build();

    ClientRegistrationResponseDTO mockResponse = Mockito.mock(ClientRegistrationResponseDTO.class);
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any(), Mockito.anyString()))
        .thenReturn(mockResponse);

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
  void register_withEmptyLocalizedContentMap_ko() {
    // given
    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        // The payload contains a empty map which fails the @LocalizedContentMapCheck validation
        .localizedContentMap(new HashMap<>())
        .build();

    given()
        .contentType("application/json")
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(400);
  }

  @Test
  void register_withInvalidLanguageLocalizedContentMap_ko() {
    // given
    String lang = "invalid-lang"; // Invalid language code
    Map<String, Map<String, Client.LocalizedContent>> localizedContentMap = new HashMap<>();
    Map<String, Client.LocalizedContent> defaultLangs = new HashMap<>();
    defaultLangs.put(lang, // Invalid language code
        new Client.LocalizedContent("Title of minimum 10 characters",
            "Description of minimum 20 characters to pass the constraint",
            "http://test.com", null, null));
    localizedContentMap.put("default", defaultLangs);

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        // The payload contains a map which fails the @LocalizedContentMapCheck validation
        .localizedContentMap(localizedContentMap)
        .build();

    given()
        .contentType("application/json")
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(400);
  }

  @Test
  void register_withEmptyLanguageMapLocalizedContentMap_ko() {
    // given
    Map<String, Map<String, Client.LocalizedContent>> localizedContentMap = new HashMap<>();
    Map<String, Client.LocalizedContent> defaultLangs = new HashMap<>();
    localizedContentMap.put("default", defaultLangs);

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        // The payload contains a map which fails the @LocalizedContentMapCheck validation
        .localizedContentMap(localizedContentMap)
        .build();

    given()
        .contentType("application/json")
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(400);
  }

  @Test
  void register_withInvalidTitleLocalizedContentMap_ko() {
    // given
    Map<String, Map<String, Client.LocalizedContent>> localizedContentMap = new HashMap<>();
    Map<String, Client.LocalizedContent> defaultLangs = new HashMap<>();
    defaultLangs.put("it", // Invalid language code
        new Client.LocalizedContent("short", // Title too short
            "Description of minimum 20 characters to pass the constraint",
            "http://test.com", null, null));
    localizedContentMap.put("default", defaultLangs);

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        // The payload contains a map which fails the @LocalizedContentMapCheck validation
        .localizedContentMap(localizedContentMap)
        .build();

    given()
        .contentType("application/json")
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(400);
  }

  @Test
  void register_withInvalidDescriptionLocalizedContentMap_ko() {
    // given
    Map<String, Map<String, Client.LocalizedContent>> localizedContentMap = new HashMap<>();
    Map<String, Client.LocalizedContent> defaultLangs = new HashMap<>();
    defaultLangs.put("it", // Invalid language code
        new Client.LocalizedContent("Title that is ok",
            "short", // Description too short
            "http://test.com", null, null));
    localizedContentMap.put("default", defaultLangs);

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        // The payload contains a map which fails the @LocalizedContentMapCheck validation
        .localizedContentMap(localizedContentMap)
        .build();

    given()
        .contentType("application/json")
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(400);
  }

  @Test
  void register_withEmptyContentLocalizedContentMap_ko() {
    // given
    Map<String, Map<String, Client.LocalizedContent>> localizedContentMap = new HashMap<>();
    Map<String, Client.LocalizedContent> defaultLangs = new HashMap<>();
    defaultLangs.put("it", null); // Null content
    localizedContentMap.put("default", defaultLangs);

    ClientRegistrationDTO clientRegistrationDTO = ClientRegistrationDTO.builder()
        .redirectUris(Set.of("http://test.com"))
        .clientName("test")
        .logoUri("http://test.com")
        .policyUri("http://test.com")
        .tosUri("http://test.com")
        .defaultAcrValues(Set.of(AuthLevel.L2.getValue()))
        .samlRequestedAttributes(Set.of("name"))
        .a11yUri("http://test.com")
        .backButtonEnabled(false)
        .spidMinors(false)
        .spidProfessionals(false)
        .pairwise(false)
        // The payload contains a map which fails the @LocalizedContentMapCheck validation
        .localizedContentMap(localizedContentMap)
        .build();

    given()
        .contentType("application/json")
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
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
    //userId "1234567890" in the bearer token
    String bearer = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
    String userId = "1234567890";

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
            clientRegistrationServiceImpl.getClientByUserId(Mockito.eq(userId)))
        .thenReturn(mockClient);

    given()
        .header(HttpHeaders.AUTHORIZATION, bearer)
        .contentType("application/json")
        .when()
        .get("/register")
        .then()
        .statusCode(200);
  }

  @Test
  void getClient_missingUserId_ko() {

    given()
        .contentType("application/json")
        .when()
        .get("/register")
        .then()
        .statusCode(401); // Unauthorized due to missing userId
  }

  @Test
  void getClient_clientNotFound_ko() {
    //userId "1234567890" in the bearer token
    String bearer = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
    String userId = "1234567890";

    Mockito.when(clientRegistrationServiceImpl.getClientByUserId(Mockito.eq(userId)))
        .thenThrow(ClientNotFoundException.class);

    given()
        .header(HttpHeaders.AUTHORIZATION, bearer)
        .contentType("application/json")
        .when()
        .get("/register")
        .then()
        .statusCode(404); // NotFound due to client not found
  }

  //Update Client

  @Test
  void updateClient_ok() {
    //userId "1234567890" in the bearer token
    String bearer = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
    String userId = "1234567890";
    // given
    Map<String, Map<String, Client.LocalizedContent>> localizedContentMap = new HashMap<>();
    Map<String, Client.LocalizedContent> defaultLangs = new HashMap<>();
    defaultLangs.put("en",
        new Client.LocalizedContent("Title of minimum 10 characters",
            "Description of minimum 20 characters to pass the constraint",
            "http://test.com", null, null));
    localizedContentMap.put("default", defaultLangs);
    // Add new theme 'optional'
    Map<String, Client.LocalizedContent> optionalLangs = new HashMap<>();
    optionalLangs.put("de",
        new Client.LocalizedContent("Title of minimum 10 characters",
            "Description of minimum 20 characters to pass the constraint", "http://test.com", null,
            ""));
    localizedContentMap.put("optional", optionalLangs);

    String clientId = "testClientId";

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
        .localizedContentMap(
            Map.of("removeTheme",
                Map.of("en",
                    new Client.LocalizedContent("Title of minimum 10 characters",
                        "Description of minimum 20 characters to pass the constraint",
                        "http://test.com",
                        "test", "test")
                )
            ))
        .build();

    ClientRegistrationDTO updatedDto = ClientRegistrationDTO.builder()
        .defaultAcrValues(Set.of("https://www.spid.gov.it/SpidL2"))
        .clientName("updatedName")
        .redirectUris(Set.of("http://updated.com"))
        .samlRequestedAttributes(Set.of("spidCode"))
        .localizedContentMap(localizedContentMap)
        .build();

    Mockito.when(clientRegistrationServiceImpl.getClientByClientId(Mockito.eq(clientId)
        ))
        .thenReturn(existingClient);

    given()
        .header(HttpHeaders.AUTHORIZATION, bearer)
        .contentType("application/json")
        .pathParam("client_id", clientId)
        .body(updatedDto)
        .when()
        .put("/register/client_id/{client_id}")
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

    ClientRegistrationDTO updatedDto = ClientRegistrationDTO.builder()
        //.userId(userId) // Intentionally missing userId to simulate error
        .clientName("newName")
        .redirectUris(Set.of("http://new.com"))
        .build();

    Mockito.when(clientRegistrationServiceImpl.getClientByClientId(Mockito.eq(clientId)
        ))
        .thenReturn(existingClient);

    given()
        .contentType("application/json")
        .pathParam("client_id", clientId)
        .body(updatedDto)
        .when()
        .put("/register/client_id/{client_id}")
        .then()
        .statusCode(400); // Bad Request due to missing userId in request body
  }

  @Test
  void refreshClientSecret() {
    String clientId = "testClientId";
    String userId = "testUserId";
    String bearer = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
    Mockito.when(clientRegistrationServiceImpl.refreshClientSecret(clientId, userId))
        .thenReturn("NewSecret");

    given()
        .contentType("application/json")
        .header(HttpHeaders.AUTHORIZATION, bearer)
        .pathParam("client_id", clientId)
        .when()
        .post("/clients/{client_id}/secret/refresh")
        .then()
        .statusCode(200);
  }
}