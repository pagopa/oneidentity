package it.pagopa.oneid.service;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import it.pagopa.oneid.common.connector.PDVApiClient;
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.utils.SSMConnectorUtilsImpl;
import it.pagopa.oneid.connector.CloudWatchConnectorImpl;
import it.pagopa.oneid.exception.InvalidAccessTokenException;
import it.pagopa.oneid.model.session.AccessTokenSession;
import it.pagopa.oneid.model.session.SAMLSession;
import it.pagopa.oneid.model.session.enums.RecordType;
import it.pagopa.oneid.service.utils.OIDCUtils;
import it.pagopa.oneid.web.dto.AuthorizationRequestDTOExtended;
import jakarta.ws.rs.ProcessingException;

class UserInfoServiceImplTest {

  @Test
  void getSignedUserInfo_invalidJwtThrowsInvalidAccessTokenException() throws Exception {
    UserInfoServiceImpl userInfoService = new UserInfoServiceImpl();
    SessionServiceImpl<AccessTokenSession> accessTokenSessionService = mock(SessionServiceImpl.class);
    SessionServiceImpl<SAMLSession> samlSessionService = mock(SessionServiceImpl.class);
    CloudWatchConnectorImpl cloudWatchConnector = mock(CloudWatchConnectorImpl.class);
    AccessTokenSession accessTokenSession = mock(AccessTokenSession.class);
    SAMLSession samlSession = mock(SAMLSession.class);
    AuthorizationRequestDTOExtended authorizationRequest = mock(AuthorizationRequestDTOExtended.class);

    setField(userInfoService, "accessTokenSessionService", accessTokenSessionService);
    setField(userInfoService, "samlSessionService", samlSessionService);
    setField(userInfoService, "cloudWatchConnectorImpl", cloudWatchConnector);

    when(accessTokenSessionService.getSession("access-token", RecordType.ACCESS_TOKEN))
        .thenReturn(accessTokenSession);
    when(accessTokenSession.getSamlRequestID()).thenReturn("saml-request-id");
    when(samlSessionService.getSession("saml-request-id", RecordType.SAML)).thenReturn(samlSession);
    when(samlSession.getAuthorizationRequestDTOExtended()).thenReturn(authorizationRequest);
    when(authorizationRequest.getClientId()).thenReturn("client-id");
    when(authorizationRequest.getNonce()).thenReturn("nonce-value");
    when(accessTokenSession.getIdToken()).thenReturn("not-a-jwt");

    InvalidAccessTokenException exception = assertThrows(InvalidAccessTokenException.class,
        () -> userInfoService.getSignedUserInfo("access-token"));

    assertEquals("invalid_token", exception.getMessage());
  }

  @Test
  void getSignedUserInfo_returnsSignedJwtWithPairwiseClaimWhenAlreadyAvailable() throws Exception {
    UserInfoServiceImpl userInfoService = new UserInfoServiceImpl();
    SessionServiceImpl<AccessTokenSession> accessTokenSessionService = mock(SessionServiceImpl.class);
    SessionServiceImpl<SAMLSession> samlSessionService = mock(SessionServiceImpl.class);
    OIDCUtils oidcUtils = mock(OIDCUtils.class);
    CloudWatchConnectorImpl cloudWatchConnector = mock(CloudWatchConnectorImpl.class);
    AccessTokenSession accessTokenSession = mock(AccessTokenSession.class);
    SAMLSession samlSession = mock(SAMLSession.class);
    AuthorizationRequestDTOExtended authorizationRequest = mock(AuthorizationRequestDTOExtended.class);

    setField(userInfoService, "accessTokenSessionService", accessTokenSessionService);
    setField(userInfoService, "samlSessionService", samlSessionService);
    setField(userInfoService, "oidcUtils", oidcUtils);
    setField(userInfoService, "cloudWatchConnectorImpl", cloudWatchConnector);
    setField(userInfoService, "clientsMap", Map.of());

    when(accessTokenSessionService.getSession("access-token", RecordType.ACCESS_TOKEN))
        .thenReturn(accessTokenSession);
    when(accessTokenSession.getIdToken()).thenReturn(createSerializedSignedJwt("pairwise-value"));
    when(accessTokenSession.getSamlRequestID()).thenReturn("saml-request-id");
    when(samlSessionService.getSession("saml-request-id", RecordType.SAML)).thenReturn(samlSession);
    when(samlSession.getAuthorizationRequestDTOExtended()).thenReturn(authorizationRequest);
    when(authorizationRequest.getClientId()).thenReturn("client-id");
    when(oidcUtils.createSignedJWT(any(JWTClaimsSet.class))).thenReturn("signed-userinfo-jwt");

    String signedUserInfoJwt = userInfoService.getSignedUserInfo("access-token");

    assertEquals("signed-userinfo-jwt", signedUserInfoJwt);
    verify(cloudWatchConnector).sendUserInfoSuccessMetricData("client-id");
  }

  @Test
  void getSignedUserInfo_pdvFailureReturnsSignedJwtWithoutPairwiseClaim() throws Exception {
    UserInfoServiceImpl userInfoService = new UserInfoServiceImpl();
    SessionServiceImpl<AccessTokenSession> accessTokenSessionService = mock(SessionServiceImpl.class);
    SessionServiceImpl<SAMLSession> samlSessionService = mock(SessionServiceImpl.class);
    OIDCUtils oidcUtils = mock(OIDCUtils.class);
    CloudWatchConnectorImpl cloudWatchConnector = mock(CloudWatchConnectorImpl.class);
    SSMConnectorUtilsImpl ssmConnectorUtils = mock(SSMConnectorUtilsImpl.class);
    PDVApiClient pdvApiClient = mock(PDVApiClient.class);
    AccessTokenSession accessTokenSession = mock(AccessTokenSession.class);
    SAMLSession samlSession = mock(SAMLSession.class);
    AuthorizationRequestDTOExtended authorizationRequest = mock(AuthorizationRequestDTOExtended.class);
    Client client = mock(Client.class);

    setField(userInfoService, "accessTokenSessionService", accessTokenSessionService);
    setField(userInfoService, "samlSessionService", samlSessionService);
  setField(userInfoService, "oidcUtils", oidcUtils);
    setField(userInfoService, "cloudWatchConnectorImpl", cloudWatchConnector);
    setField(userInfoService, "ssmConnectorUtilsImpl", ssmConnectorUtils);
    setField(userInfoService, "pdvApiClient", pdvApiClient);
    setField(userInfoService, "clientsMap", Map.of("client-id", client));
    setField(userInfoService, "pairwiseEnabled", true);
    setField(userInfoService, "registryEnabled", false);

    when(accessTokenSessionService.getSession("access-token", RecordType.ACCESS_TOKEN))
        .thenReturn(accessTokenSession);
    when(accessTokenSession.getIdToken()).thenReturn(createSerializedSignedJwt(null));
    when(accessTokenSession.getPairwise()).thenReturn(null);
    when(accessTokenSession.getSamlRequestID()).thenReturn("saml-request-id");
    when(samlSessionService.getSession("saml-request-id", RecordType.SAML)).thenReturn(samlSession);
    when(samlSession.getAuthorizationRequestDTOExtended()).thenReturn(authorizationRequest);
    when(authorizationRequest.getClientId()).thenReturn("client-id");
    when(client.isPairwise()).thenReturn(true);
    when(ssmConnectorUtils.getParameter("/pdv/client-id")).thenReturn(Optional.of("pdv-api-key"));
    when(pdvApiClient.upsertUser(any(), anyString())).thenThrow(new ProcessingException("pdv down"));
    when(oidcUtils.createSignedJWT(any(JWTClaimsSet.class))).thenReturn("signed-userinfo-jwt");

    String signedUserInfoJwt = userInfoService.getSignedUserInfo("access-token");

    assertEquals("signed-userinfo-jwt", signedUserInfoJwt);
    verify(cloudWatchConnector).sendUserInfoSuccessWithoutPairwiseMetricData("client-id");
    verify(accessTokenSessionService, never()).setAccessTokenPairwise(anyString(), anyString());
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = UserInfoServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private String createSerializedSignedJwt(String pairwise) {
    JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder()
        .subject("subject-123")
        .issuer("https://issuer.example.com")
        .audience("client-id")
        .issueTime(new Date())
        .expirationTime(new Date(System.currentTimeMillis() + 60000))
        .claim("nonce", "nonce-value")
        .claim("sameIdp", true)
        .claim("fiscalNumber", "ABCDEF12G34H567I");

    if (pairwise != null) {
      claimsSet.claim("pairwise", pairwise);
    }

    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
        claimsSet.build());

    return signedJWT.getHeader().toBase64URL()
        + "."
        + signedJWT.getPayload().toBase64URL()
        + ".c2lnbmF0dXJl";
  }
}
