package it.pagopa.oneid.web.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.oneid.common.model.enums.GrantType;
import it.pagopa.oneid.model.dto.JWKSSetDTO;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.service.OIDCServiceImpl;
import it.pagopa.oneid.service.SAMLServiceImpl;
import it.pagopa.oneid.service.SessionServiceImpl;
import it.pagopa.oneid.web.controller.mock.OIDCControllerTestProfile;
import it.pagopa.oneid.web.dto.TokenDataDTO;
import it.pagopa.oneid.web.dto.TokenRequestDTOExtended;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;

@TestProfile(OIDCControllerTestProfile.class)
@TestHTTPEndpoint(OIDCController.class)
@QuarkusTest
class OIDCControllerTest {


  @InjectMock
  private SAMLServiceImpl samlServiceImpl;

  // This will be mocked using @Alternative construct because of @Dependant scope
  @Inject
  private SessionServiceImpl<SAMLSession> samlSessionServiceImpl;

  @InjectMock
  private OIDCServiceImpl oidcServiceImpl;

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


  @Test
  void authorizePost() {
  }

  @Test
  void authorizeGet() {
  }

  //region /token
  @Test
  @SneakyThrows
  void token() {

    String clientId = "tesClientId";
    String clientSecret = "testClientSecret";
    String code = "token";
    String redirectUri = "https://client.example.com/callback";

    // TokenRequest creation
    TokenRequestDTOExtended tokenRequest = new TokenRequestDTOExtended();
    tokenRequest.setGrantType(GrantType.AUTHORIZATION_CODE);
    tokenRequest.setAuthorization(
        "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
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

    String clientId = "tesClientId";
    String clientSecret = "testClientSecret";
    String code = "token";
    String redirectUri = "https://client.example.com/callback";

    // TokenRequest creation
    TokenRequestDTOExtended tokenRequest = new TokenRequestDTOExtended();
    tokenRequest.setGrantType(GrantType.REFRESH_TOKEN);
    tokenRequest.setAuthorization(
        "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
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

    String clientId = "tesClientId";
    String clientSecret = "testClientSecret";
    String code = "token";
    String redirectUri = "https://client.example.com/callback";

    // TokenRequest creation
    TokenRequestDTOExtended tokenRequest = new TokenRequestDTOExtended();
    tokenRequest.setGrantType(GrantType.AUTHORIZATION_CODE);
    tokenRequest.setAuthorization(
        "Basic " + Base64.getEncoder().encodeToString((clientId + "+" + clientSecret).getBytes()));
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

    String clientId = "tesClientId";
    String clientSecret = "testClientSecret";
    String code = "InvalidGrantException";
    String redirectUri = "https://client.example.com/callback";

    // TokenRequest creation
    TokenRequestDTOExtended tokenRequest = new TokenRequestDTOExtended();
    tokenRequest.setGrantType(GrantType.AUTHORIZATION_CODE);
    tokenRequest.setAuthorization(
        "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
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

    String clientId = "tesClientId";
    String clientSecret = "testClientSecret";
    String code = "token";
    String redirectUri = "https://client.example.com/InvalidRedirecUri";

    // TokenRequest creation
    TokenRequestDTOExtended tokenRequest = new TokenRequestDTOExtended();
    tokenRequest.setGrantType(GrantType.AUTHORIZATION_CODE);
    tokenRequest.setAuthorization(
        "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
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
}