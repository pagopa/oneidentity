package it.pagopa.oneid.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import it.pagopa.oneid.common.model.Client;
import it.pagopa.oneid.common.model.dto.SecretDTO;
import it.pagopa.oneid.common.utils.SAMLUtilsConstants;
import it.pagopa.oneid.exception.InvalidClientException;
import it.pagopa.oneid.exception.OIDCSignJWTException;
import it.pagopa.oneid.model.dto.AttributeDTO;
import it.pagopa.oneid.model.dto.AuthorizationRequestDTO;
import it.pagopa.oneid.model.session.enums.ResponseType;
import it.pagopa.oneid.service.utils.OIDCUtils;
import it.pagopa.oneid.web.dto.TokenDataDTO;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(MyMockyTestProfile.class)
public class OIDCServiceImplTest {

  @Inject
  OIDCServiceImpl oidcServiceImpl;

  @Inject
  SAMLUtilsConstants samlConstants;

  @Inject
  OIDCUtils oidcUtils;

  @Inject
  ClientConnectorImpl clientConnectorImpl;

  @Inject
  Map<String, Client> clientsMap;

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
    String clientSecret = "ZHVtbXk="; // base 64 encoded of 'dummy'
    String salt = "c2FsdGZvb2Jhcg=="; //base64 encoded of 'saltfoobar'
    String hashedSecret = "qE1dd7kBTrtsKyU5CErJkj6g8Nhd25zlz97STo27iDg"; // argon2 of salt = 'saltfoobar' and secret = 'dummy'
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
