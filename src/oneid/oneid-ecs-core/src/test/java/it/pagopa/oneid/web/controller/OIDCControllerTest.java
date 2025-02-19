package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static it.pagopa.oneid.model.session.enums.ResponseType.CODE;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.oneid.common.model.IDP;
import it.pagopa.oneid.common.model.enums.GrantType;
import it.pagopa.oneid.common.model.enums.IDPStatus;
import it.pagopa.oneid.common.model.enums.LatestTAG;
import it.pagopa.oneid.common.model.exception.OneIdentityException;
import it.pagopa.oneid.model.dto.JWKSSetDTO;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.service.utils.SAMLUtilsExtendedCore;
import it.pagopa.oneid.web.controller.mock.OIDCControllerTestProfile;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedGet;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtendedPost;
import it.pagopa.oneid.web.dto.TokenDataDTO;
import it.pagopa.oneid.web.dto.TokenRequestDTOExtended;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.w3c.dom.Element;

@TestProfile(OIDCControllerTestProfile.class)
@TestHTTPEndpoint(OIDCController.class)
@QuarkusTest
class OIDCControllerTest {

  private final String CLIENT_ID = "test";


  @InjectMock
  private SAMLServiceImpl samlServiceImpl;

  // This will be mocked using @Alternative construct because of @Dependant scope
  @Inject
  private SessionServiceImpl<SAMLSession> samlSessionServiceImpl;

  @InjectMock
  private OIDCServiceImpl oidcServiceImpl;

  @Inject
  private SAMLUtilsExtendedCore samlUtilsExtendedCore;

  @Test
  @SneakyThrows
  void getObject_Exception() {
    //given

    //Class
    OIDCController oidcController = new OIDCController();
    //Method
    Method getObject = OIDCController.class.getDeclaredMethod("getObject", Object.class);
    getObject.setAccessible(true);

    //when
    Executable executable = () -> getObject.invoke(oidcController, "test");

    //then
    InvocationTargetException exception = Assertions.assertThrows(InvocationTargetException.class,
        executable);
    Assertions.assertTrue(
        exception.getCause().getMessage().contains("Invalid object for /oidc/authorize route"));

  }

  @Test
  void keys() {
    JWKSSetDTO jwksPublicKey = mock(JWKSSetDTO.class);
    when(oidcServiceImpl.getJWKSPublicKey()).thenReturn(jwksPublicKey);

    given()
        .when().get("/keys")
        .then()
        .statusCode(200)
        .body(notNullValue());
  }

  //region /authorize
  @Test
  @SneakyThrows
  void authorizePost() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = getAuthorizationRequestDTOExtendedPost();

    //when

    // Mock "2. Check if idp exists"

    IDP testIDP = IDP.builder()
        .entityID("https://localhost:8443")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    Mockito.when(samlServiceImpl.getIDPFromEntityID(Mockito.any()))
        .thenReturn(Optional.of(testIDP));

    // Mock "6. Create SAML Authn Request using SAMLServiceImpl"
    AuthnRequest authnRequest = buildAuthnRequest("https://demo.spid.gov.it");

    Mockito.when(
            samlServiceImpl.buildAuthnRequest(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString()))
        .thenReturn(authnRequest);

    Mockito.when(oidcServiceImpl.getStringValue(Mockito.any())).thenReturn("test");
    Element elementMock = Mockito.mock(Element.class);
    Mockito.when(oidcServiceImpl.getElementValueFromAuthnRequest(Mockito.any()))
        .thenReturn(elementMock);

    //then
    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedPost.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedPost.getIdp(),
            "client_id", authorizationRequestDTOExtendedPost.getClientId(),
            "response_type", authorizationRequestDTOExtendedPost.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedPost.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedPost.getScope(),
            "nonce", authorizationRequestDTOExtendedPost.getNonce(),
            "state", authorizationRequestDTOExtendedPost.getState()
        ))
        .when().post("/authorize")
        .then()
        .statusCode(200)
        .body(notNullValue());
  }

  @Test
  void authorizePost_ClientNotFound() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = getAuthorizationRequestDTOExtendedPost();
    authorizationRequestDTOExtendedPost.setClientId("no_client");

    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedPost.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedPost.getIdp(),
            "client_id", authorizationRequestDTOExtendedPost.getClientId(),
            "response_type", authorizationRequestDTOExtendedPost.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedPost.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedPost.getScope(),
            "nonce", authorizationRequestDTOExtendedPost.getNonce(),
            "state", authorizationRequestDTOExtendedPost.getState()
        ))
        .when().post("/authorize")
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .body(notNullValue());

  }

  @Test
  @SneakyThrows
  void authorizePost_RedirectUriNotFound() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = getAuthorizationRequestDTOExtendedPost();
    authorizationRequestDTOExtendedPost.setRedirectUri("test");

    //then
    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedPost.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedPost.getIdp(),
            "client_id", authorizationRequestDTOExtendedPost.getClientId(),
            "response_type", authorizationRequestDTOExtendedPost.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedPost.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedPost.getScope(),
            "nonce", authorizationRequestDTOExtendedPost.getNonce(),
            "state", authorizationRequestDTOExtendedPost.getState()
        ))
        .when().post("/authorize")
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .body(notNullValue());
  }

  @Test
  @SneakyThrows
  void authorizePost_idpEmpty() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = getAuthorizationRequestDTOExtendedPost();

    //then
    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedPost.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedPost.getIdp(),
            "client_id", authorizationRequestDTOExtendedPost.getClientId(),
            "response_type", authorizationRequestDTOExtendedPost.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedPost.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedPost.getScope(),
            "nonce", authorizationRequestDTOExtendedPost.getNonce(),
            "state", authorizationRequestDTOExtendedPost.getState()
        ))
        .when().post("/authorize")
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .body(notNullValue());
  }

  @Test
  @SneakyThrows
  void authorizePost_getEntityDescriptorFromEntityID_Exception() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = getAuthorizationRequestDTOExtendedPost();

    //then
    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedPost.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedPost.getIdp(),
            "client_id", authorizationRequestDTOExtendedPost.getClientId(),
            "response_type", authorizationRequestDTOExtendedPost.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedPost.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedPost.getScope(),
            "nonce", authorizationRequestDTOExtendedPost.getNonce(),
            "state", authorizationRequestDTOExtendedPost.getState()
        ))
        .when().post("/authorize")
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .body(notNullValue());
  }

  @Test
  @SneakyThrows
  void authorizePost_authorizePost_ScopeNull() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = getAuthorizationRequestDTOExtendedPost();
    authorizationRequestDTOExtendedPost.setScope("");

    //when

    // Mock "2. Check if idp exists"
    IDP testIDP = IDP.builder()
        .entityID("https://localhost:8443")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    Mockito.when(samlServiceImpl.getIDPFromEntityID(Mockito.any()))
        .thenReturn(Optional.of(testIDP));

    // Mock "6. Create SAML Authn Request using SAMLServiceImpl"
    AuthnRequest authnRequest = buildAuthnRequest("https://demo.spid.gov.it");

    Mockito.when(
            samlServiceImpl.buildAuthnRequest(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString()))
        .thenReturn(authnRequest);

    Mockito.when(oidcServiceImpl.getStringValue(Mockito.any())).thenReturn("test");
    Element elementMock = Mockito.mock(Element.class);
    Mockito.when(oidcServiceImpl.getElementValueFromAuthnRequest(Mockito.any()))
        .thenReturn(elementMock);

    //then
    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedPost.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedPost.getIdp(),
            "client_id", authorizationRequestDTOExtendedPost.getClientId(),
            "response_type", authorizationRequestDTOExtendedPost.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedPost.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedPost.getScope(),
            "nonce", authorizationRequestDTOExtendedPost.getNonce(),
            "state", authorizationRequestDTOExtendedPost.getState()
        ))
        .when().post("/authorize")
        .then()
        .statusCode(400)
        .body(notNullValue());
  }

  @Test
  @SneakyThrows
  void authorizePost_InvalidScopeException_NotOpenid() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = getAuthorizationRequestDTOExtendedPost();
    authorizationRequestDTOExtendedPost.setScope("test");

    // Mock "2. Check if idp exists"
    IDP testIDP = IDP.builder()
        .entityID("https://localhost:8443")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    Mockito.when(samlServiceImpl.getIDPFromEntityID(Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    //then
    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedPost.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedPost.getIdp(),
            "client_id", authorizationRequestDTOExtendedPost.getClientId(),
            "response_type", authorizationRequestDTOExtendedPost.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedPost.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedPost.getScope(),
            "nonce", authorizationRequestDTOExtendedPost.getNonce(),
            "state", authorizationRequestDTOExtendedPost.getState()
        ))
        .when().post("/authorize")
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .body(notNullValue());
  }

  @Test
  @SneakyThrows
  void authorizePost_UnsupportedResponseTypeException() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = getAuthorizationRequestDTOExtendedPost();
    authorizationRequestDTOExtendedPost.setResponseType(ResponseType.NONE);

    // Mock "2. Check if idp exists"
    IDP testIDP = IDP.builder()
        .entityID("https://localhost:8443")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    Mockito.when(samlServiceImpl.getIDPFromEntityID(Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    //then
    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedPost.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedPost.getIdp(),
            "client_id", authorizationRequestDTOExtendedPost.getClientId(),
            "response_type", authorizationRequestDTOExtendedPost.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedPost.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedPost.getScope(),
            "nonce", authorizationRequestDTOExtendedPost.getNonce(),
            "state", authorizationRequestDTOExtendedPost.getState()
        ))
        .when().post("/authorize")
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .body(notNullValue());
  }

  @Test
  @SneakyThrows
  void authorizePost_buildAuthnRequest_Exception() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = getAuthorizationRequestDTOExtendedPost();

    // Mock "2. Check if idp exists"
    IDP testIDP = IDP.builder()
        .entityID("https://localhost:8443")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    Mockito.when(samlServiceImpl.getIDPFromEntityID(Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    // Mock "6. Create SAML Authn Request using SAMLServiceImpl" with error
    Mockito.when(
            samlServiceImpl.buildAuthnRequest(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString()))
        .thenThrow(OneIdentityException.class);

    //then
    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedPost.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedPost.getIdp(),
            "client_id", authorizationRequestDTOExtendedPost.getClientId(),
            "response_type", authorizationRequestDTOExtendedPost.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedPost.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedPost.getScope(),
            "nonce", authorizationRequestDTOExtendedPost.getNonce(),
            "state", authorizationRequestDTOExtendedPost.getState()
        ))
        .when().post("/authorize")
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .body(notNullValue());
  }

  @Test
  @SneakyThrows
  void authorizePost_SessionException() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = getAuthorizationRequestDTOExtendedPost();
    authorizationRequestDTOExtendedPost.setIdp("testSessionException");

    // Mock "2. Check if idp exists"
    IDP testIDP = IDP.builder()
        .entityID("https://localhost:8443")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    Mockito.when(samlServiceImpl.getIDPFromEntityID(Mockito.any()))
        .thenReturn(Optional.of(testIDP));
    // Mock "6. Create SAML Authn Request using SAMLServiceImpl"
    AuthnRequest authnRequest = buildAuthnRequest("https://localhost:8443");

    Mockito.when(
            samlServiceImpl.buildAuthnRequest(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString()))
        .thenReturn(authnRequest);

    Mockito.when(oidcServiceImpl.getStringValue(Mockito.any())).thenReturn("test");
    Element elementMock = Mockito.mock(Element.class);
    Mockito.when(oidcServiceImpl.getElementValueFromAuthnRequest(Mockito.any()))
        .thenReturn(elementMock);

    //then
    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedPost.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedPost.getIdp(),
            "client_id", authorizationRequestDTOExtendedPost.getClientId(),
            "response_type", authorizationRequestDTOExtendedPost.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedPost.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedPost.getScope(),
            "nonce", authorizationRequestDTOExtendedPost.getNonce(),
            "state", authorizationRequestDTOExtendedPost.getState()
        ))
        .when().post("/authorize")
        .then()
        .statusCode(Status.FOUND.getStatusCode())
        .body(notNullValue());
  }

  @Test
  @SneakyThrows
  void authorizeGet() {
    //given

    // AuthorizationDTO Creation
    AuthorizationRequestDTOExtendedGet authorizationRequestDTOExtendedGet = new AuthorizationRequestDTOExtendedGet();
    authorizationRequestDTOExtendedGet.setIpAddress("test");
    authorizationRequestDTOExtendedGet.setIdp("test");
    authorizationRequestDTOExtendedGet.setClientId(CLIENT_ID);
    authorizationRequestDTOExtendedGet.setResponseType(CODE);
    authorizationRequestDTOExtendedGet.setRedirectUri("foo.bar");
    authorizationRequestDTOExtendedGet.setScope("openid");
    authorizationRequestDTOExtendedGet.setNonce("test");
    authorizationRequestDTOExtendedGet.setState("test");

    //when

    // Mock "2. Check if idp exists"
    IDP testIDP = IDP.builder()
        .entityID("https://localhost:8443")
        .certificates(Set.of(
            "MIIEGDCCAwCgAwIBAgIJAOrYj9oLEJCwMA0GCSqGSIb3DQEBCwUAMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDAeFw0xOTA0MTExMDAyMDhaFw0yNTAzMDgxMDAyMDhaMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK8kJVo+ugRrbbv9xhXCuVrqi4B7/MQzQc62ocwlFFujJNd4m1mXkUHFbgvwhRkQqo2DAmFeHiwCkJT3K1eeXIFhNFFroEzGPzONyekLpjNvmYIs1CFvirGOj0bkEiGaKEs+/umzGjxIhy5JQlqXE96y1+Izp2QhJimDK0/KNij8I1bzxseP0Ygc4SFveKS+7QO+PrLzWklEWGMs4DM5Zc3VRK7g4LWPWZhKdImC1rnS+/lEmHSvHisdVp/DJtbSrZwSYTRvTTz5IZDSq4kAzrDfpj16h7b3t3nFGc8UoY2Ro4tRZ3ahJ2r3b79yK6C5phY7CAANuW3gDdhVjiBNYs0CAwEAAaOByjCBxzAdBgNVHQ4EFgQU3/7kV2tbdFtphbSA4LH7+w8SkcwwgZcGA1UdIwSBjzCBjIAU3/7kV2tbdFtphbSA4LH7+w8SkcyhaaRnMGUxCzAJBgNVBAYTAklUMQ4wDAYDVQQIEwVJdGFseTENMAsGA1UEBxMEUm9tZTENMAsGA1UEChMEQWdJRDESMBAGA1UECxMJQWdJRCBURVNUMRQwEgYDVQQDEwthZ2lkLmdvdi5pdIIJAOrYj9oLEJCwMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAJNFqXg/V3aimJKUmUaqmQEEoSc3qvXFITvT5f5bKw9yk/NVhR6wndL+z/24h1OdRqs76blgH8k116qWNkkDtt0AlSjQOx5qvFYh1UviOjNdRI4WkYONSw+vuavcx+fB6O5JDHNmMhMySKTnmRqTkyhjrch7zaFIWUSV7hsBuxpqmrWDoLWdXbV3eFH3mINA5AoIY/m0bZtzZ7YNgiFWzxQgekpxd0vcTseMnCcXnsAlctdir0FoCZztxMuZjlBjwLTtM6Ry3/48LMM8Z+lw7NMciKLLTGQyU8XmKKSSOh0dGh5Lrlt5GxIIJkH81C0YimWebz8464QPL3RbLnTKg+c="))
        .friendlyName("Test IDP")
        .idpSSOEndpoints(Map.of("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
            "https://localhost:8443/samlsso"))
        .isActive(true)
        .pointer(String.valueOf(LatestTAG.LATEST_SPID))
        .status(IDPStatus.OK)
        .build();

    Mockito.when(samlServiceImpl.getIDPFromEntityID(Mockito.any()))
        .thenReturn(Optional.of(testIDP));

    // Mock "6. Create SAML Authn Request using SAMLServiceImpl"
    AuthnRequest authnRequest = buildAuthnRequest("https://demo.spid.gov.it");

    Mockito.when(
            samlServiceImpl.buildAuthnRequest(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString()))
        .thenReturn(authnRequest);

    Mockito.when(oidcServiceImpl.getStringValue(Mockito.any())).thenReturn("test");
    Element elementMock = Mockito.mock(Element.class);
    Mockito.when(oidcServiceImpl.getElementValueFromAuthnRequest(Mockito.any()))
        .thenReturn(elementMock);

    //then
    given()
        .contentType("application/x-www-form-urlencoded")
        .header("X-Forwarded-For", authorizationRequestDTOExtendedGet.getIpAddress())
        .formParams(Map.of(
            "idp", authorizationRequestDTOExtendedGet.getIdp(),
            "client_id", authorizationRequestDTOExtendedGet.getClientId(),
            "response_type", authorizationRequestDTOExtendedGet.getResponseType(),
            "redirect_uri", authorizationRequestDTOExtendedGet.getRedirectUri(),
            "scope", authorizationRequestDTOExtendedGet.getScope(),
            "nonce", authorizationRequestDTOExtendedGet.getNonce(),
            "state", authorizationRequestDTOExtendedGet.getState()
        ))
        .when().get("/authorize")
        .then()
        .statusCode(200)
        .body(notNullValue());
  }
  //endregion

  //region /token
  @Test
  @SneakyThrows
  void token() {

    String clientSecret = "testClientSecret";
    String code = "token";
    String redirectUri = "https://client.example.com/callback";

    // TokenRequest creation
    TokenRequestDTOExtended tokenRequest = new TokenRequestDTOExtended();
    tokenRequest.setGrantType(GrantType.AUTHORIZATION_CODE);
    tokenRequest.setAuthorization(
        "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + clientSecret).getBytes()));
    tokenRequest.setCode(code);
    tokenRequest.setRedirectUri(redirectUri);

    doNothing().when(oidcServiceImpl).authorizeClient(anyString(), anyString());

    Response mockSamlResponse = mock(
        org.opensaml.saml.saml2.core.Response.class);
    Assertion mockAssertion = mock(Assertion.class);
    List<Assertion> assertions = List.of(mockAssertion);
    // Ensure the mock returns a non-null list of assertions
    when(mockSamlResponse.getAssertions()).thenReturn(assertions);
    // Ensure `getSAMLResponseFromString` returns the mocked response
    when(samlServiceImpl.getSAMLResponseFromString(anyString())).thenReturn(mockSamlResponse);

    Assertion assertion = mock(Assertion.class);

    when(samlServiceImpl.getAttributesFromSAMLAssertion(assertion))
        .thenReturn(new ArrayList<>());

    TokenDataDTO tokenDataDTO = mock(TokenDataDTO.class);
    when(
        oidcServiceImpl.getOIDCTokens(anyString(), anyString(), Mockito.anyList(),
            anyString())).thenReturn(tokenDataDTO);

    given()
        .contentType("application/x-www-form-urlencoded")
        .header("Authorization", tokenRequest.getAuthorization())
        .formParam("code", tokenRequest.getCode())
        .formParam("redirect_uri", tokenRequest.getRedirectUri())
        .formParam("grant_type", tokenRequest.getGrantType())
        .when().post("/token")
        .then()
        .statusCode(200)
        .body(notNullValue());

  }

  @Test
  @SneakyThrows
  void token_UnsupportedGrantTypeException() {

    String clientSecret = "testClientSecret";
    String code = "token";
    String redirectUri = "https://client.example.com/callback";

    // TokenRequest creation
    TokenRequestDTOExtended tokenRequest = new TokenRequestDTOExtended();
    tokenRequest.setGrantType(GrantType.REFRESH_TOKEN);
    tokenRequest.setAuthorization(
        "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + clientSecret).getBytes()));
    tokenRequest.setCode(code);
    tokenRequest.setRedirectUri(redirectUri);

    given()
        .contentType("application/x-www-form-urlencoded")
        .header("Authorization", tokenRequest.getAuthorization())
        .formParam("code", tokenRequest.getCode())
        .formParam("redirect_uri", tokenRequest.getRedirectUri())
        .formParam("grant_type", tokenRequest.getGrantType())
        .when().post("/token")
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body(notNullValue());

  }

  @Test
  @SneakyThrows
  void token_InvalidRequestMalformedHeaderAuthorizationException() {

    String clientSecret = "testClientSecret";
    String code = "token";
    String redirectUri = "https://client.example.com/callback";

    // TokenRequest creation
    TokenRequestDTOExtended tokenRequest = new TokenRequestDTOExtended();
    tokenRequest.setGrantType(GrantType.AUTHORIZATION_CODE);
    tokenRequest.setAuthorization(
        "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + "+" + clientSecret).getBytes()));
    tokenRequest.setCode(code);
    tokenRequest.setRedirectUri(redirectUri);

    given()
        .contentType("application/x-www-form-urlencoded")
        .header("Authorization", tokenRequest.getAuthorization())
        .formParam("code", tokenRequest.getCode())
        .formParam("redirect_uri", tokenRequest.getRedirectUri())
        .formParam("grant_type", tokenRequest.getGrantType())
        .when().post("/token")
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body(notNullValue())
    ;

  }

  @Test
  @SneakyThrows
  void token_InvalidGrantException() {

    String clientSecret = "testClientSecret";
    String code = "InvalidGrantException";
    String redirectUri = "https://client.example.com/callback";

    // TokenRequest creation
    TokenRequestDTOExtended tokenRequest = new TokenRequestDTOExtended();
    tokenRequest.setGrantType(GrantType.AUTHORIZATION_CODE);
    tokenRequest.setAuthorization(
        "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + clientSecret).getBytes()));
    tokenRequest.setCode(code);
    tokenRequest.setRedirectUri(redirectUri);

    doNothing().when(oidcServiceImpl).authorizeClient(anyString(), anyString());

    given()
        .contentType("application/x-www-form-urlencoded")
        .header("Authorization", tokenRequest.getAuthorization())
        .formParam("code", tokenRequest.getCode())
        .formParam("redirect_uri", tokenRequest.getRedirectUri())
        .formParam("grant_type", tokenRequest.getGrantType())
        .when().post("/token")
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body(notNullValue());

  }

  @Test
  @SneakyThrows
  void token_RedirectUriInvalidGrantException() {

    String clientSecret = "testClientSecret";
    String code = "token";
    String redirectUri = "https://client.example.com/InvalidRedirecUri";

    // TokenRequest creation
    TokenRequestDTOExtended tokenRequest = new TokenRequestDTOExtended();
    tokenRequest.setGrantType(GrantType.AUTHORIZATION_CODE);
    tokenRequest.setAuthorization(
        "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + clientSecret).getBytes()));
    tokenRequest.setCode(code);
    tokenRequest.setRedirectUri(redirectUri);

    doNothing().when(oidcServiceImpl).authorizeClient(anyString(), anyString());

    Response mockSamlResponse = mock(
        org.opensaml.saml.saml2.core.Response.class);
    Assertion mockAssertion = mock(Assertion.class);
    List<Assertion> assertions = List.of(mockAssertion);
    // Ensure the mock returns a non-null list of assertions
    when(mockSamlResponse.getAssertions()).thenReturn(assertions);
    // Ensure `getSAMLResponseFromString` returns the mocked response
    when(samlServiceImpl.getSAMLResponseFromString(anyString())).thenReturn(mockSamlResponse);

    given()
        .contentType("application/x-www-form-urlencoded")
        .header("Authorization", tokenRequest.getAuthorization())
        .formParam("code", tokenRequest.getCode())
        .formParam("redirect_uri", tokenRequest.getRedirectUri())
        .formParam("grant_type", tokenRequest.getGrantType())
        .when().post("/token")
        .then()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body(notNullValue());

  }
  //endregion

  //region private methods


  private AuthorizationRequestDTOExtendedPost getAuthorizationRequestDTOExtendedPost() {
    AuthorizationRequestDTOExtendedPost authorizationRequestDTOExtendedPost = new AuthorizationRequestDTOExtendedPost();
    authorizationRequestDTOExtendedPost.setIpAddress("test");
    authorizationRequestDTOExtendedPost.setIdp("test");
    authorizationRequestDTOExtendedPost.setClientId(CLIENT_ID);
    authorizationRequestDTOExtendedPost.setResponseType(CODE);
    authorizationRequestDTOExtendedPost.setRedirectUri("foo.bar");
    authorizationRequestDTOExtendedPost.setScope("openid");
    authorizationRequestDTOExtendedPost.setNonce("test");
    authorizationRequestDTOExtendedPost.setState("test");
    return authorizationRequestDTOExtendedPost;
  }

  @SneakyThrows
  private AuthnRequest buildAuthnRequest(String idpID) {
    AuthnRequest authnRequest = samlUtilsExtendedCore.buildSAMLObject(AuthnRequest.class);
    authnRequest.setID("test");
    authnRequest.setDestination("https://demo.spid.gov.it");
    authnRequest.setIssueInstant(Instant.now());
    return authnRequest;
  }

  //endregion
}