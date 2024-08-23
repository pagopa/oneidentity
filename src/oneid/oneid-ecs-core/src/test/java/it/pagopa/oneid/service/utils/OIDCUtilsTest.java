package it.pagopa.oneid.service.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.oneid.connector.KMSConnectorImpl;
import it.pagopa.oneid.model.dto.AttributeDTO;
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

}
