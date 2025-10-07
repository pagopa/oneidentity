package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.id.State;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.OIDCSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.controller.interceptors.CurrentAuthDTO;
import it.pagopa.oneid.web.controller.mock.SAMLControllerTestProfile;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import jakarta.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.Response;

@QuarkusTest
@TestProfile(SAMLControllerTestProfile.class)
@TestHTTPEndpoint(SAMLController.class)
public class SAMLControllerTest {

  @InjectMock
  SAMLServiceImpl samlServiceImpl;

  @Inject
  CurrentAuthDTO currentAuthDTO;

  @InjectMock
  OIDCServiceImpl oidcServiceImpl;

  // This will be mocked using @Alternative construct because of @Dependant scope
  @Inject
  SessionServiceImpl<SAMLSession> samlSessionService;

  // This will be mocked using @Alternative construct because of @Dependant scope
  @Inject
  SessionServiceImpl<OIDCSession> oidcSessionSessionService;

  // This will be mocked using @Alternative construct because of @Dependant scope
  @Inject
  SessionServiceImpl<AccessTokenSession> accessTokenSessionSessionService;

  @ConfigProperty(name = "base_path")
  String BASE_PATH;

  @Test
  @SneakyThrows
  void samlACS_ok() {
    // given
    Map<String, String> samlResponseDTO = new HashMap<>();
    samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
    samlResponseDTO.put("RelayState", "dummyRelayState");

    // setup samlServiceImplMock

    Response response = Mockito.mock(Response.class);
    Mockito.when(response.getInResponseTo()).thenReturn("Dummy");
    Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

    doNothing().when(samlServiceImpl)
        .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    doNothing().when(samlServiceImpl)
        .validateSAMLResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    // setup oidcServiceImpl mock
    AuthorizationRequest authorizationRequest = Mockito.mock(AuthorizationRequest.class);
    Mockito.when(oidcServiceImpl.buildAuthorizationRequest(Mockito.any()))
        .thenReturn(authorizationRequest);
    // mocking of AuthorizationResponse object
    AuthorizationResponse authorizationResponse = Mockito.mock(AuthorizationResponse.class);
    AuthorizationSuccessResponse authorizationSuccessResponse = Mockito.mock(
        AuthorizationSuccessResponse.class);
    AuthorizationCode authorizationCode = Mockito.mock(AuthorizationCode.class);

    Mockito.when(authorizationCode.getValue()).thenReturn("DummyCode");
    Mockito.when(authorizationCode.toString()).thenReturn("DummyCode");
    Mockito.when(authorizationSuccessResponse.getAuthorizationCode()).thenReturn(authorizationCode);
    Mockito.when(authorizationSuccessResponse.getState()).thenReturn(new State("DummyState"));
    Mockito.when(authorizationResponse.toSuccessResponse())
        .thenReturn(authorizationSuccessResponse);

    Mockito.when(oidcServiceImpl.getAuthorizationResponse(Mockito.any()))
        .thenReturn(authorizationResponse);

    // setup of samlSessionService mock and oidcSessionService mock is implemented in MockSAMLControllerSessionServiceImpl class
    // due to its @Dependent scope which does not permit to use the @InjectMock annotation

    // location header to verify
    String headerLocation = "test?code=" + authorizationCode + "&state="
        + authorizationResponse.getState();
    String location = given()
        .formParams(samlResponseDTO)
        .when()
        .post("/acs")
        .then()
        .statusCode(302)
        .extract()
        .header("location");

    Assertions.assertTrue(location.contains(headerLocation));
  }

  @Test
  @SneakyThrows
  void samlACS_exceptionInGetSAMLResponseFromString() {
    // given
    Map<String, String> samlResponseDTO = new HashMap<>();
    samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
    samlResponseDTO.put("RelayState", "dummyRelayState");

    // setup samlServiceImplMock

    Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any()))
        .thenThrow(new OneIdentityException());

    String headerLocation = BASE_PATH + "/login/error?error_code=" + URLEncoder.encode(
        ErrorCode.GENERIC_HTML_ERROR.getErrorCode(),
        StandardCharsets.UTF_8);

    String location = given()
        .formParams(samlResponseDTO)
        .when()
        .post("/acs")
        .then()
        .statusCode(302)
        .extract()
        .header("location");

    Assertions.assertTrue(location.contains(headerLocation));
  }

  @Test
  @SneakyThrows
  void samlACS_exceptionInGetSessionSAML() {
    // given
    Map<String, String> samlResponseDTO = new HashMap<>();
    samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
    samlResponseDTO.put("RelayState", "dummyRelayState");

    // setup samlServiceImplMock

    Response response = Mockito.mock(Response.class);
    Mockito.when(response.getInResponseTo()).thenReturn("exceptionSAML");
    Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

    // location header to verify
    String headerLocation = BASE_PATH + "/login/error?error_code=" + URLEncoder.encode(
        ErrorCode.SESSION_ERROR.getErrorCode(),
        StandardCharsets.UTF_8);
    String location = given()
        .formParams(samlResponseDTO)
        .when()
        .post("/acs")
        .then()
        .statusCode(302)
        .extract()
        .header("location");

    Assertions.assertTrue(location.contains(headerLocation));
  }

  @Test
  @SneakyThrows
  void samlACS_exceptionInSetSAMLResponse() {
    // given
    Map<String, String> samlResponseDTO = new HashMap<>();
    samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
    samlResponseDTO.put("RelayState", "dummyRelayState");

    // setup samlServiceImplMock

    Response response = Mockito.mock(Response.class);
    Mockito.when(response.getInResponseTo()).thenReturn("exceptionSAMLResponse");
    Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

    // location header to verify
    String headerLocation = BASE_PATH + "/login/error?error_code=" + URLEncoder.encode(
        ErrorCode.SESSION_ERROR.getErrorCode(),
        StandardCharsets.UTF_8);
    String location = given()
        .formParams(samlResponseDTO)
        .when()
        .post("/acs")
        .then()
        .statusCode(302)
        .extract()
        .header("location");

    Assertions.assertTrue(location.contains(headerLocation));
  }

  @Test
  @SneakyThrows
  void samlACS_exceptionInCheckSAMLStatus() {
    // given
    Map<String, String> samlResponseDTO = new HashMap<>();
    samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
    samlResponseDTO.put("RelayState", "dummyRelayState");

    // setup samlServiceImplMock

    Response response = Mockito.mock(Response.class);
    Mockito.when(response.getInResponseTo()).thenReturn("dummyInResponseTo");
    Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

    doThrow(new OneIdentityException()).when(samlServiceImpl)
        .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    // location header to verify
    String headerLocation = BASE_PATH + "/login/error?error_code=" + URLEncoder.encode(
        ErrorCode.GENERIC_HTML_ERROR.getErrorCode(),
        StandardCharsets.UTF_8);
    String location = given()
        .formParams(samlResponseDTO)
        .when()
        .post("/acs")
        .then()
        .statusCode(302)
        .extract()
        .header("location");

    Assertions.assertTrue(location.contains(headerLocation));
  }

  @SneakyThrows
  @Test
  void samlACS_exceptionInValidateSAMLResponse() {
    // given
    Map<String, String> samlResponseDTO = new HashMap<>();
    samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
    samlResponseDTO.put("RelayState", "dummyRelayState");

    // setup samlServiceImplMock

    Response response = Mockito.mock(Response.class);
    SAMLValidationException samlValidationException = Mockito.mock(SAMLValidationException.class);
    Mockito.when(samlValidationException.getRedirectUri()).thenReturn("test.com");
    Mockito.when(samlValidationException.getState()).thenReturn("dummyState");
    Mockito.when(samlValidationException.getClientId()).thenReturn("dummyClientId");
    Mockito.when(samlValidationException.getMessage())
        .thenReturn(ErrorCode.IDP_ERROR_INVALID_SAML_VERSION.getErrorMessage());
    Mockito.when(samlValidationException.getErrorCode())
        .thenReturn(ErrorCode.IDP_ERROR_INVALID_SAML_VERSION);

    Mockito.when(response.getInResponseTo()).thenReturn("dummyInResponseTo");
    Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

    doNothing().when(samlServiceImpl)
        .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    samlValidationException.setRedirectUri("test.com");
    doThrow(samlValidationException).when(
            samlServiceImpl)
        .validateSAMLResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    // location header to verify
    String headerLocation = BASE_PATH + "/login/error?error_code=" +
        URLEncoder.encode(ErrorCode.IDP_ERROR_INVALID_SAML_VERSION.getErrorCode(),
            StandardCharsets.UTF_8) +
        "&redirect_uri=" + URLEncoder.encode("test.com", StandardCharsets.UTF_8);
    String location = given()
        .formParams(samlResponseDTO)
        .when()
        .post("/acs")
        .then()
        .statusCode(302)
        .extract()
        .header("location");

    Assertions.assertTrue(location.contains(headerLocation));
  }

  @Test
  @SneakyThrows
  void samlACS_exceptionInSaveSessionOIDC() {
    // given
    Map<String, String> samlResponseDTO = new HashMap<>();
    samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
    samlResponseDTO.put("RelayState", "dummyRelayState");

    // setup samlServiceImplMock

    Response response = Mockito.mock(Response.class);
    Mockito.when(response.getInResponseTo()).thenReturn("exceptionOIDC");
    Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

    doNothing().when(samlServiceImpl)
        .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    doNothing().when(samlServiceImpl)
        .validateSAMLResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    // setup oidcServiceImpl mock
    AuthorizationRequest authorizationRequest = Mockito.mock(AuthorizationRequest.class);
    Mockito.when(oidcServiceImpl.buildAuthorizationRequest(Mockito.any()))
        .thenReturn(authorizationRequest);
    // mocking of AuthorizationResponse object
    AuthorizationResponse authorizationResponse = Mockito.mock(AuthorizationResponse.class);
    AuthorizationSuccessResponse authorizationSuccessResponse = Mockito.mock(
        AuthorizationSuccessResponse.class);
    AuthorizationCode authorizationCode = Mockito.mock(AuthorizationCode.class);

    Mockito.when(authorizationCode.getValue()).thenReturn("DummyCode");
    Mockito.when(authorizationCode.toString()).thenReturn("DummyCode");
    Mockito.when(authorizationSuccessResponse.getAuthorizationCode()).thenReturn(authorizationCode);
    Mockito.when(authorizationSuccessResponse.getState()).thenReturn(new State("DummyState"));
    Mockito.when(authorizationResponse.toSuccessResponse())
        .thenReturn(authorizationSuccessResponse);

    Mockito.when(oidcServiceImpl.getAuthorizationResponse(Mockito.any()))
        .thenReturn(authorizationResponse);

    // setup of samlSessionService mock and oidcSessionService mock is implemented in MockSAMLControllerSessionServiceImpl class
    // due to its @Dependent scope which does not permit to use the @InjectMock annotation

    // location header to verify
    String headerLocation = BASE_PATH + "/login/error?error_code=" + URLEncoder.encode(
        ErrorCode.SESSION_ERROR.getErrorCode(),
        StandardCharsets.UTF_8);
    String location = given()
        .formParams(samlResponseDTO)
        .when()
        .post("/acs")
        .then()
        .statusCode(302)
        .extract()
        .header("location");

    Assertions.assertTrue(location.contains(headerLocation));

  }

  @Test
  @SneakyThrows
  void samlACS_exceptionInCreatingCallbackURI() {
    // given
    Map<String, String> samlResponseDTO = new HashMap<>();
    samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
    samlResponseDTO.put("RelayState", "dummyRelayState");

    // setup samlServiceImplMock

    Response response = Mockito.mock(Response.class);
    Mockito.when(response.getInResponseTo()).thenReturn("dummyInResponseTo");
    Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

    doNothing().when(samlServiceImpl)
        .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    doNothing().when(samlServiceImpl)
        .validateSAMLResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    // setup oidcServiceImpl mock
    AuthorizationRequest authorizationRequest = Mockito.mock(AuthorizationRequest.class);
    Mockito.when(oidcServiceImpl.buildAuthorizationRequest(Mockito.any()))
        .thenReturn(authorizationRequest);
    // mocking of AuthorizationResponse object
    AuthorizationResponse authorizationResponse = Mockito.mock(AuthorizationResponse.class);
    // Mock DummyState in invalid manner in order to obtain a URISyntaxException when constructing the Callback URI
    Mockito.when(authorizationResponse.getState()).thenReturn(new State("DummyState<<<>>???!!!@"));

    AuthorizationSuccessResponse authorizationSuccessResponse = Mockito.mock(
        AuthorizationSuccessResponse.class);
    AuthorizationCode authorizationCode = Mockito.mock(AuthorizationCode.class);

    Mockito.when(authorizationCode.getValue()).thenReturn("DummyCode");
    Mockito.when(authorizationCode.toString()).thenReturn("DummyCode");
    Mockito.when(authorizationSuccessResponse.getAuthorizationCode()).thenReturn(authorizationCode);
    Mockito.when(authorizationSuccessResponse.getState()).thenReturn(new State("DummyState"));
    Mockito.when(authorizationResponse.toSuccessResponse())
        .thenReturn(authorizationSuccessResponse);

    Mockito.when(oidcServiceImpl.getAuthorizationResponse(Mockito.any()))
        .thenReturn(authorizationResponse);

    // setup of samlSessionService mock and oidcSessionService mock is implemented in MockSAMLControllerSessionServiceImpl class
    // due to its @Dependent scope which does not permit to use the @InjectMock annotation

    // location header to verify
    String headerLocation = BASE_PATH + "/login/error?error_code=" + URLEncoder.encode(
        ErrorCode.GENERIC_HTML_ERROR.getErrorCode(),
        StandardCharsets.UTF_8);
    String location = given()
        .formParams(samlResponseDTO)
        .when()
        .post("/acs")
        .then()
        .statusCode(302)
        .extract()
        .header("location");

    Assertions.assertTrue(location.contains(headerLocation));
  }

  @Test
  @SneakyThrows
  void samlACS_SAMLResponseWithMultipleSignatures() {
    // given
    CurrentAuthDTO mockAuthDTO = Mockito.mock(CurrentAuthDTO.class);
    QuarkusMock.installMockForType(mockAuthDTO, CurrentAuthDTO.class);

    Map<String, String> samlResponseDTO = new HashMap<>();
    samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
    samlResponseDTO.put("RelayState", "dummyRelayState");

    // Mock CurrentAuthDTO to simulate multiple signatures scenario
    Mockito.when(mockAuthDTO.isResponseWithMultipleSignatures()).thenReturn(true);

    // Setup mocks for response and samlSession as usual, but flow will stop at the multiple signatures check
    Response response = Mockito.mock(Response.class);
    Mockito.when(response.getInResponseTo()).thenReturn("Dummy");
    Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);
    Mockito.when(mockAuthDTO.getResponse()).thenReturn(response);

    AuthorizationRequestDTOExtended dto = Mockito.mock(AuthorizationRequestDTOExtended.class);
    Mockito.when(dto.getIdp()).thenReturn("dummy-idp"); // Stub idp for cloudwatch metrics
    SAMLSession samlSession = Mockito.mock(SAMLSession.class);
    Mockito.when(samlSession.getAuthorizationRequestDTOExtended()).thenReturn(dto);
    Mockito.when(mockAuthDTO.getSamlSession()).thenReturn(samlSession);

    // HTTP 302
    String location = given()
        .formParams(samlResponseDTO)
        .when()
        .post("/acs")
        .then()
        .statusCode(302)
        .extract()
        .header("location");
    
    Assertions.assertTrue(location.contains(
        ErrorCode.IDP_ERROR_MULTIPLE_SAMLRESPONSE_SIGNATURES_PRESENT.getErrorCode()));

  }

  @Test
  void assertion_ok() {
    given()
        .queryParam("access_token", "dummy")
        .when()
        .get("/assertion")
        .then()
        .statusCode(200);
  }

  @Test
  void assertion_SessionException() {
    given()
        .queryParam("access_token", "exceptionAccessToken")
        .when()
        .get("/assertion")
        .then()
        .statusCode(404);
  }

}
