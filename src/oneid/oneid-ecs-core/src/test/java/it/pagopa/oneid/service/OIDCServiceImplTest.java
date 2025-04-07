package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.ResponseMode;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.oneid.common.connector.ClientConnectorImpl;
import it.pagopa.oneid.common.model.dto.SecretDTO;
import it.pagopa.oneid.connector.KMSConnectorImpl;
import it.pagopa.oneid.exception.InvalidClientException;
import it.pagopa.oneid.exception.OIDCSignJWTException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.model.dto.JWKSSetDTO;
import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.service.utils.OIDCUtils;
import it.pagopa.oneid.web.dto.TokenDataDTO;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.GetPublicKeyResponse;

@QuarkusTest
@TestProfile(MyMockyTestProfile.class)
public class OIDCServiceImplTest {

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  OIDCUtils oidcUtils;

  @Inject
  ClientConnectorImpl clientConnectorImpl;

  @Inject
  KMSConnectorImpl kmsConnectorImpl;

  private static String getPublicKeyPEM() {
    String key = """
        -----BEGIN PUBLIC KEY-----
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtaRBmc1/lka2E+ShEm/p
        drX2SSsjddhFGF16rFcOHY/F8jdG7ZMVzYqQASzykUee8tPRj6of+djRiiwWsCHT
        DqIQZtBbj5TQE96ZGZsErcUdsgvuCfaTXPUGoO+H9PnDVslfRX+Obb+V5zgUtUss
        yauDJaCECjiwrpWvqO2nQn0iJvFOvwd9CpZvUKRcG4Ie20OY605ZwiSi4QO9+XgM
        ROXSLlputAus1iYo9gPnx14KwDMNEnQmjHVytJUnzWBgOQYWN/rP6BkOgfa4qgjy
        CWeIuCM/lpIG5LPXc/DXkmQP0k/8Hvc8EhzJr5w28l1zI8gF94hUPRfviUFHMWEN
        ewIDAQAB
        -----END PUBLIC KEY-----
        """;
    return key
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replaceAll(System.lineSeparator(), "")
        .replace("-----END PUBLIC KEY-----", "");
  }

  private static String getInvalidPublicKeyPEM() {
    String key = """
        -----BEGIN PUBLIC KEY-----
        MIIBIdANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtaRBmc1/lka2E+ShEm/p
        -----END PUBLIC KEY-----
        """;
    return key
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replaceAll(System.lineSeparator(), "")
        .replace("-----END PUBLIC KEY-----", "");
  }

  @Test
  void getJWKSPublicKey() {
    // given
    String keyId = "aws/idkey";
    String publicKeyPEM = getPublicKeyPEM();

    byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

    kmsConnectorImpl = Mockito.mock(KMSConnectorImpl.class);

    GetPublicKeyResponse getPublicKeyResponse = GetPublicKeyResponse.builder()
        .publicKey(SdkBytes.fromByteArray(encoded))
        .keyId(keyId)
        .build();

    Mockito.when(kmsConnectorImpl.getPublicKey(Mockito.any())).thenReturn(getPublicKeyResponse);
    QuarkusMock.installMockForType(kmsConnectorImpl, KMSConnectorImpl.class);

    // then
    JWKSSetDTO jwksSetDTO = oidcServiceImpl.getJWKSPublicKey();

    assertFalse(jwksSetDTO.getKeyList().isEmpty());
  }

  @Test
  void getJWKSPublicKey_invalidKey() {
    // given
    String keyId = "aws/idkey";
    String publicKeyPEM = getInvalidPublicKeyPEM();

    byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

    kmsConnectorImpl = Mockito.mock(KMSConnectorImpl.class);

    GetPublicKeyResponse getPublicKeyResponse = GetPublicKeyResponse.builder()
        .publicKey(SdkBytes.fromByteArray(encoded))
        .keyId(keyId)
        .build();

    Mockito.when(kmsConnectorImpl.getPublicKey(Mockito.any())).thenReturn(getPublicKeyResponse);
    QuarkusMock.installMockForType(kmsConnectorImpl, KMSConnectorImpl.class);

    // then
    assertThrows(RuntimeException.class, () -> oidcServiceImpl.getJWKSPublicKey());
  }

  @Test
  void buildOIDCProviderMetadata() {
    assertDoesNotThrow(() -> oidcServiceImpl.buildOIDCProviderMetadata());
  }

  @Test
  void buildAuthorizationRequest() {
    // given
    AuthorizationRequestDTO authorizationRequestDTO = new AuthorizationRequestDTO("test",
        ResponseType.CODE, "test.com", "dummy", "foo", "bar");

    AuthorizationRequest authorizationRequestResponse = oidcServiceImpl.buildAuthorizationRequest(
        authorizationRequestDTO);

    // then
    assertEquals(authorizationRequestResponse.getClientID().getValue(),
        authorizationRequestDTO.getClientId());
  }

  @Test
  void buildAuthorizationRequest_RuntimeException() {
    // given
    AuthorizationRequestDTO authorizationRequestDTO = new AuthorizationRequestDTO("test",
        ResponseType.CODE, "test..!!@@<>com", "dummy", "foo", "bar");
    // then
    assertThrows(RuntimeException.class, () -> oidcServiceImpl.buildAuthorizationRequest(
        authorizationRequestDTO));
  }

  @Test
  void getAuthorizationResponse() throws URISyntaxException {
    // given
    AuthorizationRequest authorizationRequest = new AuthorizationRequest(new URI("foo.bar"),
        com.nimbusds.oauth2.sdk.ResponseType.CODE, ResponseMode.JWT, new ClientID("test"),
        new URI("foo.bar"), new Scope("dummy_scope"), new State("dummy_state"));

    // then
    AuthorizationResponse authorizationResponse = oidcServiceImpl.getAuthorizationResponse(
        authorizationRequest);
    assertEquals(authorizationResponse.getState(), authorizationRequest.getState());
  }

  @Test
  void getOIDCTokens() throws ParseException {
    // given
    String requestID = "dummyId";
    String clientID = "foobar";
    String nonce = "dummyNonce";
    ArrayList<AttributeDTO> attributeDTOList = new ArrayList<>();

    oidcUtils = Mockito.mock(OIDCUtils.class);
    String validJWT = "\n"
        + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MjQ4NTMxODcsImV4cCI6MTc1NjM4OTE4NywiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsImZpc2NhbE51bWJlciI6InRlc3QifQ.LMeTd56BOmN-uEvSnXhIzmUjUQ7OQNATOUp2OYhsEOc";
    Mockito.when(
            oidcUtils.createSignedJWT(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(validJWT);
    QuarkusMock.installMockForType(oidcUtils, OIDCUtils.class);

    // then
    TokenDataDTO tokenDataDTO = oidcServiceImpl.getOIDCTokens(requestID, clientID, attributeDTOList,
        nonce);

    Base64.Decoder decoder = Base64.getUrlDecoder();
    String[] chunks = tokenDataDTO.getIdToken().split("\\.");
    String payload = new String(decoder.decode(chunks[1]));

    Object obj = new JSONParser().parse(payload);
    JSONObject jo = (JSONObject) obj;

    String fiscalNumber = (String) jo.get("fiscalNumber");

    assertEquals(fiscalNumber, "test");
  }

  @Test
  void getOIDCTokens_OIDCSignJWTException() {
    // given
    String requestID = "dummyId";
    String clientID = "foobar";
    String nonce = "dummyNonce";
    ArrayList<AttributeDTO> attributeDTOList = new ArrayList<>();

    oidcUtils = Mockito.mock(OIDCUtils.class);
    String invalidJWT = "dummy";
    Mockito.when(
            oidcUtils.createSignedJWT(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(invalidJWT);
    QuarkusMock.installMockForType(oidcUtils, OIDCUtils.class);

    // then
    assertThrows(OIDCSignJWTException.class, () ->
        oidcServiceImpl.getOIDCTokens(requestID, clientID, attributeDTOList,
            nonce));
  }

  @Test
  void authorizeClient() {
    // given
    String clientID = "test";
    String clientSecret = "iB7QoTLouHD6szYS3sB7Ehjs7KClXnCki4kL4DBC3zc";
    String salt = "wnPIm5bGfbSX5W5LcyScBQ";
    String hashedSecret = "eQCAH3sO2CiN+6MYx/BdfMOH8vhMUN5ySTp0n6vG3Qk";
    clientConnectorImpl = Mockito.mock(ClientConnectorImpl.class);
    SecretDTO secretDTO = new SecretDTO(hashedSecret, salt);

    Mockito.when(clientConnectorImpl.getClientSecret(Mockito.any()))
        .thenReturn(Optional.of(secretDTO));

    QuarkusMock.installMockForType(clientConnectorImpl, ClientConnectorImpl.class);

    // then
    assertDoesNotThrow(
        () -> oidcServiceImpl.authorizeClient(clientID, clientSecret));

  }

  @Test
  void authorizeClient_invalidSecret() {
    // given
    String clientID = "test";
    String invalidSecret = "aW52YWxpZFNlY3JldA=="; // base 64 encoded of 'invalid_secret'
    String salt = "c2FsdGZvb2Jhcg=="; //base64 encoded of 'saltfoobar'
    String hashedSecret = "qE1dd7kBTrtsKyU5CErJkj6g8Nhd25zlz97STo27iDg"; // argon2 of salt = 'saltfoobar' and secret = 'dummy'
    clientConnectorImpl = Mockito.mock(ClientConnectorImpl.class);
    SecretDTO secretDTO = new SecretDTO(hashedSecret, salt);

    Mockito.when(clientConnectorImpl.getClientSecret(Mockito.any()))
        .thenReturn(Optional.of(secretDTO));

    QuarkusMock.installMockForType(clientConnectorImpl, ClientConnectorImpl.class);

    // then
    assertThrows(InvalidClientException.class,
        () -> oidcServiceImpl.authorizeClient(clientID, invalidSecret));

  }

  @Test
  void authorizeClient_invalidBase64Salt() {
    // given
    String clientID = "test";
    String secret = "aW52YWxpZFNlY3JldA==";
    String salt = "-c2FsdGZvb2Jhcg=="; //salt with an invalid base64 character '-'
    String hashedSecret = "qE1dd7kBTrtsKyU5CErJkj6g8Nhd25zlz97STo27iDg";
    clientConnectorImpl = Mockito.mock(ClientConnectorImpl.class);
    SecretDTO secretDTO = new SecretDTO(hashedSecret, salt);

    Mockito.when(clientConnectorImpl.getClientSecret(Mockito.any()))
        .thenReturn(Optional.of(secretDTO));

    QuarkusMock.installMockForType(clientConnectorImpl, ClientConnectorImpl.class);

    // then
    assertThrows(RuntimeException.class,
        () -> oidcServiceImpl.authorizeClient(clientID, secret));

  }

  @Test
  void authorizeClient_invalidBase64Secret() {
    // given
    String clientID = "test";
    String invalidSecret = "-aW52YWxpZFNlY3JldA=="; // secret with invalid base64 character '-'
    String salt = "c2FsdGZvb2Jhcg==";
    String hashedSecret = "qE1dd7kBTrtsKyU5CErJkj6g8Nhd25zlz97STo27iDg";
    clientConnectorImpl = Mockito.mock(ClientConnectorImpl.class);
    SecretDTO secretDTO = new SecretDTO(hashedSecret, salt);

    Mockito.when(clientConnectorImpl.getClientSecret(Mockito.any()))
        .thenReturn(Optional.of(secretDTO));

    QuarkusMock.installMockForType(clientConnectorImpl, ClientConnectorImpl.class);

    // then
    assertThrows(InvalidClientException.class,
        () -> oidcServiceImpl.authorizeClient(clientID, invalidSecret));

  }

  @Test
  void authorizeClient_nullClientID() {
    // given
    String clientID = "nullClient";
    String clientSecret = "nullClientSecret";

    // then
    assertThrows(InvalidClientException.class,
        () -> oidcServiceImpl.authorizeClient(clientID, clientSecret));

  }

  @Test
  void authorizeClient_nullClientSecret() {
    // given
    String clientID = "test";
    String clientSecret = "DummyClientSecret";

    clientConnectorImpl = Mockito.mock(ClientConnectorImpl.class);

    Mockito.when(clientConnectorImpl.getClientSecret(Mockito.any()))
        .thenReturn(Optional.empty());

    QuarkusMock.installMockForType(clientConnectorImpl, ClientConnectorImpl.class);
    // then
    assertThrows(InvalidClientException.class,
        () -> oidcServiceImpl.authorizeClient(clientID, clientSecret));

  }

}
