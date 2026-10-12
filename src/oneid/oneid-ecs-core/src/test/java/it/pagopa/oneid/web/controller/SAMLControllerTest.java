package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.id.State;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit5.virtual.ShouldNotPin;
import io.quarkus.test.junit5.virtual.VirtualThreadUnit;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.common.model.enums.AuthLevel;
import it.pagopa.oneid.common.model.exception.enums.ErrorCode;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import it.pagopa.oneid.exception.SAMLValidationException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.OIDCSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.AuthnContextComparisonType;
import it.pagopa.oneid.service.BrowserBindingService;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore;
import it.pagopa.oneid.web.controller.mock.SAMLControllerTestProfile;
import jakarta.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Base64;
import java.util.Map;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.Response;

@QuarkusTest
@TestProfile(SAMLControllerTestProfile.class)
@TestHTTPEndpoint(SAMLController.class)
@VirtualThreadUnit
@ShouldNotPin
public class SAMLControllerTest {

        @InjectMock
        SAMLServiceImpl samlServiceImpl;

        @InjectMock
        OIDCServiceImpl oidcServiceImpl;

        @InjectMock
        BrowserBindingService browserBindingService;

        @InjectMock
        CloudWatchConnectorImpl cloudWatchConnectorImpl;

        @Inject
        SAMLUtilsExtendedCore samlUtils;

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

        @ParameterizedTest
        @CsvSource(value = {
                        "NULL,dummyRelayState",
                        "'',dummyRelayState",
                        "' ',dummyRelayState",
                        "dummySAMLResponse,NULL",
                        "dummySAMLResponse,''",
                        "dummySAMLResponse,' '"
        }, nullValues = "NULL")
        @DisplayName("Missing or blank ACS fields return a local HTTP redirect")
        void given_invalid_acs_fields_when_posting_then_redirect_locally(
                        String samlResponse, String relayState) {
                Map<String, String> parameters = new HashMap<>();
                if (samlResponse != null) {
                        parameters.put("SAMLResponse", samlResponse);
                }
                if (relayState != null) {
                        parameters.put("RelayState", relayState);
                }

                String location = given().redirects().follow(false)
                                .formParams(parameters)
                                .when().post("/acs")
                                .then().statusCode(302).extract().header("Location");

                Assertions.assertEquals(BASE_PATH + "/login/error?error_code=GENERIC_HTML_ERROR", location);
                Mockito.verifyNoInteractions(samlServiceImpl, oidcServiceImpl, browserBindingService);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                        "<Assertion xmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\"/>",
                        "<AuthnRequest xmlns=\"urn:oasis:names:tc:SAML:2.0:protocol\"/>"
        })
        @DisplayName("Unexpected SAML XML roots return a local HTTP redirect")
        @SneakyThrows
        void given_unexpected_xml_root_when_posting_acs_then_redirect_locally(String xml) {
                when(samlServiceImpl.getSAMLResponseFromString(Mockito.any()))
                                .thenAnswer(invocation -> samlUtils
                                                .getSAMLResponseFromString(invocation.getArgument(0)));
                String encoded = Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8));

                String location = given().redirects().follow(false)
                                .formParam("SAMLResponse", encoded)
                                .formParam("RelayState", "saved-relay-state")
                                .when().post("/acs")
                                .then().statusCode(302).extract().header("Location");

                Assertions.assertEquals(BASE_PATH + "/login/error?error_code=GENERIC_HTML_ERROR", location);
                Mockito.verifyNoInteractions(browserBindingService, oidcServiceImpl);
        }

        @ParameterizedTest
        @EnumSource(value = BrowserBindingService.Outcome.class, names = { "MISSING", "MISMATCH", "EXPIRED" })
        @DisplayName("Cookie rejection returns HTTP 302 with the saved error-page callback context")
        @SneakyThrows
        void givenRejectedBinding_whenPostingAcs_thenContextualHttpRedirect(
                        BrowserBindingService.Outcome outcome) {
                Mockito.when(browserBindingService.enabled()).thenReturn(true);
                Mockito.when(browserBindingService.enforcing()).thenReturn(true);
                Mockito.when(browserBindingService.mode()).thenReturn(BrowserBindingService.Mode.ENFORCE);
                Mockito.when(browserBindingService.verify(Mockito.any(), Mockito.any())).thenReturn(outcome);
                Response response = Mockito.mock(Response.class);
                Mockito.when(response.getInResponseTo()).thenReturn("Dummy");
                Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

                var acsResponse = given()
                                .redirects().follow(false)
                                .formParam("SAMLResponse", "dummySAMLResponse")
                                .formParam("RelayState", "dummyRelayState")
                                .when()
                                .post("/acs")
                                .then()
                                .statusCode(302)
                                .extract().response();

                Assertions.assertEquals(BASE_PATH + "/login/error?error_code=GENERIC_HTML_ERROR"
                                + "&redirect_uri=test&state=test&client_id=test", acsResponse.getHeader("Location"));
                Assertions.assertFalse(acsResponse.getBody().asString().contains("<form"));
                Mockito.verify(samlServiceImpl, Mockito.never()).checkSAMLStatus(
                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
                Mockito.verifyNoInteractions(oidcServiceImpl);
        }

        @ParameterizedTest
        @CsvSource({
                        "MISSING,cookieRedirect,true",
                        "MISMATCH,cookieRedirect,true",
                        "EXPIRED,cookieRedirect,true",
                        "MISSING,cookiePost,false",
                        "MISMATCH,cookiePost,false",
                        "EXPIRED,cookiePost,false",
                        "MISSING,cookieDisabled,false",
                        "MISMATCH,cookieDisabled,false",
                        "EXPIRED,cookieDisabled,false",
                        "MISSING,cookieInvalidCallback,false",
                        "MISMATCH,cookieInvalidCallback,false",
                        "EXPIRED,cookieInvalidCallback,false"
        })
        @DisplayName("Cookie rejection respects the client's direct redirect policy without authenticating")
        @SneakyThrows
        void given_rejected_binding_when_posting_acs_then_follow_client_redirect_policy(
                        BrowserBindingService.Outcome outcome, String sessionId, boolean directRedirect) {
                when(browserBindingService.enabled()).thenReturn(true);
                when(browserBindingService.enforcing()).thenReturn(true);
                when(browserBindingService.mode()).thenReturn(BrowserBindingService.Mode.ENFORCE);
                when(browserBindingService.verify(Mockito.any(), Mockito.any())).thenReturn(outcome);
                Response response = Mockito.mock(Response.class);
                when(response.getInResponseTo()).thenReturn(sessionId);
                when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

                var acsResponse = given().redirects().follow(false)
                                .formParam("SAMLResponse", "dummySAMLResponse")
                                .formParam("RelayState", "https://attacker.example/untrusted-state")
                                .when().post("/acs")
                                .then().statusCode(302).extract().response();

                String state = URLEncoder.encode("saved state&value=1+%{token}", StandardCharsets.UTF_8);
                if (directRedirect) {
                        Assertions.assertEquals("https://client.example.com/callback?error=access_denied"
                                        + "&error_description=GENERIC_HTML_ERROR&state=" + state,
                                        acsResponse.getHeader("Location"));
                } else {
                        String callback = "cookieInvalidCallback".equals(sessionId)
                                        ? "https://attacker.example/callback"
                                        : "https://client.example.com/callback";
                        String clientId = "cookieDisabled".equals(sessionId) ? "testRedirect"
                                        : "cookiePost".equals(sessionId) ? "cookiePost" : "cookieRedirect";
                        Assertions.assertEquals(BASE_PATH + "/login/error?error_code=GENERIC_HTML_ERROR"
                                        + "&redirect_uri=" + URLEncoder.encode(callback, StandardCharsets.UTF_8)
                                        + "&state=" + state + "&client_id=" + clientId,
                                        acsResponse.getHeader("Location"));
                }
                Assertions.assertNull(acsResponse.getHeader("Set-Cookie"));
                Mockito.verify(cloudWatchConnectorImpl).sendBrowserBindingMetricData(outcome.name());
                Mockito.verify(samlServiceImpl, Mockito.never()).checkSAMLStatus(
                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
                Mockito.verify(samlServiceImpl, Mockito.never()).validateSAMLResponse(
                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
                Mockito.verifyNoInteractions(oidcServiceImpl);
        }

        @Test
        @SneakyThrows
        void samlACS_ok() {
                // given
                Mockito.when(browserBindingService.enabled()).thenReturn(true);
                Mockito.when(browserBindingService.verify(Mockito.any(), Mockito.any()))
                                .thenReturn(BrowserBindingService.Outcome.MATCHED);
                Mockito.when(browserBindingService.clear(Mockito.any()))
                                .thenReturn("__Host-OI-test=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=None");
                Map<String, String> samlResponseDTO = new HashMap<>();
                samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
                samlResponseDTO.put("RelayState", "dummyRelayState");

                // setup samlServiceImplMock

                Response response = Mockito.mock(Response.class);
                Mockito.when(response.getInResponseTo()).thenReturn("Dummy");
                Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

                doNothing().when(samlServiceImpl)
                                .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any());
                doNothing().when(samlServiceImpl)
                                .validateSAMLResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(),
                                                Mockito.any());

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
                when(authorizationResponse.getState()).thenReturn(new State("DummyState"));
                Mockito.when(authorizationResponse.toSuccessResponse())
                                .thenReturn(authorizationSuccessResponse);

                Mockito.when(oidcServiceImpl.getAuthorizationResponse(Mockito.any()))
                                .thenReturn(authorizationResponse);

                // setup of samlSessionService mock and oidcSessionService mock is implemented
                // in MockSAMLControllerSessionServiceImpl class
                // due to its @Dependent scope which does not permit to use the @InjectMock
                // annotation

                // location header to verify
                String headerLocation = "test?code=" + authorizationCode + "&state="
                                + authorizationResponse.getState();
                var acsResponse = given()
                                .formParams(samlResponseDTO)
                                .when()
                                .post("/acs")
                                .then()
                                .statusCode(302)
                                .extract().response();

                Assertions.assertTrue(acsResponse.getHeader("location").contains(headerLocation));
                Assertions.assertTrue(acsResponse.getHeader("Set-Cookie").contains("__Host-OI-test=; Max-Age=0"));
        }

        @Test
        @SneakyThrows
        void samlACS_withRequestedAuthLevel_usesSessionAuthLevel() {
                Map<String, String> samlResponseDTO = new HashMap<>();
                samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
                samlResponseDTO.put("RelayState", "withRequestedAuthLevel");

                Response response = Mockito.mock(Response.class);
                Mockito.when(response.getInResponseTo()).thenReturn("withRequestedAuthLevel");
                Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

                doNothing().when(samlServiceImpl)
                                .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any());
                doNothing().when(samlServiceImpl)
                                .validateSAMLResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(),
                                                Mockito.any());

                AuthorizationRequest authorizationRequest = Mockito.mock(AuthorizationRequest.class);
                Mockito.when(oidcServiceImpl.buildAuthorizationRequest(Mockito.any()))
                                .thenReturn(authorizationRequest);
                AuthorizationResponse authorizationResponse = Mockito.mock(AuthorizationResponse.class);
                AuthorizationSuccessResponse authorizationSuccessResponse = Mockito.mock(
                                AuthorizationSuccessResponse.class);
                AuthorizationCode authorizationCode = Mockito.mock(AuthorizationCode.class);

                Mockito.when(authorizationCode.getValue()).thenReturn("DummyCode");
                Mockito.when(authorizationCode.toString()).thenReturn("DummyCode");
                Mockito.when(authorizationSuccessResponse.getAuthorizationCode()).thenReturn(authorizationCode);
                Mockito.when(authorizationSuccessResponse.getState()).thenReturn(new State("DummyState"));
                when(authorizationResponse.getState()).thenReturn(new State("DummyState"));
                Mockito.when(authorizationResponse.toSuccessResponse())
                                .thenReturn(authorizationSuccessResponse);
                Mockito.when(oidcServiceImpl.getAuthorizationResponse(Mockito.any()))
                                .thenReturn(authorizationResponse);

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

                Mockito.verify(samlServiceImpl).validateSAMLResponse(Mockito.any(), Mockito.any(),
                                Mockito.any(), Mockito.any(), Mockito.eq(AuthLevel.L3),
                                Mockito.eq(AuthnContextComparisonType.EXACT), Mockito.any(), Mockito.any(),
                                Mockito.any(), Mockito.any());
        }

        @Test
        @SneakyThrows
        void samlACS_withoutComparisonType_usesSessionAuthLevelAndMinimumComparison() {
                Map<String, String> samlResponseDTO = new HashMap<>();
                samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
                samlResponseDTO.put("RelayState", "withoutComparisonType");

                Response response = Mockito.mock(Response.class);
                Mockito.when(response.getInResponseTo()).thenReturn("withoutComparisonType");
                Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);
                doNothing().when(samlServiceImpl)
                                .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any());
                doNothing().when(samlServiceImpl)
                                .validateSAMLResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(),
                                                Mockito.any());

                AuthorizationRequest authorizationRequest = Mockito.mock(AuthorizationRequest.class);
                Mockito.when(oidcServiceImpl.buildAuthorizationRequest(Mockito.any()))
                                .thenReturn(authorizationRequest);
                AuthorizationResponse authorizationResponse = Mockito.mock(AuthorizationResponse.class);
                AuthorizationSuccessResponse authorizationSuccessResponse = Mockito.mock(
                                AuthorizationSuccessResponse.class);
                AuthorizationCode authorizationCode = Mockito.mock(AuthorizationCode.class);
                Mockito.when(authorizationSuccessResponse.getAuthorizationCode()).thenReturn(authorizationCode);
                Mockito.when(authorizationSuccessResponse.getState()).thenReturn(new State("DummyState"));
                Mockito.when(authorizationResponse.toSuccessResponse())
                                .thenReturn(authorizationSuccessResponse);
                Mockito.when(oidcServiceImpl.getAuthorizationResponse(Mockito.any()))
                                .thenReturn(authorizationResponse);

                given()
                                .formParams(samlResponseDTO)
                                .when()
                                .post("/acs")
                                .then()
                                .statusCode(302);

                Mockito.verify(samlServiceImpl).validateSAMLResponse(Mockito.any(), Mockito.any(),
                                Mockito.any(), Mockito.any(), Mockito.eq(AuthLevel.L3),
                                Mockito.eq(AuthnContextComparisonType.MINIMUM), Mockito.any(), Mockito.any(),
                                Mockito.any(), Mockito.any());
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
                Mockito.when(browserBindingService.enabled()).thenReturn(true);
                Mockito.when(browserBindingService.verify(Mockito.any(), Mockito.any()))
                                .thenReturn(BrowserBindingService.Outcome.MATCHED);
                Map<String, String> samlResponseDTO = new HashMap<>();
                samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
                samlResponseDTO.put("RelayState", "dummyRelayState");

                // setup samlServiceImplMock

                Response response = Mockito.mock(Response.class);
                Mockito.when(response.getInResponseTo()).thenReturn("dummyInResponseTo");
                Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

                doThrow(new OneIdentityException()).when(samlServiceImpl)
                                .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any());

                // location header to verify
                String headerLocation = BASE_PATH + "/login/error?error_code=" + URLEncoder.encode(
                                ErrorCode.GENERIC_HTML_ERROR.getErrorCode(),
                                StandardCharsets.UTF_8);
                var acsResponse = given()
                                .formParams(samlResponseDTO)
                                .when()
                                .post("/acs")
                                .then()
                                .statusCode(302)
                                .extract().response();

                Assertions.assertTrue(acsResponse.getHeader("location").contains(headerLocation));
                Assertions.assertNull(acsResponse.getHeader("Set-Cookie"));
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
                                .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any());
                samlValidationException.setRedirectUri("test.com");
                doThrow(samlValidationException).when(
                                samlServiceImpl)
                                .validateSAMLResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(),
                                                Mockito.any());
                // location header to verify
                String headerLocation = BASE_PATH + "/login/error?error_code=" +
                                URLEncoder.encode(ErrorCode.IDP_ERROR_INVALID_SAML_VERSION.getErrorCode(),
                                                StandardCharsets.UTF_8)
                                +
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
                                .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any());
                doNothing().when(samlServiceImpl)
                                .validateSAMLResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(),
                                                Mockito.any());

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

                // setup of samlSessionService mock and oidcSessionService mock is implemented
                // in MockSAMLControllerSessionServiceImpl class
                // due to its @Dependent scope which does not permit to use the @InjectMock
                // annotation

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
        void samlACS_reservedStateIsEncoded() {
                // given
                Map<String, String> samlResponseDTO = new HashMap<>();
                samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
                samlResponseDTO.put("RelayState", "dummyRelayState");

                // setup samlServiceImplMock

                Response response = Mockito.mock(Response.class);
                Mockito.when(response.getInResponseTo()).thenReturn("dummyInResponseTo");
                Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any())).thenReturn(response);

                doNothing().when(samlServiceImpl)
                                .checkSAMLStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any());
                doNothing().when(samlServiceImpl)
                                .validateSAMLResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                Mockito.any(),
                                                Mockito.any());

                // setup oidcServiceImpl mock
                AuthorizationRequest authorizationRequest = Mockito.mock(AuthorizationRequest.class);
                Mockito.when(oidcServiceImpl.buildAuthorizationRequest(Mockito.any()))
                                .thenReturn(authorizationRequest);
                // mocking of AuthorizationResponse object
                AuthorizationResponse authorizationResponse = Mockito.mock(AuthorizationResponse.class);
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

                // setup of samlSessionService mock and oidcSessionService mock is implemented
                // in MockSAMLControllerSessionServiceImpl class
                // due to its @Dependent scope which does not permit to use the @InjectMock
                // annotation

                // location header to verify
                String headerLocation = "test?code=DummyCode&state=" + URLEncoder.encode(
                                "DummyState<<<>>???!!!@", StandardCharsets.UTF_8);
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
                Map<String, String> samlResponseDTO = new HashMap<>();
                samlResponseDTO.put("SAMLResponse", "dummySAMLResponse");
                samlResponseDTO.put("RelayState", "dummyRelayState");

                Mockito.when(samlServiceImpl.getSAMLResponseFromString(Mockito.any()))
                                .thenThrow(new OneIdentityException(
                                                ErrorCode.IDP_ERROR_MULTIPLE_SAMLRESPONSE_SIGNATURES_PRESENT));

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
