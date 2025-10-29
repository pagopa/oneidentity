package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.mockito.Mockito.doThrow;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import it.pagopa.oneid.common.connector.exception.NoMasterKeyException;
import it.pagopa.oneid.common.connector.exception.PDVException;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.ClientExtended;
import it.pagopa.oneid.common.model.dto.PDVApiKeysDTO;
import it.pagopa.oneid.common.model.dto.PDVPlanDTO;
import it.pagopa.oneid.common.model.dto.PDVValidateApiKeyDTO;
import it.pagopa.oneid.common.model.dto.PDVValidationResponseDTO;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.ClientNotFoundException;
import it.pagopa.oneid.exception.InvalidPDVPlanException;
import it.pagopa.oneid.model.dto.ClientRegistrationDTO;
import it.pagopa.oneid.model.dto.ClientRegistrationResponseDTO;
import it.pagopa.oneid.service.ClientRegistrationServiceImpl;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any(), Mockito.anyString(),
            Mockito.notNull(), Mockito.notNull()))
        .thenReturn(mockResponse);

    given()
        .contentType("application/json")
        .header(new Header("Authorization",
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30"))
        .header(new Header("PDV-X-Api-Key", "dummy-key"))
        .header(new Header("PDV-Plan-Name", "dummy-name"))
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(201);
  }

  @Test
  void register_pairWiseEnabled_ok() {
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
        .pairwise(true)
        .localizedContentMap(localizedContentMap)
        .build();

    ClientRegistrationResponseDTO mockResponse = Mockito.mock(ClientRegistrationResponseDTO.class);
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any(), Mockito.anyString(),
            Mockito.notNull(), Mockito.notNull()))
        .thenReturn(mockResponse);

    given()
        .contentType("application/json")
        .header(new Header("Authorization",
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30"))
        .header(new Header("PDV-X-Api-Key", "dummy-key"))
        .header(new Header("PDV-Plan-Name", "dummy-name"))
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(201);
  }

  @Test
  void register_pairWiseEnabled_no_pdv_info_ko() {
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
        .pairwise(true)
        .localizedContentMap(localizedContentMap)
        .build();

    doThrow(new InvalidPDVPlanException("Invalid PDV data"))
        .when(clientRegistrationServiceImpl)
        .validateClientRegistrationInfo(Mockito.any(), Mockito.any(), Mockito.any());

    given()
        .contentType("application/json")
        .header(new Header("Authorization",
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30"))
        .header(new Header("PDV-X-Api-Key", ""))
        .header(new Header("PDV-Plan-Name", ""))
        .body(clientRegistrationDTO)
        .when()
        .post("/register")
        .then()
        .statusCode(400);
  }

  @Test
  void register_differentContentType() {
    ClientRegistrationResponseDTO mockResponse = Mockito.mock(ClientRegistrationResponseDTO.class);
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any(), Mockito.anyString(),
            Mockito.notNull(), Mockito.notNull()))
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
    Mockito.when(clientRegistrationServiceImpl.saveClient(Mockito.any(), Mockito.anyString(),
            Mockito.notNull(), Mockito.notNull()))
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
        .get("/register/user_id/{user_id}", userId)
        .then()
        .statusCode(200);
  }

  @Test
  void getClient_missingUserId_ko() {

    given()
        .contentType("application/json")
        .when()
        .get("/register/user_id/{user_id}", "someUserId")
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
        .get("/register/user_id/{user_id}", userId)
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

    ClientExtended existingClientExtended = ClientExtended.builder()
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

    Mockito.when(clientRegistrationServiceImpl.getClientExtendedByClientId(Mockito.eq(clientId)
        ))
        .thenReturn(existingClientExtended);

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
  void updateClient_pairWise_ok() {
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

    ClientExtended existingClientExtended = ClientExtended.builder()
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
        .pairwise(true)
        .build();

    Mockito.when(clientRegistrationServiceImpl.getClientExtendedByClientId(Mockito.eq(clientId)
        ))
        .thenReturn(existingClientExtended);

    given()
        .header(HttpHeaders.AUTHORIZATION, bearer)
        .contentType("application/json")
        .pathParam("client_id", clientId)
        .body(updatedDto)
        .header(new Header("PDV-X-Api-Key", "dummy-key"))
        .header(new Header("PDV-Plan-Name", "dummy-name"))
        .when()
        .put("/register/client_id/{client_id}")
        .then()
        .statusCode(204);
  }

  @Test
  void updateClient_missingUserId_ko() {
    String clientId = "testClientId";
    String userId = "testUserId";

    ClientExtended existingClientExtended = ClientExtended.builder()
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

    Mockito.when(clientRegistrationServiceImpl.getClientExtendedByClientId(Mockito.eq(clientId)
        ))
        .thenReturn(existingClientExtended);

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

  //Get PDV Plan

  @Test
  void getPDVPlan_ok() {

    PDVPlanDTO plan1 = PDVPlanDTO.builder()
        .id("id1")
        .name("name1")
        .build();
    PDVPlanDTO plan2 = PDVPlanDTO.builder()
        .id("id2")
        .name("name2")
        .build();

    PDVApiKeysDTO mockResponse = PDVApiKeysDTO.builder()
        .apiKeys(List.of(plan1, plan2))
        .build();

    Mockito.when(clientRegistrationServiceImpl.getPDVPlanList()).thenReturn(mockResponse);
    given()
        .contentType("application/json")
        .when()
        .get("/register/plan_list")
        .then()
        .statusCode(200)
        .body("api_keys", org.hamcrest.Matchers.hasSize(2))
        .body("api_keys.id", org.hamcrest.Matchers.contains(plan1.getId(), plan2.getId()))
        .body("api_keys.name", org.hamcrest.Matchers.contains(plan1.getName(), plan2.getName()));
  }


  @Test
  void getPDVPlan_NotFound_ko() {
    Mockito.when(clientRegistrationServiceImpl.getPDVPlanList()).thenThrow(
        new PDVException(
            "PDV response not ok",
            NOT_FOUND.getStatusCode(),
            Optional.of("not found"),
            new WebApplicationException(
                Response.status(NOT_FOUND).entity("not found").build()
            )
        ));

    given()
        .contentType("application/json")
        .when()
        .get("/register/plan_list")
        .then()
        .statusCode(404);
  }

  @Test
  void getPDVPlan_OtherError_ko() {
    Mockito.when(clientRegistrationServiceImpl.getPDVPlanList()).thenThrow(
        NoMasterKeyException.class);

    given()
        .contentType("application/json")
        .when()
        .get("/register/plan_list")
        .then()
        .statusCode(502);
  }

  @Test
  void validatePDVApiKey_ok() {
    PDVValidationResponseDTO response = PDVValidationResponseDTO.builder().valid(true).build();

    Mockito.when(clientRegistrationServiceImpl.validatePDVApiKey(Mockito.any()))
        .thenReturn(response);

    given()
        .contentType("application/json")
        .body(PDVValidateApiKeyDTO.builder().apiKeyId("id").apiKeyValue("apiKey").build())
        .when().post("/register/validate_api_key")
        .then().statusCode(200)
        .body("valid", org.hamcrest.Matchers.is(true));
  }

  @Test
  void validatePDVApiKey_NotFound_ko() {
    PDVValidateApiKeyDTO request = PDVValidateApiKeyDTO.builder()
        .apiKeyId("api_key")
        .apiKeyValue("value")
        .build();

    Mockito.when(clientRegistrationServiceImpl.validatePDVApiKey(Mockito.any()))
        .thenThrow(new PDVException(
            "PDV response not ok",
            NOT_FOUND.getStatusCode(),
            Optional.of("not found"),
            new WebApplicationException(Response.status(NOT_FOUND).entity("not found").build())
        ));

    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/register/validate_api_key")
        .then()
        .statusCode(404);
  }

  @Test
  void validatePDVApiKey_MalformedJson_ko() {
    String malformedJson = "{\"someField\": \"missingEndQuote}"; // invalid JSON

    given()
        .contentType("application/json")
        .body(malformedJson)
        .when()
        .post("/register/validate_api_key")
        .then()
        .statusCode(400);
  }

  @Test
  void validatePDVApiKey_OtherError_ko() {
    Mockito.when(clientRegistrationServiceImpl.validatePDVApiKey(Mockito.any())).thenThrow(
        NoMasterKeyException.class);

    given()
        .contentType("application/json")
        .body(PDVValidateApiKeyDTO.builder().apiKeyId("id").apiKeyValue("apiKey").build())
        .when().post("/register/validate_api_key")
        .then().statusCode(502);
  }
}