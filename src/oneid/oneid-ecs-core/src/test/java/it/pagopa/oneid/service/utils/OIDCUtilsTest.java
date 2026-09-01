package it.pagopa.oneid.service.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.connector.KMSConnectorImpl;
import it.pagopa.oneid.common.model.dto.AttributeDTO;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.SignResponse;

@QuarkusTest
public class OIDCUtilsTest {

  @Inject
  OIDCUtils oidcUtils;

  @Inject
  KMSConnectorImpl kmsConnectorImpl;


  @Test
  void createSignedJWT() {
    // given
    String requestId = "requestId";
    String clientId = "clientId";
    String nonce = "nonce";
    List<AttributeDTO> attributeDTOList = new ArrayList<AttributeDTO>();
    attributeDTOList.add(new AttributeDTO("testName", "testValue"));

    kmsConnectorImpl = Mockito.mock(KMSConnectorImpl.class);
    SignResponse mockedSignResponse = Mockito.mock(SignResponse.class);
    Mockito.when(mockedSignResponse.signature())
        .thenReturn(SdkBytes.fromByteArray("test".getBytes()));
    Mockito.when(kmsConnectorImpl.sign(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(mockedSignResponse);
    QuarkusMock.installMockForType(kmsConnectorImpl, KMSConnectorImpl.class);

    Executable executable = () -> oidcUtils.createSignedJWT(requestId, clientId, attributeDTOList,
        nonce);

    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void createSignedJWT_fromClaimsSet() {
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject("requestId")
        .issuer("https://issuer.example.com")
        .audience("clientId")
        .claim("nonce", "nonce")
        .claim("fiscalNumber", "ABCDEF12G34H567I")
        .build();

    kmsConnectorImpl = Mockito.mock(KMSConnectorImpl.class);
    SignResponse mockedSignResponse = Mockito.mock(SignResponse.class);
    Mockito.when(mockedSignResponse.signature())
        .thenReturn(SdkBytes.fromByteArray("test".getBytes()));
    Mockito.when(kmsConnectorImpl.sign(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(mockedSignResponse);
    QuarkusMock.installMockForType(kmsConnectorImpl, KMSConnectorImpl.class);

    Executable executable = () -> oidcUtils.createSignedJWT(claimsSet);

    assertDoesNotThrow(executable);
  }

  @Test
    void createSignedJWT_withIdpClaim() throws Exception {
    String entityId = "https://identity-provider.example/idp";
    List<AttributeDTO> attributeDTOList = new ArrayList<>();
    attributeDTOList.add(new AttributeDTO("fiscalNumber", "ABCDEF12G34H567I"));

    kmsConnectorImpl = Mockito.mock(KMSConnectorImpl.class);
    SignResponse mockedSignResponse = Mockito.mock(SignResponse.class);
    Mockito.when(mockedSignResponse.signature())
        .thenReturn(SdkBytes.fromByteArray("test".getBytes()));
    Mockito.when(kmsConnectorImpl.sign(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(mockedSignResponse);
    QuarkusMock.installMockForType(kmsConnectorImpl, KMSConnectorImpl.class);

    String serializedJwt = oidcUtils.createSignedJWT("requestId", "clientId", attributeDTOList,
        "nonce", entityId);

    JWTClaimsSet claimsSet = SignedJWT.parse(serializedJwt).getJWTClaimsSet();
    assertEquals(entityId, claimsSet.getStringClaim("idpEntityId"));
    assertEquals("ABCDEF12G34H567I", claimsSet.getStringClaim("fiscalNumber"));

    serializedJwt = oidcUtils.createSignedJWT("requestId", "clientId", attributeDTOList,
        "nonce", entityId, true);

    claimsSet = SignedJWT.parse(serializedJwt).getJWTClaimsSet();
    assertEquals(entityId, claimsSet.getStringClaim("idpEntityId"));
    assertEquals(true, claimsSet.getBooleanClaim("sameIdp"));
  }

}
